<template>
  <div class="bi-stat-cards">
    <div
      v-for="(card, idx) in cards"
      :key="idx"
      class="bi-stat-item"
      :style="{ borderTopColor: card.color }"
    >
      <div class="stat-icon" :style="{ color: card.color }">
        <i :class="card.icon" />
      </div>
      <div class="stat-info">
        <span class="stat-value" :style="{ color: card.color }">{{ card.value }}</span>
        <span class="stat-label">{{ card.label }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BiStatCards',
  props: {
    myStats: {
      type: Object,
      default: function() {
        return {}
      }
    }
  },
  computed: {
    cards() {
      var s = this.myStats || {}
      return [
        { label: '待办', value: s.pending || 0, color: '#ff9f43', icon: 'el-icon-time' },
        { label: '进行中', value: s.running || 0, color: '#00d4ff', icon: 'el-icon-loading' },
        { label: '已完成', value: s.finished || 0, color: '#26de81', icon: 'el-icon-circle-check' },
        { label: '失败', value: s.rejected || 0, color: '#ff6b6b', icon: 'el-icon-circle-close' },
        { label: '总计', value: s.total || 0, color: '#a55eea', icon: 'el-icon-s-data' }
      ]
    }
  }
}
</script>

<style scoped>
.bi-stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.bi-stat-item {
  flex: 1;
  background: #0f2744;
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-top: 3px solid;
  border-radius: 8px;
  padding: 18px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  transition: transform 0.25s, box-shadow 0.25s;
}
.bi-stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.15);
}
.stat-icon {
  font-size: 32px;
  opacity: 0.85;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: rgba(226, 243, 255, 0.6);
  margin-top: 4px;
}
</style>
