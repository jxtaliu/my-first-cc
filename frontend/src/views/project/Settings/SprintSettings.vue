<template>
  <div class="sprint-settings-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ $t('settings.sprintSettings') }}</h1>
        <p class="page-desc">{{ $t('settings.sprintSettingsDesc') }}</p>
      </div>
    </div>

    <!-- Sprint Configuration -->
    <el-card class="settings-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ $t('settings.sprintConfiguration') }}</span>
        </div>
      </template>
      <el-form :model="sprintConfig" label-width="160px">
        <el-form-item :label="$t('settings.defaultDuration')">
          <el-input-number v-model="sprintConfig.defaultDuration" :min="1" :max="8" />
          <span class="form-help">{{ $t('settings.weeks') }}</span>
        </el-form-item>
        <el-form-item :label="$t('settings.defaultCapacity')">
          <el-input-number v-model="sprintConfig.defaultCapacity" :min="1" :max="168" />
          <span class="form-help">{{ $t('settings.hoursPerWeek') }}</span>
        </el-form-item>
        <el-form-item :label="$t('settings.workingDays')">
          <el-checkbox-group v-model="sprintConfig.workingDays">
            <el-checkbox label="1">{{ $t('settings.monday') }}</el-checkbox>
            <el-checkbox label="2">{{ $t('settings.tuesday') }}</el-checkbox>
            <el-checkbox label="3">{{ $t('settings.wednesday') }}</el-checkbox>
            <el-checkbox label="4">{{ $t('settings.thursday') }}</el-checkbox>
            <el-checkbox label="5">{{ $t('settings.friday') }}</el-checkbox>
            <el-checkbox label="6">{{ $t('settings.saturday') }}</el-checkbox>
            <el-checkbox label="0">{{ $t('settings.sunday') }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item :label="$t('settings.startDay')">
          <el-select v-model="sprintConfig.startDay" style="width: 150px">
            <el-option :label="$t('settings.monday')" value="1" />
            <el-option :label="$t('settings.sunday')" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('settings.autoStartSprint')">
          <el-switch v-model="sprintConfig.autoStartSprint" />
        </el-form-item>
        <el-form-item :label="$t('settings.autoCompleteSprint')">
          <el-switch v-model="sprintConfig.autoCompleteSprint" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Sprint Features -->
    <el-card class="settings-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ $t('settings.sprintFeatures') }}</span>
        </div>
      </template>
      <el-form label-width="160px">
        <el-form-item :label="$t('settings.enableSprintGoal')">
          <el-switch v-model="features.enableSprintGoal" />
          <span class="form-help">{{ $t('settings.enableSprintGoalDesc') }}</span>
        </el-form-item>
        <el-form-item :label="$t('settings.enableCapacityPlanning')">
          <el-switch v-model="features.enableCapacityPlanning" />
          <span class="form-help">{{ $t('settings.enableCapacityPlanningDesc') }}</span>
        </el-form-item>
        <el-form-item :label="$t('settings.enableBurndownChart')">
          <el-switch v-model="features.enableBurndownChart" />
          <span class="form-help">{{ $t('settings.enableBurndownChartDesc') }}</span>
        </el-form-item>
        <el-form-item :label="$t('settings.enableVelocityTracking')">
          <el-switch v-model="features.enableVelocityTracking" />
          <span class="form-help">{{ $t('settings.enableVelocityTrackingDesc') }}</span>
        </el-form-item>
        <el-form-item :label="$t('settings.enableSprintRetrospective')">
          <el-switch v-model="features.enableSprintRetrospective" />
          <span class="form-help">{{ $t('settings.enableSprintRetrospectiveDesc') }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Notification Settings -->
    <el-card class="settings-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ $t('settings.notificationSettings') }}</span>
        </div>
      </template>
      <el-form label-width="160px">
        <el-form-item :label="$t('settings.sprintStartNotification')">
          <el-switch v-model="notifications.sprintStart" />
        </el-form-item>
        <el-form-item :label="$t('settings.sprintEndNotification')">
          <el-switch v-model="notifications.sprintEnd" />
        </el-form-item>
        <el-form-item :label="$t('settings.sprintEndReminder')">
          <el-switch v-model="notifications.sprintEndReminder" />
          <span class="form-help">{{ $t('settings.reminderDaysBefore') }}</span>
          <el-input-number
            v-if="notifications.sprintEndReminder"
            v-model="notifications.reminderDays"
            :min="1"
            :max="7"
            size="small"
            style="width: 80px; margin-left: 8px"
          />
        </el-form-item>
        <el-form-item :label="$t('settings.capacityWarning')">
          <el-switch v-model="notifications.capacityWarning" />
          <span class="form-help">{{ $t('settings.capacityWarningDesc') }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Action Buttons -->
    <div class="action-buttons">
      <el-button @click="handleReset">{{ $t('settings.resetDefaults') }}</el-button>
      <el-button type="primary" @click="handleSave">{{ $t('common.save') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const route = useRoute()
const { t } = useI18n()

const projectId = ref(parseInt(route.params.id) || 1)

const sprintConfig = ref({
  defaultDuration: 2,
  defaultCapacity: 40,
  workingDays: ['1', '2', '3', '4', '5'],
  startDay: '1',
  autoStartSprint: false,
  autoCompleteSprint: false
})

const features = reactive({
  enableSprintGoal: true,
  enableCapacityPlanning: true,
  enableBurndownChart: true,
  enableVelocityTracking: true,
  enableSprintRetrospective: false
})

const notifications = reactive({
  sprintStart: true,
  sprintEnd: true,
  sprintEndReminder: true,
  reminderDays: 2,
  capacityWarning: true
})

const handleSave = async () => {
  try {
    // In real implementation, save to API
    // await updateSprintSettings(projectId.value, { sprintConfig: sprintConfig.value, features, notifications })
    ElMessage.success(t('settings.settingsSaved'))
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleReset = () => {
  sprintConfig.value = {
    defaultDuration: 2,
    defaultCapacity: 40,
    workingDays: ['1', '2', '3', '4', '5'],
    startDay: '1',
    autoStartSprint: false,
    autoCompleteSprint: false
  }
  features.enableSprintGoal = true
  features.enableCapacityPlanning = true
  features.enableBurndownChart = true
  features.enableVelocityTracking = true
  features.enableSprintRetrospective = false
  notifications.sprintStart = true
  notifications.sprintEnd = true
  notifications.sprintEndReminder = true
  notifications.reminderDays = 2
  notifications.capacityWarning = true
  ElMessage.info(t('settings.settingsReset'))
}

onMounted(() => {
  // In real implementation, fetch settings from API
})
</script>

<style scoped>
.sprint-settings-page {
  max-width: 900px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 4px 0;
}

.page-desc {
  margin: 0;
  color: var(--pm-text-secondary);
  font-size: 14px;
}

.settings-card {
  margin-bottom: 16px;
}

.card-header {
  font-weight: 600;
}

.form-help {
  margin-left: 12px;
  color: var(--pm-text-muted);
  font-size: 13px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>
