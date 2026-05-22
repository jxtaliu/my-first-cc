<template>
  <div class="pm-stat-card" :class="{ 'clickable': clickable }" @click="onClick">
    <div class="pm-stat-header">
      <span class="pm-stat-label">{{ label }}</span>
      <span v-if="trend" class="pm-stat-trend" :class="trendClass">
        <span class="pm-stat-trend-icon">{{ trendIcon }}</span>
        {{ Math.abs(trend) }}%
      </span>
    </div>
    <div class="pm-stat-value">{{ formattedValue }}</div>
    <div v-if="subtitle" class="pm-stat-subtitle">{{ subtitle }}</div>
    <div v-if="showProgress" class="pm-stat-progress">
      <div class="pm-progress">
        <div
          class="pm-progress-bar"
          :class="progressClass"
          :style="{ width: progress + '%' }"
        ></div>
      </div>
      <span class="pm-stat-progress-text">{{ progress }}%</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  unit: {
    type: String,
    default: ''
  },
  trend: {
    type: Number,
    default: null // positive = up, negative = down
  },
  subtitle: {
    type: String,
    default: ''
  },
  showProgress: {
    type: Boolean,
    default: false
  },
  progress: {
    type: Number,
    default: 0
  },
  clickable: {
    type: Boolean,
    default: false
  },
  format: {
    type: String,
    default: 'number' // number, percent, hours, currency
  }
})

const emit = defineEmits(['click'])

const formattedValue = computed(() => {
  if (props.format === 'percent') {
    return `${props.value}%`
  }
  if (props.format === 'hours') {
    return `${props.value}h`
  }
  if (props.format === 'currency') {
    return `¥${props.value.toLocaleString()}`
  }
  return props.value
})

const trendClass = computed(() => {
  if (!props.trend) return ''
  return props.trend > 0 ? 'positive' : 'negative'
})

const trendIcon = computed(() => {
  if (!props.trend) return ''
  return props.trend > 0 ? '↑' : '↓'
})

const progressClass = computed(() => {
  if (props.progress >= 100) return 'success'
  if (props.progress >= 70) return 'success'
  if (props.progress >= 30) return 'warning'
  return 'danger'
})

const onClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style scoped>
.pm-stat-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
  transition: all var(--pm-transition-normal);
}

.pm-stat-card.clickable {
  cursor: pointer;
}

.pm-stat-card.clickable:hover {
  box-shadow: var(--pm-shadow-md);
  transform: translateY(-2px);
}

.pm-stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pm-stat-label {
  font-size: 13px;
  color: var(--pm-text-secondary);
  font-weight: 500;
}

.pm-stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 600;
}

.pm-stat-trend.positive {
  color: var(--pm-status-done);
}

.pm-stat-trend.negative {
  color: var(--pm-status-blocked);
}

.pm-stat-trend-icon {
  font-size: 14px;
}

.pm-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--pm-text-primary);
  font-family: 'Plus Jakarta Sans', 'Inter', sans-serif;
  line-height: 1.2;
}

.pm-stat-subtitle {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.pm-stat-progress {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  margin-top: var(--pm-space-sm);
}

.pm-stat-progress .pm-progress {
  flex: 1;
}

.pm-stat-progress-text {
  font-size: 12px;
  color: var(--pm-text-secondary);
  font-weight: 500;
  min-width: 36px;
  text-align: right;
}
</style>
