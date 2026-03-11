<template>
  <div class="notification-bell">
    <el-popover
      v-model="popoverVisible"
      placement="bottom"
      width="420"
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
              v-if="warningList.length > 0 && unreadCount > 0"
              type="text"
              size="mini"
              @click="handleReadAll"
            >全部已读</el-button>
            <el-button
              v-if="hasResolved"
              type="text"
              size="mini"
              style="color: #f56c6c"
              @click="handleClearResolved"
            >清空已处理</el-button>
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
            :class="itemClass(item)"
            @click="handleReadOne(item)"
          >
            <div class="warning-item-header">
              <span class="warning-node-name">
                <span v-if="item.taskName" class="warning-task-name">{{ item.taskName }}</span>
                <span v-if="item.taskName && item.nodeName"> - </span>
                {{ item.nodeName || item.nodeKey }}
              </span>
              <el-tag
                v-if="item.resolved === 1"
                type="success"
                size="mini"
              >已处理</el-tag>
              <el-tag
                v-else-if="item.warnType === 'overdue'"
                type="danger"
                size="mini"
              >{{ item.isRead === 1 ? '已超时·未处理' : '已超时' }}</el-tag>
              <el-tag
                v-else
                type="warning"
                size="mini"
              >{{ item.isRead === 1 ? '即将超时·未处理' : '即将超时' }}</el-tag>
            </div>
            <div class="warning-item-info">
              <span v-if="item.teamName" class="warning-team">{{ item.teamName }}</span>
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
import { getWarningList, getUnreadWarningCount, markAllRead, markOneRead, triggerScan, clearResolved } from '@/api/flowable/warning'

export default {
  name: 'NotificationBell',
  data: function() {
    return {
      unreadCount: 0,
      warningList: [],
      popoverVisible: false,
      pollTimer: null,
      scanning: false
    }
  },
  computed: {
    hasResolved: function() {
      return this.warningList.some(function(item) { return item.resolved === 1 })
    }
  },
  created: function() {
    this.fetchUnreadCount()
    var self = this
    this.pollTimer = setInterval(function() {
      self.fetchUnreadCount()
    }, 5 * 60 * 1000)
  },
  beforeDestroy: function() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },
  methods: {
    itemClass: function(item) {
      if (item.resolved === 1) {
        return 'is-resolved'
      }
      if (item.isRead === 0) {
        return 'is-unread'
      }
      return 'is-read-unresolved'
    },
    fetchUnreadCount: function() {
      var self = this
      getUnreadWarningCount().then(function(res) {
        if (res && res.data && res.data.count !== undefined) {
          self.unreadCount = res.data.count
        }
      }).catch(function() {})
    },
    onPopoverShow: function() {
      var self = this
      getWarningList({ pageNum: 1, pageSize: 20 }).then(function(res) {
        if (res && res.data) {
          self.warningList = res.data.rows || []
          if (res.data.unreadCount !== undefined) {
            self.unreadCount = res.data.unreadCount
          }
        }
      }).catch(function() {})
    },
    handleReadAll: function() {
      var self = this
      markAllRead().then(function() {
        self.onPopoverShow()
        self.fetchUnreadCount()
      }).catch(function() {})
    },
    handleReadOne: function(item) {
      var self = this
      if (item.isRead === 0) {
        markOneRead(item.id).then(function() {
          self.$set(item, 'isRead', 1)
          if (self.unreadCount > 0) {
            self.unreadCount--
          }
        }).catch(function() {})
      }
    },
    handleScan: function() {
      var self = this
      this.scanning = true
      triggerScan().then(function() {
        self.$message.success('扫描完成')
        self.onPopoverShow()
        self.fetchUnreadCount()
      }).catch(function() {
        self.$message.error('扫描失败')
      }).then(function() {
        self.scanning = false
      })
    },
    handleClearResolved: function() {
      var self = this
      this.$confirm('确认删除所有已处理的预警消息?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        clearResolved().then(function(res) {
          self.$message.success(res.msg || '清空成功')
          self.onPopoverShow()
          self.fetchUnreadCount()
        }).catch(function() {
          self.$message.error('清空失败')
        })
      }).catch(function() {})
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

  ::v-deep .el-badge__content {
    top: 8px;
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
    max-height: 360px;
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

    /* 未读：蓝色高亮 */
    &.is-unread {
      background-color: #ecf5ff;
      border-left: 3px solid #409EFF;

      &:hover {
        background-color: #d9ecff;
      }
    }

    /* 已读未处理：淡橙色 */
    &.is-read-unresolved {
      background-color: #fdf6ec;
      border-left: 3px solid #e6a23c;

      &:hover {
        background-color: #faecd8;
      }
    }

    /* 已处理：半透明 */
    &.is-resolved {
      opacity: 0.5;
      border-left: 3px solid #67c23a;
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
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 8px;

        .warning-task-name {
          color: #409EFF;
          font-weight: 600;
        }
      }
    }

    .warning-item-info {
      font-size: 12px;
      color: #909399;
      display: flex;
      gap: 12px;

      .warning-team {
        color: #67c23a;
        font-weight: 500;
      }
    }
  }
}
</style>
