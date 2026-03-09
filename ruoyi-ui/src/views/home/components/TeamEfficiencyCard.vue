<template>
  <el-card class="efficiency-card" shadow="hover">
    <div slot="header" class="card-header">
      <span class="card-title"><i class="el-icon-s-flag" /> {{ teamName }}效率概览</span>
    </div>
    <div class="efficiency-body">
      <!-- 完成率环形图 -->
      <div class="rate-ring">
        <el-progress
          type="circle"
          :percentage="ratePercent"
          :width="100"
          :stroke-width="8"
          :color="rateColor"
        >
          <template slot="default">
            <div class="ring-inner">
              <span class="ring-value">{{ ratePercent }}%</span>
              <span class="ring-label">完成率</span>
            </div>
          </template>
        </el-progress>
      </div>
      <!-- 指标列表 -->
      <div class="metric-list">
        <div class="metric-item">
          <div class="metric-icon" style="background: rgba(102,126,234,0.1)">
            <i class="el-icon-time" style="color: #667eea" />
          </div>
          <div class="metric-detail">
            <div class="metric-value">{{ avgHours }}</div>
            <div class="metric-label">平均耗时(h)</div>
          </div>
        </div>
        <div class="metric-item">
          <div class="metric-icon" style="background: rgba(103,194,58,0.1)">
            <i class="el-icon-circle-check" style="color: #67C23A" />
          </div>
          <div class="metric-detail">
            <div class="metric-value">{{ myTeamEfficiency.finishedCount || 0 }}</div>
            <div class="metric-label">已完成</div>
          </div>
        </div>
        <div class="metric-item">
          <div class="metric-icon" style="background: rgba(96,98,102,0.1)">
            <i class="el-icon-s-data" style="color: #606266" />
          </div>
          <div class="metric-detail">
            <div class="metric-value">{{ myTeamEfficiency.totalCount || 0 }}</div>
            <div class="metric-label">任务总量</div>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'TeamEfficiencyCard',
  props: {
    myTeamEfficiency: {
      type: Object,
      default: function() { return {} }
    }
  },
  computed: {
    teamName: function() {
      return this.myTeamEfficiency.teamName || '我的班组'
    },
    avgHours: function() {
      var val = this.myTeamEfficiency.avgDurationHours
      return val ? val.toFixed(1) : '0.0'
    },
    ratePercent: function() {
      var val = this.myTeamEfficiency.completionRate
      return val ? Math.round(val * 100) : 0
    },
    rateColor: function() {
      if (this.ratePercent >= 80) return '#67C23A'
      if (this.ratePercent >= 50) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>

<style scoped>
.efficiency-card {
  border-radius: 8px;
  height: 100%;
}
.card-header {
  display: flex;
  align-items: center;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.card-title i {
  margin-right: 6px;
  color: #764ba2;
}
.efficiency-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
}
.rate-ring {
  margin-bottom: 20px;
}
.ring-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.ring-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.ring-label {
  font-size: 11px;
  color: #909399;
}
.metric-list {
  width: 100%;
}
.metric-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.metric-item:hover {
  background: #f5f7fa;
}
.metric-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
}
.metric-icon i {
  font-size: 18px;
}
.metric-detail {
  flex: 1;
}
.metric-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}
.metric-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
