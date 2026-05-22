package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.ProjectRole;

import java.util.List;

public interface IProjectRoleService extends IService<ProjectRole> {
    List<ProjectRole> findByProjectId(Long projectId);
    List<ProjectRole> findByUserId(Long userId);
    ProjectRole findByProjectAndUser(Long projectId, Long userId);
    List<ProjectRole> findByProjectAndRole(Long projectId, String role);
    void assignRole(Long projectId, Long userId, String role);
    void removeRole(Long projectId, Long userId);
}
