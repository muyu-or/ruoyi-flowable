<template>
  <div class="live-monitor-container">
    <!-- 顶部工具栏 -->
    <div class="monitor-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">
          <i class="el-icon-video-camera" />
          实时监控
        </span>
        <el-tag :type="onlineCount > 0 ? 'success' : 'info'" size="small" style="margin-left: 12px">
          在线 {{ onlineCount }} / {{ deviceList.length }}
        </el-tag>
      </div>
      <div class="toolbar-right">
        <el-radio-group v-model="gridMode" size="mini" @change="handleGridChange">
          <el-radio-button label="4">2×2</el-radio-button>
          <el-radio-button label="9">3×3</el-radio-button>
        </el-radio-group>
        <el-button size="mini" icon="el-icon-refresh" style="margin-left: 10px" @click="loadDevices">刷新</el-button>
      </div>
    </div>

    <!-- 监控画面网格 -->
    <div class="monitor-grid" :class="'grid-' + gridMode">
      <div
        v-for="(slot, index) in gridSlots"
        :key="index"
        class="monitor-cell"
        :class="{ 'cell-offline': slot.device && slot.device.status !== '1', 'cell-empty': !slot.device }"
      >
        <!-- 有设备 -->
        <template v-if="slot.device">
          <div class="video-area">
            <!-- 真实场景图片 -->
            <img
              :src="getSceneImage(slot.device, index)"
              class="scene-img"
              :class="{ 'img-offline': slot.device.status !== '1' }"
            >
            <!-- 监控色调滤镜层 -->
            <div class="cctv-filter" />
            <!-- 噪点 Canvas -->
            <canvas :ref="'noise_' + index" class="noise-canvas" />
            <!-- 扫描线 -->
            <div class="scanline" />
            <!-- CRT 横纹效果 -->
            <div class="crt-lines" />

            <!-- 设备离线 / 故障覆盖层 -->
            <div v-if="slot.device.status !== '1'" class="offline-overlay">
              <i :class="slot.device.status === '3' ? 'el-icon-warning' : 'el-icon-video-pause'" />
              <span>{{ slot.device.status === '3' ? '设备故障' : '设备离线' }}</span>
            </div>
          </div>

          <!-- 信息叠加层 -->
          <div class="info-overlay">
            <div class="info-top-left">
              <span class="device-name">{{ slot.device.deviceName }}</span>
              <span class="device-no">{{ slot.device.deviceNo }}</span>
            </div>
            <div class="info-top-right">
              <span class="status-dot" :class="'status-' + slot.device.status" />
              <span class="status-text">{{ statusLabel(slot.device.status) }}</span>
            </div>
            <div class="info-bottom-left">
              <i class="el-icon-location-outline" />
              <span>{{ slot.device.location || '未知位置' }}</span>
            </div>
            <div class="info-bottom-right">
              <span class="live-time">{{ currentTime }}</span>
            </div>
          </div>

          <!-- REC 指示器 -->
          <div v-if="slot.device.status === '1'" class="rec-indicator">
            <span class="rec-dot" />
            <span>REC</span>
          </div>
        </template>

        <!-- 无设备（空位） -->
        <template v-else>
          <div class="empty-slot">
            <i class="el-icon-video-camera" />
            <span>无信号</span>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import { listDevice } from '@/api/monitor/device'

// 每种区域类型对应的真实场景图片（Unsplash 免费图片）
const SCENE_IMAGES = {
  // 1=原料库 — 仓库货架
  '1': [
    'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800&h=600&fit=crop&q=60',
    'https://images.unsplash.com/photo-1553877522-43269d4ea984?w=800&h=600&fit=crop&q=60'
  ],
  // 2=生产车间 — 工厂产线
  '2': [
    'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=800&h=600&fit=crop&q=60',
    'https://images.unsplash.com/photo-1565043666747-69f6646db940?w=800&h=600&fit=crop&q=60'
  ],
  // 3=成品库 — 货物堆放
  '3': [
    'https://images.unsplash.com/photo-1587293852726-70cdb56c2866?w=800&h=600&fit=crop&q=60',
    'https://images.unsplash.com/photo-1600880292203-757bb62b4baf?w=800&h=600&fit=crop&q=60'
  ],
  // 4=大门 — 停车场/入口
  '4': [
    'https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?w=800&h=600&fit=crop&q=60',
    'https://images.unsplash.com/photo-1621929747188-0b4dc28498d2?w=800&h=600&fit=crop&q=60'
  ],
  // 5=办公区 — 办公室
  '5': [
    'https://images.unsplash.com/photo-1497366216548-37526070297c?w=800&h=600&fit=crop&q=60',
    'https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=800&h=600&fit=crop&q=60'
  ],
  // 6=其他 — 走廊/通道
  '6': [
    'https://images.unsplash.com/photo-1572025442646-866d16c84a54?w=800&h=600&fit=crop&q=60',
    'https://images.unsplash.com/photo-1497366754035-f200968a6e72?w=800&h=600&fit=crop&q=60'
  ]
}

export default {
  name: 'LiveMonitor',
  data() {
    return {
      deviceList: [],
      gridMode: '4',
      currentTime: '',
      timeTimer: null,
      noiseTimer: null
    }
  },
  computed: {
    onlineCount() {
      return this.deviceList.filter(d => d.status === '1').length
    },
    gridSlots() {
      const count = parseInt(this.gridMode)
      const slots = []
      for (let i = 0; i < count; i++) {
        slots.push({
          device: this.deviceList[i] || null
        })
      }
      return slots
    }
  },
  created() {
    this.loadDevices()
    this.startTimeClock()
  },
  mounted() {
    this.$nextTick(() => {
      this.startNoiseAnimation()
    })
  },
  beforeDestroy() {
    if (this.timeTimer) clearInterval(this.timeTimer)
    if (this.noiseTimer) clearInterval(this.noiseTimer)
  },
  methods: {
    /** 加载设备列表 */
    loadDevices() {
      listDevice({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.deviceList = response.rows || []
        this.$nextTick(() => {
          this.startNoiseAnimation()
        })
      })
    },
    /** 根据设备区域类型和索引获取场景图片 */
    getSceneImage(device, index) {
      const areaType = device.areaType || '6'
      const images = SCENE_IMAGES[areaType] || SCENE_IMAGES['6']
      return images[index % images.length]
    },
    /** 状态文本 */
    statusLabel(status) {
      const map = { '1': '在线', '2': '离线', '3': '故障' }
      return map[status] || '未知'
    },
    /** 时间时钟 */
    startTimeClock() {
      this.updateTime()
      this.timeTimer = setInterval(() => {
        this.updateTime()
      }, 1000)
    },
    updateTime() {
      const now = new Date()
      const y = now.getFullYear()
      const mo = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const hh = String(now.getHours()).padStart(2, '0')
      const mm = String(now.getMinutes()).padStart(2, '0')
      const ss = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = y + '-' + mo + '-' + d + ' ' + hh + ':' + mm + ':' + ss
    },
    /** 噪点动画 */
    startNoiseAnimation() {
      if (this.noiseTimer) clearInterval(this.noiseTimer)
      this.noiseTimer = setInterval(() => {
        this.drawNoise()
      }, 180)
    },
    drawNoise() {
      const count = parseInt(this.gridMode)
      for (let i = 0; i < count; i++) {
        const refKey = 'noise_' + i
        const refArr = this.$refs[refKey]
        if (!refArr || !refArr.length) continue
        const canvas = refArr[0]
        if (!canvas) continue
        const parent = canvas.parentElement
        if (!parent) continue

        const w = parent.clientWidth
        const h = parent.clientHeight
        if (w === 0 || h === 0) continue

        // 用较小的 canvas 尺寸降低性能消耗，CSS 会拉伸
        const scale = 0.25
        canvas.width = Math.floor(w * scale)
        canvas.height = Math.floor(h * scale)

        const ctx = canvas.getContext('2d')
        const cw = canvas.width
        const ch = canvas.height

        // 透明底
        ctx.clearRect(0, 0, cw, ch)

        // 稀疏噪点
        const imgData = ctx.createImageData(cw, ch)
        const data = imgData.data
        for (let p = 0; p < data.length; p += 4) {
          if (Math.random() < 0.04) {
            const v = Math.floor(Math.random() * 80) + 40
            data[p] = v
            data[p + 1] = v
            data[p + 2] = v
            data[p + 3] = Math.floor(Math.random() * 40) + 15
          }
        }
        ctx.putImageData(imgData, 0, 0)
      }
    },
    handleGridChange() {
      this.$nextTick(() => {
        this.startNoiseAnimation()
      })
    }
  }
}
</script>

<style scoped>
.live-monitor-container {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  background: #0d1117;
  padding: 12px;
  box-sizing: border-box;
}

/* 工具栏 */
.monitor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #161b22;
  border-radius: 6px;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.toolbar-left {
  display: flex;
  align-items: center;
}
.toolbar-title {
  color: #e6edf3;
  font-size: 16px;
  font-weight: 600;
}
.toolbar-title i {
  margin-right: 6px;
  color: #58a6ff;
}
.toolbar-right {
  display: flex;
  align-items: center;
}

/* 网格 */
.monitor-grid {
  flex: 1;
  display: grid;
  gap: 8px;
  min-height: 0;
}
.grid-4 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
}
.grid-9 {
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr;
}

/* 单个监控格子 */
.monitor-cell {
  position: relative;
  background: #0a0e14;
  border: 1px solid #30363d;
  border-radius: 4px;
  overflow: hidden;
  min-height: 0;
}
.monitor-cell:hover {
  border-color: #58a6ff;
  box-shadow: 0 0 12px rgba(88, 166, 255, 0.15);
}
.cell-offline {
  border-color: #f85149;
}
.cell-offline:hover {
  border-color: #f85149;
  box-shadow: 0 0 12px rgba(248, 81, 73, 0.15);
}

/* 视频区域 */
.video-area {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

/* 真实场景图片 */
.scene-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 监控摄像头色调：降低亮度、对比度，轻微去饱和 */
  filter: brightness(0.55) contrast(1.15) saturate(0.5);
}
.img-offline {
  /* 离线/故障设备：灰度 + 更暗 */
  filter: brightness(0.3) contrast(1.1) saturate(0) blur(1px);
}

/* 监控色调滤镜（绿色夜视效果叠加） */
.cctv-filter {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    rgba(0, 30, 20, 0.3) 0%,
    rgba(0, 15, 10, 0.15) 50%,
    rgba(0, 25, 15, 0.25) 100%
  );
  pointer-events: none;
}

/* 噪点 Canvas */
.noise-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0.6;
  mix-blend-mode: screen;
}

/* 扫描线 */
.scanline {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.06), transparent);
  animation: scanMove 4s linear infinite;
  pointer-events: none;
}
@keyframes scanMove {
  0% { top: 0; }
  100% { top: 100%; }
}

/* CRT 横纹效果 */
.crt-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent 2px,
    rgba(0, 0, 0, 0.08) 2px,
    rgba(0, 0, 0, 0.08) 4px
  );
  pointer-events: none;
}

/* 暗角效果 */
.video-area::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
    ellipse at center,
    transparent 40%,
    rgba(0, 0, 0, 0.5) 100%
  );
  pointer-events: none;
}

/* 离线 / 故障覆盖 */
.offline-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.55);
  z-index: 5;
}
.offline-overlay i {
  font-size: 40px;
  color: #f85149;
  margin-bottom: 8px;
}
.offline-overlay span {
  color: #f85149;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 2px;
}

/* 信息叠加层 */
.info-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 6;
}
.info-top-left {
  position: absolute;
  top: 10px;
  left: 12px;
  display: flex;
  flex-direction: column;
}
.device-name {
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9), 0 0 8px rgba(0, 0, 0, 0.5);
}
.device-no {
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  margin-top: 2px;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9);
}
.info-top-right {
  position: absolute;
  top: 10px;
  right: 12px;
  display: flex;
  align-items: center;
}
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 5px;
}
.status-1 {
  background: #3fb950;
  box-shadow: 0 0 6px rgba(63, 185, 80, 0.8);
  animation: statusPulse 2s ease-in-out infinite;
}
.status-2 {
  background: #6e7681;
}
.status-3 {
  background: #f85149;
  box-shadow: 0 0 6px rgba(248, 81, 73, 0.8);
  animation: statusPulse 1s ease-in-out infinite;
}
@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
.status-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9);
}
.info-bottom-left {
  position: absolute;
  bottom: 10px;
  left: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9);
}
.info-bottom-left i {
  margin-right: 4px;
}
.info-bottom-right {
  position: absolute;
  bottom: 10px;
  right: 12px;
}
.live-time {
  color: #fff;
  font-size: 12px;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9), 0 0 8px rgba(0, 0, 0, 0.5);
}

/* REC 指示器 */
.rec-indicator {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  z-index: 7;
  pointer-events: none;
}
.rec-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f85149;
  margin-right: 4px;
  box-shadow: 0 0 6px rgba(248, 81, 73, 0.8);
  animation: recBlink 1s step-end infinite;
}
@keyframes recBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.rec-indicator span {
  color: #f85149;
  font-size: 12px;
  font-weight: 700;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9);
}

/* 空位 */
.cell-empty {
  border-style: dashed;
  border-color: #21262d;
}
.empty-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #30363d;
}
.empty-slot i {
  font-size: 40px;
  margin-bottom: 8px;
}
.empty-slot span {
  font-size: 14px;
  font-family: 'Courier New', Courier, monospace;
}

/* 深色主题下覆盖 Element 组件样式 */
.live-monitor-container >>> .el-radio-button__inner {
  background: #21262d;
  border-color: #30363d;
  color: #8b949e;
}
.live-monitor-container >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: #1f6feb;
  border-color: #1f6feb;
  color: #fff;
}
.live-monitor-container >>> .el-button--default {
  background: #21262d;
  border-color: #30363d;
  color: #8b949e;
}
.live-monitor-container >>> .el-button--default:hover {
  background: #30363d;
  border-color: #58a6ff;
  color: #58a6ff;
}
</style>
