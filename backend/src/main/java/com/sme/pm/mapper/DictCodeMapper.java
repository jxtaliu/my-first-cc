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

    @Select("SELECT * FROM sys_dict_code WHERE dict_type_id = #{dictTypeId} ORDER BY sort_order")
    List<DictCode> findByDictTypeId(@Param("dictTypeId") Long dictTypeId);

    @Insert("INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) " +
            "VALUES (#{dictTypeId}, #{code}, #{name}, #{nameEn}, #{nameZh}, #{sortOrder}, #{extra})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDictCode(DictCode dictCode);

    @Update("<script>" +
            "UPDATE sys_dict_code SET code = #{code}, name = #{name}, name_en = #{nameEn}, " +
            "name_zh = #{nameZh}, sort_order = #{sortOrder}, extra = #{extra} " +
            "WHERE id = #{id}" +
            "</script>")
    int updateDictCode(DictCode dictCode);

    @Delete("DELETE FROM sys_dict_code WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_dict_code WHERE dict_type_id = #{dictTypeId} AND code = #{code} AND id != #{excludeId}")
    int countByTypeAndCode(@Param("dictTypeId") Long dictTypeId, @Param("code") String code, @Param("excludeId") Long excludeId);
}
