<template>
  <el-card class="chart-card" shadow="hover">
    <div slot="header" class="chart-card-header">
      <span class="chart-title"><i class="el-icon-s-custom" /> 班组任务完成率</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </el-card>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

export default {
  name: 'TeamCompletionChart',
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

      var teamNames = []
      var rateValues = []
      for (var i = 0; i < chartData.length; i++) {
        teamNames.push(chartData[i].teamName || '未知班组')
        rateValues.push(Math.round((chartData[i].completionRate || 0) * 10000) / 100)
      }

      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#eee',
          textStyle: { color: '#333' },
          formatter: function(params) {
            var item = params[0]
            return item.name + '<br/>' + item.seriesName + ': ' + item.value + '%'
          }
        },
        grid: {
          left: '3%',
          right: '12%',
          bottom: '5%',
          top: '8%',
          containLabel: true
        },
        xAxis: [{
          type: 'value',
          name: '完成率 %',
          max: 100,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
          axisLabel: { color: '#999', formatter: '{value}%' }
        }],
        yAxis: [{
          type: 'category',
          data: teamNames,
          axisLine: { lineStyle: { color: '#ddd' } },
          axisLabel: { color: '#666' },
          axisTick: { show: false }
        }],
        series: [
          {
            name: '完成率',
            type: 'bar',
            data: rateValues,
            barWidth: '50%',
            label: {
              show: true,
              position: 'right',
              formatter: '{c}%',
              color: '#666',
              fontSize: 12
            },
            itemStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
                colorStops: [
                  { offset: 0, color: '#67C23A' },
                  { offset: 1, color: '#85ce61' }
                ]
              },
              borderRadius: [0, 4, 4, 0]
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
  color: #67C23A;
}
</style>
