package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DataStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataStandardMapper extends BaseMapper<DataStandard> {

    @Select("SELECT * FROM data_standard WHERE deleted = 0 ORDER BY created_at DESC")
    List<DataStandard> findAll();

    @Select("SELECT * FROM data_standard WHERE id = #{id} AND deleted = 0")
    DataStandard findById(@Param("id") Long id);

    @Select("SELECT * FROM data_standard WHERE type = #{type} AND deleted = 0 ORDER BY created_at DESC")
    List<DataStandard> findByType(@Param("type") String type);

    @Select("SELECT * FROM data_standard WHERE code = #{code} AND deleted = 0")
    DataStandard findByCode(@Param("code") String code);
}
