<template>
  <el-card class="chart-card" shadow="hover">
    <div slot="header" class="chart-card-header">
      <span class="chart-title"><i class="el-icon-box" /> 入库分类统计</span>
    </div>
    <div ref="chartDom" :style="{ height: height, width: '100%' }" />
  </el-card>
</template>

<script>
var echarts = require('echarts')
require('echarts/theme/macarons')

// 物料大类字典值 → 中文标签映射（与 material_category 字典保持一致）
var CATEGORY_LABELS = {
  'raw': '原材料',
  'semi': '半成品',
  'product': '成品',
  'auxiliary': '辅料'
}

export default {
  name: 'InventoryOverviewChart',
  props: {
    height: {
      type: String,
      default: '320px'
    },
    chartData: {
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
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler: function() {
        if (this.chart) {
          this.setOptions()
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
      if (self.chart) {
        self.chart.resize()
        // resize 后重新计算中心像素坐标
        self.setOptions()
      }
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
    getCategoryLabel: function(value) {
      // 优先从字典 prop 中查找
      if (this.categoryDicts && this.categoryDicts.length) {
        for (var i = 0; i < this.categoryDicts.length; i++) {
          if (String(this.categoryDicts[i].value) === String(value)) {
            return this.categoryDicts[i].label
          }
        }
      }
      // 降级用内置映射
      return CATEGORY_LABELS[value] || value || '未分类'
    },
    initChart: function() {
      this.chart = echarts.init(this.$refs.chartDom, 'macarons')
      this.setOptions()
    },
    setOptions: function() {
      var chartData = this.chartData
      if (!chartData || chartData.length === 0) {
        this.chart.clear()
        this.chart.setOption({
          title: {
            text: '暂无入库数据',
            left: 'center',
            top: 'center',
            textStyle: { color: '#909399', fontSize: 14 }
          }
        })
        return
      }

      var self = this
      var seriesData = []
      var total = 0
      for (var i = 0; i < chartData.length; i++) {
        var qty = chartData[i].totalQty || 0
        total += qty
        seriesData.push({
          name: self.getCategoryLabel(chartData[i].category),
          value: qty
        })
      }

      var colors = ['#667eea', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#6554C0']

      // 计算饼图中心像素坐标，用于 graphic 精确定位
      var chartWidth = this.chart.getWidth()
      var chartHeight = this.chart.getHeight()
      var pixelX = chartWidth * 0.35
      var pixelY = chartHeight * 0.5

      this.chart.clear()
      // 先解绑旧事件
      this.chart.off('mouseover')
      this.chart.off('mouseout')

      var defaultText = '{total|' + total + '}\n{label|入库总量}'
      var richStyle = {
        total: { fontSize: 22, fontWeight: 'bold', fill: '#303133', lineHeight: 30 },
        label: { fontSize: 12, fill: '#909399', lineHeight: 20 }
      }

      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#eee',
          textStyle: { color: '#333' },
          formatter: function(params) {
            return params.name + '<br/>数量: ' + params.value + '<br/>占比: ' + params.percent + '%'
          }
        },
        legend: {
          orient: 'vertical',
          right: '5%',
          top: 'center',
          textStyle: { color: '#666', fontSize: 12 },
          formatter: function(name) {
            for (var j = 0; j < seriesData.length; j++) {
              if (seriesData[j].name === name) {
                return name + '  ' + seriesData[j].value
              }
            }
            return name
          }
        },
        color: colors,
        graphic: [{
          type: 'text',
          position: [pixelX, pixelY],
          style: {
            text: defaultText,
            textAlign: 'center',
            textVerticalAlign: 'middle',
            rich: richStyle
          },
          z: 100
        }],
        series: [{
          name: '入库分类统计',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false },
          emphasis: {
            scale: true,
            scaleSize: 6
          },
          labelLine: { show: false },
          data: seriesData
        }]
      })

      // 通过事件动态更新中心文字
      var chart = this.chart
      chart.on('mouseover', function(params) {
        chart.setOption({
          graphic: [{
            style: {
              text: '{total|' + params.value + '}\n{label|' + params.name + '}',
              rich: richStyle
            }
          }]
        })
      })
      chart.on('mouseout', function() {
        chart.setOption({
          graphic: [{
            style: {
              text: defaultText,
              rich: richStyle
            }
          }]
        })
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
  color: #667eea;
}
</style>
