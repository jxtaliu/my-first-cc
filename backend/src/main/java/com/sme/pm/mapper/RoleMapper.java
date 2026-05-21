package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT * FROM sys_role WHERE deleted = 0")
    List<Role> findAll();
}
