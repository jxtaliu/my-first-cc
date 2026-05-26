import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProjects } from '@/api/project'

export const useProjectStore = defineStore('project', () => {
  // 当前选中的项目ID
  const currentProjectId = ref(localStorage.getItem('currentProjectId') || null)
  // 项目列表
  const projects = ref([])
  // 加载状态
  const loading = ref(false)

  // 当前选中的项目对象
  const currentProject = computed(() => {
    return projects.value.find(p => p.projectId === currentProjectId.value) || null
  })

  // 设置当前项目
  function setCurrentProject(projectId) {
    currentProjectId.value = projectId
    if (projectId) {
      localStorage.setItem('currentProjectId', projectId)
    } else {
      localStorage.removeItem('currentProjectId')
    }
  }

  // 加载项目列表
  async function loadProjects() {
    loading.value = true
    try {
      const res = await getProjects()
      projects.value = res.data || []
      // 如果没有选中项目且有项目列表，默认选中第一个
      if (!currentProjectId.value && projects.value.length > 0) {
        setCurrentProject(projects.value[0].projectId)
      }
    } catch (e) {
      console.error('Failed to load projects:', e)
    } finally {
      loading.value = false
    }
  }

  return {
    currentProjectId,
    currentProject,
    projects,
    loading,
    setCurrentProject,
    loadProjects
  }
})
