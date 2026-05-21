package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DictCode;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DictCodeMapper extends BaseMapper<DictCode> {

    @Select("SELECT dc.* FROM sys_dict_code dc " +
            "JOIN sys_dict_type dt ON dc.dict_type_id = dt.id " +
            "WHERE dt.code = #{type} ORDER BY dc.sort_order")
    List<DictCode> findByTypeCode(@Param("type") String type);

    @Select("SELECT dc.* FROM sys_dict_code dc " +
            "JOIN sys_dict_type dt ON dc.dict_type_id = dt.id " +
            "WHERE dt.code = #{type} AND dc.code = #{code}")
    DictCode findByTypeCodeAndCode(@Param("type") String type, @Param("code") String code);
}
