package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.ProjectRole;
import com.sme.pm.mapper.ProjectRoleMapper;
import com.sme.pm.service.IProjectRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectRoleServiceImpl extends ServiceImpl<ProjectRoleMapper, ProjectRole> implements IProjectRoleService {

    @Override
    public List<ProjectRole> findByProjectId(String projectId) {
        LambdaQueryWrapper<ProjectRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectRole::getProjectId, projectId)
               .eq(ProjectRole::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public List<ProjectRole> findByUserId(Long userId) {
        LambdaQueryWrapper<ProjectRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectRole::getUserId, userId)
               .eq(ProjectRole::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public ProjectRole findByProjectAndUser(String projectId, Long userId) {
        LambdaQueryWrapper<ProjectRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectRole::getProjectId, projectId)
               .eq(ProjectRole::getUserId, userId)
               .eq(ProjectRole::getDeleted, 0);
        return getOne(wrapper);
    }

    @Override
    public List<ProjectRole> findByProjectAndRole(String projectId, String role) {
        LambdaQueryWrapper<ProjectRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectRole::getProjectId, projectId)
               .eq(ProjectRole::getRole, role)
               .eq(ProjectRole::getDeleted, 0);
        return list(wrapper);
    }

    @Override
    public void assignRole(String projectId, Long userId, String role) {
        ProjectRole existing = findByProjectAndUser(projectId, userId);
        if (existing != null) {
            existing.setRole(role);
            updateById(existing);
        } else {
            ProjectRole newRole = new ProjectRole();
            newRole.setProjectId(projectId);
            newRole.setUserId(userId);
            newRole.setRole(role);
            save(newRole);
        }
    }

    @Override
    public void removeRole(String projectId, Long userId) {
        LambdaQueryWrapper<ProjectRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectRole::getProjectId, projectId)
               .eq(ProjectRole::getUserId, userId);
        remove(wrapper);
    }
}
