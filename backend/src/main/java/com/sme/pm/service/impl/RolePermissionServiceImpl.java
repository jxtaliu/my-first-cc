package com.sme.pm.service.impl;

import com.sme.pm.entity.ProjectRole;
import com.sme.pm.mapper.ProjectRoleMapper;
import com.sme.pm.service.IRolePermissionService;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl implements IRolePermissionService {

    private final ProjectRoleMapper projectRoleMapper;

    public RolePermissionServiceImpl(ProjectRoleMapper projectRoleMapper) {
        this.projectRoleMapper = projectRoleMapper;
    }

    @Override
    public boolean canApproveTimesheet(Long userId, String projectId) {
        ProjectRole role = projectRoleMapper.findByProjectAndUser(projectId, userId);
        if (role == null) {
            return false;
        }
        String roleName = role.getRole();
        return "PROJECT_OWNER".equals(roleName) || "PROJECT_MANAGER".equals(roleName);
    }

    @Override
    public boolean canManageSprint(Long userId, String projectId) {
        ProjectRole role = projectRoleMapper.findByProjectAndUser(projectId, userId);
        if (role == null) {
            return false;
        }
        String roleName = role.getRole();
        return "PROJECT_OWNER".equals(roleName) || "PROJECT_MANAGER".equals(roleName);
    }

    @Override
    public boolean canManageTasks(Long userId, String projectId) {
        ProjectRole role = projectRoleMapper.findByProjectAndUser(projectId, userId);
        if (role == null) {
            return false;
        }
        String roleName = role.getRole();
        return "PROJECT_OWNER".equals(roleName) || "PROJECT_MANAGER".equals(roleName) || "DEV_LEAD".equals(roleName);
    }

    @Override
    public boolean canManageMembers(Long userId, String projectId) {
        ProjectRole role = projectRoleMapper.findByProjectAndUser(projectId, userId);
        if (role == null) {
            return false;
        }
        String roleName = role.getRole();
        return "PROJECT_OWNER".equals(roleName) || "PROJECT_MANAGER".equals(roleName);
    }

    @Override
    public boolean canConfigureProject(Long userId, String projectId) {
        ProjectRole role = projectRoleMapper.findByProjectAndUser(projectId, userId);
        if (role == null) {
            return false;
        }
        String roleName = role.getRole();
        return "PROJECT_OWNER".equals(roleName);
    }

    @Override
    public String getProjectRole(Long userId, String projectId) {
        ProjectRole role = projectRoleMapper.findByProjectAndUser(projectId, userId);
        if (role == null) {
            return "NONE";
        }
        return role.getRole();
    }
}
