<template>
  <el-card class="chart-card" shadow="hover">
    <div slot="header" class="chart-card-header">
      <span class="chart-title"><i class="el-icon-sell" /> 入库 / 出库趋势</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </el-card>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'StockTrendChart',
  props: {
    height: {
      type: String,
      default: '320px'
    },
    chartData: {
      type: Array,
      default: function() { return [] }
    }
  },
  data: function() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler: function(val) {
        if (this.chart) {
          this.setOptions(val)
        }
      }
    }
  },
  mounted: function() {
    var self = this
    this.$nextTick(function() {
      self.initChart()
    })
    window.addEventListener('resize', this._handleResize = function() {
      if (self.chart) self.chart.resize()
    })
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
      this.chart = echarts.init(this.$refs.chartDom, 'macarons')
      this.setOptions(this.chartData)
    },
    setOptions: function(chartData) {
      if (!chartData || chartData.length === 0) {
        this.chart.clear()
        this.chart.setOption({
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#909399', fontSize: 14 }
          }
        })
        return
      }

      var labels = []
      var inboundValues = []
      var outboundValues = []
      for (var i = 0; i < chartData.length; i++) {
        labels.push(chartData[i].label)
        inboundValues.push(chartData[i].inboundQty || 0)
        outboundValues.push(chartData[i].outboundQty || 0)
      }

      this.chart.clear()
      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' },
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#eee',
          textStyle: { color: '#333' }
        },
        legend: {
          data: ['入库量', '出库量'],
          bottom: 0,
          textStyle: { color: '#666' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '12%',
          top: '8%',
          containLabel: true
        },
        xAxis: [{
          type: 'category',
          boundaryGap: false,
          data: labels,
          axisLine: { lineStyle: { color: '#ddd' } },
          axisLabel: { color: '#666' }
        }],
        yAxis: [{
          type: 'value',
          name: '数量',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
          axisLabel: { color: '#999' }
        }],
        series: [
          {
            name: '入库量',
            type: 'line',
            data: inboundValues,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: { width: 3 },
            itemStyle: { color: '#409EFF' },
            areaStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(64,158,255,0.25)' },
                  { offset: 1, color: 'rgba(64,158,255,0.02)' }
                ]
              }
            }
          },
          {
            name: '出库量',
            type: 'line',
            data: outboundValues,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: { width: 3 },
            itemStyle: { color: '#E6A23C' },
            areaStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(230,162,60,0.25)' },
                  { offset: 1, color: 'rgba(230,162,60,0.02)' }
                ]
              }
            }
          }
        ]
      })
    }
  }
}
</script>

<style scoped>
.chart-card {
  margin-bottom: 0;
  border-radius: 8px;
}
.chart-card-header {
  display: flex;
  align-items: center;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.chart-title i {
  margin-right: 6px;
  color: #409EFF;
}
</style>
