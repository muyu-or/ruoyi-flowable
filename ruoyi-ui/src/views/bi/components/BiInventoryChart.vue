<template>
  <div class="bi-chart-card">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-pie-chart" /> 库存分类占比</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </div>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'BiInventoryChart',
  props: {
    chartData: {
      type: Array,
      default: function() {
        return []
      }
    },
    categoryDicts: {
      type: Array,
      default: function() {
        return []
      }
    },
    height: {
      type: String,
      default: '220px'
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
    },
    categoryDicts: {
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
    getDictLabel: function(value) {
      if (!this.categoryDicts || !this.categoryDicts.length) return value
      for (var i = 0; i < this.categoryDicts.length; i++) {
        if (this.categoryDicts[i].value === value) {
          return this.categoryDicts[i].label
        }
      }
      return value || '未分类'
    },
    renderChart: function() {
      if (!this.chart) return
      var self = this
      var colors = ['#00d4ff', '#26de81', '#ff9f43', '#a55eea', '#ff6b6b', '#45aaf2']
      var data = this.chartData.map(function(d, i) {
        return {
          name: self.getDictLabel(d.category),
          value: d.totalQty || 0,
          itemStyle: { color: colors[i % colors.length] }
        }
      })
      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(15,39,68,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#e2f3ff', fontSize: 12 },
          formatter: '{b}: {c} ({d}%)'
        },
        series: [
          {
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '55%'],
            avoidLabelOverlap: true,
            label: {
              color: 'rgba(226,243,255,0.8)',
              fontSize: 11,
              formatter: '{b}\n{d}%'
            },
            labelLine: {
              lineStyle: { color: 'rgba(0,212,255,0.3)' }
            },
            data: data
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
