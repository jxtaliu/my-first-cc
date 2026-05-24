package com.sme.pm.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sme.pm.common.NotificationType;
import com.sme.pm.entity.Milestone;
import com.sme.pm.entity.Project;
import com.sme.pm.entity.Sprint;
import com.sme.pm.entity.Task;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.event.MilestoneDueEvent;
import com.sme.pm.event.SprintEvent;
import com.sme.pm.mapper.MilestoneMapper;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.SprintMapper;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.mapper.TaskStatusMapper;
import com.sme.pm.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private MilestoneMapper milestoneMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskStatusMapper taskStatusMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private INotificationService notificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationScheduler(
            sprintMapper,
            milestoneMapper,
            taskMapper,
            taskStatusMapper,
            projectMapper,
            notificationService,
            eventPublisher
        );
    }

    @Test
    void checkSprintReminders_shouldNotifyUpcomingSprint() {
        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setProjectId("PRJ_001");
        sprint.setName("Sprint 1");
        sprint.setStatus("PLANNING"); // Planning status
        sprint.setStartDate(LocalDate.now().plusDays(1)); // Starting tomorrow
        sprint.setEndDate(LocalDate.now().plusDays(14));

        List<Long> memberIds = Arrays.asList(100L, 200L);

        // Return sprint only for the starting sprints query (status=1)
        // Return empty list for ending sprints query (status=2)
        when(sprintMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(sprint))
            .thenReturn(Collections.emptyList());
        when(projectMapper.findMemberIds("PRJ_001")).thenReturn(memberIds);

        scheduler.checkUpcomingSprints();

        // Should publish SPRINT_START events for both members (2 events)
        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<SprintEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());

        for (SprintEvent event : capturedEvents) {
            assertEquals(1L, event.getSprintId());
            assertEquals(NotificationType.SPRINT_START, event.getEventType());
            assertEquals(10L, event.getRelatedProjectId());
        }
    }

    @Test
    void checkSprintReminders_shouldNotifyEndingSprint() {
        Sprint sprint = new Sprint();
        sprint.setId(2L);
        sprint.setProjectId("PRJ_001");
        sprint.setName("Sprint 2");
        sprint.setStatus("ACTIVE"); // Active status
        sprint.setStartDate(LocalDate.now().minusDays(13));
        sprint.setEndDate(LocalDate.now().plusDays(1)); // Ending tomorrow

        List<Long> memberIds = Arrays.asList(100L, 200L);

        // Return empty list for starting sprints query (status=1)
        // Return sprint only for ending sprints query (status=2)
        when(sprintMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList())
            .thenReturn(Collections.singletonList(sprint));
        when(projectMapper.findMemberIds("PRJ_001")).thenReturn(memberIds);

        scheduler.checkUpcomingSprints();

        // Should publish SPRINT_END events for both members (2 events)
        ArgumentCaptor<SprintEvent> eventCaptor = ArgumentCaptor.forClass(SprintEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<SprintEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());

        for (SprintEvent event : capturedEvents) {
            assertEquals(2L, event.getSprintId());
            assertEquals(NotificationType.SPRINT_END, event.getEventType());
            assertEquals(10L, event.getRelatedProjectId());
        }
    }

    @Test
    void checkMilestoneReminders_shouldNotifyDueMilestone() {
        Milestone milestone = new Milestone();
        milestone.setId(1L);
        milestone.setProjectId("PRJ_001");
        milestone.setName("Q1 Milestone");
        milestone.setTargetDate(LocalDate.now().plusDays(2)); // Within 3 days
        milestone.setStatus("ACTIVE");

        List<Long> memberIds = Arrays.asList(100L, 200L);

        // Return milestone for milestone query, empty for overdue tasks query
        when(milestoneMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(milestone));
        when(taskMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());
        when(projectMapper.findMemberIds("PRJ_001")).thenReturn(memberIds);

        scheduler.checkMilestonesAndOverdueTasks();

        // Should publish MILESTONE_DUE events for both members (2 events)
        ArgumentCaptor<MilestoneDueEvent> eventCaptor = ArgumentCaptor.forClass(MilestoneDueEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<MilestoneDueEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());

        for (MilestoneDueEvent event : capturedEvents) {
            assertEquals(1L, event.getMilestoneId());
            assertEquals(LocalDate.now().plusDays(2), event.getTargetDate());
            assertEquals(10L, event.getRelatedProjectId());
        }
    }
}
