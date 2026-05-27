package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DataStandardEnumItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataStandardEnumItemMapper extends BaseMapper<DataStandardEnumItem> {

    @Select("SELECT * FROM data_standard_enum_item WHERE standard_id = #{standardId} ORDER BY sort_order ASC")
    List<DataStandardEnumItem> findByStandardId(@Param("standardId") Long standardId);

    @Select("SELECT COUNT(*) FROM data_standard_enum_item WHERE standard_id = #{standardId}")
    int countByStandardId(@Param("standardId") Long standardId);
}
