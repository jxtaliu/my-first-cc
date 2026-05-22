<template>
  <div class="radar-chart-container" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
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
  projectNames: {
    type: Array,
    default: () => []
  },
  height: {
    type: String,
    default: '400px'
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  // Build indicator data
  const indicator = props.labels.map((label, index) => ({
    name: label,
    max: 100
  }))

  // Build series data
  const series = props.data.map((values, index) => ({
    value: values,
    name: props.projectNames[index] || `Project ${index + 1}`,
    lineStyle: {
      color: props.colors[index % props.colors.length]
    },
    areaStyle: {
      color: props.colors[index % props.colors.length],
      opacity: 0.15
    },
    itemStyle: {
      color: props.colors[index % props.colors.length]
    }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: {
        color: '#1E293B'
      },
      formatter: function (params) {
        let result = `<strong>${params.name}</strong><br/>`
        params.value.forEach((val, idx) => {
          result += `${props.labels[idx]}: ${val.toFixed(1)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: props.projectNames.length > 0 ? props.projectNames : ['Project 1', 'Project 2'],
      bottom: 0,
      textStyle: {
        fontSize: 12,
        color: '#64748B'
      },
      itemWidth: 14,
      itemHeight: 8
    },
    radar: {
      indicator: indicator.length > 0 ? indicator : [
        { name: 'Completion Rate', max: 100 },
        { name: 'Work Efficiency', max: 100 },
        { name: 'Quality', max: 100 },
        { name: 'Throughput', max: 100 },
        { name: 'Milestone', max: 100 },
        { name: 'Schedule', max: 100 }
      ],
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#64748B',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(248, 250, 252, 0.5)', 'rgba(241, 245, 249, 0.5)']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: series.length > 0 ? series : [
          {
            value: [85, 78, 92, 88, 76, 90],
            name: 'Project 1',
            lineStyle: {
              color: '#00D4AA'
            },
            areaStyle: {
              color: '#00D4AA',
              opacity: 0.15
            },
            itemStyle: {
              color: '#00D4AA'
            }
          },
          {
            value: [72, 85, 78, 82, 90, 75],
            name: 'Project 2',
            lineStyle: {
              color: '#3B82F6'
            },
            areaStyle: {
              color: '#3B82F6',
              opacity: 0.15
            },
            itemStyle: {
              color: '#3B82F6'
            }
          }
        ]
      }
    ]
  }

  chartInstance.setOption(option)
}

const updateChart = () => {
  if (chartInstance) {
    // Build indicator data
    const indicator = props.labels.map((label) => ({
      name: label,
      max: 100
    }))

    // Build series data
    const series = props.data.map((values, index) => ({
      value: values,
      name: props.projectNames[index] || `Project ${index + 1}`,
      lineStyle: {
        color: props.colors[index % props.colors.length]
      },
      areaStyle: {
        color: props.colors[index % props.colors.length],
        opacity: 0.15
      },
      itemStyle: {
        color: props.colors[index % props.colors.length]
      }
    }))

    chartInstance.setOption({
      legend: {
        data: props.projectNames.length > 0 ? props.projectNames : ['Project 1', 'Project 2']
      },
      radar: {
        indicator: indicator.length > 0 ? indicator : [
          { name: 'Completion Rate', max: 100 },
          { name: 'Work Efficiency', max: 100 },
          { name: 'Quality', max: 100 },
          { name: 'Throughput', max: 100 },
          { name: 'Milestone', max: 100 },
          { name: 'Schedule', max: 100 }
        ]
      },
      series: [
        {
          type: 'radar',
          data: series.length > 0 ? series : [
            {
              value: [85, 78, 92, 88, 76, 90],
              name: 'Project 1'
            }
          ]
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

watch(() => [props.data, props.labels, props.projectNames], () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.radar-chart-container {
  width: 100%;
  height: v-bind(height);
}
</style>
