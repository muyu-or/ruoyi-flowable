<template>
  <div class="bi-chart-card bi-recent-tasks">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-notebook-2" /> 最近任务动态</span>
    </div>
    <div ref="scrollWrap" class="task-scroll-wrap">
      <div ref="scrollInner" class="task-scroll-inner">
        <div v-for="(item, idx) in displayList" :key="idx" class="task-row">
          <span class="task-dot" />
          <span class="task-node">{{ item.nodeName }}</span>
          <span class="task-sep">-</span>
          <span class="task-proc">{{ item.procName }}</span>
          <span class="task-user">{{ item.claimUserName }}</span>
          <span class="task-time">{{ item.completeTime }}</span>
          <span v-if="item.processDuration" class="task-duration">{{ formatDuration(item.processDuration) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BiRecentTasks',
  props: {
    recentTasks: {
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
    displayList: function() {
      // Duplicate for seamless scroll
      var list = this.recentTasks || []
      if (list.length <= 5) return list
      return list.concat(list)
    }
  },
  watch: {
    recentTasks: function() {
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
    formatDuration: function(seconds) {
      if (!seconds || seconds <= 0) return ''
      var h = Math.floor(seconds / 3600)
      var m = Math.floor((seconds % 3600) / 60)
      if (h > 0) return h + 'h' + m + 'm'
      return m + 'm'
    },
    startScroll: function() {
      this.stopScroll()
      var list = this.recentTasks || []
      if (list.length <= 5) return
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
}
.bi-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #00d4ff;
}
.bi-recent-tasks {
  height: 180px;
  display: flex;
  flex-direction: column;
}
.task-scroll-wrap {
  flex: 1;
  overflow: hidden;
  position: relative;
  max-height: 140px;
}
.task-scroll-inner {
  transition: transform 0.05s linear;
}
.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.06);
  font-size: 12px;
  color: rgba(226, 243, 255, 0.8);
  white-space: nowrap;
  overflow: hidden;
}
.task-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #26de81;
  flex-shrink: 0;
}
.task-node {
  color: #00d4ff;
  font-weight: 600;
  flex-shrink: 0;
}
.task-sep {
  color: rgba(226, 243, 255, 0.3);
  flex-shrink: 0;
}
.task-proc {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}
.task-user {
  color: rgba(226, 243, 255, 0.5);
  flex-shrink: 0;
}
.task-time {
  color: rgba(226, 243, 255, 0.4);
  flex-shrink: 0;
}
.task-duration {
  color: #ff9f43;
  font-size: 11px;
  flex-shrink: 0;
}
</style>
