package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public Project create(Project project) {
        // Check for duplicate project_id
        Project existing = projectMapper.findByProjectId(project.getProjectId());
        if (existing != null) {
            throw new IllegalArgumentException("项目ID已存在: " + project.getProjectId());
        }
        projectMapper.insert(project);
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
    public void addMember(String projectId, Long userId) {
        projectMapper.addMember(projectId, userId);
    }

    @Override
    @Transactional
    public void removeMember(String projectId, Long userId) {
        projectMapper.removeMember(projectId, userId);
    }

    @Override
    public List<Map<String, Object>> getMembers(String projectId) {
        return new ArrayList<>();
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
