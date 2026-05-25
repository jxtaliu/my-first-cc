package com.sme.pm.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sme.pm.common.NotificationType;
import com.sme.pm.entity.*;
import com.sme.pm.event.MilestoneDueEvent;
import com.sme.pm.event.SprintEvent;
import com.sme.pm.event.TaskDependencyBlockedEvent;
import com.sme.pm.mapper.*;
import com.sme.pm.service.INotificationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private final SprintMapper sprintMapper;
    private final MilestoneMapper milestoneMapper;
    private final TaskMapper taskMapper;
    private final TaskStatusMapper taskStatusMapper;
    private final ProjectMapper projectMapper;
    private final INotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationScheduler(SprintMapper sprintMapper,
                                  MilestoneMapper milestoneMapper,
                                  TaskMapper taskMapper,
                                  TaskStatusMapper taskStatusMapper,
                                  ProjectMapper projectMapper,
                                  INotificationService notificationService,
                                  ApplicationEventPublisher eventPublisher) {
        this.sprintMapper = sprintMapper;
        this.milestoneMapper = milestoneMapper;
        this.taskMapper = taskMapper;
        this.taskStatusMapper = taskStatusMapper;
        this.projectMapper = projectMapper;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Daily at 9:00 AM - Check sprints starting/ending within 24 hours
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkUpcomingSprints() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);

        // Find sprints starting within 24 hours (status = 2: active planning)
        LambdaQueryWrapper<Sprint> startWrapper = new LambdaQueryWrapper<>();
        startWrapper.eq(Sprint::getStatus, 1)
                    .ge(Sprint::getStartDate, now)
                    .le(Sprint::getStartDate, tomorrow)
                    .eq(Sprint::getDeleted, 0);
        List<Sprint> startingSprints = sprintMapper.selectList(startWrapper);

        for (Sprint sprint : startingSprints) {
            notifySprintMembers(sprint, NotificationType.SPRINT_START,
                    "Sprint Starting Soon",
                    "Sprint \"" + sprint.getName() + "\" will start within 24 hours");
        }

        // Find sprints ending within 24 hours
        LambdaQueryWrapper<Sprint> endWrapper = new LambdaQueryWrapper<>();
        endWrapper.eq(Sprint::getStatus, 2)
                  .ge(Sprint::getEndDate, now)
                  .le(Sprint::getEndDate, tomorrow)
                  .eq(Sprint::getDeleted, 0);
        List<Sprint> endingSprints = sprintMapper.selectList(endWrapper);

        for (Sprint sprint : endingSprints) {
            notifySprintMembers(sprint, NotificationType.SPRINT_END,
                    "Sprint Ending Soon",
                    "Sprint \"" + sprint.getName() + "\" will end within 24 hours");
        }
    }

    /**
     * Daily at 9:30 AM - Check milestones due within 3 days and overdue tasks
     */
    @Scheduled(cron = "0 30 9 * * *")
    public void checkMilestonesAndOverdueTasks() {
        // Check milestones due within 3 days
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(3);

        LambdaQueryWrapper<Milestone> milestoneWrapper = new LambdaQueryWrapper<>();
        milestoneWrapper.ge(Milestone::getTargetDate, today)
                       .le(Milestone::getTargetDate, deadline)
                       .eq(Milestone::getDeleted, 0);
        List<Milestone> dueMilestones = milestoneMapper.selectList(milestoneWrapper);

        for (Milestone milestone : dueMilestones) {
            notifyMilestoneMembers(milestone, NotificationType.MILESTONE_DUE,
                    "Milestone Due Soon",
                    "Milestone \"" + milestone.getName() + "\" is due within 3 days");
        }

        // Check overdue tasks
        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.lt(Task::getDueDate, today)
                   .eq(Task::getDeleted, 0);
        // Exclude completed tasks - we need to find tasks not in "done" status
        List<Task> overdueTasks = taskMapper.selectList(taskWrapper);

        // Get done status code
        LambdaQueryWrapper<TaskStatus> statusWrapper = new LambdaQueryWrapper<>();
        statusWrapper.eq(TaskStatus::getCode, "DONE");
        List<TaskStatus> doneStatuses = taskStatusMapper.selectList(statusWrapper);

        for (Task task : overdueTasks) {
            // Check if task is not in done status
            boolean isDone = doneStatuses.stream()
                    .anyMatch(s -> s.getCode().equals(task.getStatus()));
            if (!isDone && task.getAssigneeId() != null) {
                MilestoneDueEvent event = new MilestoneDueEvent(
                        this,
                        task.getAssigneeId(),
                        null,
                        task.getDueDate(),
                        "Task Overdue",
                        "Task \"" + task.getTitle() + "\" is overdue",
                        task.getProjectId()
                );
                eventPublisher.publishEvent(event);
            }
        }
    }

    private void notifySprintMembers(Sprint sprint, String notificationType, String title, String content) {
        List<Long> memberIds = projectMapper.findMemberIds(sprint.getProjectId());
        for (Long userId : memberIds) {
            SprintEvent event = new SprintEvent(
                    this,
                    userId,
                    sprint.getId(),
                    notificationType,
                    title,
                    content,
                    sprint.getProjectId()
            );
            eventPublisher.publishEvent(event);
        }
    }

    private void notifyMilestoneMembers(Milestone milestone, String notificationType, String title, String content) {
        if (milestone.getProjectId() != null) {
            List<Long> memberIds = projectMapper.findMemberIds(milestone.getProjectId());
            for (Long userId : memberIds) {
                MilestoneDueEvent event = new MilestoneDueEvent(
                        this,
                        userId,
                        milestone.getId(),
                        milestone.getTargetDate(),
                        title,
                        content,
                        milestone.getProjectId()
                );
                eventPublisher.publishEvent(event);
            }
        }
    }
}
