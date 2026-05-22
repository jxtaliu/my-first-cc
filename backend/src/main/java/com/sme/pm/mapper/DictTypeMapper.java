package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.DictType;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {

    @Update("<script>" +
            "UPDATE sys_dict_type SET code = COALESCE(#{code}, code), name = #{name}, description = #{description} " +
            "WHERE id = #{id}" +
            "</script>")
    int updateDictType(DictType dictType);

    @Delete("DELETE FROM sys_dict_type WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_dict_code WHERE dict_type_id = #{dictTypeId}")
    int countItemsByTypeId(Long dictTypeId);
}
