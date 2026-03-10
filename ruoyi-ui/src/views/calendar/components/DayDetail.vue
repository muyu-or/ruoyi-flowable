<template>
  <el-drawer
    :visible.sync="drawerVisible"
    direction="rtl"
    size="480px"
    :show-close="false"
    custom-class="day-detail-drawer"
    @close="handleClose"
  >
    <!-- 自定义 header -->
    <template slot="title">
      <div class="drawer-header">
        <div class="header-left">
          <span class="header-date">{{ formatHeaderDate }}</span>
          <span class="header-weekday">{{ weekdayText }}</span>
        </div>
        <div class="header-right">
          <span
            v-if="allCompletedCount > 0"
            class="filter-tag filter-tag-completed"
            :class="{ 'filter-active': activeFilter === 'completed', 'filter-dimmed': activeFilter && activeFilter !== 'completed' }"
            @click="toggleFilter('completed')"
          >
            <i class="el-icon-circle-check" /> 已完成 {{ allCompletedCount }}
          </span>
          <span
            v-if="allPendingCount > 0"
            class="filter-tag filter-tag-pending"
            :class="{ 'filter-active': activeFilter === 'pending', 'filter-dimmed': activeFilter && activeFilter !== 'pending' }"
            @click="toggleFilter('pending')"
          >
            <i class="el-icon-time" /> 待处理 {{ allPendingCount }}
          </span>
          <span
            v-if="allTimeoutCount > 0"
            class="filter-tag filter-tag-timeout"
            :class="{ 'filter-active': activeFilter === 'timeout', 'filter-dimmed': activeFilter && activeFilter !== 'timeout' }"
            @click="toggleFilter('timeout')"
          >
            <i class="el-icon-warning" /> 已超时 {{ allTimeoutCount }}
          </span>
          <span
            v-if="allRejectedCount > 0"
            class="filter-tag filter-tag-rejected"
            :class="{ 'filter-active': activeFilter === 'rejected', 'filter-dimmed': activeFilter && activeFilter !== 'rejected' }"
            @click="toggleFilter('rejected')"
          >
            <i class="el-icon-circle-close" /> 失败 {{ allRejectedCount }}
          </span>
        </div>
      </div>
    </template>

    <div class="day-detail-content">
      <!-- 空状态 -->
      <div v-if="filteredEvents.length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="el-icon-date" />
        </div>
        <p class="empty-title">{{ events.length === 0 ? '当天暂无任务' : '没有符合筛选的任务' }}</p>
        <p class="empty-desc">{{ events.length === 0 ? '该日期没有计划截止的节点任务' : '点击上方标签切换或取消筛选' }}</p>
      </div>

      <!-- 事件卡片列表 -->
      <transition-group v-else name="card-list" tag="div">
        <div
          v-for="(item, index) in filteredEvents"
          :key="item.id"
          class="event-card"
          :class="'card-' + getCategory(item)"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <!-- 状态色带 -->
          <div class="card-accent" :style="{ background: accentGradient(item) }" />

          <div class="card-body">
            <!-- 头部：状态 + 标题 -->
            <div class="card-header">
              <span class="status-badge" :class="'badge-' + getCategory(item)">
                <i :class="statusIcon(item)" />
                {{ statusLabel(item) }}
              </span>
            </div>
            <div class="card-title">
              {{ item.taskName ? item.taskName + ' · ' : '' }}{{ item.nodeName || '未知节点' }}
            </div>

            <!-- 信息区 -->
            <div class="card-info">
              <div class="info-item">
                <i class="el-icon-office-building" />
                <span>{{ item.teamName || '未分配' }}</span>
              </div>
              <div v-if="item.claimUserName" class="info-item">
                <i class="el-icon-user" />
                <span>{{ item.claimUserName }}</span>
              </div>
            </div>

            <!-- 时间区：计划开始 → 计划截止 -->
            <div class="card-time">
              <div class="time-row">
                <span class="time-label">开始</span>
                <span class="time-value">{{ item.planStartDate || '-' }}</span>
              </div>
              <div class="time-arrow">
                <i class="el-icon-right" />
              </div>
              <div class="time-row">
                <span class="time-label">截止</span>
                <span class="time-value">{{ item.planEndDate || '-' }}</span>
              </div>
            </div>

            <!-- 底部：已完成 -->
            <div v-if="item.status === 'completed' && item.timeoutFlag !== 1" class="card-footer completed-footer">
              <div class="info-item success-text">
                <i class="el-icon-circle-check" />
                <span>{{ formatTime(item.completeTime) }} 完成</span>
              </div>
              <span v-if="item.processDuration" class="duration-tag">
                <i class="el-icon-timer" /> {{ formatDuration(item.processDuration) }}
              </span>
            </div>
            <!-- 底部：失败 -->
            <div v-else-if="item.status === 'rejected'" class="card-footer rejected-footer">
              <div class="info-item rejected-text">
                <i class="el-icon-circle-close" />
                <span>{{ formatTime(item.completeTime) }} 审批未通过</span>
              </div>
            </div>
            <!-- 底部：已超时 -->
            <div v-else-if="item.timeoutFlag === 1" class="card-footer timeout-footer">
              <div class="info-item timeout-text">
                <i class="el-icon-warning" />
                <span v-if="item.status === 'completed'">已完成（超时）</span>
                <span v-else>已超过截止时间</span>
              </div>
              <span v-if="item.processDuration" class="duration-tag">
                <i class="el-icon-timer" /> {{ formatDuration(item.processDuration) }}
              </span>
            </div>
            <!-- 底部：待处理 -->
            <div v-else class="card-footer pending-footer">
              <div class="progress-bar">
                <div class="progress-fill" :class="item.status === 'claimed' ? 'fill-claimed' : 'fill-pending'" />
              </div>
              <span class="progress-label">{{ item.status === 'claimed' ? '处理中...' : '等待处理' }}</span>
            </div>
          </div>
        </div>
      </transition-group>
    </div>
  </el-drawer>
</template>

<script>
var WEEKDAY_NAMES = ['日', '一', '二', '三', '四', '五', '六']

export default {
  name: 'DayDetail',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    date: {
      type: String,
      default: ''
    },
    events: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      activeFilter: ''
    }
  },
  computed: {
    drawerVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    formatHeaderDate() {
      if (!this.date) return ''
      var parts = this.date.split('-')
      return parts[1] + '月' + parseInt(parts[2]) + '日'
    },
    weekdayText() {
      if (!this.date) return ''
      var d = new Date(this.date)
      return '周' + WEEKDAY_NAMES[d.getDay()]
    },
    allCompletedCount() {
      var self = this
      return this.events.filter(function(e) { return self.getCategory(e) === 'completed' }).length
    },
    allPendingCount() {
      var self = this
      return this.events.filter(function(e) { return self.getCategory(e) === 'pending' }).length
    },
    allTimeoutCount() {
      var self = this
      return this.events.filter(function(e) { return self.getCategory(e) === 'timeout' }).length
    },
    allRejectedCount() {
      var self = this
      return this.events.filter(function(e) { return self.getCategory(e) === 'rejected' }).length
    },
    filteredEvents() {
      if (!this.activeFilter) return this.events
      var self = this
      var filter = this.activeFilter
      return this.events.filter(function(e) {
        return self.getCategory(e) === filter
      })
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.activeFilter = ''
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false)
    },
    /** 四大分类：completed / pending / timeout / rejected */
    getCategory(evt) {
      if (evt.timeoutFlag === 1) return 'timeout'
      if (evt.status === 'rejected') return 'rejected'
      if (evt.status === 'completed') return 'completed'
      return 'pending'
    },
    toggleFilter(type) {
      if (this.activeFilter === type) {
        this.activeFilter = ''
      } else {
        this.activeFilter = type
      }
    },
    accentGradient(item) {
      var cat = this.getCategory(item)
      if (cat === 'timeout') return 'linear-gradient(180deg, #FF6B6B, #EE5A5A)'
      if (cat === 'rejected') return 'linear-gradient(180deg, #909399, #787D85)'
      if (cat === 'completed') return 'linear-gradient(180deg, #26DE81, #1BC97A)'
      return 'linear-gradient(180deg, #409EFF, #3A8EE6)'
    },
    statusLabel(item) {
      var cat = this.getCategory(item)
      if (cat === 'timeout') return '已超时'
      if (cat === 'rejected') return '失败'
      if (cat === 'completed') return '已完成'
      return '待处理'
    },
    statusIcon(item) {
      var cat = this.getCategory(item)
      if (cat === 'timeout') return 'el-icon-warning'
      if (cat === 'rejected') return 'el-icon-circle-close'
      if (cat === 'completed') return 'el-icon-circle-check'
      if (item.status === 'claimed') return 'el-icon-loading'
      return 'el-icon-time'
    },
    formatTime(time) {
      if (!time) return '-'
      var str = String(time)
      if (str.length >= 16) {
        return str.substring(5, 16)
      }
      if (str.length === 10) {
        return str.substring(5)
      }
      return str
    },
    formatDuration(val) {
      if (!val && val !== 0) return ''
      var seconds = parseInt(val)
      if (isNaN(seconds) || seconds < 0) return val + ''
      if (seconds < 60) return seconds + '秒'
      if (seconds < 3600) return Math.floor(seconds / 60) + '分钟'
      var hours = Math.floor(seconds / 3600)
      var mins = Math.floor((seconds % 3600) / 60)
      if (mins === 0) return hours + '小时'
      return hours + '小时' + mins + '分'
    }
  }
}
</script>

<style scoped>
/* ===== 抽屉全局 ===== */
.day-detail-content {
  padding: 0 20px 20px;
}

/* ===== 自定义 header ===== */
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.header-date {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.header-weekday {
  font-size: 13px;
  color: #909399;
  font-weight: 400;
}
.header-right {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 筛选标签 */
.filter-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  border: 1px solid transparent;
}
.filter-tag i {
  font-size: 13px;
}
.filter-tag:hover {
  transform: scale(1.05);
}
.filter-tag.filter-active {
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.08);
}
.filter-tag.filter-dimmed {
  opacity: 0.3;
}
.filter-tag-completed {
  background: #E1F9EC;
  color: #1B9E5A;
}
.filter-tag-completed.filter-active {
  border-color: #1B9E5A;
}
.filter-tag-pending {
  background: #E8F4FD;
  color: #2B6CB0;
}
.filter-tag-pending.filter-active {
  border-color: #2B6CB0;
}
.filter-tag-timeout {
  background: #FEE2E2;
  color: #DC2626;
}
.filter-tag-timeout.filter-active {
  border-color: #DC2626;
}
.filter-tag-rejected {
  background: #F0F1F3;
  color: #606266;
}
.filter-tag-rejected.filter-active {
  border-color: #909399;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}
.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0F5FF, #E8EFFF);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.empty-icon i {
  font-size: 36px;
  color: #409EFF;
  opacity: 0.6;
}
.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: #606266;
  margin: 0 0 6px;
}
.empty-desc {
  font-size: 13px;
  color: #C0C4CC;
  margin: 0;
}

/* ===== 卡片列表动画 ===== */
.card-list-enter-active {
  transition: all 0.3s ease;
}
.card-list-enter {
  opacity: 0;
  transform: translateX(20px);
}

/* ===== 事件卡片 ===== */
.event-card {
  display: flex;
  border-radius: 10px;
  margin-bottom: 12px;
  background: #fff;
  overflow: hidden;
  border: 1px solid #EBEEF5;
  transition: all 0.25s ease;
  animation: cardSlideIn 0.3s ease forwards;
  opacity: 0;
}
@keyframes cardSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.event-card:hover {
  border-color: #D0D7DE;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.card-timeout {
  border-color: #FECACA;
  background: linear-gradient(135deg, #FFFBFB, #FFF5F5);
}
.card-timeout:hover {
  border-color: #FCA5A5;
}
.card-rejected {
  border-color: #E0E2E5;
  background: linear-gradient(135deg, #FAFBFC, #F4F5F7);
}
.card-rejected:hover {
  border-color: #C0C4CC;
}

/* 左侧色带 */
.card-accent {
  width: 5px;
  flex-shrink: 0;
}
.card-body {
  flex: 1;
  padding: 14px 16px;
  min-width: 0;
}

/* 头部 */
.card-header {
  margin-bottom: 6px;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
}
.status-badge i {
  font-size: 11px;
}
.badge-completed {
  background: #E1F9EC;
  color: #1B9E5A;
}
.badge-pending {
  background: #E8F4FD;
  color: #2B6CB0;
}
.badge-timeout {
  background: #FEE2E2;
  color: #DC2626;
}
.badge-rejected {
  background: #F0F1F3;
  color: #606266;
}

/* 标题 */
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  line-height: 1.4;
  word-break: break-all;
}

/* 信息区 */
.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 8px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}
.info-item i {
  font-size: 14px;
  color: #C0C4CC;
}

/* 时间区 */
.card-time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #FAFBFC;
  border-radius: 6px;
  border: 1px solid #F0F2F5;
}
.time-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}
.time-label {
  font-size: 11px;
  color: #C0C4CC;
  font-weight: 500;
}
.time-value {
  font-size: 13px;
  color: #606266;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.time-arrow {
  color: #DCDFE6;
  font-size: 14px;
  flex-shrink: 0;
}

/* 底部通用 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px dashed #F0F2F5;
}
.success-text {
  color: #1B9E5A;
}
.success-text i {
  color: #26DE81;
}
.rejected-text {
  color: #606266;
}
.rejected-text i {
  color: #909399;
}
.timeout-text {
  color: #DC2626;
}
.timeout-text i {
  color: #FF6B6B;
}
.duration-tag {
  font-size: 12px;
  color: #909399;
  background: #F5F7FA;
  padding: 2px 8px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.duration-tag i {
  font-size: 12px;
}

/* 进度条（待处理） */
.pending-footer {
  flex-direction: column;
  align-items: stretch;
  gap: 4px;
}
.progress-bar {
  height: 3px;
  background: #EBEEF5;
  border-radius: 3px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}
.fill-claimed {
  width: 60%;
  background: linear-gradient(90deg, #409EFF, #66B1FF);
  animation: progressPulse 1.5s ease-in-out infinite;
}
.fill-pending {
  width: 10%;
  background: linear-gradient(90deg, #409EFF, #66B1FF);
}
@keyframes progressPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
.progress-label {
  font-size: 11px;
  color: #C0C4CC;
  text-align: right;
}
</style>
