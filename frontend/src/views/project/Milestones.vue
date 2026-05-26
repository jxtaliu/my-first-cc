<template>
  <div class="milestones-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.milestones') }}</h1>
        <p class="pm-text-small">{{ $t('project.milestonesDesc') }}</p>
      </div>
    </div>

    <!-- Filter and Actions -->
    <div class="milestones-tabs">
      <el-select v-model="filterStatus" :placeholder="$t('project.filterByStatus')" clearable>
        <el-option :label="$t('project.allMilestones')" :value="null" />
        <el-option :label="$t('project.planning')" value="PLANNING" />
        <el-option :label="$t('project.inProgress')" value="IN_PROGRESS" />
        <el-option :label="$t('project.completed')" value="COMPLETED" />
      </el-select>
      <el-button type="primary" @click="onCreateMilestone">
        <el-icon><Plus /></el-icon>
        {{ $t('project.createMilestone') }}
      </el-button>
    </div>

    <!-- Milestones Grid -->
    <div class="milestones-grid" v-loading="loading">
      <MilestoneCard
        v-for="milestone in filteredMilestones"
        :key="milestone.id"
        :milestone="milestone"
        @edit="onEditMilestone"
        @delete="onDeleteMilestone"
        @complete="onMarkComplete"
      />
      <el-empty v-if="filteredMilestones.length === 0" :description="$t('project.noMilestones')" />
    </div>

    <!-- Create/Edit Milestone Dialog -->
    <el-dialog
      v-model="showMilestoneDialog"
      :title="editingMilestone?.id ? $t('project.editMilestone') : $t('project.createMilestone')"
      width="500px"
      class="pm-dialog"
    >
      <el-form :model="milestoneForm" label-position="top">
        <el-form-item :label="$t('project.milestoneName')">
          <el-input v-model="milestoneForm.name" :placeholder="$t('project.milestoneNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.targetDate')">
          <el-date-picker
            v-model="milestoneForm.targetDate"
            type="date"
            :placeholder="$t('project.selectDate')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('project.description')">
          <el-input
            v-model="milestoneForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('project.descriptionPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMilestoneDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onSaveMilestone">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import MilestoneCard from '@/components/charts/MilestoneCard.vue'
import { useProjectStore } from '@/stores/project'
import {
  getMilestonesByProject,
  createMilestone,
  updateMilestone,
  deleteMilestone,
  completeMilestone
} from '@/api/milestone'

const { t } = useI18n()

const projectStore = useProjectStore()
const milestones = ref([])
const loading = ref(false)
const filterStatus = ref(null)
const showMilestoneDialog = ref(false)
const editingMilestone = ref(null)

const milestoneForm = ref({
  name: '',
  description: '',
  targetDate: null
})

const filteredMilestones = computed(() => {
  let result = milestones.value
  if (filterStatus.value) {
    result = result.filter(m => m.status === filterStatus.value)
  }
  return result
})

const loadMilestones = async () => {
  const projectId = projectStore.currentProjectId
  if (!projectId) {
    milestones.value = []
    return
  }
  loading.value = true
  try {
    const res = await getMilestonesByProject(projectId)
    milestones.value = res.data || []
  } catch (e) {
    console.error('Failed to load milestones:', e)
  } finally {
    loading.value = false
  }
}

const onCreateMilestone = () => {
  editingMilestone.value = null
  milestoneForm.value = {
    name: '',
    description: '',
    targetDate: null
  }
  showMilestoneDialog.value = true
}

const onEditMilestone = (milestone) => {
  editingMilestone.value = milestone
  milestoneForm.value = {
    name: milestone.name,
    description: milestone.description,
    targetDate: milestone.targetDate
  }
  showMilestoneDialog.value = true
}

const onSaveMilestone = async () => {
  if (!milestoneForm.value.name) {
    ElMessage.warning(t('project.milestoneNameRequired'))
    return
  }
  try {
    const data = {
      name: milestoneForm.value.name,
      description: milestoneForm.value.description,
      targetDate: milestoneForm.value.targetDate,
      projectId: projectStore.currentProjectId,
      status: editingMilestone.value ? undefined : 'PLANNING'
    }
    if (editingMilestone.value) {
      await updateMilestone(editingMilestone.value.id, data)
    } else {
      await createMilestone(data)
    }
    ElMessage.success(t('project.milestoneSaved'))
    showMilestoneDialog.value = false
    loadMilestones()
  } catch (e) {
    ElMessage.error(t('project.saveFailed'))
  }
}

const onMarkComplete = async (milestone) => {
  try {
    await completeMilestone(milestone.id)
    ElMessage.success(t('project.milestoneCompleted'))
    loadMilestones()
  } catch (e) {
    ElMessage.error(t('project.completeFailed'))
  }
}

const onDeleteMilestone = async (milestone) => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmDeleteMilestone'),
      t('project.deleteMilestone'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await deleteMilestone(milestone.id)
    ElMessage.success(t('project.milestoneDeleted'))
    loadMilestones()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('project.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadMilestones()
})

watch(() => projectStore.currentProjectId, () => {
  loadMilestones()
})
</script>

<style scoped>
.milestones-page {
  max-width: 1400px;
}

.milestones-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: var(--pm-space-xl) 0;
}

.milestones-section {
  margin-bottom: var(--pm-space-3xl);
}

.milestones-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin-bottom: var(--pm-space-lg);
  padding-bottom: var(--pm-space-sm);
  border-bottom: 2px solid var(--pm-border);
}

.milestones-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: var(--pm-space-lg);
}
</style>
