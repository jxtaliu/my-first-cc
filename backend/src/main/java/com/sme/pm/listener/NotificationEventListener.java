package com.sme.pm.listener;

import com.sme.pm.common.NotificationType;
import com.sme.pm.entity.Notification;
import com.sme.pm.entity.ProjectRole;
import com.sme.pm.entity.User;
import com.sme.pm.event.*;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.service.IProjectRoleService;
import com.sme.pm.service.INotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NotificationEventListener {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private final INotificationService notificationService;
    private final UserMapper userMapper;
    private final IProjectRoleService projectRoleService;

    public NotificationEventListener(INotificationService notificationService, UserMapper userMapper,
                                     IProjectRoleService projectRoleService) {
        this.notificationService = notificationService;
        this.userMapper = userMapper;
        this.projectRoleService = projectRoleService;
    }

    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedTaskId(event.getTaskId());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Async
    @EventListener
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.TASK_STATUS_CHANGED);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedTaskId(event.getTaskId());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Async
    @EventListener
    public void handleTaskComment(TaskCommentEvent event) {
        // Notify the user who owns the task
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.TASK_COMMENT);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedTaskId(event.getTaskId());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);

        // Parse mentions and create notifications for mentioned users
        String[] mentions = parseMentions(event.getContent());
        for (String username : mentions) {
            User user = userMapper.findByUsername(username);
            if (user != null) {
                Notification mentionNotification = new Notification();
                mentionNotification.setUserId(user.getId());
                mentionNotification.setType(NotificationType.TASK_MENTION);
                mentionNotification.setTitle(event.getTitle());
                mentionNotification.setContent(event.getContent());
                mentionNotification.setRelatedTaskId(event.getTaskId());
                mentionNotification.setRelatedProjectId(event.getRelatedProjectId());
                mentionNotification.setIsRead(0);
                notificationService.save(mentionNotification);
            }
        }
    }

    @Async
    @EventListener
    public void handleTaskDependencyBlocked(TaskDependencyBlockedEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.TASK_DEPENDENCY_BLOCKED);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedTaskId(event.getRelatedTaskId());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Async
    @EventListener
    public void handleMilestoneDue(MilestoneDueEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.MILESTONE_DUE);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Async
    @EventListener
    public void handleSprintEvent(SprintEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.SPRINT_START.equals(event.getEventType())
                ? NotificationType.SPRINT_START : NotificationType.SPRINT_END);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Async
    @EventListener
    public void handleTimesheetApproval(TimesheetApprovalEvent event) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setType(NotificationType.TIMESHEET_APPROVED.equals(event.getApprovalStatus())
                ? NotificationType.TIMESHEET_APPROVED : NotificationType.TIMESHEET_REJECTED);
        notification.setTitle(event.getTitle());
        notification.setContent(event.getContent());
        notification.setRelatedProjectId(event.getRelatedProjectId());
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    @Async
    @EventListener
    public void handleTimesheetSubmitted(TimesheetSubmittedEvent event) {
        // Notify all PROJECT_MANAGERs of the project
        List<ProjectRole> pmRoles = projectRoleService.findByProjectAndRole(event.getRelatedProjectId(), "PROJECT_MANAGER");
        for (ProjectRole pmRole : pmRoles) {
            Notification notification = new Notification();
            notification.setUserId(pmRole.getUserId());
            notification.setType(NotificationType.TIMESHEET_SUBMITTED);
            notification.setTitle(event.getTitle());
            notification.setContent(event.getContent());
            notification.setRelatedProjectId(event.getRelatedProjectId());
            notification.setIsRead(0);
            notificationService.save(notification);
        }
    }

    private String[] parseMentions(String content) {
        if (content == null || content.isEmpty()) {
            return new String[0];
        }
        Set<String> mentions = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions.toArray(new String[0]);
    }
}
