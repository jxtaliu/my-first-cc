package com.sme.pm.service;

public interface IRolePermissionService {
    boolean canApproveTimesheet(Long userId, String projectId);
    boolean canManageSprint(Long userId, String projectId);
    boolean canManageTasks(Long userId, String projectId);
    boolean canManageMembers(Long userId, String projectId);
    boolean canConfigureProject(Long userId, String projectId);
    String getProjectRole(Long userId, String projectId);
}
