<template>
  <div class="bi-root">
    <div v-if="!isAdmin" class="bi-no-perm">
      <i class="el-icon-lock" style="font-size:48px;color:#ff6b6b;display:block;margin-bottom:16px;" />
      <span>仅管理员可查看</span>
    </div>
    <div v-else v-loading="loading" class="bi-screen" element-loading-background="rgba(10,22,40,0.8)" element-loading-text="加载数据中...">
      <!-- 顶部栏 -->
      <div class="bi-header">
        <div class="bi-header-left">
          <i class="el-icon-data-analysis bi-header-icon" />
          <span class="bi-header-title">数据统计中心</span>
        </div>
        <div class="bi-header-center">
          <span class="bi-clock">{{ clockStr }}</span>
        </div>
        <div class="bi-header-right">
          <el-radio-group v-model="currentPeriod" size="mini" @change="onPeriodChange">
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 指标卡片行 -->
      <BiStatCards :my-stats="companyStatsData" />

      <!-- 三列主体 -->
      <div class="bi-main-row">
        <!-- 左列 -->
        <div class="bi-col bi-col-left">
          <BiStockTrendChart :chart-data="statsData.stockTrend || []" height="240px" />
          <div style="margin-top:16px;">
            <BiTeamProgressChart :team-progress-list="statsData.teamProgress || []" height="200px" />
          </div>
        </div>
        <!-- 中列 -->
        <div class="bi-col bi-col-center">
          <BiMonitorPanel />
        </div>
        <!-- 右列 -->
        <div class="bi-col bi-col-right">
          <BiInventoryChart :chart-data="statsData.inventoryOverview || []" :category-dicts="categoryDicts" height="220px" />
          <div style="margin-top:16px;">
            <BiTeamTop5Chart :user-top5-list="statsData.userTop5 || []" height="200px" />
          </div>
        </div>
      </div>

      <!-- 底部行 -->
      <div class="bi-bottom-row">
        <div class="bi-bottom-left">
          <BiRecentTasks :recent-tasks="statsData.recentTasks || []" />
        </div>
        <div class="bi-bottom-right">
          <BiWarningList :warnings="warnings" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getHomeStats } from '@/api/flowable/stat'
import { getWarningList } from '@/api/flowable/warning'
import BiStatCards from './components/BiStatCards'
import BiStockTrendChart from './components/BiStockTrendChart'
import BiInventoryChart from './components/BiInventoryChart'
import BiTeamProgressChart from './components/BiTeamProgressChart'
import BiTeamTop5Chart from './components/BiTeamTop5Chart'
import BiMonitorPanel from './components/BiMonitorPanel'
import BiRecentTasks from './components/BiRecentTasks'
import BiWarningList from './components/BiWarningList'

var WEEK_NAMES = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

function padZero(n) {
  return n < 10 ? '0' + n : '' + n
}

export default {
  name: 'BiIndex',
  components: {
    BiStatCards,
    BiStockTrendChart,
    BiInventoryChart,
    BiTeamProgressChart,
    BiTeamTop5Chart,
    BiMonitorPanel,
    BiRecentTasks,
    BiWarningList
  },
  dicts: ['material_category'],
  data: function() {
    return {
      loading: false,
      currentPeriod: 'month',
      statsData: {},
      warnings: [],
      now: new Date(),
      clockTimer: null
    }
  },
  computed: {
    isAdmin: function() {
      var roles = this.$store.state.user.roles
      return roles && roles.indexOf('admin') !== -1
    },
    categoryDicts: function() {
      return (this.dict && this.dict.type && this.dict.type.material_category) || []
    },
    clockStr: function() {
      var d = this.now
      var y = d.getFullYear()
      var mon = padZero(d.getMonth() + 1)
      var day = padZero(d.getDate())
      var h = padZero(d.getHours())
      var m = padZero(d.getMinutes())
      var s = padZero(d.getSeconds())
      var w = WEEK_NAMES[d.getDay()]
      return y + '-' + mon + '-' + day + ' ' + h + ':' + m + ':' + s + ' ' + w
    },
    companyStatsData: function() {
      return this.statsData.companyStats || this.statsData.myStats || {}
    }
  },
  created: function() {
    if (this.isAdmin) {
      this.loadData()
    }
  },
  mounted: function() {
    var self = this
    this.clockTimer = setInterval(function() {
      self.now = new Date()
    }, 1000)
  },
  beforeDestroy: function() {
    if (this.clockTimer) {
      clearInterval(this.clockTimer)
      this.clockTimer = null
    }
  },
  methods: {
    onPeriodChange: function() {
      this.loadData()
    },
    loadData: function() {
      var self = this
      this.loading = true
      var p1 = getHomeStats({ period: this.currentPeriod }).then(function(res) {
        self.statsData = (res && res.data) || {}
      })
      var p2 = getWarningList({ pageNum: 1, pageSize: 20 }).then(function(res) {
        self.warnings = (res && res.data && res.data.rows) || []
      })
      Promise.all([p1, p2]).catch(function() {
        // silent
      }).then(function() {
        self.loading = false
      })
    }
  }
}
</script>

<style scoped>
.bi-root {
  min-height: calc(100vh - 84px);
  background: #0a1628;
}
.bi-no-perm {
  min-height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(226, 243, 255, 0.6);
  font-size: 16px;
}
.bi-screen {
  padding: 0 20px 20px;
  color: #e2f3ff;
}

/* --- Header --- */
.bi-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 212, 255, 0.15);
  margin-bottom: 16px;
}
.bi-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.bi-header-icon {
  font-size: 24px;
  color: #00d4ff;
}
.bi-header-title {
  font-size: 18px;
  font-weight: 700;
  color: #00d4ff;
  letter-spacing: 3px;
}
.bi-header-center {
  flex: 1;
  text-align: center;
}
.bi-clock {
  font-size: 14px;
  color: rgba(226, 243, 255, 0.7);
  letter-spacing: 1px;
}
.bi-header-right {
  display: flex;
  align-items: center;
}

/* Override el-radio-button for dark theme */
.bi-header-right >>> .el-radio-button__inner {
  background: transparent;
  border-color: rgba(0, 212, 255, 0.3);
  color: rgba(226, 243, 255, 0.6);
}
.bi-header-right >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: rgba(0, 212, 255, 0.15);
  border-color: #00d4ff;
  color: #00d4ff;
  box-shadow: -1px 0 0 0 #00d4ff;
}

/* --- Main 3-col --- */
.bi-main-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.bi-col-left {
  flex: 3;
  min-width: 0;
}
.bi-col-center {
  flex: 4;
  min-width: 0;
}
.bi-col-right {
  flex: 3;
  min-width: 0;
}

/* --- Bottom row --- */
.bi-bottom-row {
  display: flex;
  gap: 16px;
}
.bi-bottom-left {
  flex: 1;
  min-width: 0;
}
.bi-bottom-right {
  flex: 1;
  min-width: 0;
}
</style>
