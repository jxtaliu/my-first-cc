package com.sme.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sme.pm.entity.Notification;

import java.util.List;

public interface INotificationService extends IService<Notification> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findUnreadByUserId(Long userId);
    int countUnread(Long userId);
    void markAsRead(Long id);
    void markAllAsRead(Long userId);
}
