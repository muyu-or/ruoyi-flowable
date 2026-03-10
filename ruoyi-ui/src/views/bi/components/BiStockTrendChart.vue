<template>
  <div class="bi-chart-card">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-data-line" /> 出入库趋势</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </div>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'BiStockTrendChart',
  props: {
    chartData: {
      type: Array,
      default: function() {
        return []
      }
    },
    height: {
      type: String,
      default: '240px'
    }
  },
  data: function() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      handler: function() {
        this.renderChart()
      },
      deep: true
    }
  },
  mounted: function() {
    var self = this
    this.$nextTick(function() {
      self.initChart()
    })
    this._handleResize = function() {
      if (self.chart) {
        self.chart.resize()
      }
    }
    window.addEventListener('resize', this._handleResize)
  },
  beforeDestroy: function() {
    window.removeEventListener('resize', this._handleResize)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    initChart: function() {
      this.chart = echarts.init(this.$refs.chartDom)
      this.renderChart()
    },
    renderChart: function() {
      if (!this.chart) return
      var labels = this.chartData.map(function(d) { return d.label })
      var inbound = this.chartData.map(function(d) { return d.inboundQty || 0 })
      var outbound = this.chartData.map(function(d) { return d.outboundQty || 0 })
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(15,39,68,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#e2f3ff', fontSize: 12 }
        },
        legend: {
          top: 0,
          right: 0,
          textStyle: { color: 'rgba(226,243,255,0.6)', fontSize: 11 },
          data: ['入库', '出库']
        },
        grid: { top: 30, left: 40, right: 16, bottom: 24 },
        xAxis: {
          type: 'category',
          data: labels,
          axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' }},
          axisLabel: { color: 'rgba(226,243,255,0.6)', fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' }},
          axisLabel: { color: 'rgba(226,243,255,0.6)', fontSize: 10 }
        },
        series: [
          {
            name: '入库',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: { color: '#00d4ff', width: 2 },
            itemStyle: { color: '#00d4ff' },
            areaStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(0,212,255,0.25)' },
                  { offset: 1, color: 'rgba(0,212,255,0.02)' }
                ]
              }
            },
            data: inbound
          },
          {
            name: '出库',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: { color: '#a55eea', width: 2 },
            itemStyle: { color: '#a55eea' },
            areaStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(165,94,234,0.25)' },
                  { offset: 1, color: 'rgba(165,94,234,0.02)' }
                ]
              }
            },
            data: outbound
          }
        ]
      })
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
</style>
