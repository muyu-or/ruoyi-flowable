<template>
  <el-card class="recent-tasks-card" shadow="hover">
    <div slot="header" class="card-header">
      <span class="card-title"><i class="el-icon-finished" /> 最近完成任务</span>
    </div>
    <div v-if="recentTasks && recentTasks.length" class="task-list">
      <div
        v-for="(item, idx) in recentTasks"
        :key="idx"
        class="task-item"
      >
        <span class="node-tag" :style="{ background: tagColors[idx % tagColors.length] }">
          {{ item.nodeName || '--' }}
        </span>
        <div class="task-info">
          <div class="task-proc-name">{{ item.procName || '--' }}</div>
          <div class="task-meta">
            <span v-if="isLeader && item.claimUserName" class="meta-user">
              <i class="el-icon-user" /> {{ item.claimUserName }}
            </span>
            <span class="meta-time">
              <i class="el-icon-time" /> {{ item.completeTime || '--' }}
            </span>
            <span v-if="item.processDuration" class="meta-duration">
              {{ formatDuration(item.processDuration) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-tip">
      <i class="el-icon-document" />
      <p>暂无已完成任务</p>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'RecentTasksCard',
  props: {
    recentTasks: {
      type: Array,
      default: function() { return [] }
    },
    isLeader: {
      type: Boolean,
      default: false
    }
  },
  data: function() {
    return {
      tagColors: [
        '#667eea', '#764ba2', '#67C23A', '#E6A23C',
        '#409EFF', '#F56C6C', '#909399', '#6554C0'
      ]
    }
  },
  methods: {
    goFinished: function() {
      this.$router.push('/flowable/task/finished')
    },
    formatDuration: function(seconds) {
      if (!seconds || seconds <= 0) return ''
      var hours = seconds / 3600
      if (hours >= 1) {
        return hours.toFixed(1) + 'h'
      }
      var minutes = Math.round(seconds / 60)
      return minutes + 'min'
    }
  }
}
</script>

<style scoped>
.recent-tasks-card {
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.card-title i {
  margin-right: 6px;
  color: #667eea;
}
.more-btn {
  padding: 0;
  font-size: 13px;
  color: #667eea;
}
.task-list {
  max-height: 440px;
  overflow-y: auto;
}
.task-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  border-radius: 6px;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}
.task-item:last-child {
  border-bottom: none;
}
.task-item:hover {
  background: #f5f7fa;
}
.node-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 28px;
  border-radius: 14px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  padding: 0 10px;
  flex-shrink: 0;
  margin-right: 12px;
}
.task-info {
  flex: 1;
  min-width: 0;
}
.task-proc-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
.task-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}
.task-meta i {
  margin-right: 3px;
}
.meta-duration {
  color: #667eea;
  font-weight: 500;
}
.empty-tip {
  text-align: center;
  padding: 30px 0;
  color: #c0c4cc;
}
.empty-tip i {
  font-size: 40px;
  display: block;
  margin-bottom: 8px;
}
.empty-tip p {
  margin: 0;
  font-size: 13px;
}
</style>
