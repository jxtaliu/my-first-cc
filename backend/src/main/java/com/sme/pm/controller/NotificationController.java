package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Notification;
import com.sme.pm.service.INotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 * 提供通知管理相关API，包括通知查询、已读标记、删除等
 *
 * 用法：
 * - GET /api/v1/notifications/user/{userId} - 获取用户的所有通知
 * - GET /api/v1/notifications/user/{userId}/unread - 获取用户的未读通知
 * - GET /api/v1/notifications/user/{userId}/unread-count - 获取未读通知数量
 * - PUT /api/v1/notifications/{id}/read - 标记通知为已读
 * - PUT /api/v1/notifications/user/{userId}/read-all - 标记所有通知为已读
 * - DELETE /api/v1/notifications/{id} - 删除通知
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取用户的所有通知
     * @param userId 用户ID
     * @return 通知列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Notification>> findByUserId(@PathVariable Long userId) {
        return Result.success(notificationService.findByUserId(userId));
    }

    /**
     * 获取用户的未读通知列表
     * @param userId 用户ID
     * @return 未读通知列表
     */
    @GetMapping("/user/{userId}/unread")
    public Result<List<Notification>> findUnreadByUserId(@PathVariable Long userId) {
        return Result.success(notificationService.findUnreadByUserId(userId));
    }

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public Result<Integer> countUnread(@PathVariable Long userId) {
        return Result.success(notificationService.countUnread(userId));
    }

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @return 成功返回空结果
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     * @return 成功返回空结果
     */
    @PutMapping("/user/{userId}/read-all")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     * @param id 通知ID
     * @return 成功返回空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.removeById(id);
        return Result.success();
    }
}
