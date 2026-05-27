package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DataStandardCodeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataStandardCodeItemMapper extends BaseMapper<DataStandardCodeItem> {

    @Select("SELECT * FROM data_standard_code_item WHERE standard_id = #{standardId}")
    DataStandardCodeItem findByStandardId(@Param("standardId") Long standardId);
}
