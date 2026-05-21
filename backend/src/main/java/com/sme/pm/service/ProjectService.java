package com.sme.pm.service;

import com.sme.pm.entity.Project;

import java.util.List;

public interface ProjectService {
    Project create(Project project);
    Project update(Project project);
    void delete(Long id);
    Project getById(Long id);
    List<Project> list();
    void archive(Long id);
    void restore(Long id);
    void addMember(Long projectId, Long userId);
    void removeMember(Long projectId, Long userId);
}
