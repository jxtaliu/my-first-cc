<template>
  <div class="requirements-page">
    <!-- 顶部工具栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">{{ $t('nav.requirements') }}</h1>
      </div>
      <div class="header-right">
        <el-select v-model="selectedProjectId" :placeholder="$t('requirements.selectProject')" clearable class="project-select">
          <el-option v-for="p in projects" :key="p.projectId" :label="p.name" :value="p.projectId" />
        </el-select>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          {{ $t('common.add') }}
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧：需求树 -->
      <div class="tree-panel">
        <div class="tree-header">
          <span class="tree-title">{{ $t('requirements.requirementTree') }}</span>
          <el-button text size="small" @click="expandAll">
            {{ expanded ? $t('requirements.collapseAll') : $t('requirements.expandAll') }}
          </el-button>
        </div>
        <div class="tree-content" v-loading="loading">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :expand-on-click-node="false"
            :default-expanded-keys="defaultExpandedKeys"
            @node-click="handleNodeClick"
            @node-expand="handleNodeExpand"
            @node-collapse="handleNodeCollapse"
          >
            <template #default="{ node, data }">
              <div class="tree-node" :class="`type-${data.type?.toLowerCase()}`">
                <span class="node-icon">
                  <el-icon v-if="data.type === 'EPIC'"><Folder /></el-icon>
                  <el-icon v-else-if="data.type === 'FEATURE'"><FolderOpened /></el-icon>
                  <el-icon v-else><Document /></el-icon>
                </span>
                <span class="node-label">{{ data.title }}</span>
                <span class="node-type-badge" :style="{ backgroundColor: getTypeColor(data.type) }">
                  {{ $t(`requirements.type_${data.type?.toLowerCase()}`) }}
                </span>
              </div>
            </template>
          </el-tree>
          <el-empty v-if="!loading && treeData.length === 0" :description="$t('common.noData')" />
        </div>

        <!-- Bug 快捷入口 -->
        <div class="bug-section">
          <div class="section-header" @click="showBugs = !showBugs">
            <el-icon><Warning /></el-icon>
            <span>{{ $t('requirements.bugs') }}</span>
            <el-icon class="arrow"><ArrowRight v-if="!showBugs" /><ArrowDown v-else /></el-icon>
          </div>
          <div v-show="showBugs" class="bug-list">
            <div v-for="bug in bugs" :key="bug.id" class="bug-item" @click="handleBugClick(bug)">
              <span class="bug-title">{{ bug.title }}</span>
              <el-tag size="small" :color="getBugStatusColor(bug.bugStatusId)">
                {{ getBugStatusName(bug.bugStatusId) }}
              </el-tag>
            </div>
            <el-empty v-if="bugs.length === 0" :description="$t('requirements.noBugs')" size="small" />
          </div>
        </div>
      </div>

      <!-- 右侧：详情/子项 -->
      <div class="detail-panel">
        <div v-if="!selectedItem" class="detail-empty">
          <el-empty :description="$t('requirements.selectToViewDetail')" />
        </div>
        <div v-else class="detail-content">
          <div class="detail-header">
            <div class="detail-title">
              <h2>{{ selectedItem.title }}</h2>
              <el-tag :color="getTypeColor(selectedItem.type)">
                {{ $t(`requirements.type_${selectedItem.type?.toLowerCase()}`) }}
              </el-tag>
            </div>
            <div class="detail-actions">
              <el-button v-if="selectedItem.type !== 'BUG'" size="small" @click="handleEdit">{{ $t('common.edit') }}</el-button>
              <el-button size="small" type="danger" @click="handleDelete">{{ $t('common.delete') }}</el-button>
            </div>
          </div>

          <el-descriptions :column="2" border class="detail-descriptions">
            <el-descriptions-item :label="$t('requirements.priority')">
              <el-tag :type="getPriorityType(selectedItem.priority)">{{ selectedItem.priority }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('requirements.estimateHours')">
              {{ selectedItem.estimateHours || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('requirements.dueDate')">
              {{ selectedItem.dueDate || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('requirements.assignee')">
              {{ getMemberName(selectedItem.assigneeId) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedItem.type === 'BUG'" :label="$t('requirements.status')">
              <el-tag :color="getBugStatusColor(selectedItem.bugStatusId)">
                {{ getBugStatusName(selectedItem.bugStatusId) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-description">
            <h4>{{ $t('requirements.description') }}</h4>
            <p>{{ selectedItem.description || '-' }}</p>
          </div>

          <!-- 子项列表（如果是 Epic/Feature/Story） -->
          <div v-if="['EPIC', 'FEATURE', 'STORY'].includes(selectedItem.type)" class="children-section">
            <div class="section-header">
              <h4>{{ $t('requirements.childItems') }}</h4>
              <el-button size="small" type="primary" @click="handleCreateChild">
                <el-icon><Plus /></el-icon>
                {{ $t('requirements.addChild') }}
              </el-button>
            </div>
            <el-table :data="childrenItems" border size="small">
              <el-table-column prop="title" :label="$t('requirements.title')" />
              <el-table-column :label="$t('requirements.type')" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :color="getTypeColor(row.type)">
                    {{ $t(`requirements.type_${row.type?.toLowerCase()}`) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="$t('requirements.priority')" width="80">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.priority }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="$t('requirements.status')" width="100">
                <template #default="{ row }">
                  <span>{{ getStatusName(row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('requirements.assignee')" width="120">
                <template #default="{ row }">
                  {{ getMemberName(row.assigneeId) }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('common.actions')" width="100">
                <template #default="{ row }">
                  <el-button link size="small" type="primary" @click="handleNodeClick(row)">
                    {{ $t('common.view') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="childrenItems.length === 0" :description="$t('common.noData')" size="small" />
          </div>
        </div>
      </div>
    </div>

    <!-- 创建工作项弹窗 -->
    <CreateRequirementDialog
      ref="createDialogRef"
      v-model="showCreateDialog"
      :project-id="selectedProjectId"
      :parent-item="parentForCreate"
      @success="onCreateSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Folder, FolderOpened, Document, Warning, ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { getRequirementTree, getRequirementChildren, getRequirement, getBugs, getBugStatuses, deleteRequirement } from '@/api/requirements'
import { getBugStatusesByProject } from '@/api/bugStatus'
import { getTaskStatusesByProject } from '@/api/taskStatus'
import { getProjects, getProjectMembers } from '@/api/project'
import CreateRequirementDialog from './CreateRequirementDialog.vue'

const { t, locale } = useI18n()

const loading = ref(false)
const projects = ref([])
const projectMembers = ref([])
const selectedProjectId = ref(null)
const treeData = ref([])
const expandedNodeIds = ref(new Set())
const showBugs = ref(false)
const bugs = ref([])
const bugStatuses = ref([])
const taskStatuses = ref([])

// 选中的项
const selectedItem = ref(null)
const childrenItems = ref([])
const treeRef = ref()

const showCreateDialog = ref(false)
const parentForCreate = ref(null)
const createDialogRef = ref()
const isEditMode = ref(false)

// Tree 配置
const treeProps = {
  children: 'children',
  label: 'title'
}

const defaultExpandedKeys = computed(() => Array.from(expandedNodeIds.value))

// 加载项目列表
const loadProjects = async () => {
  try {
    const res = await getProjects()
    projects.value = res.data || []
  } catch (e) {
    console.error('Failed to load projects:', e)
  }
}

// 加载需求树
const loadRequirementTree = async () => {
  if (!selectedProjectId.value) {
    treeData.value = []
    return
  }

  loading.value = true
  try {
    const res = await getRequirementTree(selectedProjectId.value)
    treeData.value = res.data || []
  } catch (e) {
    ElMessage.error(t('common.failed'))
  } finally {
    loading.value = false
  }
}

// 加载 Bug 列表
const loadBugs = async () => {
  if (!selectedProjectId.value) return

  try {
    const res = await getBugs(selectedProjectId.value)
    bugs.value = res.data || []
  } catch (e) {
    console.error('Failed to load bugs:', e)
  }
}

// 加载 Bug 状态
const loadBugStatuses = async () => {
  if (!selectedProjectId.value) return

  try {
    const res = await getBugStatuses(selectedProjectId.value)
    bugStatuses.value = res.data || []
  } catch (e) {
    console.error('Failed to load bug statuses:', e)
  }
}

// 加载项目成员
const loadProjectMembers = async () => {
  if (!selectedProjectId.value) return

  try {
    console.log('[DEBUG] loadProjectMembers called with projectId:', selectedProjectId.value)
    const res = await getProjectMembers(selectedProjectId.value)
    console.log('[DEBUG] loadProjectMembers response:', res)
    projectMembers.value = res.data || []
    console.log('[DEBUG] projectMembers.value after loading:', projectMembers.value)
  } catch (e) {
    console.error('Failed to load project members:', e)
    projectMembers.value = []
  }
}

// 获取成员名称
const getMemberName = (memberId) => {
  if (!memberId || !projectMembers.value.length) return '-'
  const member = projectMembers.value.find(m => String(m.userId) === String(memberId))
  // MyBatis maps username to 'username', real_name to 'realName'
  return member?.realName || member?.username || memberId
}

// 加载任务状态
const loadTaskStatuses = async () => {
  if (!selectedProjectId.value) return

  try {
    const res = await getTaskStatusesByProject(selectedProjectId.value)
    taskStatuses.value = res.data || []
  } catch (e) {
    console.error('Failed to load task statuses:', e)
    taskStatuses.value = []
  }
}

// 获取状态名称
const getStatusName = (statusCode) => {
  if (!statusCode || !taskStatuses.value.length) return statusCode || '-'
  // task.status 直接存储 task_status.code
  const status = taskStatuses.value.find(s => s.code === statusCode)
  return status ? (locale.value === 'zh-CN' ? status.nameZh : status.nameEn) : (statusCode || '-')
}

// 点击树节点
const handleNodeClick = (data) => {
  selectedItem.value = data
  if (['EPIC', 'FEATURE', 'STORY'].includes(data.type)) {
    // 直接使用树中已有的 children 数据
    childrenItems.value = data.children || []
  } else {
    childrenItems.value = []
  }
}

// 展开节点
const handleNodeExpand = (data) => {
  expandedNodeIds.value.add(data.id)
}

// 折叠节点
const handleNodeCollapse = (data) => {
  expandedNodeIds.value.delete(data.id)
}

// 展开/折叠全部
const expandAll = () => {
  expanded.value = !expanded.value
  if (!treeRef.value || !treeData.value.length) return

  const traverseAndExpand = (nodes, isExpand) => {
    nodes.forEach(node => {
      const treeNode = treeRef.value.getNode(node.id)
      if (treeNode) {
        if (isExpand) {
          treeNode.expand()
        } else {
          treeNode.collapse()
        }
      }
      if (node.children && node.children.length > 0) {
        traverseAndExpand(node.children, isExpand)
      }
    })
  }

  traverseAndExpand(treeData.value, expanded.value)
}

const expanded = ref(false)

// Bug 点击
const handleBugClick = (bug) => {
  selectedItem.value = bug
  childrenItems.value = []
}

// 创建
const handleCreate = () => {
  isEditMode.value = false
  parentForCreate.value = null
  showCreateDialog.value = true
}

// 创建子项
const handleCreateChild = () => {
  isEditMode.value = false
  parentForCreate.value = selectedItem.value
  showCreateDialog.value = true
}

// 编辑
const handleEdit = () => {
  if (!selectedItem.value) return
  isEditMode.value = true
  parentForCreate.value = null
  createDialogRef.value?.setEditData(selectedItem.value)
  showCreateDialog.value = true
}

// 删除
const handleDelete = async () => {
  if (!selectedItem.value) return

  try {
    await ElMessageBox.confirm(
      t('requirements.confirmDelete', { title: selectedItem.value.title }),
      t('common.warning'),
      { type: 'warning' }
    )
    await deleteRequirement(selectedItem.value.id)
    ElMessage.success(t('common.success'))
    selectedItem.value = null
    loadRequirementTree()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('common.failed'))
    }
  }
}

// 创建成功回调
const onCreateSuccess = () => {
  const previousId = selectedItem.value?.id
  loadRequirementTree().then(() => {
    if (previousId) {
      // 从新树中找到更新后的节点
      const updatedNode = findNodeById(treeData.value, previousId)
      if (updatedNode) {
        selectedItem.value = updatedNode
        childrenItems.value = updatedNode.children || []
      }
    }
  })
}

// 根据ID在树中查找节点
const findNodeById = (nodes, id) => {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children?.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 工具函数
const getTypeColor = (type) => {
  const colors = {
    EPIC: '#8B5CF6',
    FEATURE: '#3B82F6',
    STORY: '#10B981',
    TASK: '#94A3B8',
    BUG: '#EF4444',
    SUBTASK: '#64748B'
  }
  return colors[type] || '#94A3B8'
}

const getPriorityType = (priority) => {
  const types = { P0: 'danger', P1: 'danger', P2: 'warning', P3: 'info' }
  return types[priority] || 'info'
}

const getBugStatusName = (bugStatusId) => {
  if (!bugStatusId || !bugStatuses.value.length) return '-'
  const status = bugStatuses.value.find(s => s.id === bugStatusId)
  return locale.value === 'zh-CN' ? status?.nameZh : status?.nameEn
}

const getBugStatusColor = (bugStatusId) => {
  if (!bugStatusId || !bugStatuses.value.length) return '#94A3B8'
  const status = bugStatuses.value.find(s => s.id === bugStatusId)
  return status?.color || '#94A3B8'
}

// 监听项目变化
watch(selectedProjectId, (newVal) => {
  selectedItem.value = null
  childrenItems.value = []
  if (newVal) {
    loadRequirementTree()
    loadBugs()
    loadBugStatuses()
    loadProjectMembers()
    loadTaskStatuses()
  } else {
    treeData.value = []
    bugs.value = []
    bugStatuses.value = []
    projectMembers.value = []
    taskStatuses.value = []
  }
})

onMounted(() => {
  loadProjects()
})
</script>

<script>
import { useI18n } from 'vue-i18n'
export default {
  name: 'Requirements'
}
</script>

<style scoped>
.requirements-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: var(--pm-background, #F8FAFC);
}

/* 顶部工具栏 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pm-text-primary, #1E293B);
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.project-select {
  width: 200px;
}

/* 主内容区 */
.main-content {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

/* 左侧面板 */
.tree-panel {
  width: 340px;
  background: var(--pm-card, #FFFFFF);
  border: 1px solid var(--pm-border, #E2E8F0);
  border-radius: var(--pm-radius-lg, 12px);
  box-shadow: var(--pm-shadow-card, 0 2px 8px rgba(0,0,0,0.06));
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--pm-border, #E2E8F0);
  background: var(--pm-background, #F8FAFC);
}

.tree-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--pm-text-secondary, #64748B);
}

.tree-header .el-button--text {
  color: var(--pm-primary, #1E3A5F);
  font-weight: 500;
}

.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* 树节点样式 */
:deep(.el-tree-node__content) {
  height: 36px;
  border-radius: var(--pm-radius-sm, 6px);
  margin-bottom: 2px;
  transition: background 0.15s ease;
}

:deep(.el-tree-node__content:hover) {
  background: var(--pm-background, #F8FAFC);
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(30, 58, 95, 0.08);
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.node-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: var(--pm-radius-sm, 6px);
  flex-shrink: 0;
}

.type-epic .node-icon { background: rgba(139, 92, 246, 0.15); color: #8B5CF6; }
.type-feature .node-icon { background: rgba(59, 130, 246, 0.15); color: #3B82F6; }
.type-story .node-icon { background: rgba(16, 185, 129, 0.15); color: #10B981; }
.type-task .node-icon { background: rgba(100, 116, 139, 0.15); color: #64748B; }
.type-subtask .node-icon { background: rgba(71, 85, 105, 0.15); color: #475569; }

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--pm-text-primary, #1E293B);
}

.node-type-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: var(--pm-radius-xs, 4px);
  color: white;
  font-weight: 500;
  flex-shrink: 0;
}

/* Bug 区域 */
.bug-section {
  border-top: 1px solid var(--pm-border, #E2E8F0);
  background: #FEF2F2;
}

.bug-section .section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  font-weight: 500;
  font-size: 13px;
  color: #DC2626;
}

.bug-section .section-header:hover {
  background: rgba(220, 38, 38, 0.05);
}

.section-header .arrow {
  margin-left: auto;
  color: var(--pm-text-muted, #94A3B8);
}

.bug-list {
  padding: 0 12px 12px;
}

.bug-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: var(--pm-radius-sm, 6px);
  cursor: pointer;
  margin-bottom: 4px;
  background: white;
  border: 1px solid #FEE2E2;
  transition: all 0.15s ease;
}

.bug-item:hover {
  background: #FEF2F2;
  border-color: #FECACA;
}

.bug-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: var(--pm-text-primary, #1E293B);
}

/* 右侧详情面板 */
.detail-panel {
  flex: 1;
  background: var(--pm-card, #FFFFFF);
  border: 1px solid var(--pm-border, #E2E8F0);
  border-radius: var(--pm-radius-lg, 12px);
  box-shadow: var(--pm-shadow-card, 0 2px 8px rgba(0,0,0,0.06));
  overflow-y: auto;
}

.detail-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-content {
  padding: 20px 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--pm-border, #E2E8F0);
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--pm-text-primary, #1E293B);
}

.detail-title .el-tag {
  border-radius: var(--pm-radius-sm, 6px);
  padding: 4px 10px;
  font-weight: 500;
  border: none;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.detail-actions .el-button {
  border-radius: var(--pm-radius-md, 8px);
  font-weight: 500;
}

.detail-actions .el-button--small {
  padding: 6px 12px;
}

/* 描述信息 */
.detail-descriptions {
  margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
  background: var(--pm-background, #F8FAFC);
  color: var(--pm-text-secondary, #64748B);
  font-weight: 500;
}

:deep(.el-descriptions__cell) {
  border-color: var(--pm-border, #E2E8F0);
  padding: 10px 12px;
}

:deep(.el-descriptions__content) {
  color: var(--pm-text-primary, #1E293B);
}

.detail-description {
  margin-bottom: 20px;
  padding: 14px 16px;
  background: var(--pm-background, #F8FAFC);
  border-radius: var(--pm-radius-md, 8px);
  border: 1px solid var(--pm-border, #E2E8F0);
}

.detail-description h4 {
  margin: 0 0 8px 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--pm-text-secondary, #64748B);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-description p {
  margin: 0;
  color: var(--pm-text-primary, #1E293B);
  line-height: 1.6;
  font-size: 13px;
}

/* 子项区域 */
.children-section {
  margin-top: 24px;
  padding: 16px;
  background: var(--pm-background, #F8FAFC);
  border-radius: var(--pm-radius-md, 8px);
  border: 1px solid var(--pm-border, #E2E8F0);
}

.children-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 12px 0;
  margin-bottom: 12px;
  border-bottom: 1px solid var(--pm-border, #E2E8F0);
}

.children-section .section-header h4 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--pm-text-secondary, #64748B);
}

:deep(.children-section .el-table) {
  border-radius: var(--pm-radius-sm, 6px);
  overflow: hidden;
}

:deep(.children-section .el-table th) {
  background: var(--pm-background, #F8FAFC);
  color: var(--pm-text-secondary, #64748B);
  font-weight: 600;
  font-size: 12px;
  border: none;
  padding: 10px 8px;
}

:deep(.children-section .el-table td) {
  border-color: var(--pm-border, #E2E8F0);
  padding: 10px 8px;
  font-size: 13px;
}

:deep(.children-section .el-table tr:hover > td) {
  background: var(--pm-background, #F8FAFC);
}

/* 类型颜色 */
.type-epic { color: #8B5CF6; }
.type-feature { color: #3B82F6; }
.type-story { color: #10B981; }
.type-task { color: #64748B; }
.type-bug { color: #EF4444; }
.type-subtask { color: #475569; }
</style>
