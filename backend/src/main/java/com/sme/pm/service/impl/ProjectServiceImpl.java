package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.ProjectService;
import com.sme.pm.service.ITaskStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;
    private final ITaskStatusService taskStatusService;

    public ProjectServiceImpl(ProjectMapper projectMapper, TaskMapper taskMapper, ITaskStatusService taskStatusService) {
        this.projectMapper = projectMapper;
        this.taskMapper = taskMapper;
        this.taskStatusService = taskStatusService;
    }

    @Override
    @Transactional
    public Project create(Project project) {
        // Validate required fields
        if (project.getProjectId() == null || project.getProjectId().isEmpty()) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (project.getName() == null || project.getName().isEmpty()) {
            throw new IllegalArgumentException("name不能为空");
        }
        if (project.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId不能为空");
        }

        // Check for duplicate project_id
        Project existing = projectMapper.findByProjectId(project.getProjectId());
        if (existing != null) {
            throw new IllegalArgumentException("项目ID已存在: " + project.getProjectId());
        }
        projectMapper.insert(project);
        // Initialize task statuses
        taskStatusService.initializeFromDict(project.getProjectId());
        return project;
    }

    @Override
    public Project update(Project project) {
        // Preserve existing projectId if not provided
        if (project.getProjectId() == null || project.getProjectId().isEmpty()) {
            Project existing = projectMapper.findById(project.getId());
            if (existing != null) {
                project.setProjectId(existing.getProjectId());
            }
        }
        projectMapper.updateById(project);
        return project;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        projectMapper.deleteById(id);
    }

    @Override
    public Project getById(Long id) {
        Project project = projectMapper.findById(id);
        if (project != null) {
            populateProjectStats(project);
        }
        return project;
    }

    @Override
    public List<Project> list() {
        List<Project> projects = projectMapper.findAll();
        for (Project project : projects) {
            populateProjectStats(project);
        }
        return projects;
    }

    @Override
    public List<Project> listByStatus(String status) {
        List<Project> projects = projectMapper.findByStatus(status);
        for (Project project : projects) {
            populateProjectStats(project);
        }
        return projects;
    }

    private void populateProjectStats(Project project) {
        project.setMemberCount(projectMapper.findMemberIds(project.getProjectId()).size());
        project.setTaskCount(taskMapper.countByProjectId(project.getProjectId()));
        // Assuming status = 3 means completed (TODO: verify from task_status table)
        project.setCompletedTaskCount(taskMapper.countByProjectIdAndStatus(project.getProjectId(), 3));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setStatus("ARCHIVED");
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setStatus("ACTIVE");
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void addMember(String projectId, Long userId, String roleId) {
        // Check if member already exists
        List<Long> existingMembers = projectMapper.findMemberIds(projectId);
        if (existingMembers.contains(userId)) {
            throw new IllegalArgumentException("用户已是项目成员");
        }
        projectMapper.addMember(projectId, userId, roleId);
    }

    @Override
    @Transactional
    public void removeMember(String projectId, Long userId) {
        projectMapper.removeMember(projectId, userId);
    }

    @Override
    public List<Map<String, Object>> getMembers(String projectId) {
        return projectMapper.findMembersByProjectId(projectId);
    }

    @Override
    @Transactional
    public void updateMemberRole(String projectId, Long userId, String roleId) {
        projectMapper.updateMemberRole(projectId, userId, roleId);
    }

    @Override
    public Map<String, Object> getStats(String projectId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", 0);
        stats.put("completedTasks", 0);
        stats.put("totalHours", 0);
        return stats;
    }
}
