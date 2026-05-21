package com.sme.pm.service.impl;

import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;
import com.sme.pm.mapper.PermissionMapper;
import com.sme.pm.mapper.RoleMapper;
import com.sme.pm.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.findAll();
    }

    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public Role create(Role role) {
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role update(Role role) {
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    @Override
    public List<Permission> getPermissions(Long roleId) {
        return permissionMapper.findByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        roleMapper.deleteRolePermissions(roleId);
        for (Long permissionId : permissionIds) {
            roleMapper.insertRolePermission(roleId, permissionId);
        }
    }

    @Override
    public boolean canDelete(Long id) {
        return roleMapper.countUsersByRoleId(id) == 0;
    }
}
