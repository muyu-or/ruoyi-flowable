<template>
  <div ref="frameRef" class="bi-dash-frame">
    <div class="bi-dash-stage" :style="moveStyle">
      <div class="bi-dash-stage-scale" :style="scaleOnlyStyle">
        <div class="smart-device-dashboard">
          <div class="scan" />
          <div class="dashboard">
            <!-- Header -->
            <header class="header">
              <div class="line" />
              <div class="title-shell"><h1>数据统计中心</h1></div>
              <div class="line" />
            </header>

            <!-- Left Top: 2 stacked panels -->
            <section class="panel left-top">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <!-- Panel 1: Task Completion (no carousel) -->
              <div class="panel">
                <span class="cut-corner lt" /><span class="cut-corner rt" />
                <span class="cut-corner lb" /><span class="cut-corner rb" />
                <div class="inner">
                  <h3 class="panel-title">任务完成率</h3>
                  <div class="counter-label">全公司任务统计</div>
                  <div id="completionDigits" class="digits" />
                  <div class="ring-wrap">
                    <div id="completionRing" class="ring" />
                    <div class="ring-text">
                      <strong id="completionRate">0%</strong>完成率
                    </div>
                  </div>
                </div>
              </div>
              <!-- Panel 2: Warning — carousel -->
              <div class="panel carousel-host">
                <span class="cut-corner lt" /><span class="cut-corner rt" />
                <span class="cut-corner lb" /><span class="cut-corner rb" />
                <!-- Page 0: Radar -->
                <div :class="['carousel-page', carouselPage === 0 ? 'active' : '']">
                  <h3 class="panel-title">风险雷达</h3>
                  <div class="triple-stats">
                    <div class="mini-stat"><strong id="warnTotal">0</strong><span>预警总数</span></div>
                    <div class="mini-stat"><strong id="warnAvgResp">0m</strong><span>平均响应</span></div>
                    <div class="mini-stat"><strong id="warnUnresolved">0</strong><span>未处理</span></div>
                  </div>
                  <div id="riskRadarChart" ref="riskRadarChartDom" class="chart" style="height:180px;" />
                </div>
                <!-- Page 1: Warning Table -->
                <div :class="['carousel-page', carouselPage === 1 ? 'active' : '']">
                  <h3 class="panel-title">预警响应明细</h3>
                  <div class="triple-stats">
                    <div class="mini-stat"><strong id="warnTotal2">0</strong><span>预警总数</span></div>
                    <div class="mini-stat"><strong id="warnAvgResp2">0m</strong><span>平均响应</span></div>
                    <div class="mini-stat"><strong id="warnUnresolved2">0</strong><span>未处理</span></div>
                  </div>
                  <div id="warningTable" class="grid-table" style="max-height:200px;overflow-y:auto;" />
                </div>
              </div>
            </section>

            <!-- Top Strip: Status Grid -->
            <section class="panel top-strip">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <div id="statusGrid" class="status-grid" />
            </section>

            <!-- Right Top: 2 stacked panels -->
            <section class="panel right-top">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <!-- Panel 1: carousel (产能概览 / 库存概览) -->
              <div class="panel carousel-host">
                <span class="cut-corner lt" /><span class="cut-corner rt" />
                <span class="cut-corner lb" /><span class="cut-corner rb" />
                <!-- Page 0: 产能概览 -->
                <div :class="['carousel-page', carouselPage === 0 ? 'active' : '']">
                  <h3 class="panel-title">产能概览</h3>
                  <div class="energy-metrics" style="margin-top:4px;margin-bottom:4px;grid-template-columns:repeat(2,1fr);">
                    <div class="energy-card"><strong id="kpiOnTimeRate">0%</strong><span>准时完成率</span></div>
                    <div class="energy-card"><strong id="kpiDailyOutput">0</strong><span>日均完成数</span></div>
                  </div>
                  <div class="ring-wrap" style="height:110px;">
                    <div id="onTimeRing" class="ring" style="width:100px;height:100px;" />
                    <div class="ring-text">
                      <strong id="onTimeRateText" style="font-size:22px;">0%</strong>准时率
                    </div>
                  </div>
                </div>
                <!-- Page 1: 库存概览 -->
                <div :class="['carousel-page', carouselPage === 1 ? 'active' : '']">
                  <h3 class="panel-title">库存概览</h3>
                  <div class="counter-label">当前库存统计</div>
                  <div id="inventoryDigits" class="digits" />
                  <div style="font-size:14px;color:#9ce6f1;margin-bottom:12px;">库存分布</div>
                  <div id="inventoryProgress" class="progress-wrap" />
                </div>
              </div>
              <!-- Panel 2: carousel (经营指标 / 成本趋势) -->
              <div class="panel carousel-host">
                <span class="cut-corner lt" /><span class="cut-corner rt" />
                <span class="cut-corner lb" /><span class="cut-corner rb" />
                <!-- Page 0: 经营指标 -->
                <div :class="['carousel-page', carouselPage === 0 ? 'active' : '']">
                  <h3 class="panel-title">经营指标</h3>
                  <div class="energy-metrics" style="margin-top:8px;margin-bottom:14px;grid-template-columns:repeat(2,1fr);">
                    <div class="energy-card"><strong id="kpiTeamQual">0%</strong><span>班组达标率</span></div>
                    <div class="energy-card"><strong id="kpiFlowEff">0%</strong><span>流转效率</span></div>
                  </div>
                  <svg id="costTrendChart2" class="chart" viewBox="0 0 300 120" preserveAspectRatio="none" />
                </div>
                <!-- Page 1: 物料周转分析 -->
                <div :class="['carousel-page', carouselPage === 1 ? 'active' : '']">
                  <h3 class="panel-title">物料周转分析</h3>
                  <div class="energy-metrics" style="margin-top:8px;margin-bottom:14px;">
                    <div class="energy-card"><strong id="turnoverTotal">0</strong><span>总入库批次</span></div>
                    <div class="energy-card"><strong id="turnoverAvgDaily">0</strong><span>日均入库量</span></div>
                    <div class="energy-card"><strong id="turnoverCategories">0</strong><span>物料子类数</span></div>
                  </div>
                  <svg id="turnoverTrendChart" class="chart" viewBox="0 0 300 120" preserveAspectRatio="none" />
                </div>
              </div>
            </section>

            <!-- Center Main -->
            <section class="panel center-main">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <div class="inner" style="padding:10px;">
                <div class="center-shell">
                  <div class="center-title">生产监控</div>
                  <div class="chip" style="left:46px;top:68px;"><span class="badge">&#128230;</span>入库监控</div>
                  <div class="chip" style="right:46px;top:68px;"><span class="badge">&#128300;</span>质量检测</div>
                  <div class="chip" style="left:46px;bottom:88px;"><span class="badge">&#9881;</span>生产加工</div>
                  <div class="chip" style="right:46px;bottom:88px;"><span class="badge">&#128228;</span>出库调度</div>
                  <!-- Carousel metric boxes: Set A -->
                  <div :class="['hub-metrics', carouselPage === 0 ? 'active' : '']">
                    <div class="metric-box" style="left:22%;top:110px;"><strong id="hubOnTime">0%</strong><span>准时完成率</span></div>
                    <div class="metric-box" style="left:22%;top:200px;"><strong id="hubDaily">0</strong><span>日均完成数</span></div>
                    <div class="metric-box" style="right:22%;top:110px;"><strong id="hubTeamQual">0%</strong><span>班组达标率</span></div>
                    <div class="metric-box" style="right:22%;top:200px;"><strong id="hubFastTeam">--</strong><span>最优班组</span></div>
                  </div>
                  <!-- Carousel metric boxes: Set B -->
                  <div :class="['hub-metrics', carouselPage === 1 ? 'active' : '']">
                    <div class="metric-box" style="left:22%;top:110px;"><strong id="hubFlowEff">0%</strong><span>流转效率</span></div>
                    <div class="metric-box" style="left:22%;top:200px;"><strong id="hubAvgNode">0m</strong><span>平均节点耗时</span></div>
                    <div class="metric-box" style="right:22%;top:110px;"><strong id="hubOnTime2">0%</strong><span>准时完成率</span></div>
                    <div class="metric-box" style="right:22%;top:200px;"><strong id="hubDaily2">0</strong><span>日均完成数</span></div>
                  </div>
                  <div class="hub-zone">
                    <div id="liquidChart" ref="liquidChartDom" style="width:220px;height:220px;" />
                  </div>
                  <div class="runtime">
                    <div id="totalCompleted" class="days">0</div>
                    <div class="tag">已完成任务</div>
                  </div>
                </div>
              </div>
            </section>

            <!-- Left Bottom: carousel (瓶颈 / Top5 员工) -->
            <section class="panel left-bottom carousel-host">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <!-- Page 0: Node Bottleneck -->
              <div :class="['carousel-page', carouselPage === 0 ? 'active' : '']">
                <h3 class="panel-title">节点瓶颈 Top（P50/P90）</h3>
                <div id="bottleneckTable" class="grid-table" />
              </div>
              <!-- Page 1: Top5 Employees -->
              <div :class="['carousel-page', carouselPage === 1 ? 'active' : '']">
                <h3 class="panel-title">员工完成排行 Top5</h3>
                <div id="userTop5Table" class="grid-table" />
              </div>
            </section>

            <!-- Center Bottom -->
            <section class="panel center-bottom">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <div class="inner">
                <h3 class="panel-title">出入库趋势与相关性</h3>
                <div class="bottom-shell">
                  <div id="stockTrendChart" ref="stockTrendChartDom" class="chart" style="height:160px;" />
                  <div id="timeBoard" class="time-board">--</div>
                </div>
              </div>
            </section>

            <!-- Right Bottom -->
            <section class="panel right-bottom">
              <span class="cut-corner lt" /><span class="cut-corner rt" />
              <span class="cut-corner lb" /><span class="cut-corner rb" />
              <div class="inner">
                <h3 class="panel-title">班组效率 Top</h3>
                <div id="stabilityTable" class="grid-table" />
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getHomeStats } from '@/api/flowable/stat'
var echarts = require('echarts')
require('echarts/theme/macarons')
require('echarts-liquidfill')

var NODE_ICONS = { '\u539f\u6599\u5165\u5e93': '\u26a1', '\u539f\u6599\u68c0\u6d4b\u5165\u5e93': '\u26a1', '\u6d4b\u8bd5': '\u2726', '\u9884\u5904\u7406': '\u2699', '\u771f\u7a7a': '\u269b', '\u70d8\u70e4': '\u2b50', '\u68c0\u6d4b': '\u2726', '\u4ea7\u54c1\u5165\u5e93': '\u2713' }

export default {
  name: 'BiDashboard',
  dicts: ['material_category'],
  data: function() {
    return {
      currentPeriod: 'all',
      statsData: {},
      carouselPage: 0,
      carouselTimer: null,
      scaleX: 1,
      scaleY: 1,
      designW: 1568,
      designH: 890,
      chartInstances: {},
      clockTimer: null,
      refreshTimer: null,
      prevTagsView: null,
      prevSidebarHide: null
    }
  },
  computed: {
    moveStyle: function() {
      return { width: '100%', height: '100%', position: 'relative', overflow: 'hidden' }
    },
    scaleOnlyStyle: function() {
      return {
        position: 'absolute', left: '50%', top: '50%',
        width: this.designW + 'px', height: this.designH + 'px',
        transform: 'translate(-50%, -50%) scale(' + this.scaleX + ', ' + this.scaleY + ')',
        transformOrigin: 'center center'
      }
    },
    categoryDicts: function() {
      return (this.dict && this.dict.type && this.dict.type.material_category) || []
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
            if (self.chartInstances[k] && self.chartInstances[k].resize) self.chartInstances[k].resize()
          })
        }
      }
      window.addEventListener('resize', self._onResize)
      self.loadData()
      self.clockTimer = setInterval(function() { self.updateClock() }, 1000)
      self.refreshTimer = setInterval(function() { self.loadData() }, 60000)
      // Carousel: toggle page every 10s
      self.carouselTimer = setInterval(function() {
        self.carouselPage = self.carouselPage === 0 ? 1 : 0
        self.$nextTick(function() {
          // Resize radar chart when page 0 becomes active again
          if (self.carouselPage === 0 && self.chartInstances.riskRadar) {
            self.chartInstances.riskRadar.resize()
          }
        })
      }, 30000)
    })
  },
  beforeDestroy: function() {
    if (this.clockTimer) clearInterval(this.clockTimer)
    if (this.refreshTimer) clearInterval(this.refreshTimer)
    if (this.carouselTimer) clearInterval(this.carouselTimer)
    if (this._onResize) window.removeEventListener('resize', this._onResize)
    if (this.chartInstances) {
      Object.keys(this.chartInstances).forEach(function(k) {
        if (this.chartInstances[k] && this.chartInstances[k].dispose) this.chartInstances[k].dispose()
      }.bind(this))
    }
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
      var frameW = window.innerWidth
      var frameH = window.innerHeight
      if (!frameW || !frameH) return
      this.scaleX = frameW / this.designW
      this.scaleY = frameH / this.designH
    },
    onPeriodChange: function() { this.loadData() },
    loadData: function() {
      var self = this
      getHomeStats({ period: this.currentPeriod }).then(function(res) {
        self.statsData = (res && res.data) || {}
        self.$nextTick(function() { self.renderAll() })
      }).catch(function(err) { console.error('BI loadData error:', err) })
    },

    // ============ Derived KPIs ============
    computeDerivedKPIs: function() {
      var sd = this.statsData
      var cs = sd.companyStats || {}
      var ws = sd.warningStats || []
      var costSum = sd.costSummary || {}
      var ns = sd.nodeStatusSummary || []
      var st = sd.stockTrend || []
      var nb = sd.nodeBottleneck || []
      var ts = sd.teamStability || []
      var finished = cs.finished || 0

      // 准时完成率: (finished - 有预警的已完成任务数) / finished
      var warnedFinished = 0
      for (var i = 0; i < ws.length; i++) {
        warnedFinished += Math.max(0, (ws[i].warningCount || 0) - (ws[i].unresolvedCount || 0))
      }
      var onTimeRate = finished > 0 ? Math.round(Math.max(0, (finished - warnedFinished) / finished * 100)) : 0

      // 日均产出（按数据跨度天数计算）
      var periodDays = Math.max(1, st.length > 0 ? st.length : 30)
      var dailyOutput = (finished / periodDays).toFixed(1)

      // 库存周转率
      var invCost = parseFloat(costSum.totalInventoryCost || 0)
      var stockInAmt = parseFloat(costSum.totalStockInAmount || 0)
      var turnoverRate = invCost > 0 ? (stockInAmt / invCost).toFixed(1) : '0.0'

      // 产能利用率: active / (active + completed)
      var totalActive = 0
      var totalAll = 0
      for (var j = 0; j < ns.length; j++) {
        totalActive += ns[j].activeCount || 0
        totalAll += (ns[j].activeCount || 0) + (ns[j].completedCount || 0)
      }
      var capacityUtil = totalAll > 0 ? Math.round(totalActive / totalAll * 100) : 0

      // 班组达标率: 准时率 >= 80% 的班组占比
      var qualTeams = 0
      for (var k = 0; k < ts.length; k++) {
        if ((ts[k].onTimeRate || 0) >= 0.8) qualTeams++
      }
      var teamQualRate = ts.length > 0 ? Math.round(qualTeams / ts.length * 100) : 0

      // 节点流转效率: avg(completed / (active+completed)) per node
      var effSum = 0
      var effCount = 0
      for (var m = 0; m < ns.length; m++) {
        var nodeTotal = (ns[m].activeCount || 0) + (ns[m].completedCount || 0)
        if (nodeTotal > 0) {
          effSum += (ns[m].completedCount || 0) / nodeTotal
          effCount++
        }
      }
      var flowEff = effCount > 0 ? Math.round(effSum / effCount * 100) : 0

      // 平均节点耗时 (P50 avg in minutes)
      var p50Sum = 0
      for (var n = 0; n < nb.length; n++) {
        p50Sum += nb[n].p50Seconds || 0
      }
      var avgNodeTime = nb.length > 0 ? Math.round(p50Sum / nb.length / 60) : 0

      // 最快班组（准时率最高的正式班组）
      var fastestTeam = '--'
      var maxOnTimeRate = -1
      for (var p = 0; p < ts.length; p++) {
        var tName = ts[p].teamName || ''
        var tOnTime = ts[p].onTimeRate || 0
        if (tOnTime > maxOnTimeRate && tName.indexOf('\u73ed\u7ec4') >= 0 && tName.length >= 3) {
          maxOnTimeRate = tOnTime
          fastestTeam = tName
        }
      }

      return {
        onTimeRate: onTimeRate,
        dailyOutput: dailyOutput,
        turnoverRate: turnoverRate,
        capacityUtil: capacityUtil,
        teamQualRate: teamQualRate,
        flowEff: flowEff,
        avgNodeTime: avgNodeTime,
        fastestTeam: fastestTeam
      }
    },

    // ============ Main render ============
    renderAll: function() {
      var sd = this.statsData
      var cs = sd.companyStats || {}
      var ws = sd.warningStats || []
      var inv = sd.inventoryOverview || []
      var nb = sd.nodeBottleneck || []
      var ts = sd.teamStability || []
      var ns = sd.nodeStatusSummary || []
      var st = sd.stockTrend || []

      var userTop = sd.userTop5 || []

      var finished = cs.finished || 0
      var total = cs.total || 0
      var rate = total > 0 ? Math.round(finished / total * 100) : 0

      // Compute derived KPIs
      var kpis = this.computeDerivedKPIs()

      // --- Left-top Panel 1: Task Completion ---
      this.renderDigits('completionDigits', finished, 5)
      this.setTextById('completionRate', rate + '%')
      var ringEl = document.getElementById('completionRing')
      if (ringEl) ringEl.style.background = 'conic-gradient(#ffe27a 0 ' + rate + '%, rgba(106,250,255,0.92) ' + rate + '% 100%)'

      // --- Left-top Panel 2: Warning Stats (both pages) ---
      var warnTotalCount = 0
      var warnAvgTotal = 0
      var warnAvgCount = 0
      var totalUnresolved = 0
      for (var wi = 0; wi < ws.length; wi++) {
        warnTotalCount += ws[wi].warningCount || 0
        totalUnresolved += ws[wi].unresolvedCount || 0
        var resolvedInNode = (ws[wi].warningCount || 0) - (ws[wi].unresolvedCount || 0)
        if (resolvedInNode > 0 && (ws[wi].avgResponseMinutes || 0) > 0) {
          warnAvgTotal += (ws[wi].avgResponseMinutes || 0) * resolvedInNode
          warnAvgCount += resolvedInNode
        }
      }
      var globalAvgResp = warnAvgCount > 0 ? Math.round(warnAvgTotal / warnAvgCount) : 0
      var unresolvedDisplay = totalUnresolved
      var avgRespStr = globalAvgResp >= 1440 ? (Math.round(globalAvgResp / 1440 * 10) / 10) + '\u5929' : globalAvgResp > 0 ? (Math.round(globalAvgResp / 60 * 10) / 10) + '\u5c0f\u65f6' : '-'
      // Page 0 stats
      this.setTextById('warnTotal', warnTotalCount)
      this.setTextById('warnAvgResp', avgRespStr)
      this.setTextById('warnUnresolved', unresolvedDisplay)
      // Page 1 stats (duplicate)
      this.setTextById('warnTotal2', warnTotalCount)
      this.setTextById('warnAvgResp2', avgRespStr)
      this.setTextById('warnUnresolved2', unresolvedDisplay)
      // Radar chart
      this.initCharts()
      this.renderRiskRadar(ws)
      // Warning table (page 1, scrollable)
      this.renderTable('warningTable', '\u8282\u70b9', '\u54cd\u5e94(\u5929)', '\u6b21\u6570',
        ws.map(function(n) {
          var mins = n.avgResponseMinutes || 0
          var resp = mins > 0 ? (Math.round(mins / 1440 * 10) / 10) + '\u5929' : '-'
          return [n.nodeName, resp, (n.warningCount || 0) + '\u6b21']
        }))

      // --- Top Strip: Status Grid (de-duplicated last 3) ---
      var statusList = []
      ns.slice(0, 6).forEach(function(n) {
        statusList.push({ count: (n.activeCount || 0) + '/' + (n.completedCount || 0), icon: NODE_ICONS[n.nodeName] || '\u26a1', name: n.nodeName })
      })
      // New KPI cards (no longer duplicating completion rate / warnings / stock-in amount)
      statusList.push({ count: kpis.onTimeRate + '%', icon: '\ud83c\udfaf', name: '\u51c6\u65f6\u7387' })
      statusList.push({ count: kpis.flowEff + '%', icon: '\ud83d\udd04', name: '\u6d41\u8f6c\u6548\u7387' })
      statusList.push({ count: warnTotalCount, icon: '\u26a0\ufe0f', name: '\u9884\u8b66\u603b\u6570' })
      this.renderStatus(statusList.slice(0, 9))

      // --- Right-top Panel 1 Page 0: 产能概览 ---
      this.setTextById('kpiOnTimeRate', kpis.onTimeRate + '%')
      this.setTextById('kpiDailyOutput', kpis.dailyOutput)
      this.setTextById('onTimeRateText', kpis.onTimeRate + '%')
      var onTimeRingEl = document.getElementById('onTimeRing')
      if (onTimeRingEl) onTimeRingEl.style.background = 'conic-gradient(#6dffcf 0 ' + kpis.onTimeRate + '%, rgba(106,250,255,0.92) ' + kpis.onTimeRate + '% 100%)'

      // --- Right-top Panel 1 Page 1: 库存概览 ---
      var totalInvQty = 0
      for (var ii = 0; ii < inv.length; ii++) totalInvQty += (inv[ii].totalQty || 0)
      this.renderDigits('inventoryDigits', totalInvQty, 5)
      var maxInvQty = 1
      for (var ij = 0; ij < inv.length; ij++) maxInvQty = Math.max(maxInvQty, inv[ij].totalQty || 0)
      var self = this
      var progressData = inv.slice(0, 5).map(function(item) {
        return { name: self.getCategoryLabel(item.category), value: Math.max(0, Math.min(100, Math.round((item.totalQty || 0) / maxInvQty * 100))) }
      })
      this.renderProgress(progressData)

      // --- Right-top Panel 2 Page 0: 经营指标 ---
      this.setTextById('kpiTeamQual', kpis.teamQualRate + '%')
      this.setTextById('kpiFlowEff', kpis.flowEff + '%')
      if (st.length > 0) {
        var effQtyValues = st.map(function(p) { return p.inboundQty || 0 })
        var effQtyLabels = st.map(function(p) { return p.label || '' })
        this.drawLine('costTrendChart2', effQtyValues, null, effQtyLabels, '2', '\u5165\u5e93\u6570\u91cf\u8d8b\u52bf')
      }

      // --- Right-top Panel 2 Page 1: 物料周转分析 ---
      var totalInboundQty = 0
      if (st.length > 0) {
        for (var si = 0; si < st.length; si++) totalInboundQty += (st[si].inboundQty || 0)
      }
      var periodDaysForTurnover = Math.max(1, st.length > 0 ? st.length : 30)
      this.setTextById('turnoverTotal', st.length)
      this.setTextById('turnoverAvgDaily', totalInboundQty > 0 ? (totalInboundQty / periodDaysForTurnover).toFixed(1) : '0')
      this.setTextById('turnoverCategories', sd.subcategoryCount || 0)
      if (st.length > 0) {
        var inQtyValues = st.map(function(p) { return p.inboundQty || 0 })
        var inQtyLabels = st.map(function(p) { return p.label || '' })
        this.drawLine('turnoverTrendChart', inQtyValues, null, inQtyLabels, '3', '\u5165\u5e93\u91cf\u6ce2\u52a8')
      }

      // --- Center Main: Liquid ball ---
      this.renderLiquid(rate)

      // --- Center Main: Hub KPIs (Set A page 0) ---
      this.setTextById('hubOnTime', kpis.onTimeRate + '%')
      this.setTextById('hubDaily', kpis.dailyOutput)
      this.setTextById('hubTeamQual', kpis.teamQualRate + '%')
      this.setTextById('hubFastTeam', kpis.fastestTeam)
      // Hub KPIs (Set B page 1)
      this.setTextById('hubFlowEff', kpis.flowEff + '%')
      this.setTextById('hubAvgNode', (Math.round(kpis.avgNodeTime / 1440 * 10) / 10) + '天')
      this.setTextById('hubOnTime2', kpis.onTimeRate + '%')
      this.setTextById('hubDaily2', kpis.dailyOutput)
      this.setTextById('totalCompleted', finished)

      // --- Left Bottom Page 0: Node Bottleneck (P90/P50 >= 3 标红) ---
      this.renderBottleneckTable(nb.slice(0, 5))

      // --- Left Bottom Page 1: Top5 Employees ---
      this.renderRankTable('userTop5Table', userTop)

      // --- Center Bottom: Stock Trend ---
      this.renderStockTrend(st)

      // --- Right Bottom: Team Stability ---
      this.renderTable('stabilityTable', '\u73ed\u7ec4', '\u51c6\u65f6\u7387', '\u5747\u503c\u8017\u65f6',
        ts.slice(0, 5).map(function(t) { return [t.teamName, Math.round((t.onTimeRate || 0) * 100) + '%', (Math.round((t.meanSeconds || 0) / 86400 * 10) / 10) + '\u5929'] }))

      this.updateClock()
    },

    // ============ Charts ============
    initCharts: function() {
      if (!this.chartInstances) this.chartInstances = {}
      if (this.$refs.riskRadarChartDom && !this.chartInstances.riskRadar) {
        this.chartInstances.riskRadar = echarts.init(this.$refs.riskRadarChartDom, 'macarons')
      }
      if (this.$refs.stockTrendChartDom && !this.chartInstances.stockTrend) {
        this.chartInstances.stockTrend = echarts.init(this.$refs.stockTrendChartDom, 'macarons')
      }
      if (this.$refs.liquidChartDom && !this.chartInstances.liquid) {
        this.chartInstances.liquid = echarts.init(this.$refs.liquidChartDom)
      }
    },
    renderRiskRadar: function(ws) {
      if (!this.chartInstances || !this.chartInstances.riskRadar || !ws || !ws.length) return
      var chart = this.chartInstances.riskRadar
      var maxCount = 1
      var maxResp = 1
      for (var i = 0; i < ws.length; i++) {
        maxCount = Math.max(maxCount, ws[i].warningCount || 0)
        maxResp = Math.max(maxResp, ws[i].avgResponseMinutes || 0)
      }
      var categories = ws.slice(0, 6).map(function(n) { return n.nodeName })
      var values = ws.slice(0, 6).map(function(n) {
        return Math.max(0, Math.min(100, 0.55 * ((n.warningCount || 0) / maxCount) * 100 + 0.45 * ((n.avgResponseMinutes || 0) / maxResp) * 100))
      })
      chart.setOption({
        backgroundColor: 'transparent', tooltip: { show: true },
        radar: {
          radius: '52%', center: ['50%', '55%'],
          indicator: categories.map(function(name) { return { name: name, max: 100 } }),
          axisLine: { lineStyle: { color: 'rgba(0,175,255,0.18)' }},
          splitLine: { lineStyle: { color: 'rgba(0,175,255,0.10)' }},
          splitArea: { areaStyle: { color: ['rgba(0,175,255,0.02)', 'rgba(0,175,255,0.05)'] }},
          name: { textStyle: { color: 'rgba(226,243,255,0.72)', fontSize: 10 }}
        },
        series: [{ type: 'radar', data: [{ value: values, name: 'risk', areaStyle: { color: 'rgba(0,175,255,0.10)' }, lineStyle: { color: 'rgba(71,243,255,0.85)', width: 2 }, itemStyle: { color: 'rgba(71,243,255,0.85)' }}] }]
      }, true)
    },
    renderLiquid: function(rate) {
      if (!this.chartInstances || !this.chartInstances.liquid) return
      var val = rate / 100
      this.chartInstances.liquid.setOption({
        backgroundColor: 'transparent',
        series: [{
          type: 'liquidFill',
          radius: '88%',
          center: ['50%', '50%'],
          data: [val, val - 0.02, val - 0.04],
          color: ['rgba(82,244,255,0.6)', 'rgba(82,244,255,0.4)', 'rgba(82,244,255,0.2)'],
          backgroundStyle: { color: 'rgba(4,24,44,0.8)', borderColor: 'rgba(82,244,255,0.25)', borderWidth: 2 },
          outline: {
            show: true,
            borderDistance: 6,
            itemStyle: { borderWidth: 3, borderColor: 'rgba(82,244,255,0.45)', shadowBlur: 16, shadowColor: 'rgba(82,244,255,0.25)' }
          },
          label: {
            show: true,
            fontSize: 28,
            fontWeight: 800,
            color: '#e6fbff',
            insideColor: '#e6fbff',
            formatter: function() { return rate + '%\n{sub|完成率}' },
            rich: { sub: { fontSize: 12, color: '#8ffcff', lineHeight: 22 }}
          },
          itemStyle: { shadowBlur: 12, shadowColor: 'rgba(82,244,255,0.3)' },
          emphasis: { itemStyle: { opacity: 0.9 }}
        }]
      }, true)
    },
    renderStockTrend: function(st) {
      if (!this.chartInstances || !this.chartInstances.stockTrend || !st || !st.length) return
      var chart = this.chartInstances.stockTrend
      var labels = st.map(function(p) { return p.label || '' })
      var inArr = st.map(function(p) { return p.inboundQty || 0 })
      var outArr = st.map(function(p) { return p.outboundQty || 0 })
      var r = this._pearson(inArr, outArr)
      chart.setOption({
        backgroundColor: 'transparent', tooltip: { trigger: 'axis' },
        legend: { top: 0, left: 0, textStyle: { color: 'rgba(226,243,255,0.70)' }, data: ['\u5165\u5e93', '\u51fa\u5e93'] },
        grid: { left: 44, right: 18, top: 28, bottom: 28 },
        xAxis: { type: 'category', data: labels, axisLine: { lineStyle: { color: 'rgba(0,175,255,0.12)' }}, axisLabel: { color: 'rgba(226,243,255,0.45)', fontSize: 10, rotate: labels.length > 12 ? 45 : 0, interval: labels.length > 20 ? Math.floor(labels.length / 10) : 0 }},
        yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: 'rgba(226,243,255,0.45)', fontSize: 10 }, splitLine: { lineStyle: { color: 'rgba(0,175,255,0.08)' }}},
        graphic: [],
        series: [
          { name: '\u5165\u5e93', type: 'line', smooth: true, showSymbol: false, lineStyle: { width: 2, color: 'rgba(0,175,255,0.95)' }, areaStyle: { color: 'rgba(0,175,255,0.10)' }, data: inArr },
          { name: '\u51fa\u5e93', type: 'line', smooth: true, showSymbol: false, lineStyle: { width: 2, color: 'rgba(38,222,129,0.90)' }, areaStyle: { color: 'rgba(38,222,129,0.08)' }, data: outArr }
        ]
      }, true)
    },

    // ============ DOM Renderers ============
    renderDigits: function(id, value, size) {
      size = size || 5
      var s = String(value).padStart(size, '0')
      var el = document.getElementById(id)
      if (el) el.innerHTML = s.split('').map(function(n) { return '<div class="digit">' + n + '</div>' }).join('')
    },
    renderStatus: function(list) {
      var el = document.getElementById('statusGrid')
      if (el) {
        el.innerHTML = list.map(function(item) {
          return '<div class="status-card"><div class="count">' + item.count + '</div><div class="icon">' + item.icon + '</div><div class="name">' + item.name + '</div></div>'
        }).join('')
      }
    },
    renderProgress: function(data) {
      var el = document.getElementById('inventoryProgress')
      if (el) {
        el.innerHTML = data.map(function(item) {
          return '<div class="progress-item"><span>' + item.name + '</span><div class="track"><div class="bar" style="width:' + item.value + '%"></div></div><span>' + item.value + '%</span></div>'
        }).join('')
      }
    },
    renderTable: function(id, h1, h2, h3, rows) {
      var el = document.getElementById(id)
      if (!el) return
      el.innerHTML = '<div class="head"><span>' + h1 + '</span><span>' + h2 + '</span><span>' + h3 + '</span></div>' +
        (rows || []).map(function(row) {
          return '<div class="item"><span>' + row[0] + '</span><span>' + row[1] + '</span><span>' + row[2] + '</span></div>'
        }).join('')
    },
    renderBottleneckTable: function(nb) {
      var el = document.getElementById('bottleneckTable')
      if (!el) return
      var html = '<div class="head"><span>\u8282\u70b9</span><span>P50\u8017\u65f6</span><span>P90\u8017\u65f6</span></div>'
      for (var i = 0; i < nb.length; i++) {
        var n = nb[i]
        var p50 = Math.round((n.p50Seconds || 0) / 86400 * 10) / 10
        var p90 = Math.round((n.p90Seconds || 0) / 86400 * 10) / 10
        var ratio = p50 > 0 ? p90 / p50 : 0
        var warn = ratio >= 3
        var style = warn ? ' style="color:#ff6b6b;font-weight:700;"' : ''
        var icon = warn ? ' \u26a0' : ''
        html += '<div class="item"' + style + '><span>' + n.nodeName + icon + '</span><span>' + p50 + '\u5929</span><span>' + p90 + '\u5929</span></div>'
      }
      el.innerHTML = html
    },
    renderRankTable: function(id, userList) {
      var el = document.getElementById(id)
      if (!el) return
      var html = '<div class="head"><span>\u6392\u540d</span><span>\u59d3\u540d</span><span>\u5b8c\u6210\u6570</span></div>'
      var medals = ['\ud83e\udd47', '\ud83e\udd48', '\ud83e\udd49']
      for (var i = 0; i < Math.min(userList.length, 5); i++) {
        var u = userList[i]
        var rankStr = i < 3 ? medals[i] : (i + 1) + ''
        var cls = i === 0 ? ' style="color:#ffe27a;font-weight:800;"' : ''
        html += '<div class="item"' + cls + '><span>' + rankStr + '</span><span>' + (u.userName || '--') + '</span><span>' + (u.finished || 0) + '</span></div>'
      }
      el.innerHTML = html
    },
    setTextById: function(id, text) {
      var el = document.getElementById(id)
      if (el) el.textContent = text
    },
    drawLine: function(svgId, s1, s2, labels, filterSuffix, title) {
      var svg = document.getElementById(svgId)
      if (!svg) return
      var fid = 'glow' + (filterSuffix || '1')
      var w = 300
      var h = 120
      var p = { l: 10, r: 10, t: 10, b: 24 }
      var iw = w - p.l - p.r
      var ih = h - p.t - p.b
      var allValues = (s1 || []).concat(s2 || [])
      var max = allValues.length > 0 ? Math.max.apply(null, allValues) * 1.1 : 1
      if (max === 0) max = 1
      svg.innerHTML = '<defs><filter id="' + fid + '"><feGaussianBlur stdDeviation="2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter></defs>'
      var grid = ''
      for (var x = 0; x <= w; x += 30) grid += '<line x1="' + x + '" y1="0" x2="' + x + '" y2="' + h + '" stroke="rgba(82,244,255,.06)"/>'
      for (var y = 0; y <= h; y += 24) grid += '<line x1="0" y1="' + y + '" x2="' + w + '" y2="' + y + '" stroke="rgba(82,244,255,.06)"/>'
      svg.insertAdjacentHTML('beforeend', grid)
      var drawSeries = function(arr, color) {
        if (!arr || !arr.length) return
        var path = arr.map(function(v, i) {
          var px = p.l + iw * i / Math.max(arr.length - 1, 1)
          var py = p.t + ih - ih * v / max
          return (i === 0 ? 'M' : 'L') + ' ' + px + ' ' + py
        }).join(' ')
        svg.insertAdjacentHTML('beforeend', '<path d="' + path + '" fill="none" stroke="' + color + '" stroke-width="2.2" filter="url(#' + fid + ')"/>')
        arr.forEach(function(v, i) {
          var cx = p.l + iw * i / Math.max(arr.length - 1, 1)
          var cy = p.t + ih - ih * v / max
          svg.insertAdjacentHTML('beforeend', '<circle cx="' + cx + '" cy="' + cy + '" r="3.1" fill="' + color + '"/>')
        })
      }
      drawSeries(s1, '#55f7ff')
      if (s2) drawSeries(s2, '#ff9b61')
      if (labels && labels.length) {
        var step = labels.length > 8 ? 2 : 1
        labels.forEach(function(lab, i) {
          if (i % step !== 0 && i !== labels.length - 1) return
          svg.insertAdjacentHTML('beforeend', '<text x="' + (p.l + iw * i / Math.max(labels.length - 1, 1)) + '" y="' + (h - 6) + '" fill="#77b3c1" font-size="10" text-anchor="middle">' + lab + '</text>')
        })
      }
      if (title) {
        svg.insertAdjacentHTML('beforeend', '<text x="' + (w / 2) + '" y="10" fill="#8ffcff" font-size="11" font-weight="700" text-anchor="middle">' + title + '</text>')
      }
    },
    updateClock: function() {
      var d = new Date()
      var week = ['\u661f\u671f\u65e5', '\u661f\u671f\u4e00', '\u661f\u671f\u4e8c', '\u661f\u671f\u4e09', '\u661f\u671f\u56db', '\u661f\u671f\u4e94', '\u661f\u671f\u516d']
      var pad = function(n) { return String(n).padStart(2, '0') }
      var el = document.getElementById('timeBoard')
      if (el) el.innerHTML = d.getFullYear() + '.' + pad(d.getMonth() + 1) + '.' + pad(d.getDate()) + ' ' + week[d.getDay()] + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
    },

    // ============ Helpers ============
    getCategoryLabel: function(val) {
      var dicts = this.categoryDicts || []
      for (var i = 0; i < dicts.length; i++) {
        if (dicts[i].value === val || dicts[i].value === String(val)) return dicts[i].label
      }
      return val || '--'
    },
    formatMoney: function(val) {
      if (!val || val === 0) return '\u00a50'
      var n = parseFloat(val)
      if (n >= 100000000) return '\u00a5' + (n / 100000000).toFixed(1) + '\u4ebf'
      if (n >= 10000) return '\u00a5' + (n / 10000).toFixed(1) + '\u4e07'
      return '\u00a5' + n.toFixed(0)
    },
    _pearson: function(x, y) {
      if (!x || !y || x.length !== y.length || x.length === 0) return 0
      var n = x.length
      var sumX = 0
      var sumY = 0
      for (var i = 0; i < n; i++) { sumX += x[i]; sumY += y[i] }
      var meanX = sumX / n
      var meanY = sumY / n
      var num = 0
      var den1 = 0
      var den2 = 0
      for (var j = 0; j < n; j++) {
        var dx = x[j] - meanX
        var dy = y[j] - meanY
        num += dx * dy; den1 += dx * dx; den2 += dy * dy
      }
      var den = Math.sqrt(den1) * Math.sqrt(den2)
      return den ? (num / den) : 0
    }
  }
}
</script>

<style>
/* ====== Full-screen sci-fi dashboard styles ====== */
.smart-device-dashboard {
  width: 100%; height: 100%; overflow: hidden;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif; color: #e6fbff;
  background: radial-gradient(circle at 50% 14%, rgba(0,210,255,0.12), transparent 22%), radial-gradient(circle at 50% 100%, rgba(0,106,255,0.08), transparent 30%), linear-gradient(180deg, #030913 0%, #04101d 52%, #02070f 100%);
  position: relative;
}
.smart-device-dashboard::before { content: ""; position: absolute; inset: 0; background-image: linear-gradient(rgba(51,222,255,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(51,222,255,0.06) 1px, transparent 1px); background-size: 30px 30px; mask-image: radial-gradient(circle at center, black 40%, transparent 100%); pointer-events: none; opacity: 0.45; }
.smart-device-dashboard::after { content: ""; position: absolute; inset: 0; background: linear-gradient(180deg, rgba(255,255,255,.02), transparent 16%, transparent 84%, rgba(255,255,255,.02)), radial-gradient(circle at center, transparent 48%, rgba(0,0,0,0.25) 100%); pointer-events: none; }
:root { --bg-0: #020812; --bg-1: #051426; --bg-2: #071f39; --panel: rgba(8,24,44,0.78); --panel-2: rgba(5,18,35,0.92); --line: rgba(47,226,255,0.28); --line-strong: rgba(71,243,255,0.72); --cyan: #52f4ff; --cyan-2: #8ffcff; --blue: #4a8dff; --green: #6dffcf; --yellow: #ffe27a; --text: #e6fbff; --muted: #7eb9c8; --shadow: 0 0 22px rgba(36,220,255,0.12), inset 0 0 40px rgba(36,220,255,0.035); }
* { box-sizing: border-box; }
.dashboard { width: 100%; height: 100%; padding: 10px 10px 8px; display: grid; grid-template-columns: 290px minmax(900px, 1fr) 290px; grid-template-rows: 82px 96px minmax(0, 1fr) 240px; gap: 10px; position: relative; }
.scan { position: absolute; left: 0; right: 0; top: -90px; height: 90px; background: linear-gradient(180deg, transparent, rgba(82,244,255,.08), transparent); pointer-events: none; animation: scan 7s linear infinite; opacity: .65; }
@keyframes scan { 0% { transform: translateY(0); } 100% { transform: translateY(calc(100vh + 120px)); } }
.panel { position: relative; background: linear-gradient(180deg, rgba(8,26,48,0.72), rgba(3,13,26,0.92)); border: 1px solid var(--line); box-shadow: var(--shadow); overflow: hidden; min-height: 0; }
.panel::before { content: ""; position: absolute; inset: 0; background: linear-gradient(90deg, transparent, rgba(88,244,255,0.03), transparent), linear-gradient(180deg, rgba(255,255,255,0.015), transparent 28%); pointer-events: none; }
.panel::after { content: ""; position: absolute; inset: 8px; border: 1px solid rgba(89,245,255,0.06); pointer-events: none; }
.cut-corner { position: absolute; width: 14px; height: 14px; border-color: rgba(82,244,255,0.75); border-style: solid; pointer-events: none; filter: drop-shadow(0 0 4px rgba(82,244,255,.35)); }
.lt { top: 6px; left: 6px; border-width: 2px 0 0 2px; } .rt { top: 6px; right: 6px; border-width: 2px 2px 0 0; } .lb { bottom: 6px; left: 6px; border-width: 0 0 2px 2px; } .rb { bottom: 6px; right: 6px; border-width: 0 2px 2px 0; }
.header { grid-column: 1 / 4; position: relative; display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; }
.header .line { height: 2px; position: relative; background: linear-gradient(90deg, transparent, rgba(82,244,255,.9), transparent); filter: drop-shadow(0 0 6px rgba(82,244,255,.35)); }
.title-shell { margin: 0 16px; min-width: 520px; height: 68px; display: grid; place-items: center; position: relative; clip-path: polygon(5% 0, 95% 0, 100% 35%, 96% 100%, 4% 100%, 0 35%); background: linear-gradient(180deg, rgba(6,42,68,0.95), rgba(6,24,44,0.88)); border: 1px solid rgba(82,244,255,.32); box-shadow: 0 0 24px rgba(82,244,255,.12), inset 0 0 22px rgba(82,244,255,.08); }
.title-shell::before, .title-shell::after { content: ""; position: absolute; top: 14px; width: 86px; height: 18px; background-image: radial-gradient(circle, rgba(82,244,255,.85) 1.2px, transparent 1.5px); background-size: 10px 10px; opacity: .55; }
.title-shell::before { left: 34px; } .title-shell::after { right: 34px; }
.title-shell h1 { margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 800; color: #b3ffff; text-shadow: 0 0 16px rgba(82,244,255,.45), 0 0 26px rgba(82,244,255,.22); }
.left-top { grid-column: 1; grid-row: 2 / 4; display: grid; grid-template-rows: 210px 1fr; gap: 8px; } .right-top { grid-column: 3; grid-row: 2 / 4; display: grid; grid-template-rows: 210px 1fr; gap: 8px; } .top-strip { grid-column: 2; grid-row: 2; } .center-main { grid-column: 2; grid-row: 3; } .left-bottom { grid-column: 1; grid-row: 4; } .center-bottom { grid-column: 2; grid-row: 4; } .right-bottom { grid-column: 3; grid-row: 4; }
.inner { padding: 8px 10px 6px; height: 100%; position: relative; }
.panel-title { margin: 0 0 5px; display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 700; letter-spacing: 1px; color: var(--cyan-2); }
.panel-title::before, .panel-title::after { content: ""; width: 14px; height: 2px; background: linear-gradient(90deg, transparent, var(--cyan)); box-shadow: 0 0 8px rgba(82,244,255,.35); }
.counter-label { font-size: 11px; color: #a7f8ff; margin-bottom: 4px; }
.digits { display: flex; gap: 5px; margin-bottom: 6px; }
.digit { width: 34px; height: 40px; display: grid; place-items: center; font-size: 24px; font-weight: 800; color: #afffff; background: linear-gradient(180deg, rgba(0,173,255,0.16), rgba(0,76,118,0.36)); border: 1px solid rgba(82,244,255,.34); box-shadow: inset 0 0 10px rgba(82,244,255,.08), 0 0 10px rgba(82,244,255,.08); text-shadow: 0 0 10px rgba(82,244,255,.25); }
.ring-wrap { display: grid; place-items: center; position: relative; }
.ring { width: 115px; height: 115px; border-radius: 50%; background: conic-gradient(var(--yellow) 0 0%, rgba(106,250,255,0.92) 0% 100%); display: grid; place-items: center; box-shadow: 0 0 26px rgba(82,244,255,.14); position: relative; animation: breathe 3.8s ease-in-out infinite; }
.ring::before { content: ""; width: 75%; height: 75%; border-radius: 50%; background: radial-gradient(circle at center, rgba(8,31,50,0.96), rgba(3,13,24,0.96)); border: 1px solid rgba(82,244,255,.26); box-shadow: inset 0 0 18px rgba(82,244,255,.08); }
.ring::after { content: ""; position: absolute; inset: -8px; border-radius: 50%; border: 1px solid rgba(82,244,255,.12); }
.ring-text { position: absolute; text-align: center; font-size: 11px; font-weight: 700; line-height: 1.3; color: #ecffd3; } .ring-text strong { font-size: 22px; display: block; color: #fff7aa; }
.triple-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; margin-bottom: 8px; }
.mini-stat { min-height: 46px; display: flex; align-items: center; justify-content: center; flex-direction: column; text-align: center; padding: 3px 2px; border: 1px dashed rgba(82,244,255,.18); background: linear-gradient(180deg, rgba(9,39,61,0.26), rgba(5,19,34,0.16)); }
.mini-stat strong { font-size: 16px; color: #8ffcff; display: block; margin-bottom: 2px; } .mini-stat span { font-size: 10px; color: var(--muted); }
.chart { width: 100%; height: 100px; display: block; }
.grid-table { margin-top: 4px; font-size: 12px; border-top: 1px solid rgba(82,244,255,.14); }
.grid-table::-webkit-scrollbar { width: 4px; } .grid-table::-webkit-scrollbar-track { background: transparent; } .grid-table::-webkit-scrollbar-thumb { background: rgba(82,244,255,.2); border-radius: 2px; }
.grid-table .head, .grid-table .item { display: grid; grid-template-columns: 82px 1fr 64px; gap: 4px; align-items: center; padding: 6px 5px; }
.grid-table .head { color: #94f6ff; background: rgba(44,164,203,0.15); font-weight: 700; }
.grid-table .item:nth-child(odd) { background: rgba(255,255,255,.018); } .grid-table .item:nth-child(even) { background: rgba(0,0,0,.08); }
.grid-table .item span:first-child, .grid-table .head span:first-child { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.status-grid { height: 100%; display: grid; grid-template-columns: repeat(9, 1fr); gap: 6px; align-items: center; padding: 6px 6px 4px; }
.status-card { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; padding: 4px 2px; background: linear-gradient(180deg, rgba(7,28,49,0.35), rgba(3,12,24,0.08)); border: 1px solid rgba(82,244,255,.08); }
.status-card .count { font-size: 13px; font-weight: 800; color: #b4ffe6; margin-bottom: 2px; text-shadow: 0 0 8px rgba(82,244,255,.22); }
.status-card .icon { width: 30px; height: 30px; border-radius: 50%; display: grid; place-items: center; font-size: 15px; margin-bottom: 2px; background: radial-gradient(circle at 35% 30%, rgba(126,255,241,.45), rgba(0,88,122,.18) 65%, transparent 72%); border: 1px solid rgba(82,244,255,.14); box-shadow: inset 0 0 14px rgba(82,244,255,.08), 0 0 12px rgba(82,244,255,.08); }
.status-card .name { font-size: 10px; color: #93d5e2; white-space: normal; line-height: 1.15; width: 100%; }
.center-shell { position: relative; height: 100%; background: radial-gradient(circle at center, rgba(82,244,255,.08), transparent 28%), linear-gradient(180deg, rgba(4,21,39,0.4), rgba(2,10,20,0.82)); border: 1px solid rgba(82,244,255,.18); overflow: hidden; }
.center-shell::before { content: ""; position: absolute; inset: 10px; border: 1px solid rgba(82,244,255,.09); }
.center-title { position: absolute; left: 50%; top: 12px; transform: translateX(-50%); font-size: 22px; font-weight: 800; letter-spacing: 2px; color: #b4ffff; text-shadow: 0 0 12px rgba(82,244,255,.3); z-index: 3; }
.chip { position: absolute; width: 180px; height: 50px; display: flex; align-items: center; justify-content: center; gap: 8px; background: linear-gradient(90deg, rgba(8,48,70,0.34), rgba(8,108,138,0.18), rgba(8,48,70,0.34)); clip-path: polygon(10% 0, 90% 0, 100% 28%, 92% 100%, 8% 100%, 0 28%); border: 1px solid rgba(82,244,255,.18); color: #a9fdff; font-size: 18px; font-weight: 700; box-shadow: inset 0 0 18px rgba(82,244,255,.06), 0 0 14px rgba(82,244,255,.07); z-index: 2; }
.chip .badge { width: 28px; height: 28px; border-radius: 50%; display: grid; place-items: center; background: radial-gradient(circle, rgba(126,255,248,.46), rgba(0,84,109,.2)); border: 1px solid rgba(82,244,255,.22); font-size: 14px; }
.metric-box { position: absolute; width: 100px; min-height: 56px; display: flex; align-items: center; justify-content: center; flex-direction: column; text-align: center; border-radius: 6px; background: linear-gradient(180deg, rgba(11,42,70,0.76), rgba(5,18,34,0.92)); border: 1px solid rgba(82,244,255,.18); box-shadow: 0 0 12px rgba(82,244,255,.08); z-index: 2; }
.metric-box strong { font-size: 15px; color: #eaffff; display: block; margin-bottom: 3px; } .metric-box span { font-size: 11px; color: #9ccad5; }
/* Hub metrics carousel */
.hub-metrics { position: absolute; inset: 0; opacity: 0; pointer-events: none; transition: opacity 0.5s ease-in-out; z-index: 2; }
.hub-metrics.active { opacity: 1; pointer-events: auto; }
.hub-zone { position: absolute; inset: 50% auto auto 50%; transform: translate(-50%, -50%); width: 220px; height: 220px; z-index: 1; }
@keyframes breathe { 0%,100% { transform: scale(1); filter: brightness(1); } 50% { transform: scale(1.02); filter: brightness(1.08); } }
.runtime { position: absolute; left: 50%; bottom: 18px; transform: translateX(-50%); text-align: center; z-index: 2; }
.runtime .days { font-size: 32px; font-weight: 800; color: #87fbff; text-shadow: 0 0 14px rgba(82,244,255,.22); }
.runtime .tag { margin-top: 4px; display: inline-block; padding: 7px 20px 5px; font-size: 12px; color: #ebffff; background: linear-gradient(180deg, rgba(82,244,255,.24), rgba(8,56,74,0.26)); clip-path: polygon(10% 0, 90% 0, 100% 34%, 84% 100%, 16% 100%, 0 34%); border: 1px solid rgba(82,244,255,.2); }
.progress-wrap { display: grid; gap: 8px; margin-top: 6px; }
.progress-item { display: grid; grid-template-columns: 72px 1fr 38px; align-items: center; gap: 6px; font-size: 11px; color: #a8dbe6; }
.track { height: 10px; border-radius: 999px; background: rgba(255,255,255,.06); border: 1px solid rgba(82,244,255,.09); overflow: hidden; }
.bar { height: 100%; border-radius: 999px; background: linear-gradient(90deg, rgba(82,244,255,.32), rgba(109,255,207,.96)); box-shadow: 0 0 10px rgba(109,255,207,.18); animation: breathe 3.4s ease-in-out infinite; }
.energy-metrics { margin-top: 8px; margin-bottom: 10px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }
.energy-card { text-align: center; padding: 5px 0; }
.energy-card strong { display: block; font-size: 18px; color: #8ffcff; margin-bottom: 3px; } .energy-card span { color: #93cddb; font-size: 11px; }
.bottom-shell { height: 100%; display: flex; flex-direction: column; justify-content: space-between; }
.legend { display: flex; justify-content: flex-start; gap: 18px; font-size: 12px; color: #89c4cf; padding-left: 2px; }
.legend span::before { content: ""; display: inline-block; width: 10px; height: 10px; margin-right: 6px; vertical-align: -1px; }
.legend .people::before { background: #54f7ff; } .legend .car::before { background: #6dffcf; }
.time-board { text-align: center; color: #35f2ff; font-size: 20px; font-weight: 700; text-shadow: 0 0 10px rgba(82,244,255,.18); margin-top: 4px; padding: 6px 0; }
.time-board small { display: block; color: #86d9e3; font-size: 11px; margin-top: 3px; }

/* Carousel pages */
.carousel-host { position: relative; }
.carousel-page { opacity: 0; position: absolute; inset: 0; padding: 8px 10px 6px; transition: opacity 0.5s ease-in-out; pointer-events: none; z-index: 1; }
.carousel-page.active { opacity: 1; position: relative; pointer-events: auto; z-index: 2; }

/* Full-screen frame */
.bi-dash-frame { position: fixed; left: 0; right: 0; top: 0; bottom: 0; width: 100vw; height: 100vh; overflow: hidden; z-index: 9999; background-color: #020812; }
.bi-dash-stage { position: absolute; left: 0; top: 0; transform-origin: 0 0; }
.bi-dash-stage-scale { transform-origin: 0 0; }

/* Period selector */
</style>
