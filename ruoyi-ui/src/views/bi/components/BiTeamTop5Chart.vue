<template>
  <div class="bi-chart-card">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-trophy" /> 个人完成 Top5</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </div>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'BiUserTop5Chart',
  props: {
    userTop5List: {
      type: Array,
      default: function() {
        return []
      }
    },
    height: {
      type: String,
      default: '200px'
    }
  },
  data: function() {
    return {
      chart: null
    }
  },
  watch: {
    userTop5List: {
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
      var list = this.userTop5List || []
      var names = list.map(function(t) { return t.userName || '' })
      var values = list.map(function(t) { return t.finished || 0 })
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(15,39,68,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#e2f3ff', fontSize: 12 },
          axisPointer: { type: 'shadow' }
        },
        grid: { top: 10, left: 80, right: 40, bottom: 10 },
        xAxis: {
          type: 'value',
          axisLabel: { color: 'rgba(226,243,255,0.6)', fontSize: 10 },
          splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' }}
        },
        yAxis: {
          type: 'category',
          data: names,
          inverse: true,
          axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' }},
          axisLabel: { color: 'rgba(226,243,255,0.8)', fontSize: 11 }
        },
        series: [
          {
            type: 'bar',
            barWidth: 14,
            label: {
              show: true,
              position: 'right',
              color: 'rgba(226,243,255,0.8)',
              fontSize: 11,
              formatter: '{c}'
            },
            itemStyle: {
              borderRadius: [0, 4, 4, 0],
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 1, y2: 0,
                colorStops: [
                  { offset: 0, color: '#00d4ff' },
                  { offset: 1, color: '#26de81' }
                ]
              }
            },
            data: values
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
