<template>
  <div class="bi-chart-card bi-monitor-panel">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-monitor" /> 生产运营监控</span>
    </div>
    <div class="monitor-body">
      <svg class="monitor-svg" viewBox="0 0 300 400" preserveAspectRatio="xMidYMid meet">
        <!-- 背景网格 -->
        <defs>
          <pattern id="grid" width="30" height="30" patternUnits="userSpaceOnUse">
            <path d="M 30 0 L 0 0 0 30" fill="none" stroke="rgba(0,212,255,0.06)" stroke-width="0.5" />
          </pattern>
          <linearGradient id="scanGrad" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="rgba(0,212,255,0)" />
            <stop offset="50%" stop-color="rgba(0,212,255,0.15)" />
            <stop offset="100%" stop-color="rgba(0,212,255,0)" />
          </linearGradient>
        </defs>
        <rect width="300" height="400" fill="url(#grid)" />

        <!-- 扫描线动画 -->
        <rect class="scan-line" x="0" y="0" width="300" height="60" fill="url(#scanGrad)" />

        <!-- 生产节点图标 -->
        <g v-for="(node, idx) in monitorNodes" :key="idx" :transform="'translate(' + node.x + ',' + node.y + ')'">
          <circle r="18" :fill="node.active ? 'rgba(0,212,255,0.15)' : 'rgba(255,255,255,0.05)'" :stroke="node.active ? '#00d4ff' : 'rgba(226,243,255,0.2)'" stroke-width="1.5" />
          <circle v-if="node.active" r="18" fill="none" stroke="#00d4ff" stroke-width="1" opacity="0.4" class="pulse-ring" />
          <text text-anchor="middle" dy="4" :fill="node.active ? '#00d4ff' : 'rgba(226,243,255,0.4)'" font-size="14">{{ node.icon }}</text>
          <text text-anchor="middle" dy="34" fill="rgba(226,243,255,0.6)" font-size="9">{{ node.label }}</text>
        </g>

        <!-- 连接线 -->
        <line v-for="(line, idx) in connectLines" :key="'l' + idx" :x1="line.x1" :y1="line.y1" :x2="line.x2" :y2="line.y2" stroke="rgba(0,212,255,0.15)" stroke-width="1" stroke-dasharray="4,3" />
      </svg>

      <div class="status-bar">
        <div class="status-item">
          <span class="status-dot dot-green" />
          <span class="status-text">运行中</span>
        </div>
        <div class="status-item">
          <span class="status-dot dot-blue" />
          <span class="status-text">监控正常</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BiMonitorPanel',
  data: function() {
    return {
      monitorNodes: [
        { x: 150, y: 50, icon: '\u2B22', label: '原料入库', active: true },
        { x: 80, y: 130, icon: '\u2699', label: '预处理', active: true },
        { x: 220, y: 130, icon: '\u2699', label: '真空处理', active: false },
        { x: 80, y: 210, icon: '\u2B22', label: '烘烤', active: true },
        { x: 220, y: 210, icon: '\u2316', label: '检测', active: true },
        { x: 150, y: 290, icon: '\u2713', label: '出库', active: false },
        { x: 150, y: 360, icon: '\u2B22', label: '产品入库', active: false }
      ],
      connectLines: [
        { x1: 150, y1: 68, x2: 80, y2: 112 },
        { x1: 150, y1: 68, x2: 220, y2: 112 },
        { x1: 80, y1: 148, x2: 80, y2: 192 },
        { x1: 220, y1: 148, x2: 220, y2: 192 },
        { x1: 80, y1: 228, x2: 150, y2: 272 },
        { x1: 220, y1: 228, x2: 150, y2: 272 },
        { x1: 150, y1: 308, x2: 150, y2: 342 }
      ]
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
.bi-monitor-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.monitor-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}
.monitor-svg {
  width: 100%;
  max-height: 420px;
  flex: 1;
}
.scan-line {
  animation: scanMove 4s ease-in-out infinite;
}
@keyframes scanMove {
  0%   { transform: translateY(-60px); }
  50%  { transform: translateY(400px); }
  100% { transform: translateY(-60px); }
}
.pulse-ring {
  animation: pulseAnim 2s ease-out infinite;
}
@keyframes pulseAnim {
  0%   { r: 18; opacity: 0.4; }
  100% { r: 28; opacity: 0; }
}
.status-bar {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 212, 255, 0.1);
  width: 100%;
  justify-content: center;
}
.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.dot-green {
  background: #26de81;
  box-shadow: 0 0 6px rgba(38, 222, 129, 0.6);
}
.dot-blue {
  background: #00d4ff;
  box-shadow: 0 0 6px rgba(0, 212, 255, 0.6);
}
.status-text {
  font-size: 11px;
  color: rgba(226, 243, 255, 0.6);
}
</style>
