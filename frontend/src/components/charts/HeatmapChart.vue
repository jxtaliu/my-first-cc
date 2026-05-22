<template>
  <div class="heatmap-chart-container" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  // Data format: [{ assignee: '张三', priority: 'P0', count: 3 }, ...]
  data: {
    type: Array,
    default: () => []
  },
  xAxisLabels: {
    type: Array,
    default: () => ['P0', 'P1', 'P2', 'P3']
  },
  yAxisLabels: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  // Build heatmap data matrix
  // X-axis: priorities (P0-P3)
  // Y-axis: assignees
  const xLabels = props.xAxisLabels.length > 0 ? props.xAxisLabels : ['P0', 'P1', 'P2', 'P3']
  const yLabels = props.yAxisLabels.length > 0 ? props.yAxisLabels : ['张三', '李四', '王五', '赵六']

  // Convert data to heatmap format: [x, y, value]
  const heatmapData = []

  if (props.data.length > 0) {
    props.data.forEach(item => {
      const xIndex = xLabels.indexOf(item.priority)
      const yIndex = yLabels.indexOf(item.assignee)
      if (xIndex >= 0 && yIndex >= 0) {
        heatmapData.push([xIndex, yIndex, item.count])
      }
    })
  } else {
    // Generate mock data
    for (let i = 0; i < yLabels.length; i++) {
      for (let j = 0; j < xLabels.length; j++) {
        const value = Math.floor(Math.random() * 8)
        heatmapData.push([j, i, value])
      }
    }
  }

  const option = {
    tooltip: {
      position: 'top',
      formatter: function (params) {
        const xLabel = xLabels[params.value[0]]
        const yLabel = yLabels[params.value[1]]
        const value = params.value[2]
        return `${yLabel}<br/>${xLabel}: ${value} 个任务`
      }
    },
    grid: {
      left: '3%',
      right: '10%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xLabels,
      splitArea: {
        show: true
      },
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#6B7280'
      }
    },
    yAxis: {
      type: 'category',
      data: yLabels,
      splitArea: {
        show: true
      },
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#6B7280'
      }
    },
    visualMap: {
      min: 0,
      max: 10,
      calculable: true,
      orient: 'vertical',
      right: '2%',
      top: 'center',
      inRange: {
        color: ['#E5E7EB', '#BFDBFE', '#3B82F6', '#1D4ED8', '#1E3A8A']
      },
      textStyle: {
        color: '#6B7280'
      }
    },
    series: [{
      name: '任务分布',
      type: 'heatmap',
      data: heatmapData,
      label: {
        show: true,
        color: '#fff',
        fontSize: 11
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      itemStyle: {
        borderRadius: 4,
        borderWidth: 2,
        borderColor: '#fff'
      }
    }]
  }

  chartInstance.setOption(option)
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.dispose()
    initChart()
  }
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})

watch(() => [props.data, props.xAxisLabels, props.yAxisLabels], () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.heatmap-chart-container {
  width: 100%;
  height: 250px;
}
</style>
