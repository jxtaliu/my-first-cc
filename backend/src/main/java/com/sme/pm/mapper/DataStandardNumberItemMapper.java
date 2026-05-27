package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DataStandardNumberItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataStandardNumberItemMapper extends BaseMapper<DataStandardNumberItem> {

    @Select("SELECT * FROM data_standard_number_item WHERE standard_id = #{standardId}")
    DataStandardNumberItem findByStandardId(@Param("standardId") Long standardId);
}
