package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Timesheet;
import com.sme.pm.service.IRolePermissionService;
import com.sme.pm.service.TimesheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimesheetController.
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *   <li>Create timesheet</li>
 *   <li>Get timesheet by ID</li>
 *   <li>Update timesheet</li>
 *   <li>Delete timesheet</li>
 *   <li>Get weekly timesheets</li>
 *   <li>Get monthly timesheets</li>
 *   <li>Get project timesheets</li>
 *   <li>Get my timesheets</li>
 *   <li>Get timesheet stats</li>
 *   <li>Approve timesheet</li>
 *   <li>Reject timesheet</li>
 *   <li>Get pending approval timesheets</li>
 *   <li>Resubmit timesheet</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TimesheetControllerTest {

    @Mock
    private TimesheetService timesheetService;

    @Mock
    private IRolePermissionService rolePermissionService;

    private TimesheetController timesheetController;

    @BeforeEach
    void setUp() {
        timesheetController = new TimesheetController(timesheetService, rolePermissionService);
    }

    // ==================== Create Timesheet Tests ====================

    @Test
    void create_shouldReturnCreatedTimesheet() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 8);
        Timesheet created = createTimesheet(1L, 1L, "PRJ_001", 8);
        when(timesheetService.create(any(Timesheet.class))).thenReturn(created);

        // Act
        Result<Timesheet> result = timesheetController.create(timesheet, 1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
        assertEquals(1L, timesheet.getUserId());
    }

    // ==================== Get By ID Tests ====================

    @Test
    void getById_shouldReturnTimesheet() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 8);
        when(timesheetService.getById(1L)).thenReturn(timesheet);

        // Act
        Result<Timesheet> result = timesheetController.getById(1L);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(timesheetService.getById(999L)).thenReturn(null);

        // Act
        Result<Timesheet> result = timesheetController.getById(999L);

        // Assert
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    // ==================== Update Timesheet Tests ====================

    @Test
    void update_shouldReturnUpdatedTimesheet() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 10);
        when(timesheetService.update(any(Timesheet.class))).thenReturn(timesheet);

        // Act
        Result<Timesheet> result = timesheetController.update(1L, timesheet);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(10, result.getData().getHours());
    }

    // ==================== Delete Timesheet Tests ====================

    @Test
    void delete_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = timesheetController.delete(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(timesheetService).delete(1L);
    }

    // ==================== Weekly Timesheet Tests ====================

    @Test
    void weekly_shouldReturnTimesheetList() {
        // Arrange
        List<Timesheet> timesheets = Arrays.asList(
            createTimesheet(1L, 1L, "PRJ_001", 8),
            createTimesheet(2L, 1L, "PRJ_001", 6)
        );
        when(timesheetService.listWeekly(1L, "2024-01-01", "2024-01-07")).thenReturn(timesheets);

        // Act
        Result<List<Timesheet>> result = timesheetController.weekly(1L, "2024-01-01", "2024-01-07");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    // ==================== Monthly Timesheet Tests ====================

    @Test
    void monthly_shouldReturnTimesheetList() {
        // Arrange
        List<Timesheet> timesheets = Collections.singletonList(
            createTimesheet(1L, 1L, "PRJ_001", 40)
        );
        when(timesheetService.listMonthly(1L, "2024-01-01", "2024-01-31")).thenReturn(timesheets);

        // Act
        Result<List<Timesheet>> result = timesheetController.monthly(1L, "2024-01-01", "2024-01-31");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ==================== Project Timesheets Tests ====================

    @Test
    void projectTimesheets_shouldReturnTimesheetList() {
        // Arrange
        List<Timesheet> timesheets = Arrays.asList(
            createTimesheet(1L, 1L, "PRJ_001", 8),
            createTimesheet(2L, 2L, "PRJ_001", 7)
        );
        when(timesheetService.listProjectTimesheets("PRJ_001", "2024-01-01", "2024-01-31")).thenReturn(timesheets);

        // Act
        Result<List<Timesheet>> result = timesheetController.projectTimesheets("PRJ_001", "2024-01-01", "2024-01-31");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    // ==================== My Timesheets Tests ====================

    @Test
    void myTimesheets_shouldReturnAllUserTimesheets_whenNoDatesProvided() {
        // Arrange
        List<Timesheet> timesheets = Collections.singletonList(
            createTimesheet(1L, 1L, "PRJ_001", 8)
        );
        when(timesheetService.listByUser(1L)).thenReturn(timesheets);

        // Act
        Result<List<Timesheet>> result = timesheetController.myTimesheets(1L, null, null);

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void myTimesheets_shouldReturnWeeklyTimesheets_whenDatesProvided() {
        // Arrange
        List<Timesheet> timesheets = Collections.singletonList(
            createTimesheet(1L, 1L, "PRJ_001", 8)
        );
        when(timesheetService.listWeekly(1L, "2024-01-01", "2024-01-07")).thenReturn(timesheets);

        // Act
        Result<List<Timesheet>> result = timesheetController.myTimesheets(1L, "2024-01-01", "2024-01-07");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ==================== Stats Tests ====================

    @Test
    void stats_shouldReturnStatsData() {
        // Arrange
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("totalHours", 40);
        stats.put("totalDays", 5);
        when(timesheetService.getStats(1L, null, null)).thenReturn(stats);

        // Act
        Result<Object> result = timesheetController.stats(1L, null, null);

        // Assert
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    // ==================== Approve Timesheet Tests ====================

    @Test
    void approve_shouldReturnSuccess_whenPermissionGranted() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 8);
        when(timesheetService.getById(1L)).thenReturn(timesheet);
        when(rolePermissionService.canApproveTimesheet(2L, "PRJ_001")).thenReturn(true);

        // Act
        Result<Void> result = timesheetController.approve(1L, 2L);

        // Assert
        assertEquals(200, result.getCode());
        verify(timesheetService).approve(1L, 2L);
    }

    @Test
    void approve_shouldReturnError_whenTimesheetNotFound() {
        // Arrange
        when(timesheetService.getById(999L)).thenReturn(null);

        // Act
        Result<Void> result = timesheetController.approve(999L, 1L);

        // Assert
        assertEquals(500, result.getCode());
        assertEquals("Timesheet not found", result.getMessage());
    }

    @Test
    void approve_shouldReturnForbidden_whenNoPermission() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 8);
        when(timesheetService.getById(1L)).thenReturn(timesheet);
        when(rolePermissionService.canApproveTimesheet(2L, "PRJ_001")).thenReturn(false);

        // Act
        Result<Void> result = timesheetController.approve(1L, 2L);

        // Assert
        assertEquals(403, result.getCode());
    }

    // ==================== Reject Timesheet Tests ====================

    @Test
    void reject_shouldReturnSuccess_whenPermissionGranted() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 8);
        when(timesheetService.getById(1L)).thenReturn(timesheet);
        when(rolePermissionService.canApproveTimesheet(2L, "PRJ_001")).thenReturn(true);

        // Act
        Result<Void> result = timesheetController.reject(1L, 2L, "Invalid hours");

        // Assert
        assertEquals(200, result.getCode());
        verify(timesheetService).reject(1L, 2L, "Invalid hours");
    }

    @Test
    void reject_shouldReturnError_whenTimesheetNotFound() {
        // Arrange
        when(timesheetService.getById(999L)).thenReturn(null);

        // Act
        Result<Void> result = timesheetController.reject(999L, 1L, "reason");

        // Assert
        assertEquals(500, result.getCode());
        assertEquals("Timesheet not found", result.getMessage());
    }

    @Test
    void reject_shouldReturnForbidden_whenNoPermission() {
        // Arrange
        Timesheet timesheet = createTimesheet(1L, 1L, "PRJ_001", 8);
        when(timesheetService.getById(1L)).thenReturn(timesheet);
        when(rolePermissionService.canApproveTimesheet(2L, "PRJ_001")).thenReturn(false);

        // Act
        Result<Void> result = timesheetController.reject(1L, 2L, "reason");

        // Assert
        assertEquals(403, result.getCode());
    }

    // ==================== Pending Approval Tests ====================

    @Test
    void getPendingApproval_shouldReturnTimesheetList() {
        // Arrange
        List<Timesheet> timesheets = Collections.singletonList(
            createTimesheet(1L, 1L, "PRJ_001", 8)
        );
        when(timesheetService.listPendingByProject("PRJ_001")).thenReturn(timesheets);

        // Act
        Result<List<Timesheet>> result = timesheetController.getPendingApproval("PRJ_001");

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ==================== Resubmit Timesheet Tests ====================

    @Test
    void resubmit_shouldCallServiceAndReturnSuccess() {
        // Act
        Result<Void> result = timesheetController.resubmit(1L);

        // Assert
        assertEquals(200, result.getCode());
        verify(timesheetService).resubmit(1L);
    }

    // ==================== Helper Methods ====================

    private Timesheet createTimesheet(Long id, Long userId, String projectId, Integer hours) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(id);
        timesheet.setUserId(userId);
        timesheet.setProjectId(projectId);
        timesheet.setHours(hours);
        timesheet.setWorkDate(LocalDateTime.now());
        timesheet.setApprovalStatus(1); // 1: pending
        return timesheet;
    }
}
