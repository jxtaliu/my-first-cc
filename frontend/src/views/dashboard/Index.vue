<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>Projects</template>
          <div class="stat-value">{{ stats.projects }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>Tasks</template>
          <div class="stat-value">{{ stats.tasks }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>Hours This Week</template>
          <div class="stat-value">{{ stats.hours }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref({
  projects: 0,
  tasks: 0,
  hours: 0
})

onMounted(async () => {
  try {
    const res = await request.get('/projects')
    stats.value.projects = res.data?.length || 0
  } catch (e) {
    // Handle error
  }
})
</script>

<style scoped>
.stat-value {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  padding: 20px 0;
}
</style>
