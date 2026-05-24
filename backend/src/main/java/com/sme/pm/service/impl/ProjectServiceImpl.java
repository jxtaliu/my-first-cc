package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.ProjectService;
import com.sme.pm.service.TaskStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final TaskStatusService taskStatusService;

    public ProjectServiceImpl(ProjectMapper projectMapper, TaskStatusService taskStatusService) {
        this.projectMapper = projectMapper;
        this.taskStatusService = taskStatusService;
    }

    @Override
    public Project create(Project project) {
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
        return projectMapper.findById(id);
    }

    @Override
    public List<Project> list() {
        return projectMapper.findAll();
    }

    @Override
    public List<Project> listByStatus(String status) {
        return projectMapper.findByStatus(status);
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
