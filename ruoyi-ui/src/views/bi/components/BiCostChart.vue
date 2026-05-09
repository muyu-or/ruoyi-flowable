<template>
  <div class="bi-chart-card">
    <div class="bi-card-header">
      <span class="bi-card-title"><i class="el-icon-money" /> 库存成本统计</span>
    </div>
    <!-- 上半：按分类成本饼图 -->
    <div ref="pieChartDom" style="height:180px;width:100%" />
    <!-- 下半：入库金额趋势柱状图 -->
    <div class="bi-card-title" style="margin:8px 0 4px;font-size:12px;">
      <i class="el-icon-trend-charts" /> 入库金额趋势
    </div>
    <div ref="barChartDom" style="height:160px;width:100%" />
  </div>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'BiCostChart',
  props: {
    costByCategory: {
      type: Array,
      default: function() { return [] }
    },
    stockAmountTrend: {
      type: Array,
      default: function() { return [] }
    },
    categoryDicts: {
      type: Array,
      default: function() { return [] }
    }
  },
  data: function() {
    return {
      pieChart: null,
      barChart: null
    }
  },
  watch: {
    costByCategory: {
      handler: function() { this.renderPie() },
      deep: true
    },
    stockAmountTrend: {
      handler: function() { this.renderBar() },
      deep: true
    }
  },
  mounted: function() {
    var self = this
    this.$nextTick(function() {
      self.initCharts()
    })
    this._handleResize = function() {
      if (self.pieChart) self.pieChart.resize()
      if (self.barChart) self.barChart.resize()
    }
    window.addEventListener('resize', this._handleResize)
  },
  beforeDestroy: function() {
    window.removeEventListener('resize', this._handleResize)
    if (this.pieChart) { this.pieChart.dispose(); this.pieChart = null }
    if (this.barChart) { this.barChart.dispose(); this.barChart = null }
  },
  methods: {
    initCharts: function() {
      this.pieChart = echarts.init(this.$refs.pieChartDom)
      this.barChart = echarts.init(this.$refs.barChartDom)
      this.renderPie()
      this.renderBar()
    },
    getDictLabel: function(value) {
      if (!this.categoryDicts || !this.categoryDicts.length) return value || '未分类'
      for (var i = 0; i < this.categoryDicts.length; i++) {
        if (this.categoryDicts[i].value === value) return this.categoryDicts[i].label
      }
      return value || '未分类'
    },
    renderPie: function() {
      if (!this.pieChart) return
      var self = this
      var colors = ['#00d4ff', '#26de81', '#ff9f43', '#a55eea', '#ff6b6b']
      var data = (this.costByCategory || []).map(function(d, i) {
        return {
          name: self.getDictLabel(d.category),
          value: +(d.totalCost || 0).toFixed(2),
          itemStyle: { color: colors[i % colors.length] }
        }
      })
      var hasData = data.some(function(d) { return d.value > 0 })
      this.pieChart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(15,39,68,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#e2f3ff', fontSize: 12 },
          formatter: function(p) {
            return p.name + ': ¥' + p.value.toFixed(2) + ' (' + p.percent + '%)'
          }
        },
        graphic: hasData ? [] : [{
          type: 'text', left: 'center', top: 'middle',
          style: { text: '暂无成本数据', fill: 'rgba(226,243,255,0.3)', fontSize: 13 }
        }],
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '52%'],
          avoidLabelOverlap: true,
          label: {
            color: 'rgba(226,243,255,0.8)',
            fontSize: 11,
            formatter: '{b}\n¥{c}'
          },
          labelLine: { lineStyle: { color: 'rgba(0,212,255,0.3)' } },
          data: data
        }]
      })
    },
    renderBar: function() {
      if (!this.barChart) return
      var trend = this.stockAmountTrend || []
      var labels = trend.map(function(d) { return d.label })
      var amounts = trend.map(function(d) { return +(d.inboundAmount || 0).toFixed(2) })
      this.barChart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(15,39,68,0.9)',
          borderColor: 'rgba(0,212,255,0.3)',
          textStyle: { color: '#e2f3ff', fontSize: 12 },
          formatter: function(params) {
            return params[0].name + '<br/>入库金额: ¥' + params[0].value
          }
        },
        grid: { left: 50, right: 10, top: 10, bottom: 28 },
        xAxis: {
          type: 'category',
          data: labels,
          axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
          axisLabel: { color: 'rgba(226,243,255,0.5)', fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: 'rgba(226,243,255,0.5)',
            fontSize: 10,
            formatter: function(v) { return v >= 10000 ? (v / 10000).toFixed(1) + 'w' : v }
          },
          splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' } }
        },
        series: [{
          type: 'bar',
          data: amounts,
          barMaxWidth: 32,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,212,255,0.9)' },
              { offset: 1, color: 'rgba(0,212,255,0.2)' }
            ])
          }
        }]
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
