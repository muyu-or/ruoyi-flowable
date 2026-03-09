<template>
  <div class="notification-bell">
    <el-popover
      v-model="popoverVisible"
      placement="bottom"
      width="360"
      trigger="click"
      @show="onPopoverShow"
    >
      <div class="warning-panel">
        <div class="warning-header">
          <span class="warning-title">任务预警</span>
          <span>
            <el-button
              type="text"
              size="mini"
              :loading="scanning"
              @click="handleScan"
            >刷新扫描</el-button>
            <el-button
              v-if="warningList.length > 0"
              type="text"
              size="mini"
              @click="handleReadAll"
            >全部已读</el-button>
          </span>
        </div>
        <div v-if="warningList.length === 0" class="warning-empty">
          暂无预警消息
        </div>
        <div v-else class="warning-list">
          <div
            v-for="item in warningList"
            :key="item.id"
            class="warning-item"
            :class="{ 'is-unread': item.isRead === 0 }"
            @click="handleReadOne(item)"
          >
            <div class="warning-item-header">
              <span class="warning-node-name">{{ item.nodeName || item.nodeKey }}</span>
              <el-tag
                :type="item.warnType === 'overdue' ? 'danger' : 'warning'"
                size="mini"
              >{{ item.warnType === 'overdue' ? '已超时' : '即将超时' }}</el-tag>
            </div>
            <div class="warning-item-info">
              <span>截止日期：{{ item.endDate }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-badge slot="reference" :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <i class="el-icon-bell" />
      </el-badge>
    </el-popover>
  </div>
</template>

<script>
import { getWarningList, getUnreadWarningCount, markAllRead, markOneRead, triggerScan } from '@/api/flowable/warning'

export default {
  name: 'NotificationBell',
  data() {
    return {
      unreadCount: 0,
      warningList: [],
      popoverVisible: false,
      pollTimer: null,
      scanning: false
    }
  },
  created() {
    this.fetchUnreadCount()
    // 每5分钟轮询未读数
    this.pollTimer = setInterval(() => {
      this.fetchUnreadCount()
    }, 5 * 60 * 1000)
  },
  beforeDestroy() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },
  methods: {
    fetchUnreadCount() {
      getUnreadWarningCount().then(res => {
        if (res.data && res.data.count !== undefined) {
          this.unreadCount = res.data.count
        }
      }).catch(() => {})
    },
    onPopoverShow() {
      getWarningList({ pageNum: 1, pageSize: 10 }).then(res => {
        if (res.data) {
          this.warningList = res.data.rows || []
          if (res.data.unreadCount !== undefined) {
            this.unreadCount = res.data.unreadCount
          }
        }
      }).catch(() => {})
    },
    handleReadAll() {
      markAllRead().then(() => {
        this.unreadCount = 0
        this.warningList.forEach(item => {
          this.$set(item, 'isRead', 1)
        })
      }).catch(() => {})
    },
    handleReadOne(item) {
      if (item.isRead === 0) {
        markOneRead(item.id).then(() => {
          this.$set(item, 'isRead', 1)
          if (this.unreadCount > 0) {
            this.unreadCount--
          }
        }).catch(() => {})
      }
    },
    handleScan() {
      this.scanning = true
      triggerScan().then(() => {
        this.$message.success('扫描完成')
        this.onPopoverShow()
        this.fetchUnreadCount()
      }).catch(() => {
        this.$message.error('扫描失败')
      }).finally(() => {
        this.scanning = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.notification-bell {
  display: inline-block;
  cursor: pointer;

  .el-icon-bell {
    font-size: 20px;
    vertical-align: middle;
  }
}

.warning-panel {
  .warning-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 8px;

    .warning-title {
      font-size: 14px;
      font-weight: bold;
      color: #303133;
    }
  }

  .warning-empty {
    text-align: center;
    color: #909399;
    padding: 20px 0;
    font-size: 13px;
  }

  .warning-list {
    max-height: 320px;
    overflow-y: auto;
  }

  .warning-item {
    padding: 8px;
    border-radius: 4px;
    margin-bottom: 4px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f7fa;
    }

    &.is-unread {
      background-color: #ecf5ff;

      &:hover {
        background-color: #d9ecff;
      }
    }

    .warning-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .warning-node-name {
        font-size: 13px;
        color: #303133;
        font-weight: 500;
      }
    }

    .warning-item-info {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
