package com.sme.pm.service.impl;

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
        return baseMapper.findByUserId(userId);
    }

    @Override
    public List<Notification> findUnreadByUserId(Long userId) {
        return baseMapper.findUnreadByUserId(userId);
    }

    @Override
    public int countUnread(Long userId) {
        return baseMapper.countUnread(userId);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = baseMapper.findById(id);
        if (notification != null) {
            notification.setIsRead(1);
            baseMapper.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unread = baseMapper.findUnreadByUserId(userId);
        unread.forEach(n -> {
            n.setIsRead(1);
            baseMapper.updateById(n);
        });
    }
}
