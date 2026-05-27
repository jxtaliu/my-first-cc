package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DataStandardStringItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataStandardStringItemMapper extends BaseMapper<DataStandardStringItem> {

    @Select("SELECT * FROM data_standard_string_item WHERE standard_id = #{standardId}")
    DataStandardStringItem findByStandardId(@Param("standardId") Long standardId);
}
