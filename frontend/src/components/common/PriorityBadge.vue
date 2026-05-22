<template>
  <span class="pm-priority-badge" :class="[variant, priorityClass]">
    <span class="pm-priority-dot"></span>
    <span v-if="showLabel || $slots.default" class="pm-priority-label">
      <slot>{{ priority }}</slot>
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  priority: {
    type: String,
    required: true,
    validator: (value) => ['P0', 'P1', 'P2', 'P3'].includes(value)
  },
  showLabel: {
    type: Boolean,
    default: false
  },
  variant: {
    type: String,
    default: 'badge',
    validator: (value) => ['badge', 'dot'].includes(value)
  }
})

const priorityClass = computed(() => {
  return `priority-${props.priority.toLowerCase()}`
})
</script>

<style scoped>
.pm-priority-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.pm-priority-badge.badge {
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
}

.pm-priority-badge.dot {
  gap: 0;
}

.pm-priority-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.pm-priority-badge.dot .pm-priority-dot {
  width: 6px;
  height: 6px;
}

.pm-priority-label {
  line-height: 1;
}

/* P0 - Critical - Red */
.pm-priority-badge.priority-p0 .pm-priority-dot {
  background-color: #EF4444;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2);
}

.pm-priority-badge.priority-p0.badge {
  background-color: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

/* P1 - High - Orange */
.pm-priority-badge.priority-p1 .pm-priority-dot {
  background-color: #F97316;
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.2);
}

.pm-priority-badge.priority-p1.badge {
  background-color: rgba(249, 115, 22, 0.1);
  color: #F97316;
}

/* P2 - Medium - Yellow */
.pm-priority-badge.priority-p2 .pm-priority-dot {
  background-color: #FBBF24;
  box-shadow: 0 0 0 2px rgba(251, 191, 36, 0.2);
}

.pm-priority-badge.priority-p2.badge {
  background-color: rgba(251, 191, 36, 0.15);
  color: #B45309;
}

/* P3 - Low - Gray */
.pm-priority-badge.priority-p3 .pm-priority-dot {
  background-color: #6B7280;
  box-shadow: 0 0 0 2px rgba(107, 114, 128, 0.2);
}

.pm-priority-badge.priority-p3.badge {
  background-color: rgba(107, 114, 128, 0.1);
  color: #6B7280;
}
</style>
