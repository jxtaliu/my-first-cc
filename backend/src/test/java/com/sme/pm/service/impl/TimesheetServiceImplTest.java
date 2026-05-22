package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.entity.Timesheet;
import com.sme.pm.event.TimesheetApprovalEvent;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.TimesheetMapper;
import com.sme.pm.service.IProjectRoleService;
import com.sme.pm.service.TimesheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceImplTest {

    @Mock
    private TimesheetMapper timesheetMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private IProjectRoleService projectRoleService;

    private TimesheetService timesheetService;

    @BeforeEach
    void setUp() {
        timesheetService = new TimesheetServiceImpl(timesheetMapper, projectMapper, eventPublisher, projectRoleService);
    }

    @Test
    void create_shouldSetPendingStatus_forExternalProject() {
        Timesheet timesheet = new Timesheet();
        timesheet.setProjectId(1L);
        timesheet.setHours(8);
        timesheet.setWorkDate(LocalDateTime.now());

        when(timesheetMapper.insert(any(Timesheet.class))).thenReturn(1);

        Timesheet result = timesheetService.create(timesheet);

        assertEquals(1, result.getApprovalStatus()); // Pending
        verify(timesheetMapper).insert(timesheet);
    }

    @Test
    void update_shouldUpdateTimesheetAndReturn() {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(1L);
        timesheet.setHours(10);

        when(timesheetMapper.updateById(timesheet)).thenReturn(1);

        Timesheet result = timesheetService.update(timesheet);

        assertNotNull(result);
        assertEquals(10, result.getHours());
        verify(timesheetMapper).updateById(timesheet);
    }

    @Test
    void delete_shouldCallDeleteById() {
        Long timesheetId = 1L;

        timesheetService.delete(timesheetId);

        verify(timesheetMapper).deleteById(timesheetId);
    }

    @Test
    void listWeekly_shouldReturnTimesheetsInDateRange() {
        Long userId = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-01-07";

        Timesheet ts1 = new Timesheet();
        ts1.setId(1L);
        ts1.setUserId(userId);

        Timesheet ts2 = new Timesheet();
        ts2.setId(2L);
        ts2.setUserId(userId);

        when(timesheetMapper.findByUserAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList(ts1, ts2));

        List<Timesheet> result = timesheetService.listWeekly(userId, startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(timesheetMapper).findByUserAndDateRange(userId, startDate, endDate);
    }

    @Test
    void listMonthly_shouldReturnTimesheetsInDateRange() {
        Long userId = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        when(timesheetMapper.findByUserAndDateRange(userId, startDate, endDate))
            .thenReturn(Arrays.asList());

        List<Timesheet> result = timesheetService.listMonthly(userId, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listProjectTimesheets_shouldReturnProjectTimesheets() {
        Long projectId = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        Timesheet ts1 = new Timesheet();
        ts1.setId(1L);
        ts1.setProjectId(projectId);

        when(timesheetMapper.findByProjectAndDateRange(projectId, startDate, endDate))
            .thenReturn(Arrays.asList(ts1));

        List<Timesheet> result = timesheetService.listProjectTimesheets(projectId, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(timesheetMapper).findByProjectAndDateRange(projectId, startDate, endDate);
    }

    @Test
    void approve_shouldSetApprovalStatusAndPublishEvent() {
        Long timesheetId = 1L;
        Long approverId = 2L;
        Long userId = 3L;
        Long projectId = 10L;

        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setUserId(userId);
        timesheet.setProjectId(projectId);
        timesheet.setApprovalStatus(1);

        when(timesheetMapper.selectById(timesheetId)).thenReturn(timesheet);
        when(timesheetMapper.updateById(any(Timesheet.class))).thenReturn(1);

        timesheetService.approve(timesheetId, approverId);

        verify(timesheetMapper).updateById(argThat(t ->
            t.getId().equals(timesheetId) &&
            t.getApprovalStatus() == 2 &&
            t.getApproverId().equals(approverId) &&
            t.getApprovedAt() != null
        ));

        ArgumentCaptor<TimesheetApprovalEvent> eventCaptor = ArgumentCaptor.forClass(TimesheetApprovalEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TimesheetApprovalEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(timesheetId, capturedEvent.getTimesheetId());
        assertEquals("APPROVED", capturedEvent.getApprovalStatus());
        assertEquals("Timesheet Approved", capturedEvent.getTitle());
        assertEquals(projectId, capturedEvent.getRelatedProjectId());
    }

    @Test
    void reject_shouldSetRejectionReasonAndPublishEvent() {
        Long timesheetId = 1L;
        Long approverId = 2L;
        Long userId = 3L;
        Long projectId = 10L;
        String reason = "Hours exceed estimate";

        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setUserId(userId);
        timesheet.setProjectId(projectId);
        timesheet.setApprovalStatus(1);

        when(timesheetMapper.selectById(timesheetId)).thenReturn(timesheet);
        when(timesheetMapper.updateById(any(Timesheet.class))).thenReturn(1);

        timesheetService.reject(timesheetId, approverId, reason);

        verify(timesheetMapper).updateById(argThat(t -> {
            assertEquals(timesheetId, t.getId());
            assertEquals(3, t.getApprovalStatus());
            assertEquals(approverId, t.getApproverId());
            assertEquals(reason, t.getRejectionReason());
            return true;
        }));

        ArgumentCaptor<TimesheetApprovalEvent> eventCaptor = ArgumentCaptor.forClass(TimesheetApprovalEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TimesheetApprovalEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(timesheetId, capturedEvent.getTimesheetId());
        assertEquals("REJECTED", capturedEvent.getApprovalStatus());
        assertEquals("Timesheet Rejected", capturedEvent.getTitle());
        assertTrue(capturedEvent.getContent().contains(reason));
        assertEquals(projectId, capturedEvent.getRelatedProjectId());
    }

    @Test
    void resubmit_shouldResetStatusToPending() {
        Long timesheetId = 1L;

        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(2);

        when(timesheetMapper.updateById(any(Timesheet.class))).thenReturn(1);

        timesheetService.resubmit(timesheetId);

        verify(timesheetMapper).updateById(argThat(t -> {
            assertEquals(timesheetId, t.getId());
            assertEquals(1, t.getApprovalStatus());
            return true;
        }));
    }
}
