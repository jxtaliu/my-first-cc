package com.sme.pm.service;

import com.sme.pm.entity.Project;

import java.util.List;
import java.util.Map;

public interface ProjectService {
    Project create(Project project);
    Project update(Project project);
    void delete(Long id);
    Project getById(Long id);
    List<Project> list();
    List<Project> listByStatus(String status);
    void archive(Long id);
    void restore(Long id);
    void addMember(String projectId, Long userId);
    void removeMember(String projectId, Long userId);
    List<Map<String, Object>> getMembers(String projectId);
    Map<String, Object> getStats(String projectId);
}
