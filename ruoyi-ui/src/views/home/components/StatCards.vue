<template>
  <el-row :gutter="16" class="stat-cards">
    <el-col v-for="item in statItems" :key="item.key" :xs="12" :sm="8" :md="4">
      <div class="stat-card-item" :style="{ borderTop: '3px solid ' + item.color }">
        <div class="stat-icon-wrap" :style="{ background: item.bgColor }">
          <i :class="item.icon" :style="{ color: item.color }" />
        </div>
        <div class="stat-info">
          <div class="stat-num" :style="{ color: item.color }">
            {{ myStats ? (myStats[item.key] || 0) : '-' }}
          </div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: 'StatCards',
  props: {
    myStats: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      statItems: [
        { key: 'pending', label: '待办任务', color: '#E6A23C', bgColor: 'rgba(230,162,60,0.1)', icon: 'el-icon-time' },
        { key: 'running', label: '进行中', color: '#409EFF', bgColor: 'rgba(64,158,255,0.1)', icon: 'el-icon-loading' },
        { key: 'finished', label: '已完成', color: '#67C23A', bgColor: 'rgba(103,194,58,0.1)', icon: 'el-icon-circle-check' },
        { key: 'rejected', label: '已拒绝', color: '#F56C6C', bgColor: 'rgba(245,108,108,0.1)', icon: 'el-icon-circle-close' },
        { key: 'total', label: '任务总计', color: '#606266', bgColor: 'rgba(96,98,102,0.1)', icon: 'el-icon-s-data' }
      ]
    }
  }
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 20px;
}
.stat-card-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: default;
  margin-bottom: 16px;
}
.stat-card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px 0 rgba(0, 0, 0, 0.1);
}
.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16px;
}
.stat-icon-wrap i {
  font-size: 24px;
}
.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-num {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
