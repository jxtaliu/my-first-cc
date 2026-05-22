package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.ProjectRole;

import java.util.List;

public interface IProjectRoleService extends IService<ProjectRole> {
    List<ProjectRole> findByProjectId(String projectId);
    List<ProjectRole> findByUserId(Long userId);
    ProjectRole findByProjectAndUser(String projectId, Long userId);
    List<ProjectRole> findByProjectAndRole(String projectId, String role);
    void assignRole(String projectId, Long userId, String role);
    void removeRole(String projectId, Long userId);
}
