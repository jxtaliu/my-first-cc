package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Notification;
import com.sme.pm.service.INotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public Result<List<Notification>> findByUserId(@PathVariable Long userId) {
        return Result.success(notificationService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public Result<List<Notification>> findUnreadByUserId(@PathVariable Long userId) {
        return Result.success(notificationService.findUnreadByUserId(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public Result<Integer> countUnread(@PathVariable Long userId) {
        return Result.success(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/user/{userId}/read-all")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.removeById(id);
        return Result.success();
    }
}
