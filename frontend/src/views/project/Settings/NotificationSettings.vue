<template>
  <div class="notification-settings-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('nav.notificationSettings') }}</h1>
    </div>

    <el-card>
      <h3 class="settings-section-title">{{ $t('notification.notificationTypes') }}</h3>
      <el-form :model="settings" label-width="200px">
        <el-form-item :label="$t('notification.taskAssigned')">
          <el-switch v-model="settings.taskAssigned" />
        </el-form-item>
        <el-form-item :label="$t('notification.taskStatusChanged')">
          <el-switch v-model="settings.taskStatusChanged" />
        </el-form-item>
        <el-form-item :label="$t('notification.sprintStart')">
          <el-switch v-model="settings.sprintStart" />
        </el-form-item>
        <el-form-item :label="$t('notification.sprintEnd')">
          <el-switch v-model="settings.sprintEnd" />
        </el-form-item>
        <el-form-item :label="$t('notification.timesheetApproved')">
          <el-switch v-model="settings.timesheetApproved" />
        </el-form-item>
        <el-form-item :label="$t('notification.timesheetRejected')">
          <el-switch v-model="settings.timesheetRejected" />
        </el-form-item>
      </el-form>

      <el-divider />

      <h3 class="settings-section-title">{{ $t('notification.notificationMethods') }}</h3>
      <el-form label-width="200px">
        <el-form-item :label="$t('notification.emailNotification')">
          <el-switch v-model="settings.emailEnabled" />
        </el-form-item>
        <el-form-item :label="$t('notification.inAppNotification')">
          <el-switch v-model="settings.inAppEnabled" />
        </el-form-item>
      </el-form>

      <el-divider />

      <h3 class="settings-section-title">{{ $t('notification.quietHours') }}</h3>
      <el-form label-width="200px">
        <el-form-item :label="$t('notification.enableQuietHours')">
          <el-switch v-model="settings.quietHoursEnabled" />
        </el-form-item>
        <el-form-item v-if="settings.quietHoursEnabled" :label="$t('notification.quietHoursStart')">
          <el-time-select
            v-model="settings.quietHoursStart"
            start="00:00"
            step="00:30"
            end="23:30"
          />
        </el-form-item>
        <el-form-item v-if="settings.quietHoursEnabled" :label="$t('notification.quietHoursEnd')">
          <el-time-select
            v-model="settings.quietHoursEnd"
            start="00:00"
            step="00:30"
            end="23:30"
          />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button type="primary" @click="onSave" :loading="saving">
          {{ $t('common.save') }}
        </el-button>
        <el-button @click="onReset">
          {{ $t('common.reset') }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const saving = ref(false)

const settings = ref({
  taskAssigned: true,
  taskStatusChanged: true,
  sprintStart: true,
  sprintEnd: true,
  timesheetApproved: true,
  timesheetRejected: true,
  emailEnabled: true,
  inAppEnabled: true,
  quietHoursEnabled: false,
  quietHoursStart: '22:00',
  quietHoursEnd: '08:00'
})

const defaultSettings = { ...settings.value }

const onSave = async () => {
  saving.value = true
  try {
    // TODO: Call API to save notification settings
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(t('common.saveSuccess'))
  } catch (e) {
    ElMessage.error(t('common.failed'))
  } finally {
    saving.value = false
  }
}

const onReset = () => {
  settings.value = { ...defaultSettings }
}

onMounted(() => {
  // TODO: Load notification settings from API
})
</script>

<style scoped>
.notification-settings-page {
  max-width: 800px;
}

.pm-page-header {
  margin-bottom: 24px;
}

.settings-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0 0 16px 0;
}

.form-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>
