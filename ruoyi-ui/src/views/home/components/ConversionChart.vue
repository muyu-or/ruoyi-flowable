<template>
  <el-card class="chart-card" shadow="hover">
    <div slot="header" class="chart-card-header">
      <span class="chart-title"><i class="el-icon-s-marketing" /> 物料转化统计</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </el-card>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'ConversionChart',
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
      var rawInValues = []
      var productInValues = []
      for (var i = 0; i < chartData.length; i++) {
        labels.push(chartData[i].label)
        rawInValues.push(chartData[i].rawIn || 0)
        productInValues.push(chartData[i].productIn || 0)
      }

      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#eee',
          textStyle: { color: '#333' }
        },
        legend: {
          data: ['原料入库', '产品入库'],
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
          data: labels,
          axisTick: { alignWithLabel: true },
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
            name: '原料入库',
            type: 'bar',
            barWidth: '30%',
            data: rawInValues,
            itemStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: '#f6d365' },
                  { offset: 1, color: '#fda085' }
                ]
              },
              borderRadius: [4, 4, 0, 0]
            }
          },
          {
            name: '产品入库',
            type: 'bar',
            barWidth: '30%',
            data: productInValues,
            itemStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: '#a1c4fd' },
                  { offset: 1, color: '#c2e9fb' }
                ]
              },
              borderRadius: [4, 4, 0, 0]
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
  color: #f6d365;
}
</style>
