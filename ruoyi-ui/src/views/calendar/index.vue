<template>
  <div class="app-container calendar-container">
    <!-- 顶部工具栏 -->
    <div class="calendar-toolbar">
      <div class="toolbar-left">
        <div class="month-nav">
          <el-button class="nav-btn" icon="el-icon-arrow-left" circle size="mini" @click="changeMonth(-1)" />
          <div class="month-display">
            <el-date-picker
              v-model="currentMonth"
              type="month"
              placeholder="选择月份"
              format="yyyy年M月"
              value-format="yyyy-MM"
              size="small"
              :clearable="false"
              class="month-picker"
              @change="onMonthChange"
            />
          </div>
          <el-button class="nav-btn" icon="el-icon-arrow-right" circle size="mini" @click="changeMonth(1)" />
        </div>
        <el-button class="today-btn" size="small" @click="goToday">
          <i class="el-icon-aim" /> 本月
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-tag :type="roleTagType" size="small" effect="plain" class="role-tag">
          <i :class="roleIcon" /> {{ roleTip }}
        </el-tag>
        <div class="legend">
          <span
            class="legend-item"
            :class="{ 'legend-active': isFilterActive('completed'), 'legend-dimmed': hasActiveFilter && !isFilterActive('completed') }"
            @click="toggleFilter('completed')"
          ><i class="dot dot-completed" />已完成</span>
          <span
            class="legend-item"
            :class="{ 'legend-active': isFilterActive('pending'), 'legend-dimmed': hasActiveFilter && !isFilterActive('pending') }"
            @click="toggleFilter('pending')"
          ><i class="dot dot-pending" />待处理</span>
          <span
            class="legend-item"
            :class="{ 'legend-active': isFilterActive('timeout'), 'legend-dimmed': hasActiveFilter && !isFilterActive('timeout') }"
            @click="toggleFilter('timeout')"
          ><i class="dot dot-timeout" />已超时</span>
          <span
            class="legend-item"
            :class="{ 'legend-active': isFilterActive('rejected'), 'legend-dimmed': hasActiveFilter && !isFilterActive('rejected') }"
            @click="toggleFilter('rejected')"
          ><i class="dot dot-rejected" />失败</span>
        </div>
      </div>
    </div>

    <!-- 星期表头 -->
    <div class="weekday-header">
      <span v-for="w in weekdays" :key="w" class="weekday-cell" :class="{ 'weekend': w === '六' || w === '日' }">{{ w }}</span>
    </div>

    <!-- 日历主体 -->
    <div v-loading="loading" class="calendar-body">
      <el-calendar v-model="calendarDate">
        <template slot="dateCell" slot-scope="{ data }">
          <div
            class="cell-wrapper"
            :class="{
              'cell-today': isToday(data.day),
              'cell-other-month': !isCurrentMonth(data.day),
              'cell-has-events': getFilteredEventsForDay(data.day).length > 0,
              'cell-weekend': isWeekend(data.day)
            }"
            @click="openDetail(data.day)"
          >
            <!-- 日期头部 -->
            <div class="cell-header">
              <span class="day-number" :class="{ 'today-badge': isToday(data.day) }">
                {{ getDayNum(data.day) }}
              </span>
              <span v-if="hasPastSummary(data.day)" class="day-summary">
                <span class="summary-badge done-badge">
                  <i class="el-icon-check" />{{ getSummary(data.day).done }}
                </span>
                <span v-if="getSummary(data.day).undone > 0" class="summary-badge undone-badge">
                  <i class="el-icon-close" />{{ getSummary(data.day).undone }}
                </span>
              </span>
            </div>
            <!-- 事件列表 -->
            <div class="cell-events">
              <div
                v-for="evt in getVisibleEvents(data.day)"
                :key="evt.id"
                class="event-chip"
                :class="'chip-' + chipType(evt)"
              >
                <span class="chip-dot" />
                <span class="chip-text">{{ chipLabel(evt) }}</span>
              </div>
              <div v-if="getOverflowCount(data.day) > 0" class="event-overflow">
                +{{ getOverflowCount(data.day) }} 更多
              </div>
            </div>
            <!-- 今日指示线 -->
            <div v-if="isToday(data.day)" class="today-indicator" />
          </div>
        </template>
      </el-calendar>
    </div>

    <!-- 详情抽屉 -->
    <day-detail
      :visible.sync="drawerVisible"
      :date="selectedDate"
      :events="selectedDayAllEvents"
    />
  </div>
</template>

<script>
import { getCalendarEvents } from '@/api/flowable/calendar'
import DayDetail from './components/DayDetail'

var MAX_VISIBLE = 3

export default {
  name: 'CalendarBoard',
  components: { DayDetail },
  data() {
    return {
      calendarDate: new Date(),
      currentMonth: '',
      events: [],
      eventMap: {},
      loading: false,
      drawerVisible: false,
      selectedDate: '',
      selectedDayAllEvents: [],
      isAdmin: false,
      isLeader: false,
      weekdays: ['一', '二', '三', '四', '五', '六', '日'],
      activeFilter: ''
    }
  },
  computed: {
    roleTip() {
      return this.$store.getters.nickName || ''
    },
    roleTagType() {
      if (this.isAdmin) return 'danger'
      if (this.isLeader) return 'warning'
      return ''
    },
    roleIcon() {
      if (this.isAdmin) return 'el-icon-s-custom'
      if (this.isLeader) return 'el-icon-s-check'
      return 'el-icon-user'
    },
    hasActiveFilter() {
      return this.activeFilter !== ''
    }
  },
  created() {
    var now = new Date()
    this.currentMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0')
    this.checkRole()
    this.fetchEvents()
  },
  methods: {
    checkRole() {
      var roles = this.$store.getters.roles || []
      this.isAdmin = roles.indexOf('admin') > -1
      this.isLeader = false
    },
    fetchEvents() {
      var parts = this.currentMonth.split('-')
      var year = parseInt(parts[0])
      var month = parseInt(parts[1])
      this.loading = true
      getCalendarEvents(year, month).then(res => {
        this.events = res.data || []
        this.buildEventMap()
        if (!this.isAdmin && this.events.length > 0) {
          var hasTeam = this.events.some(function(e) { return e.teamName })
          this.isLeader = hasTeam
        }
      }).finally(() => {
        this.loading = false
      })
    },
    buildEventMap() {
      var map = {}
      for (var i = 0; i < this.events.length; i++) {
        var evt = this.events[i]
        var day = evt.planEndDate
        if (!day) continue
        if (!map[day]) {
          map[day] = []
        }
        map[day].push(evt)
      }
      this.eventMap = map
    },
    getEventsForDay(day) {
      return this.eventMap[day] || []
    },
    /** 将事件归类为四大类之一 */
    getCategory(evt) {
      if (evt.timeoutFlag === 1) return 'timeout'
      if (evt.status === 'rejected') return 'rejected'
      if (evt.status === 'completed') return 'completed'
      return 'pending' // pending / claimed / submitted 全部归为待处理
    },
    matchesFilter(evt) {
      if (!this.activeFilter) return true
      return this.getCategory(evt) === this.activeFilter
    },
    getFilteredEventsForDay(day) {
      var list = this.getEventsForDay(day)
      if (!this.activeFilter) return list
      var self = this
      return list.filter(function(e) { return self.matchesFilter(e) })
    },
    getVisibleEvents(day) {
      var list = this.getFilteredEventsForDay(day)
      return list.slice(0, MAX_VISIBLE)
    },
    getOverflowCount(day) {
      var list = this.getFilteredEventsForDay(day)
      return list.length > MAX_VISIBLE ? list.length - MAX_VISIBLE : 0
    },
    getDayNum(day) {
      return parseInt(day.split('-')[2])
    },
    isToday(day) {
      var now = new Date()
      var todayStr = now.getFullYear() + '-' +
        String(now.getMonth() + 1).padStart(2, '0') + '-' +
        String(now.getDate()).padStart(2, '0')
      return day === todayStr
    },
    isCurrentMonth(day) {
      return day.substring(0, 7) === this.currentMonth
    },
    isWeekend(day) {
      var d = new Date(day)
      var dow = d.getDay()
      return dow === 0 || dow === 6
    },
    hasPastSummary(day) {
      var now = new Date()
      var todayStr = now.getFullYear() + '-' +
        String(now.getMonth() + 1).padStart(2, '0') + '-' +
        String(now.getDate()).padStart(2, '0')
      if (day >= todayStr) return false
      var list = this.getEventsForDay(day)
      return list.length > 0
    },
    getSummary(day) {
      var list = this.getEventsForDay(day)
      var done = 0
      var undone = 0
      for (var i = 0; i < list.length; i++) {
        if (list[i].status === 'completed') {
          done++
        } else {
          undone++
        }
      }
      return { done: done, undone: undone }
    },
    /** 色块类型：已超时 > 失败 > 已完成 > 待处理 */
    chipType(evt) {
      return this.getCategory(evt)
    },
    chipLabel(evt) {
      var name = evt.taskName ? evt.taskName : ''
      var node = evt.nodeName || ''
      if (name && node) return name + ' · ' + node
      return name || node || '未知'
    },
    toggleFilter(type) {
      if (this.activeFilter === type) {
        this.activeFilter = ''
      } else {
        this.activeFilter = type
      }
    },
    isFilterActive(type) {
      return this.activeFilter === type
    },
    changeMonth(delta) {
      var parts = this.currentMonth.split('-')
      var y = parseInt(parts[0])
      var m = parseInt(parts[1]) + delta
      if (m < 1) { m = 12; y-- }
      if (m > 12) { m = 1; y++ }
      this.currentMonth = y + '-' + String(m).padStart(2, '0')
      this.calendarDate = new Date(y, m - 1, 1)
      this.fetchEvents()
    },
    onMonthChange() {
      var parts = this.currentMonth.split('-')
      this.calendarDate = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, 1)
      this.fetchEvents()
    },
    goToday() {
      var now = new Date()
      this.currentMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0')
      this.calendarDate = now
      this.fetchEvents()
    },
    openDetail(day) {
      this.selectedDate = day
      this.selectedDayAllEvents = this.getEventsForDay(day)
      this.drawerVisible = true
    }
  }
}
</script>

<style scoped>
/* ===== 容器 ===== */
.calendar-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ee 100%);
  padding: 20px;
  min-height: calc(100vh - 84px);
}

/* ===== 工具栏 ===== */
.calendar-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 14px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  flex-wrap: wrap;
  gap: 12px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.month-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-btn {
  border: none;
  color: #606266;
  transition: all 0.25s;
}
.nav-btn:hover {
  color: #409EFF;
  background: #ECF5FF;
  transform: scale(1.1);
}
.month-picker {
  width: 150px;
}
.month-picker >>> .el-input__inner {
  border: none;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  background: transparent;
  padding: 0 8px;
}
.today-btn {
  border-radius: 18px;
  font-weight: 500;
}
.today-btn:hover {
  background: #ECF5FF;
  color: #409EFF;
  border-color: #409EFF;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.role-tag {
  border-radius: 12px;
  font-weight: 500;
}

/* 图例（可点击筛选） */
.legend {
  display: flex;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 14px;
  border: 1px solid transparent;
  transition: all 0.2s;
  user-select: none;
}
.legend-item:hover {
  background: #F5F7FA;
}
.legend-item.legend-active {
  border-color: #D0D7DE;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  font-weight: 600;
}
.legend-item.legend-dimmed {
  opacity: 0.35;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.1);
}
.dot-completed { background: linear-gradient(135deg, #26DE81, #55EFC4); }
.dot-pending { background: linear-gradient(135deg, #409EFF, #66B1FF); }
.dot-timeout { background: linear-gradient(135deg, #FF6B6B, #FF8787); }
.dot-rejected { background: linear-gradient(135deg, #909399, #B0B5BC); }

/* ===== 星期表头 ===== */
.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fff;
  border-radius: 12px 12px 0 0;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}
.weekday-cell {
  text-align: center;
  padding: 10px 0;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  background: linear-gradient(180deg, #fafbfc 0%, #f0f2f5 100%);
  border-bottom: 2px solid #E4E7ED;
}
.weekday-cell.weekend {
  color: #F56C6C;
  background: linear-gradient(180deg, #fff8f8 0%, #fef0f0 100%);
}

/* ===== 日历主体 ===== */
.calendar-body {
  background: #fff;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

/* ===== 隐藏默认 header & 表头 ===== */
.calendar-container >>> .el-calendar__header {
  display: none;
}
.calendar-container >>> .el-calendar-table thead {
  display: none;
}
.calendar-container >>> .el-calendar__body {
  padding: 0;
}
.calendar-container >>> .el-calendar-table {
  border-collapse: collapse;
}

/* ===== 格子通用 ===== */
.calendar-container >>> .el-calendar-table .el-calendar-day {
  height: auto;
  min-height: 110px;
  padding: 0;
}
.calendar-container >>> .el-calendar-table td {
  border: 1px solid #EBEEF5;
}
.calendar-container >>> .el-calendar-table td.is-selected {
  background: transparent;
}

/* ===== cell-wrapper ===== */
.cell-wrapper {
  position: relative;
  height: 100%;
  min-height: 110px;
  padding: 6px 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 2px;
}
.cell-wrapper:hover {
  background: #F5F9FF;
  z-index: 1;
}
.cell-wrapper.cell-today {
  background: linear-gradient(135deg, #F0F7FF 0%, #E8F4FD 100%);
}
.cell-wrapper.cell-today:hover {
  background: linear-gradient(135deg, #E3F0FF 0%, #D6ECFB 100%);
}
.cell-wrapper.cell-other-month {
  opacity: 0.35;
}
.cell-wrapper.cell-weekend {
  background: #FEFBFB;
}
.cell-wrapper.cell-has-events {
  border-radius: 2px;
}

/* 今日底部指示线 */
.today-indicator {
  position: absolute;
  bottom: 0;
  left: 8px;
  right: 8px;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: linear-gradient(90deg, #409EFF, #66B1FF, #409EFF);
  animation: shimmer 2s ease-in-out infinite;
}
@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ===== 日期头部 ===== */
.cell-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.day-number {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}
.day-number.today-badge {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

/* 汇总角标 */
.day-summary {
  display: flex;
  gap: 3px;
}
.summary-badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 8px;
  font-weight: 600;
  line-height: 16px;
  display: inline-flex;
  align-items: center;
  gap: 1px;
}
.summary-badge i {
  font-size: 9px;
}
.done-badge {
  background: #E1F9EC;
  color: #1B9E5A;
}
.undone-badge {
  background: #FEE2E2;
  color: #E53E3E;
}

/* ===== 事件色块 ===== */
.cell-events {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.event-chip {
  display: flex;
  align-items: center;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 11px;
  line-height: 18px;
  overflow: hidden;
  transition: all 0.15s ease;
}
.event-chip:hover {
  transform: translateX(2px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

/* 色块类型 */
.chip-completed {
  background: linear-gradient(135deg, #ECFDF5, #F0FFF4);
  color: #1B9E5A;
}
.chip-completed .chip-dot { background: #26DE81; }

.chip-pending {
  background: linear-gradient(135deg, #EBF5FF, #F0F7FF);
  color: #2B6CB0;
}
.chip-pending .chip-dot { background: #409EFF; }

.chip-timeout {
  background: linear-gradient(135deg, #FFF0F0, #FFF5F5);
  color: #E53E3E;
}
.chip-timeout .chip-dot { background: #FF6B6B; }

.chip-rejected {
  background: linear-gradient(135deg, #F4F5F7, #EBEDF0);
  color: #606266;
}
.chip-rejected .chip-dot { background: #909399; }

.chip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 5px;
  flex-shrink: 0;
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.15);
}
.chip-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

/* 更多 */
.event-overflow {
  font-size: 10px;
  color: #909399;
  text-align: center;
  padding: 1px 0;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.15s;
}
.event-overflow:hover {
  background: #F2F6FC;
  color: #409EFF;
}
</style>
