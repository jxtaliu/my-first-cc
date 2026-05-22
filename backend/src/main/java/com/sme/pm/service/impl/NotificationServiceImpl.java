package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.Notification;
import com.sme.pm.mapper.NotificationMapper;
import com.sme.pm.service.INotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Override
    public List<Notification> findByUserId(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getDeleted, 0)
               .orderByDesc(Notification::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public List<Notification> findUnreadByUserId(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false)
               .eq(Notification::getDeleted, 0)
               .orderByDesc(Notification::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public int countUnread(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, false)
               .eq(Notification::getDeleted, 0);
        return (int) count(wrapper);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = getById(id);
        if (notification != null) {
            notification.setIsRead(1);
            updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .eq(Notification::getDeleted, 0);
        List<Notification> unread = list(wrapper);
        unread.forEach(n -> n.setIsRead(1));
        updateBatchById(unread);
    }
}
