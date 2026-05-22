<template>
  <div class="project-sidebar" :class="{ 'collapsed': isCollapsed }">
    <!-- Sidebar Toggle -->
    <div class="sidebar-toggle" @click="toggleSidebar">
      <el-icon>
        <component :is="isCollapsed ? 'DArrowRight' : 'DArrowLeft'" />
      </el-icon>
    </div>

    <!-- Sidebar Content -->
    <div class="sidebar-content" v-show="!isCollapsed">
      <!-- Search Projects -->
      <div class="sidebar-search">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('sidebar.searchProjects')"
          prefix-icon="Search"
          size="small"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- Quick Actions -->
      <div class="sidebar-quick-actions">
        <el-button type="primary" class="quick-add-btn" @click="handleQuickAdd">
          <el-icon><Plus /></el-icon>
          {{ $t('sidebar.quickAdd') }}
        </el-button>
      </div>

      <!-- Favorites Section -->
      <div class="sidebar-section" v-if="favorites.length > 0">
        <div class="sidebar-section-header">
          <span class="sidebar-section-title">{{ $t('sidebar.favorites') }}</span>
          <el-badge :value="favorites.length" type="warning" />
        </div>
        <div class="sidebar-section-content">
          <div
            v-for="project in favorites"
            :key="project.id"
            class="sidebar-project-item"
            :class="{ 'active': currentProjectId === project.id }"
            @click="navigateToProject(project)"
          >
            <span class="project-status-dot" :style="{ background: getStatusColor(project.status) }"></span>
            <span class="project-name">{{ project.name }}</span>
            <el-button
              type="danger"
              text
              size="small"
              class="unfavorite-btn"
              @click.stop="removeFavorite(project)"
            >
              <el-icon><StarFilled /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- Recent Projects Section -->
      <div class="sidebar-section" v-if="recentProjects.length > 0">
        <div class="sidebar-section-header">
          <span class="sidebar-section-title">{{ $t('sidebar.recent') }}</span>
        </div>
        <div class="sidebar-section-content">
          <div
            v-for="project in recentProjects"
            :key="project.id"
            class="sidebar-project-item"
            :class="{ 'active': currentProjectId === project.id }"
            @click="navigateToProject(project)"
          >
            <span class="project-status-dot" :style="{ background: getStatusColor(project.status) }"></span>
            <span class="project-name">{{ project.name }}</span>
            <el-button
              v-if="!isFavorite(project.id)"
              type="warning"
              text
              size="small"
              class="favorite-btn"
              @click.stop="addFavorite(project)"
            >
              <el-icon><Star /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- All Projects Section -->
      <div class="sidebar-section">
        <div class="sidebar-section-header">
          <span class="sidebar-section-title">{{ $t('sidebar.allProjects') }}</span>
          <el-badge :value="filteredProjects.length" type="info" />
        </div>
        <div class="sidebar-section-content">
          <div
            v-for="project in filteredProjects"
            :key="project.id"
            class="sidebar-project-item"
            :class="{ 'active': currentProjectId === project.id }"
            @click="navigateToProject(project)"
          >
            <span class="project-status-dot" :style="{ background: getStatusColor(project.status) }"></span>
            <span class="project-name">{{ project.name }}</span>
            <el-dropdown trigger="click" @click.stop>
              <el-button type="default" text size="small" class="more-btn">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click.stop="navigateToProject(project)">
                    <el-icon><View /></el-icon>
                    {{ $t('sidebar.viewProject') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="navigateToBoard(project)">
                    <el-icon><Board /></el-icon>
                    {{ $t('sidebar.board') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="navigateToGantt(project)">
                    <el-icon><Histogram /></el-icon>
                    {{ $t('sidebar.gantt') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="navigateToStats(project)">
                    <el-icon><DataAnalysis /></el-icon>
                    {{ $t('sidebar.stats') }}
                  </el-dropdown-item>
                  <el-dropdown-item divided @click.stop="addFavorite(project)" v-if="!isFavorite(project.id)">
                    <el-icon><Star /></el-icon>
                    {{ $t('sidebar.addToFavorites') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="removeFavorite(project)" v-else>
                    <el-icon><StarFilled /></el-icon>
                    {{ $t('sidebar.removeFromFavorites') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- Collapse Button -->
      <div class="sidebar-collapse" @click="toggleSidebar">
        <el-icon><DArrowLeft /></el-icon>
        <span>{{ $t('sidebar.collapse') }}</span>
      </div>
    </div>

    <!-- Collapsed State -->
    <div class="sidebar-collapsed-content" v-show="isCollapsed">
      <div
        class="collapsed-item"
        :title="$t('sidebar.searchProjects')"
        @click="toggleSidebar"
      >
        <el-icon><Search /></el-icon>
      </div>
      <div
        v-for="project in favorites.slice(0, 5)"
        :key="project.id"
        class="collapsed-item"
        :class="{ 'active': currentProjectId === project.id }"
        :title="project.name"
        @click="navigateToProject(project)"
      >
        <span class="collapsed-status" :style="{ background: getStatusColor(project.status) }"></span>
      </div>
      <div class="collapsed-divider"></div>
      <div
        class="collapsed-item"
        :title="$t('sidebar.allProjects')"
        @click="toggleSidebar"
      >
        <el-icon><Folder /></el-icon>
      </div>
    </div>

    <!-- Quick Add Dialog -->
    <el-dialog
      v-model="quickAddVisible"
      :title="$t('sidebar.quickAddProject')"
      width="400px"
    >
      <el-form :model="quickAddForm" :rules="quickAddRules" ref="quickAddFormRef" label-width="100px">
        <el-form-item :label="$t('project.name')" prop="name">
          <el-input v-model="quickAddForm.name" :placeholder="$t('sidebar.projectNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.type')" prop="type">
          <el-select v-model="quickAddForm.type" style="width: 100%">
            <el-option :label="$t('project.scrum')" value="SCRUM" />
            <el-option :label="$t('project.kanban')" value="KANBAN" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.description')">
          <el-input
            v-model="quickAddForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('sidebar.projectDescPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quickAddVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleQuickAddSubmit" :loading="quickAddLoading">
          {{ $t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  Search,
  Plus,
  Star,
  StarFilled,
  More,
  View,
  Board,
  Histogram,
  DataAnalysis,
  DArrowLeft,
  DArrowRight,
  Folder
} from '@element-plus/icons-vue'
import { getProjects, createProject } from '@/api/project'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

// State
const isCollapsed = ref(false)
const searchQuery = ref('')
const projects = ref([])
const favorites = ref([])
const recentProjects = ref([])
const currentProjectId = computed(() => route.params.id)
const quickAddVisible = ref(false)
const quickAddLoading = ref(false)
const quickAddFormRef = ref()
const quickAddForm = ref({
  name: '',
  type: 'SCRUM',
  description: ''
})

const quickAddRules = {
  name: [{ required: true, message: () => t('project.projectNameRequired') }],
  type: [{ required: true, message: () => t('project.projectTypeRequired') }]
}

// Load favorites and recent from localStorage
const loadStoredData = () => {
  try {
    const storedFavorites = localStorage.getItem('projectFavorites')
    if (storedFavorites) {
      favorites.value = JSON.parse(storedFavorites)
    }

    const storedRecent = localStorage.getItem('projectRecent')
    if (storedRecent) {
      recentProjects.value = JSON.parse(storedRecent)
    }
  } catch (error) {
    console.error('Failed to load stored data:', error)
  }
}

// Save favorites to localStorage
const saveFavorites = () => {
  try {
    localStorage.setItem('projectFavorites', JSON.stringify(favorites.value))
  } catch (error) {
    console.error('Failed to save favorites:', error)
  }
}

// Save recent to localStorage
const saveRecent = () => {
  try {
    localStorage.setItem('projectRecent', JSON.stringify(recentProjects.value))
  } catch (error) {
    console.error('Failed to save recent:', error)
  }
}

// Computed
const filteredProjects = computed(() => {
  if (!searchQuery.value) return projects.value

  const query = searchQuery.value.toLowerCase()
  return projects.value.filter(p =>
    p.name.toLowerCase().includes(query) ||
    p.description?.toLowerCase().includes(query)
  )
})

// Methods
const getStatusColor = (status) => {
  switch (status) {
    case 'active': return '#10B981'
    case 'planning': return '#3B82F6'
    case 'completed': return '#8B5CF6'
    case 'paused': return '#F59E0B'
    default: return '#94A3B8'
  }
}

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleSearch = () => {
  // Search is handled by computed property
}

const navigateToProject = (project) => {
  router.push(`/projects/${project.id}`)
  addToRecent(project)
}

const navigateToBoard = (project) => {
  router.push(`/projects/board/${project.id}`)
  addToRecent(project)
}

const navigateToGantt = (project) => {
  router.push(`/projects/gantt/${project.id}`)
  addToRecent(project)
}

const navigateToStats = (project) => {
  router.push(`/projects/stats`)
}

const addToRecent = (project) => {
  // Remove if already exists
  recentProjects.value = recentProjects.value.filter(p => p.id !== project.id)
  // Add to beginning
  recentProjects.value.unshift(project)
  // Keep only last 5
  recentProjects.value = recentProjects.value.slice(0, 5)
  saveRecent()
}

const isFavorite = (projectId) => {
  return favorites.value.some(p => p.id === projectId)
}

const addFavorite = (project) => {
  if (!isFavorite(project.id)) {
    favorites.value.push(project)
    saveFavorites()
    ElMessage.success(t('sidebar.addedToFavorites'))
  }
}

const removeFavorite = (project) => {
  favorites.value = favorites.value.filter(p => p.id !== project.id)
  saveFavorites()
  ElMessage.success(t('sidebar.removedFromFavorites'))
}

const handleQuickAdd = () => {
  quickAddForm.value = {
    name: '',
    type: 'SCRUM',
    description: ''
  }
  quickAddVisible.value = true
}

const handleQuickAddSubmit = async () => {
  const valid = await quickAddFormRef.value.validate().catch(() => false)
  if (!valid) return

  quickAddLoading.value = true
  try {
    const res = await createProject(quickAddForm.value)
    const newProject = res.data || res
    projects.value.unshift(newProject)
    quickAddVisible.value = false
    ElMessage.success(t('project.projectCreated'))
    navigateToProject(newProject)
  } catch (error) {
    console.error('Failed to create project:', error)
    ElMessage.error(t('common.failed'))
  } finally {
    quickAddLoading.value = false
  }
}

// Load projects
const loadProjects = async () => {
  try {
    const res = await getProjects()
    projects.value = res.data || res || []
  } catch (error) {
    console.error('Failed to load projects:', error)
    // Mock data
    projects.value = [
      { id: 1, name: 'SME-PM系统', description: '中小企业工时与项目管理工具', type: 'SCRUM', status: 'active' },
      { id: 2, name: '客户CRM项目', description: '客户关系管理系统', type: 'SCRUM', status: 'active' },
      { id: 3, name: '电商平台', description: 'B2C电商平台', type: 'KANBAN', status: 'planning' },
      { id: 4, name: '物流系统', description: '智能物流调度', type: 'SCRUM', status: 'paused' }
    ]
  }
}

onMounted(() => {
  loadStoredData()
  loadProjects()
})
</script>

<style scoped>
.project-sidebar {
  position: relative;
  background: var(--pm-card);
  border-right: 1px solid var(--pm-border);
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: width var(--pm-transition-normal);
  width: 280px;
}

.project-sidebar.collapsed {
  width: 56px;
}

/* Sidebar Toggle */
.sidebar-toggle {
  position: absolute;
  top: 12px;
  right: -12px;
  width: 24px;
  height: 24px;
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 10;
  transition: all var(--pm-transition-fast);
}

.sidebar-toggle:hover {
  background: var(--pm-background);
  border-color: var(--pm-accent);
}

.sidebar-toggle .el-icon {
  font-size: 14px;
  color: var(--pm-text-secondary);
}

/* Sidebar Content */
.sidebar-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--pm-space-md);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

/* Sidebar Search */
.sidebar-search {
  padding: var(--pm-space-sm) 0;
}

/* Quick Actions */
.sidebar-quick-actions {
  padding: 0;
}

.quick-add-btn {
  width: 100%;
  justify-content: flex-start;
  gap: var(--pm-space-sm);
}

/* Sidebar Section */
.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.sidebar-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-xs) var(--pm-space-sm);
}

.sidebar-section-title {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--pm-text-muted);
}

.sidebar-section-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* Sidebar Project Item */
.sidebar-project-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  padding: var(--pm-space-sm) var(--pm-space-md);
  border-radius: var(--pm-radius-md);
  cursor: pointer;
  transition: all var(--pm-transition-fast);
  position: relative;
}

.sidebar-project-item:hover {
  background: var(--pm-background);
}

.sidebar-project-item.active {
  background: rgba(0, 212, 170, 0.1);
}

.sidebar-project-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: var(--pm-accent);
  border-radius: 0 2px 2px 0;
}

.project-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.project-name {
  flex: 1;
  font-size: 13px;
  color: var(--pm-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-project-item:hover .unfavorite-btn,
.sidebar-project-item:hover .favorite-btn,
.sidebar-project-item:hover .more-btn {
  opacity: 1;
}

.unfavorite-btn,
.favorite-btn,
.more-btn {
  opacity: 0;
  transition: opacity var(--pm-transition-fast);
  padding: 4px !important;
  min-height: auto;
}

.unfavorite-btn .el-icon,
.favorite-btn .el-icon {
  font-size: 12px;
}

/* Sidebar Collapse */
.sidebar-collapse {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  padding: var(--pm-space-md);
  color: var(--pm-text-muted);
  cursor: pointer;
  font-size: 12px;
  margin-top: auto;
  border-top: 1px solid var(--pm-border);
  transition: color var(--pm-transition-fast);
}

.sidebar-collapse:hover {
  color: var(--pm-text-primary);
}

/* Collapsed State */
.sidebar-collapsed-content {
  padding: var(--pm-space-md) var(--pm-space-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--pm-space-sm);
}

.collapsed-item {
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: var(--pm-radius-md);
  cursor: pointer;
  position: relative;
  transition: all var(--pm-transition-fast);
}

.collapsed-item:hover {
  background: var(--pm-background);
}

.collapsed-item.active {
  background: rgba(0, 212, 170, 0.1);
}

.collapsed-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 60%;
  background: var(--pm-accent);
  border-radius: 0 2px 2px 0;
}

.collapsed-item .el-icon {
  font-size: 18px;
  color: var(--pm-text-secondary);
}

.collapsed-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.collapsed-divider {
  width: 24px;
  height: 1px;
  background: var(--pm-border);
  margin: var(--pm-space-xs) 0;
}

/* Scrollbar Styling */
.sidebar-content::-webkit-scrollbar {
  width: 4px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: var(--pm-border);
  border-radius: 2px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: var(--pm-text-muted);
}
</style>
