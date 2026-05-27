import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue')
      },
      {
        path: 'portfolio-dashboard',
        name: 'PortfolioDashboard',
        component: () => import('@/views/dashboard/PortfolioDashboard.vue')
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/List.vue')
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/Detail.vue')
      },
      {
        path: 'projects/board/:id',
        name: 'ProjectBoard',
        component: () => import('@/views/project/ProjectBoard.vue')
      },
      {
        path: 'projects/dashboard/:id',
        name: 'ProjectDashboard',
        component: () => import('@/views/project/ProjectDashboard.vue')
      },
      {
        path: 'projects/my-board',
        name: 'MyBoard',
        component: () => import('@/views/project/MyBoard.vue')
      },
      {
        path: 'projects/my-tasks',
        name: 'MyTasks',
        component: () => import('@/views/project/MyTasks.vue')
      },
      {
        path: 'projects/gantt/:id?',
        name: 'GanttView',
        component: () => import('@/views/project/GanttView.vue')
      },
      {
        path: 'projects/sprint-board/:id',
        name: 'SprintBoard',
        component: () => import('@/views/project/SprintBoard.vue')
      },
      {
        path: 'projects/kanban/:projectId',
        name: 'TaskBoard',
        component: () => import('@/views/project/TaskBoard.vue')
      },
      {
        path: 'projects/sprint-management/:projectId',
        name: 'SprintManagement',
        component: () => import('@/views/project/SprintManagement.vue')
      },
      {
        path: 'projects/team-board',
        name: 'TeamBoard',
        component: () => import('@/views/project/TeamBoard.vue')
      },
      {
        path: 'projects/backlog/:id?',
        name: 'BacklogBoard',
        component: () => import('@/views/project/BacklogBoard.vue')
      },
      {
        path: 'projects/milestones',
        name: 'Milestones',
        component: () => import('@/views/project/Milestones.vue')
      },
      {
        path: 'projects/stats',
        name: 'ProjectStats',
        component: () => import('@/views/project/StatsDashboard.vue')
      },
      {
        path: 'projects/compare',
        name: 'ProjectCompare',
        component: () => import('@/views/project/ProjectCompare.vue')
      },
      {
        path: 'projects/portfolio',
        name: 'PortfolioBoard',
        component: () => import('@/views/project/PortfolioBoard.vue')
      },
      {
        path: 'requirements',
        name: 'Requirements',
        component: () => import('@/views/requirements/Requirements.vue')
      },
      {
        path: 'projects/:id/milestone/:milestoneId',
        name: 'MilestoneDetail',
        component: () => import('@/views/project/MilestoneDetail.vue')
      },
      {
        path: 'projects/:id/sprints',
        name: 'SprintList',
        component: () => import('@/views/project/SprintList.vue')
      },
      {
        path: 'projects/:id/sprints/:sprintId',
        name: 'SprintDetail',
        component: () => import('@/views/project/SprintBoard.vue')
      },
      {
        path: 'projects/:id/settings/members',
        name: 'MemberRoles',
        component: () => import('@/views/project/Settings/MemberRoles.vue')
      },
      {
        path: 'projects/:id/settings/status',
        name: 'StatusConfig',
        component: () => import('@/views/project/Settings/StatusConfig.vue')
      },
      {
        path: 'projects/:id/settings/sprint',
        name: 'SprintSettings',
        component: () => import('@/views/project/Settings/SprintSettings.vue')
      },
      {
        path: 'projects/settings/templates',
        name: 'ProjectTemplates',
        component: () => import('@/views/project/Settings/ProjectTemplates.vue')
      },
      {
        path: 'projects/settings/notifications',
        name: 'NotificationSettings',
        component: () => import('@/views/project/Settings/NotificationSettings.vue')
      },
      {
        path: 'projects/settings/sprint',
        name: 'GlobalSprintSettings',
        component: () => import('@/views/project/Settings/SprintSettings.vue')
      },
      {
        path: 'timesheet',
        name: 'Timesheet',
        component: () => import('@/views/timesheet/Index.vue')
      },
      {
        path: 'timesheet/my',
        name: 'TimesheetMy',
        component: () => import('@/views/timesheet/Index.vue')
      },
      {
        path: 'timesheet/approval',
        name: 'TimesheetApproval',
        component: () => import('@/views/timesheet/Approval.vue')
      },
      {
        path: 'notification',
        name: 'NotificationCenter',
        component: () => import('@/views/notification/NotificationCenter.vue')
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue')
      },
      {
        path: 'admin/roles',
        name: 'AdminRoles',
        component: () => import('@/views/admin/Roles.vue')
      },
      {
        path: 'admin/departments',
        name: 'AdminDepartments',
        component: () => import('@/views/admin/Departments.vue')
      },
      {
        path: 'admin/dicts',
        name: 'AdminDicts',
        component: () => import('@/views/admin/Dict.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth !== false && !authStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
