<template>
  <div class="dashboard-container" v-loading="loading" element-loading-text="加载统计数据中...">
    <!-- 顶部欢迎栏 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h2 class="welcome-text">欢迎回来，{{ userName }}</h2>
        <p class="welcome-sub">{{ todayStr }} | 以下是您的工作概览</p>
      </div>
      <el-radio-group v-model="currentPeriod" size="small" @change="onPeriodChange">
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="quarter">本季度</el-radio-button>
        <el-radio-button label="year">本年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 任务卡片（管理角色显示全公司，其他显示个人） -->
    <StatCards :my-stats="statsData.companyStats || statsData.myStats" />

    <!-- 入库/出库趋势 + 库存总览（admin + leader） -->
    <el-row v-if="isAdmin" :gutter="20" class="charts-row">
      <el-col :span="12">
        <StockTrendChart :chart-data="statsData.stockTrend || []" />
      </el-col>
      <el-col :span="12">
        <InventoryOverviewChart :chart-data="statsData.inventoryOverview || []" :category-dicts="categoryDicts" />
      </el-col>
    </el-row>

    <!-- 班组进度汇总（admin） -->
    <TeamProgressTable v-if="isAdmin" :team-progress-list="statsData.teamProgress" />

    <!-- 我的班组效率 + 成员情况 / 最近完成任务（leader + 成员） -->
    <el-row v-if="(isLeader || !isAdmin) && hasTeamData" :gutter="20" class="team-row">
      <el-col :span="8">
        <TeamEfficiencyCard :my-team-efficiency="statsData.myTeamEfficiency || {}" />
      </el-col>
      <!-- 班组长：右侧放成员统计 -->
      <el-col v-if="isLeader" :span="16">
        <MemberStatsTable :member-stats-list="statsData.memberStats" />
      </el-col>
      <!-- 普通成员：右侧放最近完成任务（填满空白） -->
      <el-col v-if="!isLeader && hasRecentTasks" :span="16">
        <RecentTasksCard
          :recent-tasks="statsData.recentTasks"
          :is-leader="false"
        />
      </el-col>
    </el-row>

    <!-- 班组长的最近完成任务（独占一行） -->
    <el-row v-if="isLeader && hasRecentTasks" class="recent-tasks-row">
      <el-col :span="24">
        <RecentTasksCard
          :recent-tasks="statsData.recentTasks"
          :is-leader="true"
        />
      </el-col>
    </el-row>

    <!-- 管理角色的最近完成任务（独占一行） -->
    <el-row v-if="isAdmin && hasRecentTasks" class="recent-tasks-row">
      <el-col :span="24">
        <RecentTasksCard
          :recent-tasks="statsData.recentTasks"
          :is-leader="true"
        />
      </el-col>
    </el-row>

    <!-- 数据为空提示 -->
    <div v-if="!loading && isEmpty" class="empty-state">
      <i class="el-icon-data-analysis empty-icon" />
      <p>暂无统计数据</p>
    </div>
  </div>
</template>

<script>
import { getHomeStats } from '@/api/flowable/stat'
import StatCards from './components/StatCards.vue'
import StockTrendChart from './components/StockTrendChart.vue'
import InventoryOverviewChart from './components/InventoryOverviewChart.vue'
import TeamProgressTable from './components/TeamProgressTable.vue'
import TeamEfficiencyCard from './components/TeamEfficiencyCard.vue'
import MemberStatsTable from './components/MemberStatsTable.vue'
import RecentTasksCard from './components/RecentTasksCard.vue'

export default {
  name: 'HomePage',
  dicts: ['material_category'],
  components: {
    StatCards,
    StockTrendChart,
    InventoryOverviewChart,
    TeamProgressTable,
    TeamEfficiencyCard,
    MemberStatsTable,
    RecentTasksCard
  },
  data() {
    return {
      statsData: {
        myStats: null,
        stockTrend: [],
        inventoryOverview: [],
        teamProgress: [],
        myTeamEfficiency: {},
        memberStats: [],
        recentTasks: []
      },
      currentPeriod: 'month',
      loading: false
    }
  },
  computed: {
    isAdmin() {
      // 后端返回了 companyStats 或 teamProgress 说明拥有全局视角
      return !!(this.statsData.companyStats || (this.statsData.teamProgress && this.statsData.teamProgress.length > 0 && this.statsData.stockTrend))
    },
    isLeader() {
      // 后端返回了 memberStats 说明是班组长
      return !!(this.statsData.memberStats && this.statsData.memberStats.length > 0)
    },
    userName() {
      return this.$store.state.user.name || '用户'
    },
    todayStr() {
      var d = new Date()
      var weekNames = ['日', '一', '二', '三', '四', '五', '六']
      return d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日 星期' + weekNames[d.getDay()]
    },
    hasTeamData() {
      return this.statsData.myTeamEfficiency && this.statsData.myTeamEfficiency.teamId
    },
    hasRecentTasks() {
      return this.statsData.recentTasks && this.statsData.recentTasks.length > 0
    },
    categoryDicts() {
      return this.dict.type.material_category || []
    },
    isEmpty() {
      return !this.statsData.myStats && (!this.statsData.teamProgress || this.statsData.teamProgress.length === 0)
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      this.loading = true
      getHomeStats({ period: this.currentPeriod })
        .then(function(res) {
          if (res.code === 200) {
            this.statsData = res.data || this.statsData
          } else {
            this.$message.error('加载统计数据失败: ' + res.msg)
          }
        }.bind(this))
        .catch(function(err) {
          console.error('加载统计数据失败', err)
        }.bind(this))
        .finally(function() {
          this.loading = false
        }.bind(this))
    },
    onPeriodChange() {
      this.loadStats()
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: #fff;
}
.header-left {
  display: flex;
  flex-direction: column;
}
.welcome-text {
  margin: 0 0 4px 0;
  font-size: 22px;
  font-weight: 600;
  color: #fff;
}
.welcome-sub {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}
.dashboard-header >>> .el-radio-button__inner {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}
.dashboard-header >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.5);
  color: #fff;
  box-shadow: -1px 0 0 0 rgba(255, 255, 255, 0.5);
}
.charts-row {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
}
.charts-row >>> .el-col {
  display: flex;
  flex-direction: column;
}
.charts-row >>> .el-col > * {
  flex: 1;
}
.team-row {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
}
.team-row >>> .el-col {
  display: flex;
  flex-direction: column;
}
.team-row >>> .el-col > * {
  flex: 1;
}
.recent-tasks-row {
  margin-bottom: 20px;
}
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #909399;
}
.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
  display: block;
}
</style>
