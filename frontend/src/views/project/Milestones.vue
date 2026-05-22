<template>
  <div class="milestones-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.milestones') }}</h1>
        <p class="pm-text-small">{{ $t('project.milestonesDesc') }}</p>
      </div>
      <el-button type="primary" @click="onCreateMilestone">
        <el-icon><Plus /></el-icon>
        {{ $t('project.createMilestone') }}
      </el-button>
    </div>

    <!-- Filter Tabs -->
    <div class="milestones-tabs">
      <el-radio-group v-model="activeTab">
        <el-radio-button label="all">{{ $t('project.allMilestones') }}</el-radio-button>
        <el-radio-button label="cross-project">{{ $t('project.crossProject') }}</el-radio-button>
        <el-radio-button label="project">{{ $t('project.projectMilestones') }}</el-radio-button>
      </el-radio-group>
      <el-select v-model="filterProject" :placeholder="$t('project.filterByProject')" clearable style="width: 180px">
        <el-option
          v-for="project in projects"
          :key="project.id"
          :label="project.name"
          :value="project.id"
        />
      </el-select>
    </div>

    <!-- Cross-Project Milestones -->
    <div class="milestones-section" v-if="activeTab === 'all' || activeTab === 'cross-project'">
      <h3 class="milestones-section-title" v-if="activeTab === 'all'">{{ $t('project.crossProject') }}</h3>
      <div class="milestones-grid">
        <MilestoneCard
          v-for="milestone in crossProjectMilestones"
          :key="milestone.id"
          :milestone="milestone"
          @click="onMilestoneClick"
        />
      </div>
      <el-empty v-if="crossProjectMilestones.length === 0" :description="$t('project.noMilestones')" />
    </div>

    <!-- Project Milestones -->
    <div class="milestones-section" v-if="activeTab === 'all' || activeTab === 'project'">
      <h3 class="milestones-section-title" v-if="activeTab === 'all'">{{ $t('project.projectMilestones') }}</h3>
      <div class="milestones-grid">
        <MilestoneCard
          v-for="milestone in projectMilestones"
          :key="milestone.id"
          :milestone="milestone"
          @click="onMilestoneClick"
        />
      </div>
      <el-empty v-if="projectMilestones.length === 0" :description="$t('project.noMilestones')" />
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
        <el-form-item :label="$t('project.relatedProjects')">
          <el-select v-model="milestoneForm.projectIds" multiple style="width: 100%">
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
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
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import MilestoneCard from '@/components/charts/MilestoneCard.vue'

const { t } = useI18n()

const activeTab = ref('all')
const filterProject = ref(null)
const projects = ref([])
const milestones = ref([])
const showMilestoneDialog = ref(false)
const editingMilestone = ref(null)

const milestoneForm = ref({
  name: '',
  targetDate: null,
  projectIds: [],
  description: ''
})

const crossProjectMilestones = computed(() => {
  return milestones.value.filter(m => m.isCrossProject)
})

const projectMilestones = computed(() => {
  let result = milestones.value.filter(m => !m.isCrossProject)
  if (filterProject.value) {
    result = result.filter(m => m.projectIds?.includes(filterProject.value))
  }
  return result
})

const loadMockData = () => {
  projects.value = [
    { id: 1, name: 'SME-PM系统' },
    { id: 2, name: '客户CRM项目' },
    { id: 3, name: '电商平台' }
  ]

  milestones.value = [
    {
      id: 1,
      name: '发布 V2.0',
      targetDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      isCrossProject: true,
      totalTasks: 25,
      completedTasks: 20,
      projectNames: ['SME-PM系统', '客户CRM项目', '电商平台'],
      projectIds: [1, 2, 3]
    },
    {
      id: 2,
      name: 'Sprint 15 完成',
      targetDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000),
      isCrossProject: false,
      totalTasks: 20,
      completedTasks: 16,
      projectNames: ['SME-PM系统'],
      projectIds: [1]
    },
    {
      id: 3,
      name: '用户模块上线',
      targetDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
      isCrossProject: false,
      totalTasks: 15,
      completedTasks: 5,
      projectNames: ['客户CRM项目'],
      projectIds: [2]
    },
    {
      id: 4,
      name: 'Alpha 版本发布',
      targetDate: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // Overdue
      isCrossProject: true,
      totalTasks: 30,
      completedTasks: 22,
      projectNames: ['SME-PM系统', '电商平台'],
      projectIds: [1, 3]
    },
    {
      id: 5,
      name: '支付模块集成',
      targetDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      isCrossProject: false,
      totalTasks: 8,
      completedTasks: 0,
      projectNames: ['电商平台'],
      projectIds: [3]
    }
  ]
}

const onCreateMilestone = () => {
  editingMilestone.value = null
  milestoneForm.value = {
    name: '',
    targetDate: null,
    projectIds: [],
    description: ''
  }
  showMilestoneDialog.value = true
}

const onMilestoneClick = (milestone) => {
  ElMessage.info(`查看里程碑: ${milestone.name}`)
}

const onSaveMilestone = () => {
  if (!milestoneForm.value.name) {
    ElMessage.warning(t('project.milestoneNameRequired'))
    return
  }
  ElMessage.success(milestoneForm.value.id ? t('project.milestoneUpdated') : t('project.milestoneCreated'))
  showMilestoneDialog.value = false
}

onMounted(() => {
  loadMockData()
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
