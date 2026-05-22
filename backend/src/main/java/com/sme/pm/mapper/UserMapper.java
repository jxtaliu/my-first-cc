package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.User;
import com.sme.pm.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteUserRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    List<User> findUsersByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    void physicalDeleteById(@Param("id") Long id);

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(user_id, 5) AS UNSIGNED)), 0) FROM sys_user WHERE user_id LIKE 'USR_%'")
    Long getMaxUserIdNumber();
}
