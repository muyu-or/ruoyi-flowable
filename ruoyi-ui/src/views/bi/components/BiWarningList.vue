<template>
  <div class="bi-chart-card bi-warning-list">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-warning-outline" /> 超时预警任务列表</span>
      <span class="warn-count" v-if="unresolvedList.length > 0">{{ unresolvedList.length }}条</span>
    </div>
    <div ref="scrollWrap" class="warning-scroll-wrap">
      <div v-if="unresolvedList.length === 0" class="warning-empty">
        <i class="el-icon-check" style="font-size:24px;color:#26de81;" />
        <p>暂无未处理预警</p>
      </div>
      <div v-else ref="scrollInner" class="warning-scroll-inner">
        <div v-for="(item, idx) in displayList" :key="idx" class="warning-row" :class="rowClass(item)">
          <span :class="['warn-dot', dotClass(item)]" />
          <span :class="['warn-type', typeClass(item)]">{{ typeLabel(item) }}</span>
          <span class="warn-task">{{ item.taskName || '' }}</span>
          <span v-if="item.taskName && item.nodeName" class="warn-sep">-</span>
          <span class="warn-node">{{ item.nodeName || '' }}</span>
          <span v-if="item.teamName" class="warn-team">{{ item.teamName }}</span>
          <span class="warn-date">截止 {{ item.endDate }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BiWarningList',
  props: {
    warnings: {
      type: Array,
      default: function() {
        return []
      }
    }
  },
  data: function() {
    return {
      scrollTimer: null
    }
  },
  computed: {
    /** 只展示未处理的预警 */
    unresolvedList: function() {
      return (this.warnings || []).filter(function(item) {
        return item.resolved !== 1
      })
    },
    /** 超过6条时拼接一份实现无缝滚动 */
    displayList: function() {
      var list = this.unresolvedList
      if (list.length <= 6) return list
      return list.concat(list)
    }
  },
  watch: {
    unresolvedList: function() {
      this.startScroll()
    }
  },
  mounted: function() {
    this.startScroll()
  },
  beforeDestroy: function() {
    this.stopScroll()
  },
  methods: {
    rowClass: function(item) {
      if (item.isRead === 1) return 'is-read-unresolved'
      return ''
    },
    dotClass: function(item) {
      if (item.isRead === 1) return 'dot-read'
      return item.warnType === 'overdue' ? 'dot-overdue' : 'dot-soon'
    },
    typeClass: function(item) {
      if (item.isRead === 1) return 'type-unresolved'
      return item.warnType === 'overdue' ? 'type-overdue' : 'type-soon'
    },
    typeLabel: function(item) {
      if (item.isRead === 1) return '未处理'
      return item.warnType === 'overdue' ? '已超时' : '即将超时'
    },
    startScroll: function() {
      this.stopScroll()
      var list = this.unresolvedList
      if (list.length <= 6) return
      var self = this
      var offset = 0
      this.scrollTimer = setInterval(function() {
        if (!self.$refs.scrollInner) return
        offset += 0.5
        var halfHeight = self.$refs.scrollInner.scrollHeight / 2
        if (offset >= halfHeight) {
          offset = 0
        }
        self.$refs.scrollInner.style.transform = 'translateY(-' + offset + 'px)'
      }, 80)
    },
    stopScroll: function() {
      if (this.scrollTimer) {
        clearInterval(this.scrollTimer)
        this.scrollTimer = null
      }
    }
  }
}
</script>

<style scoped>
.bi-chart-card {
  background: #0f2744;
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  padding: 14px 16px;
  position: relative;
}
.bi-chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 14px;
  height: 14px;
  border-top: 2px solid rgba(0, 212, 255, 0.5);
  border-left: 2px solid rgba(0, 212, 255, 0.5);
}
.bi-card-header {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.bi-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #00d4ff;
}
.warn-count {
  font-size: 11px;
  color: #ff6b6b;
}
.bi-warning-list {
  height: 240px;
  display: flex;
  flex-direction: column;
}
.warning-scroll-wrap {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.warning-scroll-inner {
  transition: transform 0.05s linear;
}
.warning-empty {
  text-align: center;
  padding: 30px 0;
  color: rgba(226, 243, 255, 0.4);
  font-size: 12px;
}
.warning-empty p {
  margin: 8px 0 0;
}
.warning-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.06);
  font-size: 12px;
}
/* 已读未处理：微弱透明 */
.warning-row.is-read-unresolved {
  opacity: 0.7;
}
.warn-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-overdue {
  background: #ff6b6b;
  box-shadow: 0 0 6px rgba(255, 107, 107, 0.6);
}
.dot-soon {
  background: #ff9f43;
  box-shadow: 0 0 6px rgba(255, 159, 67, 0.6);
}
.dot-read {
  background: #e6a23c;
}
.warn-type {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}
.type-overdue {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
}
.type-soon {
  background: rgba(255, 159, 67, 0.2);
  color: #ff9f43;
}
.type-unresolved {
  background: rgba(230, 162, 60, 0.2);
  color: #e6a23c;
}
.warn-task {
  color: #00d4ff;
  font-weight: 600;
  flex-shrink: 0;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.warn-sep {
  color: rgba(226, 243, 255, 0.3);
  flex-shrink: 0;
}
.warn-node {
  color: rgba(226, 243, 255, 0.8);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.warn-team {
  color: #a55eea;
  font-size: 11px;
  flex-shrink: 0;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.warn-date {
  color: rgba(226, 243, 255, 0.4);
  flex-shrink: 0;
  font-size: 11px;
}
</style>
