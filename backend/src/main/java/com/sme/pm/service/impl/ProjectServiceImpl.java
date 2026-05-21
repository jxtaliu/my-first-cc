package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public Project create(Project project) {
        projectMapper.insert(project);
        return project;
    }

    @Override
    public Project update(Project project) {
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
    @Transactional
    public void archive(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(3);  // archived
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(2);  // active
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void addMember(Long projectId, Long userId) {
        projectMapper.addMember(projectId, userId);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        projectMapper.removeMember(projectId, userId);
    }
}
