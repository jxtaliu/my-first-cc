package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0 ORDER BY created_at DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    int countUnread(@Param("userId") Long userId);

    @Select("SELECT * FROM notification WHERE id = #{id} AND deleted = 0")
    Notification findById(@Param("id") Long id);
}
