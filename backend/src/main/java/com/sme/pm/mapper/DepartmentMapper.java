package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Department;
import com.sme.pm.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT * FROM sys_department WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort_order")
    List<Department> findRootDepartments();

    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order")
    List<Department> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM sys_user WHERE department_id = #{departmentId} AND deleted = 0")
    List<User> findUsersByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("SELECT * FROM sys_user WHERE department_id IS NULL AND deleted = 0")
    List<User> findUsersWithoutDepartment();

    @Select("SELECT COUNT(*) FROM sys_user WHERE department_id = #{departmentId} AND deleted = 0")
    int countUsersByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("SELECT COUNT(*) FROM sys_department WHERE parent_id = #{departmentId} AND deleted = 0")
    int countChildrenByDepartmentId(@Param("departmentId") Long departmentId);

    @Delete("DELETE FROM sys_department WHERE id = #{id}")
    void physicalDeleteById(@Param("id") Long id);
}