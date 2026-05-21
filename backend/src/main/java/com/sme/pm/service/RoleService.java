package com.sme.pm.service;

import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getById(Long id);
    Role create(Role role);
    Role update(Role role);
    void delete(Long id);
    List<Permission> getPermissions(Long roleId);
    void assignPermissions(Long roleId, List<Long> permissionIds);
    boolean canDelete(Long id);
}
