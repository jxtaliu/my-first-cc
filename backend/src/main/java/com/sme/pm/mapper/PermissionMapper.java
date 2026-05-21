package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY module, name")
    List<Permission> findAll();

    @Select("SELECT module, GROUP_CONCAT(id) as permission_ids, GROUP_CONCAT(name) as permission_names " +
            "FROM sys_permission WHERE deleted = 0 GROUP BY module")
    List<Map<String, Object>> findGroupedByModule();

    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<Permission> findByRoleId(Long roleId);
}