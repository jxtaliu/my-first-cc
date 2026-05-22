package com.sme.pm.listener;

import com.sme.pm.common.NotificationType;
import com.sme.pm.entity.Notification;
import com.sme.pm.entity.User;
import com.sme.pm.event.TaskAssignedEvent;
import com.sme.pm.event.TaskCommentEvent;
import com.sme.pm.event.TaskStatusChangedEvent;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationEventListenerTest {

    @Mock
    private INotificationService notificationService;

    @Mock
    private UserMapper userMapper;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(notificationService, userMapper);
    }

    @Test
    void handleTaskAssignedEvent_shouldCreateNotification() {
        Long userId = 1L;
        Long taskId = 100L;
        Long assigneeId = 2L;
        Long projectId = 10L;
        String title = "Task Assigned";
        String content = "You have been assigned to task: Test Task";

        TaskAssignedEvent event = new TaskAssignedEvent(
            this, userId, taskId, assigneeId, title, content, projectId
        );

        listener.handleTaskAssigned(event);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).save(notificationCaptor.capture());

        Notification captured = notificationCaptor.getValue();
        assertEquals(userId, captured.getUserId());
        assertEquals(NotificationType.TASK_ASSIGNED, captured.getType());
        assertEquals(title, captured.getTitle());
        assertEquals(content, captured.getContent());
        assertEquals(taskId, captured.getRelatedTaskId());
        assertEquals(projectId, captured.getRelatedProjectId());
        assertEquals(0, captured.getIsRead());
    }

    @Test
    void handleTaskStatusChangedEvent_shouldCreateNotification() {
        Long userId = 1L;
        Long taskId = 100L;
        Long oldStatusId = 10L;
        Long newStatusId = 20L;
        Long projectId = 10L;
        String title = "Task Status Changed";
        String content = "Task \"Test Task\" status changed to In Progress";

        TaskStatusChangedEvent event = new TaskStatusChangedEvent(
            this, userId, taskId, oldStatusId, newStatusId, title, content, projectId
        );

        listener.handleTaskStatusChanged(event);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).save(notificationCaptor.capture());

        Notification captured = notificationCaptor.getValue();
        assertEquals(userId, captured.getUserId());
        assertEquals(NotificationType.TASK_STATUS_CHANGED, captured.getType());
        assertEquals(title, captured.getTitle());
        assertEquals(content, captured.getContent());
        assertEquals(taskId, captured.getRelatedTaskId());
        assertEquals(projectId, captured.getRelatedProjectId());
        assertEquals(0, captured.getIsRead());
    }

    @Test
    void handleTaskCommentEvent_shouldParseMentions() {
        Long userId = 1L;
        Long taskId = 100L;
        Long commentId = 200L;
        Long projectId = 10L;
        String title = "New Comment";
        String content = "Hey @john and @jane, please review this task";

        TaskCommentEvent event = new TaskCommentEvent(
            this, userId, taskId, commentId, null, title, content, projectId
        );

        User johnUser = new User();
        johnUser.setId(10L);
        johnUser.setUsername("john");

        User janeUser = new User();
        janeUser.setId(20L);
        janeUser.setUsername("jane");

        when(userMapper.findByUsername("john")).thenReturn(johnUser);
        when(userMapper.findByUsername("jane")).thenReturn(janeUser);

        listener.handleTaskComment(event);

        // Verify all 3 notifications are saved (1 for task owner + 2 for mentions)
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService, times(3)).save(notificationCaptor.capture());

        // Check that mentions were processed
        // The listener should have created notifications for both mentioned users
        boolean foundJohnMention = false;
        boolean foundJaneMention = false;
        for (Notification n : notificationCaptor.getAllValues()) {
            if (n.getType().equals(NotificationType.TASK_MENTION)) {
                if (n.getUserId().equals(10L)) foundJohnMention = true;
                if (n.getUserId().equals(20L)) foundJaneMention = true;
            }
        }
        assertTrue(foundJohnMention, "John should be notified");
        assertTrue(foundJaneMention, "Jane should be notified");
    }

    @Test
    void handleTaskCommentEvent_shouldNotNotifyAuthor() {
        Long userId = 1L;
        Long taskId = 100L;
        Long commentId = 200L;
        Long projectId = 10L;
        String title = "New Comment";
        String content = "I commented on this task";

        TaskCommentEvent event = new TaskCommentEvent(
            this, userId, taskId, commentId, null, title, content, projectId
        );

        // No mentions in this comment - no userMapper calls needed

        listener.handleTaskComment(event);

        // Should only save notification for task owner (userId = 1L), not for anyone else
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService, times(1)).save(notificationCaptor.capture());

        Notification captured = notificationCaptor.getValue();
        assertEquals(userId, captured.getUserId());
    }
}
