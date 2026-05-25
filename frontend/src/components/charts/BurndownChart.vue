<template>
  <div class="burndown-chart-container" ref="chartRef"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  idealData: {
    type: Array,
    default: () => []
  },
  actualData: {
    type: Array,
    default: () => []
  },
  xAxisData: {
    type: Array,
    default: () => []
  },
  totalPoints: {
    type: Number,
    default: 100
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
        })
        return result
      }
    },
    legend: {
      data: [t('chart.idealCurve'), t('chart.actualCurve')],
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
      data: props.xAxisData.length > 0 ? props.xAxisData : ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5'],
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
      name: t('chart.remainingWorkload'),
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
      },
      min: 0,
      max: props.totalPoints
    },
    series: [
      {
        name: t('chart.idealCurve'),
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: '#94A3B8',
          type: 'dashed',
          width: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(148, 163, 184, 0.3)' },
            { offset: 1, color: 'rgba(148, 163, 184, 0)' }
          ])
        },
        data: props.idealData.length > 0 ? props.idealData : [100, 90, 80, 70, 60, 50, 40, 30, 20, 10]
      },
      {
        name: t('chart.actualCurve'),
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: '#3B82F6',
          width: 3
        },
        itemStyle: {
          color: '#3B82F6'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0)' }
          ])
        },
        data: props.actualData.length > 0 ? props.actualData : [100, 95, 85, 78, 65, 55, 45, 35, 25, null]
      }
    ]
  }

  chartInstance.setOption(option)
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption({
      xAxis: {
        data: props.xAxisData.length > 0 ? props.xAxisData : ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5']
      },
      series: [
        {
          name: t('chart.idealCurve'),
          data: props.idealData.length > 0 ? props.idealData : [100, 90, 80, 70, 60, 50, 40, 30, 20, 10]
        },
        {
          name: t('chart.actualCurve'),
          data: props.actualData.length > 0 ? props.actualData : [100, 95, 85, 78, 65, 55, 45, 35, 25, null]
        }
      ],
      yAxis: {
        max: props.totalPoints
      }
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

watch(() => [props.idealData, props.actualData, props.xAxisData], () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.burndown-chart-container {
  width: 100%;
  height: 250px;
}
</style>
