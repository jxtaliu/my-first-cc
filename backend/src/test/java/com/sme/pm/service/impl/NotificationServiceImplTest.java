package com.sme.pm.service.impl;

import com.sme.pm.entity.Notification;
import com.sme.pm.mapper.NotificationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImpl 单元测试
 *
 * 测试场景：通知服务的各项功能
 * - 根据用户ID查询通知列表
 * - 查询未读通知
 * - 统计未读数量
 * - 标记单条通知为已读
 * - 批量标记所有通知为已读
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    private NotificationServiceImpl notificationService;

    private Notification testNotification1;
    private Notification testNotification2;

    @BeforeEach
    void setUp() throws Exception {
        notificationService = new NotificationServiceImpl();

        // Use reflection to set the baseMapper field inherited from ServiceImpl
        java.lang.reflect.Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(notificationService, notificationMapper);

        testNotification1 = new Notification();
        testNotification1.setId(1L);
        testNotification1.setUserId(100L);
        testNotification1.setType("TASK_ASSIGNED");
        testNotification1.setTitle("New Task Assigned");
        testNotification1.setContent("You have been assigned to Task #123");
        testNotification1.setIsRead(0);
        testNotification1.setCreatedAt(LocalDateTime.now());

        testNotification2 = new Notification();
        testNotification2.setId(2L);
        testNotification2.setUserId(100L);
        testNotification2.setType("TASK_STATUS_CHANGED");
        testNotification2.setTitle("Task Status Changed");
        testNotification2.setContent("Task #123 has been moved to IN_PROGRESS");
        testNotification2.setIsRead(1);
        testNotification2.setCreatedAt(LocalDateTime.now());
    }

    // ==================== findByUserId Tests ====================

    /**
     * 测试场景：根据用户ID查询通知列表 - 存在多条通知
     * 预期：返回该用户的所有通知，按创建时间倒序排列
     */
    @Test
    void findByUserId_shouldReturnNotifications() {
        // Arrange
        Long userId = 100L;
        List<Notification> notifications = Arrays.asList(testNotification2, testNotification1);
        when(notificationMapper.findByUserId(userId)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.findByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        verify(notificationMapper).findByUserId(userId);
    }

    /**
     * 测试场景：根据用户ID查询通知列表 - 无通知记录
     * 预期：返回空列表
     */
    @Test
    void findByUserId_shouldReturnEmptyList_whenNoNotifications() {
        // Arrange
        Long userId = 999L;
        when(notificationMapper.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<Notification> result = notificationService.findByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(notificationMapper).findByUserId(userId);
    }

    // ==================== findUnreadByUserId Tests ====================

    /**
     * 测试场景：查询未读通知 - 存在未读通知
     * 预期：返回该用户的未读通知列表
     */
    @Test
    void findUnreadByUserId_shouldReturnUnreadNotifications() {
        // Arrange
        Long userId = 100L;
        List<Notification> unreadNotifications = Collections.singletonList(testNotification1);
        when(notificationMapper.findUnreadByUserId(userId)).thenReturn(unreadNotifications);

        // Act
        List<Notification> result = notificationService.findUnreadByUserId(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getIsRead());
        verify(notificationMapper).findUnreadByUserId(userId);
    }

    /**
     * 测试场景：查询未读通知 - 无未读通知
     * 预期：返回空列表
     */
    @Test
    void findUnreadByUserId_shouldReturnEmptyList_whenNoUnread() {
        // Arrange
        Long userId = 100L;
        when(notificationMapper.findUnreadByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<Notification> result = notificationService.findUnreadByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(notificationMapper).findUnreadByUserId(userId);
    }

    // ==================== countUnread Tests ====================

    /**
     * 测试场景：统计未读通知数量 - 存在多条未读通知
     * 预期：返回正确的未读数量
     */
    @Test
    void countUnread_shouldReturnCorrectCount() {
        // Arrange
        Long userId = 100L;
        when(notificationMapper.countUnread(userId)).thenReturn(5);

        // Act
        int count = notificationService.countUnread(userId);

        // Assert
        assertEquals(5, count);
        verify(notificationMapper).countUnread(userId);
    }

    /**
     * 测试场景：统计未读通知数量 - 无未读通知
     * 预期：返回 0
     */
    @Test
    void countUnread_shouldReturnZero_whenNoUnread() {
        // Arrange
        Long userId = 100L;
        when(notificationMapper.countUnread(userId)).thenReturn(0);

        // Act
        int count = notificationService.countUnread(userId);

        // Assert
        assertEquals(0, count);
        verify(notificationMapper).countUnread(userId);
    }

    // ==================== markAsRead Tests ====================

    /**
     * 测试场景：标记通知为已读 - 通知存在
     * 预期：通知的 isRead 设置为 1，调用 updateById
     */
    @Test
    void markAsRead_shouldUpdateNotification_whenExists() {
        // Arrange
        Long notificationId = 1L;
        when(notificationMapper.findById(notificationId)).thenReturn(testNotification1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        assertEquals(1, testNotification1.getIsRead());
        verify(notificationMapper).findById(notificationId);
        verify(notificationMapper).updateById(testNotification1);
    }

    /**
     * 测试场景：标记通知为已读 - 通知不存在
     * 预期：不调用 updateById
     */
    @Test
    void markAsRead_shouldDoNothing_whenNotFound() {
        // Arrange
        Long notificationId = 999L;
        when(notificationMapper.findById(notificationId)).thenReturn(null);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        verify(notificationMapper).findById(notificationId);
        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    // ==================== markAllAsRead Tests ====================

    /**
     * 测试场景：批量标记所有通知为已读 - 存在未读通知
     * 预期：所有未读通知的 isRead 设置为 1，调用 updateBatchById
     */
    @Test
    void markAllAsRead_shouldUpdateAllUnreadNotifications() {
        // Arrange
        Long userId = 100L;
        Notification unread1 = new Notification();
        unread1.setId(1L);
        unread1.setUserId(userId);
        unread1.setIsRead(0);

        Notification unread2 = new Notification();
        unread2.setId(2L);
        unread2.setUserId(userId);
        unread2.setIsRead(0);

        List<Notification> unreadList = Arrays.asList(unread1, unread2);
        when(notificationMapper.findUnreadByUserId(userId)).thenReturn(unreadList);

        // Act
        notificationService.markAllAsRead(userId);

        // Assert
        assertEquals(1, unread1.getIsRead());
        assertEquals(1, unread2.getIsRead());
        verify(notificationMapper).findUnreadByUserId(userId);
    }

    /**
     * 测试场景：批量标记所有通知为已读 - 无未读通知
     * 预期：不调用 updateBatchById
     */
    @Test
    void markAllAsRead_shouldDoNothing_whenNoUnread() {
        // Arrange
        Long userId = 100L;
        when(notificationMapper.findUnreadByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        notificationService.markAllAsRead(userId);

        // Assert
        verify(notificationMapper).findUnreadByUserId(userId);
    }
}
