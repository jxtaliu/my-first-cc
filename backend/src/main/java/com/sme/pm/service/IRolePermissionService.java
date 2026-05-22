package com.sme.pm.service;

public interface IRolePermissionService {
    boolean canApproveTimesheet(Long userId, Long projectId);
    boolean canManageSprint(Long userId, Long projectId);
    boolean canManageTasks(Long userId, Long projectId);
    boolean canManageMembers(Long userId, Long projectId);
    boolean canConfigureProject(Long userId, Long projectId);
    String getProjectRole(Long userId, Long projectId);
}
