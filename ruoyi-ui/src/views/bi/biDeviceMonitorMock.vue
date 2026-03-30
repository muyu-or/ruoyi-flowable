<template>
  <div class="bi-dash-frame" ref="frameRef">
    <div class="bi-dash-stage" ref="stageRef" :style="moveStyle">
      <div class="bi-dash-stage-scale" :style="scaleOnlyStyle">
        <div class="smart-device-dashboard">
        <div class="scan"></div>
        <div class="dashboard">
          <header class="header">
            <div class="line"></div>
            <div class="title-shell">
              <h1>智慧智能设备监管中心</h1>
            </div>
            <div class="line"></div>
          </header>

          <section class="panel left-top">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>

            <div class="panel">
              <span class="cut-corner lt"></span>
              <span class="cut-corner rt"></span>
              <span class="cut-corner lb"></span>
              <span class="cut-corner rb"></span>
              <div class="inner">
                <h3 class="panel-title">首检通过率 / 一次合格率</h3>
                <div class="counter-label">质量合格情况</div>
                <div class="digits" id="onlineDigits"></div>
                <div class="ring-wrap">
                  <div class="ring"></div>
                  <div class="ring-text">
                    <strong id="onlineRate">75%</strong>一次合格率
                  </div>
                </div>
              </div>
            </div>

            <div class="panel">
              <span class="cut-corner lt"></span>
              <span class="cut-corner rt"></span>
              <span class="cut-corner lb"></span>
              <span class="cut-corner rb"></span>
              <div class="inner">
                <h3 class="panel-title">风险雷达 & 预警响应时间</h3>
                <div class="triple-stats">
                  <div class="mini-stat">
                    <strong>70438</strong><span>故障总数</span>
                  </div>
                  <div class="mini-stat">
                    <strong>99</strong><span>本月故障总数</span>
                  </div>
                  <div class="mini-stat">
                    <strong>↑2%</strong><span>较上月</span>
                  </div>
                </div>
            <div class="chart" id="riskRadarChart" ref="riskRadarChartDom" />
                <div class="grid-table" id="warningTable"></div>
              </div>
            </div>
          </section>

          <section class="panel top-strip">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>
            <div class="status-grid" id="statusGrid"></div>
          </section>

          <section class="panel right-top">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>

            <div class="panel">
              <span class="cut-corner lt"></span>
              <span class="cut-corner rt"></span>
              <span class="cut-corner lb"></span>
              <span class="cut-corner rb"></span>
              <div class="inner">
                <h3 class="panel-title">外部接入设备</h3>
                <div class="counter-label">统一接入统计</div>
                <div class="digits" id="externalDigits"></div>
                <div style="font-size:14px;color:#9ce6f1;margin-bottom:12px;">运行情况</div>
                <div class="progress-wrap" id="progressWrap"></div>
              </div>
            </div>

            <div class="panel">
              <span class="cut-corner lt"></span>
              <span class="cut-corner rt"></span>
              <span class="cut-corner lb"></span>
              <span class="cut-corner rb"></span>
              <div class="inner">
                <h3 class="panel-title">能耗统计</h3>
                <div class="select-tag">2019年 ▾</div>
                <div class="energy-metrics">
                  <div class="energy-card"><strong>7038</strong><span>kWh 电耗</span></div>
                  <div class="energy-card"><strong>450</strong><span>m³ 水耗</span></div>
                  <div class="energy-card"><strong>667</strong><span>m³ 气耗</span></div>
                </div>
                <svg class="chart" id="energyChart" viewBox="0 0 300 120" preserveAspectRatio="none"></svg>
              </div>
            </div>
          </section>

          <section class="panel center-main">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>
            <div class="inner" style="padding:10px;">
              <div class="center-shell">
                <div class="center-title">设备监管</div>

                <div class="chip" style="left: 56px; top: 86px;"><span class="badge">⚡</span>数据中心</div>
                <div class="chip" style="right: 56px; top: 86px;"><span class="badge">🧊</span>预警中心</div>
                <div class="chip" style="left: 56px; bottom: 108px;"><span class="badge">⌘</span>重点场所</div>
                <div class="chip" style="right: 56px; bottom: 108px;"><span class="badge">▥</span>设备中心</div>

                <div class="metric-box" style="left: 31%; top: 158px;"><strong id="metricFirstPass">75/100</strong><span id="metricFirstPassLabel">首检通过率</span></div>
                <div class="metric-box" style="left: 35%; top: 252px;"><strong id="metricOnceQualified">34</strong><span id="metricOnceQualifiedLabel">一次合格率</span></div>
                <div class="metric-box" style="right: 31%; top: 158px;"><strong id="metricCorr">0.78</strong><span id="metricCorrLabel">相关系数r</span></div>
                <div class="metric-box" style="right: 35%; top: 252px;"><strong id="metricStability">0.12</strong><span id="metricStabilityLabel">稳定性波动率</span></div>

                <div class="hub-zone">
                  <div class="hub-outer"></div>
                  <div class="hub-middle"></div>
                  <div class="hub-core">🏙️</div>
                </div>

                <div class="runtime">
                  <div class="days" id="safeDays">34522天</div>
                  <div class="tag">已安全运行</div>
                </div>
              </div>
            </div>
          </section>

          <section class="panel left-bottom">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>
            <div class="inner">
              <h3 class="panel-title">节点瓶颈Top（P50/P90）</h3>
              <div class="grid-table" id="latestLogs"></div>
            </div>
          </section>

          <section class="panel center-bottom">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>
            <div class="inner">
              <div class="select-tag">2020.05 ▾</div>
              <h3 class="panel-title">供给波动与产能相关性</h3>
              <div class="bottom-shell">
                <div class="legend"><span class="people">人流量</span><span class="car">车流量</span></div>
                <div class="chart" style="height:120px;" id="supplyDemandCorrChart" ref="corrChartDom" />
                <div class="time-board" id="timeBoard">2021.09.09 星期四 12:59<small>实时数据刷新中</small></div>
              </div>
            </div>
          </section>

          <section class="panel right-bottom">
            <span class="cut-corner lt"></span>
            <span class="cut-corner rt"></span>
            <span class="cut-corner lb"></span>
            <span class="cut-corner rb"></span>
            <div class="inner">
              <h3 class="panel-title">班组效率稳定性Top（方差/波动率）</h3>
              <div class="grid-table" id="repairTable"></div>
            </div>
          </section>
        </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

const data = {
  onlineTotal: 75,
  externalTotal: 2643,
  onlineRate: 75,
  safeDays: 34522,
  status: [],
  progress: [],
  fault: {
    labels: ['车辆', '会议', '能耗', '发布', '物业', '考勤', '访客', '安防', '餐饮'],
    current: [31, 21, 40, 25, 43, 18, 36, 28, 30],
    last: [23, 44, 19, 37, 27, 41, 29, 35, 22]
  },
  energy: [18, 45, 32, 50, 57, 36, 73, 44, 69, 42, 71, 48],
  flow: {
    people: [498, 637, 343, 590, 998, 998, 998, 624, 808, 740, 496, 312, 808, 414, 544, 500, 808, 444, 482, 760, 760, 128, 326, 358, 791, 389, 808, 808, 656, 428],
    car: [260, 410, 190, 320, 720, 680, 700, 450, 620, 540, 320, 210, 610, 280, 390, 360, 650, 290, 310, 520, 510, 92, 210, 240, 570, 260, 610, 620, 470, 300]
  }
}

export default {
  name: 'BiDeviceMonitorMock',
  data: function() {
    return {
      timer: null,
      clockTimer: null,
      scaleX: 1,
      scaleY: 1,
      designW: 1568,
      designH: 890,
      mockData: null,
      indicators: null,
      chartInstances: {},
      prevTagsView: null,
      prevSidebarHide: null
    }
  },
  computed: {
    moveStyle: function() {
      return {
        width: '100%',
        height: '100%',
        position: 'relative',
        overflow: 'hidden'
      }
    },
    scaleOnlyStyle: function() {
      return {
        position: 'absolute',
        left: '50%',
        top: '50%',
        width: this.designW + 'px',
        height: this.designH + 'px',
        transform: `translate(-50%, -50%) scale(${this.scaleX}, ${this.scaleY})`,
        transformOrigin: 'center center'
      }
    }
  },
  mounted: function() {
    var self = this
    this.$nextTick(function() {
      if (self.$store) {
        self.prevTagsView = self.$store.state && self.$store.state.settings ? self.$store.state.settings.tagsView : null
        self.prevSidebarHide = self.$store.state && self.$store.state.app && self.$store.state.app.sidebar ? self.$store.state.app.sidebar.hide : null
        self.$store.dispatch('app/toggleSideBarHide', true)
        self.$store.dispatch('settings/changeSetting', { key: 'tagsView', value: false })
      }
      self.updateScale()
      self._onResize = function() {
        self.updateScale()
        if (self.chartInstances) {
          Object.keys(self.chartInstances).forEach(function(k) {
            if (self.chartInstances[k] && self.chartInstances[k].resize) {
              self.chartInstances[k].resize()
            }
          })
        }
      }
      window.addEventListener('resize', self._onResize)
      self.initMock()
      self.timer = setInterval(function() { self.initMock() }, 10000)
      self.clockTimer = setInterval(function() { self.updateClock() }, 1000)
    })
  },
  beforeDestroy: function() {
    if (this.timer) clearInterval(this.timer)
    if (this.clockTimer) clearInterval(this.clockTimer)
    if (this._onResize) window.removeEventListener('resize', this._onResize)

    if (this.$store) {
      if (this.prevSidebarHide !== null && this.prevSidebarHide !== undefined) {
        this.$store.dispatch('app/toggleSideBarHide', this.prevSidebarHide)
      } else {
        this.$store.dispatch('app/toggleSideBarHide', false)
      }
      if (this.prevTagsView !== null && this.prevTagsView !== undefined) {
        this.$store.dispatch('settings/changeSetting', { key: 'tagsView', value: this.prevTagsView })
      }
    }
  },
  methods: {
    updateScale: function() {
      // 强制获取整个窗口的高度和宽度，解决因左侧导航栏导致留白的问题
      var frameW = window.innerWidth
      var frameH = window.innerHeight
      if (!frameW || !frameH) return
      
      // 使用独立的 X 和 Y 缩放比例，使得画面彻底铺满整屏，杜绝黑边与留白
      this.scaleX = frameW / this.designW
      this.scaleY = frameH / this.designH
    },
    generateMock: function() {
      var nodes = ['原料入库', '预处理', '真空', '烘烤', '检测', '产品入库']
      var teams = ['班组A', '班组B', '班组C', '班组D', '班组E']
      var firstPassRate = 0.78 + Math.random() * 0.18
      var onceQualifiedRate = Math.max(0.65, firstPassRate - (Math.random() * 0.04))

      var len = 24, inventoryTrend = [], capacityTrend = []
      var inv = 0.55 + Math.random() * 0.25
      for (var i = 0; i < len; i++) {
        inv = Math.max(0.35, Math.min(0.95, inv + (Math.random() - 0.5) * 0.04))
        var inventoryHealth = inv * 100
        var cap = inventoryHealth * (0.85 + Math.random() * 0.25) + (Math.random() - 0.5) * 18
        capacityTrend.push(Math.max(0, cap))
        inventoryTrend.push(inventoryHealth)
      }

      var warnings = [], now = Date.now()
      var warnCount = 90 + Math.floor(Math.random() * 60)
      for (var w = 0; w < warnCount; w++) {
        var node = nodes[Math.floor(Math.random() * nodes.length)]
        var team = teams[Math.floor(Math.random() * teams.length)]
        var createTime = now - Math.floor(Math.random() * 6 * 60) * 60 * 1000
        var resolved = Math.random() > 0.15
        var responseMins = (8 + Math.random() * 35) * (0.85 + (nodes.indexOf(node) / 5) * 0.55)
        var endTime = resolved ? createTime + Math.floor(responseMins * 60 * 1000) : null
        warnings.push({ nodeName: node, teamName: team, createTime: createTime, endTime: endTime, resolved: resolved })
      }

      var nodeDurations = {}, teamDurations = {}
      nodes.forEach(function(n) {
        var arr = [], nodeFactor = 0.85 + (nodes.indexOf(n) / 5) * 0.8
        for (var i = 0; i < 80 + Math.floor(Math.random() * 40); i++) arr.push(Math.round((20 + Math.random() * 140) * nodeFactor))
        nodeDurations[n] = arr
      })
      teams.forEach(function(t) {
        var arr2 = [], teamFactor = 0.85 + Math.random() * 0.7
        for (var j = 0; j < 120 + Math.floor(Math.random() * 60); j++) arr2.push(Math.round(((30 + Math.random() * 160) * teamFactor) * (0.85 + Math.random() * 0.3)))
        teamDurations[t] = arr2
      })

      return { nodes: nodes, teams: teams, quality: { firstPassRate: firstPassRate, onceQualifiedRate: onceQualifiedRate }, inventoryHealthTrend: inventoryTrend, capacityTrend: capacityTrend, warnings: warnings, nodeDurations: nodeDurations, teamDurations: teamDurations }
    },
    _pearson: function(x, y) {
      if (!x || !y || x.length !== y.length || x.length === 0) return 0
      var n = x.length, sumX = 0, sumY = 0
      for (var i = 0; i < n; i++) { sumX += x[i]; sumY += y[i]; }
      var meanX = sumX / n, meanY = sumY / n, num = 0, den1 = 0, den2 = 0
      for (var j = 0; j < n; j++) {
        var dx = x[j] - meanX, dy = y[j] - meanY
        num += dx * dy; den1 += dx * dx; den2 += dy * dy
      }
      var den = Math.sqrt(den1) * Math.sqrt(den2)
      return den ? (num / den) : 0
    },
    _quantile: function(arr, q) {
      if (!arr || !arr.length) return 0
      var a = arr.slice().sort(function(m, n) { return m - n })
      var pos = (a.length - 1) * q, base = Math.floor(pos), rest = pos - base
      return a[base + 1] !== undefined ? a[base] + rest * (a[base + 1] - a[base]) : a[base]
    },
    _mean: function(arr) {
      if (!arr || !arr.length) return 0
      var s = 0; for (var i = 0; i < arr.length; i++) s += arr[i]; return s / arr.length
    },
    _std: function(arr) {
      if (!arr || arr.length < 2) return 0
      var mean = this._mean(arr), v = 0
      for (var i = 0; i < arr.length; i++) { var d = arr[i] - mean; v += d * d; }
      return Math.sqrt(v / (arr.length - 1))
    },
    computeIndicators: function(mock) {
      if (!mock) return {}
      var indicators = { firstPassRate: mock.quality.firstPassRate, onceQualifiedRate: mock.quality.onceQualifiedRate }
      var resolvedWarnings = mock.warnings.filter(function(w) { return w.resolved && w.endTime })
      indicators.avgWarningResponseMins = resolvedWarnings.length ? this._mean(resolvedWarnings.map(function(w) { return (w.endTime - w.createTime) / 60000 })) : 0

      var nodeAgg = {}
      mock.nodes.forEach(function(n) { nodeAgg[n] = { nodeName: n, count: 0, avgResponse: 0 } })
      resolvedWarnings.forEach(function(w) { nodeAgg[w.nodeName].count++ })
      mock.nodes.forEach(function(n) {
        var rr = resolvedWarnings.filter(function(w) { return w.nodeName === n })
        if (rr.length) nodeAgg[n].avgResponse = this._mean(rr.map(function(w) { return (w.endTime - w.createTime) / 60000 }))
      }, this)
      indicators.topWarningNodes = Object.keys(nodeAgg).map(function(k) { return nodeAgg[k] }).sort(function(a, b) { return b.count - a.count }).slice(0, 6)

      var teamAgg = {}
      mock.teams.forEach(function(t) { teamAgg[t] = { teamName: t, count: 0, avgResponse: 0 } })
      resolvedWarnings.forEach(function(w) { teamAgg[w.teamName].count++ })
      mock.teams.forEach(function(t) {
        var rr2 = resolvedWarnings.filter(function(w) { return w.teamName === t })
        if (rr2.length) teamAgg[t].avgResponse = this._mean(rr2.map(function(w) { return (w.endTime - w.createTime) / 60000 }))
      }, this)
      indicators.topWarningTeams = Object.keys(teamAgg).map(function(k) { return teamAgg[k] }).sort(function(a, b) { return b.count - a.count }).slice(0, 6)

      indicators.supplyDemandCorrR = this._pearson(mock.inventoryHealthTrend, mock.capacityTrend)

      var stabilityList = []
      mock.teams.forEach(function(t) {
        var arr = mock.teamDurations[t] || [], mean = this._mean(arr), std = this._std(arr)
        stabilityList.push({ teamName: t, meanSec: mean, stdSec: std, cv: mean ? (std / mean) : 0 })
      }, this)
      indicators.teamStabilityTop = stabilityList.sort(function(a, b) { return b.cv - a.cv }).slice(0, 4) // 控制数据量避免撑破容器

      var bottleneckList = []
      mock.nodes.forEach(function(n) {
        var arrB = mock.nodeDurations[n] || []
        bottleneckList.push({ nodeName: n, p50Sec: this._quantile(arrB, 0.5), p90Sec: this._quantile(arrB, 0.9) })
      }, this)
      indicators.nodeBottleneckTop = bottleneckList.sort(function(a, b) { return b.p90Sec - a.p90Sec }).slice(0, 4) // 控制数据量避免撑破容器

      var maxCount = Math.max.apply(null, indicators.topWarningNodes.map(function(n) { return n.count })) || 1
      var maxResp = Math.max.apply(null, indicators.topWarningNodes.map(function(n) { return n.avgResponse })) || 1
      indicators.riskRadar = {
        categories: indicators.topWarningNodes.map(function(n) { return n.nodeName }),
        values: indicators.topWarningNodes.map(function(n) {
          return Math.max(0, Math.min(100, 0.55 * (n.count / maxCount) * 100 + 0.45 * (n.avgResponse / maxResp) * 100))
        })
      }
      return indicators
    },
    initCharts: function() {
      if (!this.$refs.riskRadarChartDom || !this.$refs.corrChartDom) return
      if (!this.chartInstances) this.chartInstances = {}
      if (!this.chartInstances.riskRadar) this.chartInstances.riskRadar = echarts.init(this.$refs.riskRadarChartDom, 'macarons')
      if (!this.chartInstances.corr) this.chartInstances.corr = echarts.init(this.$refs.corrChartDom, 'macarons')
    },
    renderRiskRadar: function() {
      if (!this.chartInstances || !this.chartInstances.riskRadar) return
      var chart = this.chartInstances.riskRadar, risk = this.indicators && this.indicators.riskRadar ? this.indicators.riskRadar : null
      if (!risk || !risk.categories || !risk.values || !risk.categories.length) return
      chart.setOption({
        backgroundColor: 'transparent',
        tooltip: { show: true },
        radar: {
          radius: '70%', center: ['50%', '55%'],
          indicator: risk.categories.map(function(name) { return { name: name, max: 100 } }),
          axisLine: { lineStyle: { color: 'rgba(0,175,255,0.18)' } },
          splitLine: { lineStyle: { color: 'rgba(0,175,255,0.10)' } },
          splitArea: { areaStyle: { color: ['rgba(0,175,255,0.02)', 'rgba(0,175,255,0.05)'] } },
          name: { textStyle: { color: 'rgba(226,243,255,0.72)', fontSize: 11 } }
        },
        series: [{ type: 'radar', data: [{ value: risk.values, name: 'risk', areaStyle: { color: 'rgba(0,175,255,0.10)' }, lineStyle: { color: 'rgba(71,243,255,0.85)', width: 2 }, itemStyle: { color: 'rgba(71,243,255,0.85)' } }] }]
      }, true)
    },
    renderSupplyDemandCorr: function() {
      if (!this.chartInstances || !this.chartInstances.corr || !this.mockData) return
      var chart = this.chartInstances.corr, md = this.mockData, xLabels = []
      for (var i = 0; i < md.inventoryHealthTrend.length; i++) xLabels.push(i + 1)
      var capMax = Math.max.apply(null, md.capacityTrend) || 1, capMin = Math.min.apply(null, md.capacityTrend) || 0, capPad = (capMax - capMin) * 0.1
      chart.setOption({
        backgroundColor: 'transparent', tooltip: { trigger: 'axis' },
        legend: { bottom: 6, textStyle: { color: 'rgba(226,243,255,0.70)' }, data: ['库存健康度', '产能趋势'] },
        grid: { left: 44, right: 18, top: 24, bottom: 26 },
        xAxis: { type: 'category', data: xLabels, axisLine: { lineStyle: { color: 'rgba(0,175,255,0.12)' } }, axisLabel: { color: 'rgba(226,243,255,0.45)', fontSize: 10 } },
        yAxis: [
          { type: 'value', name: '健康度', min: 0, max: 100, axisLine: { show: false }, axisLabel: { color: 'rgba(226,243,255,0.45)', fontSize: 10 }, splitLine: { lineStyle: { color: 'rgba(0,175,255,0.08)' } } },
          { type: 'value', name: '产能', min: capMin - capPad, max: capMax + capPad, axisLine: { show: false }, axisLabel: { color: 'rgba(226,243,255,0.45)', fontSize: 10 }, splitLine: { show: false } }
        ],
        graphic: [{ type: 'text', left: '50%', top: 8, style: { text: '相关系数 r=' + (Math.round((this.indicators.supplyDemandCorrR || 0) * 100) / 100), fill: 'rgba(226,243,255,0.85)', fontSize: 12, fontWeight: 900 } }],
        series: [
          { name: '库存健康度', type: 'line', smooth: true, yAxisIndex: 0, showSymbol: false, lineStyle: { width: 2, color: 'rgba(0,175,255,0.95)' }, areaStyle: { color: 'rgba(0,175,255,0.10)' }, data: md.inventoryHealthTrend },
          { name: '产能趋势', type: 'line', smooth: true, yAxisIndex: 1, showSymbol: false, lineStyle: { width: 2, color: 'rgba(38,222,129,0.90)' }, areaStyle: { color: 'rgba(38,222,129,0.08)' }, data: md.capacityTrend }
        ]
      }, true)
    },
    renderDigits: function(id, value, size = 5) {
      const s = String(value).padStart(size, '0'), el = document.getElementById(id)
      if (el) el.innerHTML = s.split('').map(function(n) { return '<div class="digit">' + n + '</div>' }).join('')
    },
    renderStatus: function() {
      var el = document.getElementById('statusGrid')
      if (el) el.innerHTML = data.status.map(function(item) {
        return '<div class="status-card"><div class="count">' + item.count + '</div><div class="icon">' + item.icon + '</div><div class="name">' + item.name + '</div></div>'
      }).join('')
    },
    renderProgress: function() {
      var el = document.getElementById('progressWrap')
      if (el) el.innerHTML = data.progress.map(function(item) {
        return '<div class="progress-item"><span>' + item.name + '</span><div class="track"><div class="bar" style="width:' + item.value + '%"></div></div><span>' + item.value + '%</span></div>'
      }).join('')
    },
    renderTable: function(id, head1, head2, head3, rows) {
      var el = document.getElementById(id)
      if (el) el.innerHTML = '<div class="head"><span>' + head1 + '</span><span>' + head2 + '</span><span>' + head3 + '</span></div>' + rows.map(function(row) {
        return '<div class="item"><span>' + row[0] + '</span><span>' + row[1] + '</span><span>' + row[2] + '</span></div>'
      }).join('')
    },
    drawGrid: function(svg, w, h, sx, sy) {
      var out = ''
      for (var x = 0; x <= w; x += sx) out += '<line x1="' + x + '" y1="0" x2="' + x + '" y2="' + h + '" stroke="rgba(82,244,255,.06)"/>'
      for (var y = 0; y <= h; y += sy) out += '<line x1="0" y1="' + y + '" x2="' + w + '" y2="' + y + '" stroke="rgba(82,244,255,.06)"/>'
      svg.insertAdjacentHTML('beforeend', out)
    },
    drawLine: function(svgId, s1, s2, labels) {
      var svg = document.getElementById(svgId); if (!svg) return
      var w = 300, h = 120, p = { l: 10, r: 10, t: 10, b: 24 }, iw = w - p.l - p.r, ih = h - p.t - p.b
      var max = Math.max.apply(null, s1.concat(s2 || []).map(function(v) { return v; })) * 1.1

      svg.innerHTML = '<defs><filter id="glow1"><feGaussianBlur stdDeviation="2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter><filter id="glow2"><feGaussianBlur stdDeviation="2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter></defs>'
      this.drawGrid(svg, w, h, 30, 24)

      var drawSeries = function(arr, color, filterId) {
        var path = arr.map(function(v, i) {
          var x = p.l + iw * i / (arr.length - 1), y = p.t + ih - ih * v / max
          return (i === 0 ? 'M' : 'L') + ' ' + x + ' ' + y
        }).join(' ')
        svg.insertAdjacentHTML('beforeend', '<path d="' + path + '" fill="none" stroke="' + color + '" stroke-width="2.2" filter="url(#' + filterId + ')"/>')
        arr.forEach(function(v, i) {
          var x = p.l + iw * i / (arr.length - 1), y = p.t + ih - ih * v / max
          svg.insertAdjacentHTML('beforeend', '<circle cx="' + x + '" cy="' + y + '" r="3.1" fill="' + color + '"/>')
        })
      }

      drawSeries(s1, '#55f7ff', 'glow1')
      if (s2) drawSeries(s2, '#ff9b61', 'glow2')
      labels.forEach(function(lab, i) {
        svg.insertAdjacentHTML('beforeend', '<text x="' + (p.l + iw * i / (labels.length - 1)) + '" y="' + (h - 6) + '" fill="#77b3c1" font-size="10" text-anchor="middle">' + lab + '</text>')
      })
    },
    drawBars: function() {
      var svg = document.getElementById('flowChart'); if (!svg) return
      var people = data.flow.people, car = data.flow.car, w = 860, h = 140, p = { l: 8, r: 8, t: 8, b: 24 }, iw = w - p.l - p.r, ih = h - p.t - p.b
      var max = Math.max.apply(null, people.concat(car)) * 1.1, groupW = iw / people.length, bw = Math.min(10, groupW * 0.28)

      svg.innerHTML = ''; this.drawGrid(svg, w, h, 28, 24)
      people.forEach(function(v, i) {
        var cx = p.l + i * groupW + groupW / 2, h1 = ih * v / max, h2 = ih * car[i] / max, y1 = p.t + ih - h1, y2 = p.t + ih - h2
        svg.insertAdjacentHTML('beforeend', '<rect x="' + (cx - bw - 2) + '" y="' + y1 + '" width="' + bw + '" height="' + h1 + '" rx="1.5" fill="#54f7ff" opacity="0.92"/><rect x="' + (cx + 2) + '" y="' + y2 + '" width="' + bw + '" height="' + h2 + '" rx="1.5" fill="#6dffcf" opacity="0.88"/><text x="' + cx + '" y="' + (h - 7) + '" fill="#72afbc" font-size="9" text-anchor="middle">' + (i + 1) + '</text>')
      })
    },
    updateClock: function() {
      var d = new Date(), week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'], pad = function(n) { return String(n).padStart(2, '0') }
      var el = document.getElementById('timeBoard')
      if (el) el.innerHTML = d.getFullYear() + '.' + pad(d.getMonth() + 1) + '.' + pad(d.getDate()) + ' ' + week[d.getDay()] + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds()) + '<small>实时数据刷新中</small>'
    },
    initMock: function() {
      this.mockData = this.generateMock()
      this.indicators = this.computeIndicators(this.mockData)
      this.$nextTick(function() {
        this.initCharts()
        this.renderRiskRadar()
        this.renderSupplyDemandCorr()
      }.bind(this))

      var ind = this.indicators || {}, fpPct = Math.round((ind.firstPassRate || 0) * 100), oqPct = Math.round((ind.onceQualifiedRate || 0) * 100)
      this.renderDigits('onlineDigits', fpPct, 5)
      this.renderDigits('externalDigits', Math.round(ind.avgWarningResponseMins || 0), 5)
      if (document.getElementById('onlineRate')) document.getElementById('onlineRate').textContent = oqPct + '%'
      if (document.getElementById('metricFirstPass')) document.getElementById('metricFirstPass').textContent = fpPct + '/100'
      if (document.getElementById('metricOnceQualified')) document.getElementById('metricOnceQualified').textContent = oqPct + '%'
      if (document.getElementById('metricCorr')) document.getElementById('metricCorr').textContent = Math.round((ind.supplyDemandCorrR || 0) * 100) / 100
      if (document.getElementById('metricStability')) document.getElementById('metricStability').textContent = Math.round((ind.teamStabilityTop && ind.teamStabilityTop.length ? ind.teamStabilityTop[0].cv : 0) * 1000) / 1000
      if (document.getElementById('safeDays')) document.getElementById('safeDays').textContent = Math.round(ind.avgWarningResponseMins || 0) + 'm'

      var radarMap = {}
      if (ind.riskRadar && ind.riskRadar.categories) for (var i = 0; i < ind.riskRadar.categories.length; i++) radarMap[ind.riskRadar.categories[i]] = ind.riskRadar.values[i]
      var stageIconMap = { '原料入库': '⚡', '预处理': '⚙', '真空': '⛯', '烘烤': '⛭', '检测': '✦', '产品入库': '✓' }, statusList = []
      ;(ind.topWarningNodes || []).slice(0, 6).forEach(function(n) { statusList.push({ count: Math.round(radarMap[n.nodeName] || 0) + '/100', icon: stageIconMap[n.nodeName] || '⚡', name: n.nodeName }) })
      ;(ind.topWarningTeams || []).slice(0, 3).forEach(function(t) { statusList.push({ count: (t.count || 0) + '次', icon: '★', name: t.teamName }) })
      while (statusList.length < 9) statusList.push({ count: '--', icon: '·', name: '待补' + statusList.length })
      data.status = statusList.slice(0, 9)
      this.renderStatus()

      var bottleneckTop = ind.nodeBottleneckTop || [], maxP90 = 1
      for (var bp = 0; bp < bottleneckTop.length; bp++) maxP90 = Math.max(maxP90, bottleneckTop[bp].p90Sec || 0)
      data.progress = bottleneckTop.slice(0, 5).map(function(n) { return { name: n.nodeName, value: Math.max(0, Math.min(100, Math.round(100 - ((n.p90Sec || 0) / maxP90) * 100))) } })
      this.renderProgress()

      this.renderTable('warningTable', '预警节点', '响应(分钟)', '次数', (ind.topWarningNodes || []).map(function(n) { return [n.nodeName, (Math.round((n.avgResponse || 0) * 10) / 10) + 'm', (n.count || 0) + '次'] }))
      // 底部两个表最多渲染4行防止撑破界面导致内容切断
      this.renderTable('latestLogs', '节点', 'P50耗时', 'P90耗时', (ind.nodeBottleneckTop || []).map(function(n) { return [n.nodeName, Math.round((n.p50Sec || 0) / 60) + 'm', Math.round((n.p90Sec || 0) / 60) + 'm'] }))
      this.renderTable('repairTable', '班组', 'cv波动率', '均值耗时', (ind.teamStabilityTop || []).map(function(t) { return [t.teamName, Math.round((t.cv || 0) * 1000) / 10 + '%', Math.round((t.meanSec || 0) / 60 * 10) / 10 + 'm'] }))

      this.drawLine('faultChart', data.fault.current, data.fault.last, data.fault.labels)
      this.drawLine('energyChart', data.energy, null, ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'])
      this.drawBars()
    }
  }
}
</script>

<style>
/* ... (原样式保持不变的部分省略，下方只贴出需要修改的部分) ... */

.smart-device-dashboard {
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  color: #e6fbff;
  background:
    radial-gradient(circle at 50% 14%, rgba(0, 210, 255, 0.12), transparent 22%),
    radial-gradient(circle at 50% 100%, rgba(0, 106, 255, 0.08), transparent 30%),
    linear-gradient(180deg, #030913 0%, #04101d 52%, #02070f 100%);
  position: relative;
}

/* 其他原始样式补充在中间... （如 .ring, .digit, .panel等皆保持原样即可） */
.smart-device-dashboard::before { content: ""; position: absolute; inset: 0; background-image: linear-gradient(rgba(51, 222, 255, 0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(51, 222, 255, 0.06) 1px, transparent 1px); background-size: 30px 30px; mask-image: radial-gradient(circle at center, black 40%, transparent 100%); pointer-events: none; opacity: 0.45; }
.smart-device-dashboard::after { content: ""; position: absolute; inset: 0; background: linear-gradient(180deg, rgba(255,255,255,.02), transparent 16%, transparent 84%, rgba(255,255,255,.02)), radial-gradient(circle at center, transparent 48%, rgba(0,0,0,0.25) 100%); pointer-events: none; }
:root { --bg-0: #020812; --bg-1: #051426; --bg-2: #071f39; --panel: rgba(8, 24, 44, 0.78); --panel-2: rgba(5, 18, 35, 0.92); --line: rgba(47, 226, 255, 0.28); --line-strong: rgba(71, 243, 255, 0.72); --cyan: #52f4ff; --cyan-2: #8ffcff; --blue: #4a8dff; --green: #6dffcf; --yellow: #ffe27a; --text: #e6fbff; --muted: #7eb9c8; --shadow: 0 0 22px rgba(36, 220, 255, 0.12), inset 0 0 40px rgba(36, 220, 255, 0.035); }
* { box-sizing: border-box; }

.dashboard {
  width: 100%;
  height: 100%;
  padding: 16px 16px 12px;
  display: grid;
  grid-template-columns: 320px minmax(900px, 1fr) 320px;
  /* 修补: 将 minmax(420px, 1fr) 降为 minmax(0, 1fr)，确保不会撑破高度限制 */
  grid-template-rows: 104px 118px minmax(0, 1fr) 206px;
  gap: 14px;
  position: relative;
}

.scan { position: absolute; left: 0; right: 0; top: -90px; height: 90px; background: linear-gradient(180deg, transparent, rgba(82,244,255,.08), transparent); pointer-events: none; animation: scan 7s linear infinite; opacity: .65; }
@keyframes scan { 0% { transform: translateY(0); } 100% { transform: translateY(calc(100vh + 120px)); } }
.panel { position: relative; background: linear-gradient(180deg, rgba(8, 26, 48, 0.72), rgba(3, 13, 26, 0.92)); border: 1px solid var(--line); box-shadow: var(--shadow); overflow: hidden; min-height: 0; }
.panel::before { content: ""; position: absolute; inset: 0; background: linear-gradient(90deg, transparent, rgba(88, 244, 255, 0.03), transparent), linear-gradient(180deg, rgba(255,255,255,0.015), transparent 28%); pointer-events: none; }
.panel::after { content: ""; position: absolute; inset: 8px; border: 1px solid rgba(89, 245, 255, 0.06); pointer-events: none; }
.cut-corner { position: absolute; width: 18px; height: 18px; border-color: rgba(82, 244, 255, 0.75); border-style: solid; pointer-events: none; filter: drop-shadow(0 0 6px rgba(82,244,255,.35)); }
.lt { top: 6px; left: 6px; border-width: 2px 0 0 2px; } .rt { top: 6px; right: 6px; border-width: 2px 2px 0 0; } .lb { bottom: 6px; left: 6px; border-width: 0 0 2px 2px; } .rb { bottom: 6px; right: 6px; border-width: 0 2px 2px 0; }
.header { grid-column: 1 / 4; position: relative; display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; }
.header .line { height: 2px; position: relative; background: linear-gradient(90deg, transparent, rgba(82,244,255,.9), transparent); filter: drop-shadow(0 0 6px rgba(82,244,255,.35)); }
.title-shell { margin: 0 16px; min-width: 620px; height: 88px; display: grid; place-items: center; position: relative; clip-path: polygon(5% 0, 95% 0, 100% 35%, 96% 100%, 4% 100%, 0 35%); background: linear-gradient(180deg, rgba(6, 42, 68, 0.95), rgba(6, 24, 44, 0.88)); border: 1px solid rgba(82,244,255,.32); box-shadow: 0 0 24px rgba(82,244,255,.12), inset 0 0 22px rgba(82,244,255,.08); }
.title-shell::before, .title-shell::after { content: ""; position: absolute; top: 14px; width: 86px; height: 18px; background-image: radial-gradient(circle, rgba(82,244,255,.85) 1.2px, transparent 1.5px); background-size: 10px 10px; opacity: .55; }
.title-shell::before { left: 34px; } .title-shell::after { right: 34px; }
.title-shell h1 { margin: 0; font-size: 44px; letter-spacing: 4px; font-weight: 800; color: #b3ffff; text-shadow: 0 0 16px rgba(82,244,255,.45), 0 0 26px rgba(82,244,255,.22); }
.left-top { grid-column: 1; grid-row: 2 / 4; display: grid; grid-template-rows: 280px 1fr; gap: 14px; } .right-top { grid-column: 3; grid-row: 2 / 4; display: grid; grid-template-rows: 280px 1fr; gap: 14px; } .top-strip { grid-column: 2; grid-row: 2; } .center-main { grid-column: 2; grid-row: 3; } .left-bottom { grid-column: 1; grid-row: 4; } .center-bottom { grid-column: 2; grid-row: 4; } .right-bottom { grid-column: 3; grid-row: 4; }
.inner { padding: 16px 16px 12px; height: 100%; position: relative; }
.panel-title { margin: 0 0 14px; display: flex; align-items: center; gap: 10px; font-size: 21px; font-weight: 700; letter-spacing: 1px; color: var(--cyan-2); }
.panel-title::before, .panel-title::after { content: ""; width: 18px; height: 2px; background: linear-gradient(90deg, transparent, var(--cyan)); box-shadow: 0 0 8px rgba(82,244,255,.35); }
.counter-label { font-size: 16px; color: #a7f8ff; margin-bottom: 10px; }
.digits { display: flex; gap: 8px; margin-bottom: 18px; }
.digit { width: 46px; height: 56px; display: grid; place-items: center; font-size: 34px; font-weight: 800; color: #afffff; background: linear-gradient(180deg, rgba(0, 173, 255, 0.16), rgba(0, 76, 118, 0.36)); border: 1px solid rgba(82,244,255,.34); box-shadow: inset 0 0 10px rgba(82,244,255,.08), 0 0 10px rgba(82,244,255,.08); text-shadow: 0 0 10px rgba(82,244,255,.25); }
.ring-wrap { height: calc(100% - 92px); display: grid; place-items: center; position: relative; }
.ring { width: 168px; height: 168px; border-radius: 50%; background: conic-gradient(var(--yellow) 0 75%, rgba(106, 250, 255, 0.92) 75% 100%); display: grid; place-items: center; box-shadow: 0 0 26px rgba(82,244,255,.14); position: relative; animation: breathe 3.8s ease-in-out infinite; }
.ring::before { content: ""; width: 126px; height: 126px; border-radius: 50%; background: radial-gradient(circle at center, rgba(8, 31, 50, 0.96), rgba(3, 13, 24, 0.96)); border: 1px solid rgba(82,244,255,.26); box-shadow: inset 0 0 18px rgba(82,244,255,.08); }
.ring::after { content: ""; position: absolute; inset: -8px; border-radius: 50%; border: 1px solid rgba(82,244,255,.12); }
.ring-text { position: absolute; text-align: center; font-size: 16px; font-weight: 700; line-height: 1.45; color: #ecffd3; } .ring-text strong { font-size: 33px; display: block; color: #fff7aa; }
.triple-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 14px; }
.mini-stat { min-height: 92px; display: flex; align-items: center; justify-content: center; flex-direction: column; text-align: center; border: 1px dashed rgba(82,244,255,.18); background: linear-gradient(180deg, rgba(9, 39, 61, 0.26), rgba(5, 19, 34, 0.16)); }
.mini-stat strong { font-size: 28px; color: #8ffcff; display: block; margin-bottom: 6px; } .mini-stat span { font-size: 13px; color: var(--muted); }
.chart { width: 100%; height: 120px; display: block; }
.grid-table { margin-top: 10px; font-size: 12px; border-top: 1px solid rgba(82,244,255,.14); }
.grid-table .head, .grid-table .item { display: grid; grid-template-columns: 70px 1fr 88px; gap: 8px; align-items: center; padding: 7px 8px; }
.grid-table .head { color: #94f6ff; background: rgba(44, 164, 203, 0.15); font-weight: 700; }
.grid-table .item:nth-child(odd) { background: rgba(255,255,255,.018); } .grid-table .item:nth-child(even) { background: rgba(0,0,0,.08); }

.status-grid { height: 100%; display: grid; grid-template-columns: repeat(9, 1fr); gap: 10px; align-items: center; padding: 10px 8px 6px; }

/* 修复点: 状态卡片及内部节点名称优化展示 */
.status-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 6px 4px;
  background: linear-gradient(180deg, rgba(7, 28, 49, 0.35), rgba(3, 12, 24, 0.08));
  border: 1px solid rgba(82,244,255,.08);
}
.status-card .count { font-size: 16px; font-weight: 800; color: #b4ffe6; margin-bottom: 4px; text-shadow: 0 0 8px rgba(82,244,255,.22); }
.status-card .icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 20px;
  margin-bottom: 4px;
  background: radial-gradient(circle at 35% 30%, rgba(126,255,241,.45), rgba(0,88,122,.18) 65%, transparent 72%);
  border: 1px solid rgba(82,244,255,.14);
  box-shadow: inset 0 0 14px rgba(82,244,255,.08), 0 0 12px rgba(82,244,255,.08);
}
.status-card .name {
  font-size: 12px;
  color: #93d5e2;
  white-space: normal; /* 允许换行不再被无情切断 */
  line-height: 1.2;
  width: 100%;
}

.center-shell { position: relative; height: 100%; background: radial-gradient(circle at center, rgba(82,244,255,.08), transparent 28%), linear-gradient(180deg, rgba(4, 21, 39, 0.4), rgba(2, 10, 20, 0.82)); border: 1px solid rgba(82,244,255,.18); overflow: hidden; }
.center-shell::before { content: ""; position: absolute; inset: 10px; border: 1px solid rgba(82,244,255,.09); }
.center-title { position: absolute; left: 50%; top: 16px; transform: translateX(-50%); font-size: 30px; font-weight: 800; letter-spacing: 3px; color: #b4ffff; text-shadow: 0 0 12px rgba(82,244,255,.3); z-index: 3; }
.chip { position: absolute; width: 220px; height: 64px; display: flex; align-items: center; justify-content: center; gap: 12px; background: linear-gradient(90deg, rgba(8, 48, 70, 0.34), rgba(8, 108, 138, 0.18), rgba(8, 48, 70, 0.34)); clip-path: polygon(10% 0, 90% 0, 100% 28%, 92% 100%, 8% 100%, 0 28%); border: 1px solid rgba(82,244,255,.18); color: #a9fdff; font-size: 25px; font-weight: 700; box-shadow: inset 0 0 18px rgba(82,244,255,.06), 0 0 14px rgba(82,244,255,.07); z-index: 2; }
.chip .badge { width: 36px; height: 36px; border-radius: 50%; display: grid; place-items: center; background: radial-gradient(circle, rgba(126,255,248,.46), rgba(0,84,109,.2)); border: 1px solid rgba(82,244,255,.22); font-size: 18px; }
.metric-box { position: absolute; width: 110px; min-height: 70px; display: flex; align-items: center; justify-content: center; flex-direction: column; text-align: center; border-radius: 8px; background: linear-gradient(180deg, rgba(11, 42, 70, 0.76), rgba(5, 18, 34, 0.92)); border: 1px solid rgba(82,244,255,.18); box-shadow: 0 0 12px rgba(82,244,255,.08); z-index: 2; }
.metric-box strong { font-size: 18px; color: #eaffff; display: block; margin-bottom: 6px; } .metric-box span { font-size: 13px; color: #9ccad5; }
.hub-zone { position: absolute; inset: 50% auto auto 50%; transform: translate(-50%, -50%); width: 300px; height: 300px; z-index: 1; }
.hub-outer, .hub-middle, .hub-core { position: absolute; border-radius: 50%; left: 50%; top: 50%; transform: translate(-50%, -50%); }
.hub-outer { width: 300px; height: 300px; background: conic-gradient(from 0deg, rgba(82,244,255,.55), rgba(82,244,255,.04), rgba(82,244,255,.5), rgba(82,244,255,.05), rgba(82,244,255,.55)); box-shadow: 0 0 28px rgba(82,244,255,.1), inset 0 0 24px rgba(82,244,255,.06); animation: spin 16s linear infinite; }
.hub-outer::before { content: ""; position: absolute; inset: 18px; border-radius: 50%; border: 10px solid rgba(82,244,255,.14); }
.hub-middle { width: 202px; height: 202px; border: 2px dashed rgba(82,244,255,.22); animation: spinReverse 18s linear infinite; }
.hub-core { width: 132px; height: 132px; background: radial-gradient(circle at center, rgba(0, 244, 255, 0.32), rgba(0, 33, 60, 0.96)); border: 1px solid rgba(82,244,255,.28); box-shadow: inset 0 0 18px rgba(82,244,255,.08), 0 0 18px rgba(82,244,255,.08); display: grid; place-items: center; color: #9ffbff; font-size: 46px; text-shadow: 0 0 10px rgba(82,244,255,.22); }
@keyframes spin { from { transform: translate(-50%, -50%) rotate(0deg); } to { transform: translate(-50%, -50%) rotate(360deg); } }
@keyframes spinReverse { from { transform: translate(-50%, -50%) rotate(360deg); } to { transform: translate(-50%, -50%) rotate(0deg); } }
@keyframes breathe { 0%,100% { transform: scale(1); filter: brightness(1); } 50% { transform: scale(1.02); filter: brightness(1.08); } }
.runtime { position: absolute; left: 50%; bottom: 28px; transform: translateX(-50%); text-align: center; z-index: 2; }
.runtime .days { font-size: 42px; font-weight: 800; color: #87fbff; text-shadow: 0 0 14px rgba(82,244,255,.22); }
.runtime .tag { margin-top: 8px; display: inline-block; padding: 11px 26px 8px; font-size: 14px; color: #ebffff; background: linear-gradient(180deg, rgba(82,244,255,.24), rgba(8, 56, 74, 0.26)); clip-path: polygon(10% 0, 90% 0, 100% 34%, 84% 100%, 16% 100%, 0 34%); border: 1px solid rgba(82,244,255,.2); }
.select-tag { position: absolute; right: 16px; top: 14px; font-size: 12px; color: #b3f9ff; border: 1px solid rgba(82,244,255,.18); background: rgba(6, 18, 34, 0.86); padding: 6px 10px; z-index: 2; }
.progress-wrap { display: grid; gap: 12px; margin-top: 8px; }
.progress-item { display: grid; grid-template-columns: 92px 1fr 44px; align-items: center; gap: 8px; font-size: 13px; color: #a8dbe6; }
.track { height: 12px; border-radius: 999px; background: rgba(255,255,255,.06); border: 1px solid rgba(82,244,255,.09); overflow: hidden; }
.bar { height: 100%; border-radius: 999px; background: linear-gradient(90deg, rgba(82,244,255,.32), rgba(109,255,207,.96)); box-shadow: 0 0 10px rgba(109,255,207,.18); animation: breathe 3.4s ease-in-out infinite; }
.energy-metrics { margin-top: 12px; margin-bottom: 18px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.energy-card { text-align: center; padding: 8px 0; }
.energy-card strong { display: block; font-size: 22px; color: #8ffcff; margin-bottom: 5px; } .energy-card span { color: #93cddb; font-size: 13px; }
.bottom-shell { height: 100%; display: flex; flex-direction: column; justify-content: space-between; }
.legend { display: flex; justify-content: flex-end; gap: 18px; font-size: 12px; color: #89c4cf; padding-right: 8px; }
.legend span::before { content: ""; display: inline-block; width: 10px; height: 10px; margin-right: 6px; vertical-align: -1px; }
.legend .people::before { background: #54f7ff; } .legend .car::before { background: #6dffcf; }
.time-board { text-align: center; color: #35f2ff; font-size: 22px; font-weight: 700; text-shadow: 0 0 10px rgba(82,244,255,.18); margin-top: 2px; }
.time-board small { display: block; color: #86d9e3; font-size: 12px; margin-top: 3px; }

@media (max-width: 1580px) {
  .smart-device-dashboard { overflow: hidden; }
  .dashboard { min-width: 1580px; min-height: 100vh; }
}

/* 修补点: 通过 Fixed 并设置 width100vw，逃逸所有父级 margin，杜绝白边缝隙 */
.bi-dash-frame {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  z-index: 9999;
  background-color: #020812; /* 背景色直接融合大屏 */
}

.bi-dash-stage { position: absolute; left: 0; top: 0; transform-origin: 0 0; }
.bi-dash-stage-scale { transform-origin: 0 0; }
</style>