<template>
  <div class="cfd-chart-container" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  // Array of { date: '2024-01-01', todo: 10, inProgress: 5, done: 3 }
  data: {
    type: Array,
    default: () => []
  },
  statusColors: {
    type: Object,
    default: () => ({
      'todo': '#94A3B8',
      'in_progress': '#3B82F6',
      'testing': '#F59E0B',
      'done': '#10B981'
    })
  }
})

const chartRef = ref(null)
let chartInstance = null

const statusNames = {
  'todo': '待办',
  'in_progress': '进行中',
  'testing': '测试中',
  'development': '开发完成',
  'done': '已完成'
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  // Extract dates and status keys
  const dates = props.data.length > 0
    ? props.data.map(d => d.date)
    : ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5', 'Day 6', 'Day 7']

  const statusKeys = props.data.length > 0
    ? Object.keys(props.data[0]).filter(k => k !== 'date')
    : ['todo', 'in_progress', 'done']

  // Prepare series data for stacked area chart
  const series = statusKeys.map((status, index) => {
    const color = props.statusColors[status] || '#94A3B8'
    const data = props.data.length > 0
      ? props.data.map(d => d[status] || 0)
      : generateMockData(statusKeys.length, dates.length, index)

    return {
      name: statusNames[status] || status,
      type: 'line',
      stack: 'total',
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: color },
          { offset: 1, color: echarts.color.liftColor(color, -0.3) }
        ]),
        opacity: 0.8
      },
      lineStyle: {
        width: 1,
        color: color
      },
      itemStyle: {
        color: color
      },
      symbol: 'none',
      data: data
    }
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6B7280'
        }
      }
    },
    legend: {
      data: series.map(s => s.name),
      bottom: 0,
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
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
      type: 'value',
      name: '任务数',
      nameTextStyle: {
        color: '#6B7280'
      },
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#6B7280'
      },
      splitLine: {
        lineStyle: {
          color: '#F3F4F6'
        }
      }
    },
    series
  }

  chartInstance.setOption(option)
}

const generateMockData = (numStatuses, numDays, statusIndex) => {
  const baseValues = [20, 15, 10, 5, 2, 1, 0]
  return baseValues.slice(0, numDays).map((v, i) => {
    if (i < statusIndex) return 0
    return v + Math.floor(Math.random() * 5)
  })
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

watch(() => props.data, () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.cfd-chart-container {
  width: 100%;
  height: 250px;
}
</style>
