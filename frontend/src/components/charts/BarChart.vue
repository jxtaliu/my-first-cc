<template>
  <div class="bar-chart-container" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  labels: {
    type: Array,
    default: () => []
  },
  colors: {
    type: Array,
    default: () => ['#00D4AA', '#3B82F6', '#F59E0B', '#8B5CF6', '#EF4444', '#10B981']
  },
  showAverage: {
    type: Boolean,
    default: false
  },
  invertColor: {
    type: Boolean,
    default: false
  },
  unit: {
    type: String,
    default: ''
  },
  height: {
    type: String,
    default: '200px'
  }
})

const chartRef = ref(null)
let chartInstance = null

const average = computed(() => {
  if (!props.data.length) return 0
  const sum = props.data.reduce((acc, val) => acc + parseFloat(val), 0)
  return (sum / props.data.length).toFixed(1)
})

const getBarColor = (index) => {
  const color = props.colors[index % props.colors.length]
  if (props.invertColor) {
    // For metrics where lower is better (like defect density)
    const value = parseFloat(props.data[index])
    const avg = parseFloat(average.value)
    if (value < avg) {
      return '#10B981' // Green for good (below average)
    } else if (value > avg * 1.2) {
      return '#EF4444' // Red for bad (much above average)
    }
  }
  return color
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function (params) {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          const value = param.value + props.unit
          result += `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${param.color};"></span>${param.seriesName}: ${value}<br/>`
        })
        if (props.showAverage && params.length > 0) {
          result += `<br/><span style="color:#94A3B8;">Average: ${average.value}${props.unit}</span>`
        }
        return result
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: props.showAverage ? '15%' : '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.labels.length > 0 ? props.labels : ['Project 1', 'Project 2', 'Project 3'],
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#64748B',
        fontSize: 11,
        rotate: props.labels.length > 3 ? 15 : 0,
        overflow: 'truncate',
        width: 60
      }
    },
    yAxis: {
      type: 'value',
      name: props.unit,
      nameTextStyle: {
        color: '#94A3B8',
        fontSize: 11
      },
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#64748B'
      },
      splitLine: {
        lineStyle: {
          color: '#F1F5F9'
        }
      }
    },
    series: [
      {
        name: 'Value',
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: function (params) {
            return getBarColor(params.dataIndex)
          }
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        data: props.data.length > 0 ? props.data : [75, 62, 85, 45]
      }
    ]
  }

  // Add average line if needed
  if (props.showAverage) {
    option.series.push({
      name: 'Average',
      type: 'line',
      data: Array(props.labels.length || 3).fill(average.value),
      lineStyle: {
        color: '#94A3B8',
        type: 'dashed',
        width: 2
      },
      symbol: 'none',
      tooltip: {
        show: false
      }
    })
    option.legend = {
      data: ['Value', 'Average'],
      bottom: 0,
      textStyle: {
        fontSize: 11
      }
    }
  }

  chartInstance.setOption(option)
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption({
      xAxis: {
        data: props.labels.length > 0 ? props.labels : ['Project 1', 'Project 2', 'Project 3']
      },
      series: [
        {
          data: props.data.length > 0 ? props.data : [75, 62, 85, 45],
          itemStyle: {
            color: function (params) {
              return getBarColor(params.dataIndex)
            }
          }
        }
      ]
    })
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

watch(() => [props.data, props.labels], () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.bar-chart-container {
  width: 100%;
  height: v-bind(height);
}
</style>
