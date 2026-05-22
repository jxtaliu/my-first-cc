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
    void addMember(String projectId, Long userId, String roleId);
    void removeMember(String projectId, Long userId);
    List<Map<String, Object>> getMembers(String projectId);
    void updateMemberRole(String projectId, Long userId, String roleId);
    Map<String, Object> getStats(String projectId);
}
