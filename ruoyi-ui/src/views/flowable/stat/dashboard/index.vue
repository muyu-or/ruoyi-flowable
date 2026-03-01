<template>
  <div class="app-container">
    <!-- 我的任务统计 -->
    <el-card class="stat-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>我的任务统计</span>
      </div>
      <el-row v-if="statsData" :gutter="20">
        <el-col v-for="item in myStatItems" :key="item.key" :span="4">
          <div class="stat-item">
            <div class="stat-num" :style="{ color: item.color }">
              {{ statsData.myStats ? statsData.myStats[item.key] : 0 }}
            </div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
      <div v-else class="loading-placeholder">加载中...</div>
    </el-card>

    <!-- 班组任务汇总（仅班组长可见，支持多班组） -->
    <template v-if="statsData && statsData.isLeader && statsData.teamStatsList && statsData.teamStatsList.length">
      <el-card
        v-for="team in statsData.teamStatsList"
        :key="team.teamId"
        class="stat-card team-card"
        shadow="hover"
      >
        <div slot="header" class="card-header">
          <span>
            {{ team.teamName }} 班组任务汇总
            <el-tag size="small" type="info" style="margin-left:8px;">
              共 {{ team.memberCount }} 名成员
            </el-tag>
          </span>
        </div>
        <el-row :gutter="20">
          <el-col v-for="item in teamStatItems" :key="item.key" :span="4">
            <div class="stat-item">
              <div class="stat-num" :style="{ color: item.color }">
                {{ team[item.key] }}
              </div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </template>
  </div>
</template>

<script>
import { getDashboardStats } from '@/api/flowable/stat'

export default {
  name: 'TaskDashboard',
  data() {
    return {
      statsData: null,
      myStatItems: [
        { key: 'pending', label: '待办', color: '#E6A23C' },
        { key: 'running', label: '进行中', color: '#409EFF' },
        { key: 'finished', label: '已完成', color: '#67C23A' },
        { key: 'rejected', label: '失败', color: '#F56C6C' },
        { key: 'total', label: '合计', color: '#303133' }
      ],
      teamStatItems: [
        { key: 'pending', label: '待办', color: '#E6A23C' },
        { key: 'running', label: '进行中', color: '#409EFF' },
        { key: 'finished', label: '已完成', color: '#67C23A' },
        { key: 'rejected', label: '失败', color: '#F56C6C' },
        { key: 'total', label: '合计', color: '#303133' }
      ]
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      getDashboardStats().then(res => {
        if (res.code === 200) {
          this.statsData = res.data
        }
      }).catch(err => {
        console.error('加载统计数据失败', err)
        this.$message.error('加载统计数据失败')
      })
    }
  }
}
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}
.team-card {
  border: 1px solid #409EFF;
}
.card-header {
  font-size: 16px;
  font-weight: bold;
}
.stat-item {
  text-align: center;
  padding: 20px 0;
}
.stat-num {
  font-size: 36px;
  font-weight: bold;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
.loading-placeholder {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
</style>
