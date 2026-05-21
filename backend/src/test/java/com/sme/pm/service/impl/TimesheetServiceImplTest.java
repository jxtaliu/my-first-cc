package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.entity.Timesheet;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.mapper.TimesheetMapper;
import com.sme.pm.service.TimesheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    private TimesheetService timesheetService;

    @BeforeEach
    void setUp() {
        timesheetService = new TimesheetServiceImpl(timesheetMapper, projectMapper);
    }

    @Test
    void create_shouldAutoApprove_forInternalProject() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectType(1); // Internal

        Timesheet timesheet = new Timesheet();
        timesheet.setProjectId(1L);
        timesheet.setHours(8);
        timesheet.setWorkDate(LocalDate.now());

        when(projectMapper.findById(1L)).thenReturn(project);
        when(timesheetMapper.insert(any(Timesheet.class))).thenReturn(1);

        Timesheet result = timesheetService.create(timesheet);

        assertEquals(2, result.getApprovalStatus()); // Auto-approved
        verify(timesheetMapper).insert(timesheet);
    }

    @Test
    void create_shouldSetPending_forExternalProject() {
        Project project = new Project();
        project.setId(1L);
        project.setProjectType(2); // External/Client

        Timesheet timesheet = new Timesheet();
        timesheet.setProjectId(1L);
        timesheet.setHours(8);
        timesheet.setWorkDate(LocalDate.now());

        when(projectMapper.findById(1L)).thenReturn(project);
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
    void approve_shouldSetApprovalStatusAndApprover() {
        Long timesheetId = 1L;
        Long approverId = 2L;

        when(timesheetMapper.updateById(any(Timesheet.class))).thenReturn(1);

        timesheetService.approve(timesheetId, approverId);

        verify(timesheetMapper).updateById(argThat(timesheet -> {
            timesheet.setId(timesheetId);
            timesheet.setApprovalStatus(2);
            timesheet.setApproverId(approverId);
            timesheet.setApprovedAt(any(LocalDateTime.class));
            return true;
        }));
    }

    @Test
    void reject_shouldSetRejectionStatusAndReason() {
        Long timesheetId = 1L;
        Long approverId = 2L;
        String reason = "Hours exceed estimate";

        when(timesheetMapper.updateById(any(Timesheet.class))).thenReturn(1);

        timesheetService.reject(timesheetId, approverId, reason);

        verify(timesheetMapper).updateById(argThat(timesheet -> {
            assertEquals(timesheetId, timesheet.getId());
            assertEquals(3, timesheet.getApprovalStatus());
            assertEquals(approverId, timesheet.getApproverId());
            assertEquals(reason, timesheet.getRejectionReason());
            return true;
        }));
    }

    @Test
    void resubmit_shouldResetStatusToPending() {
        Long timesheetId = 1L;

        when(timesheetMapper.updateById(any(Timesheet.class))).thenReturn(1);

        timesheetService.resubmit(timesheetId);

        verify(timesheetMapper).updateById(argThat(timesheet -> {
            assertEquals(timesheetId, timesheet.getId());
            assertEquals(1, timesheet.getApprovalStatus());
            return true;
        }));
    }
}
