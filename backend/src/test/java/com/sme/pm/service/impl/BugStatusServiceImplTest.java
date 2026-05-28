package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.BugStatus;
import com.sme.pm.entity.BugStatusTransition;
import com.sme.pm.mapper.BugStatusMapper;
import com.sme.pm.mapper.BugStatusTransitionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BugStatusServiceImplTest {

    @Mock
    private BugStatusMapper bugStatusMapper;

    @Mock
    private BugStatusTransitionMapper transitionMapper;

    private BugStatusServiceImpl bugStatusService;

    @BeforeEach
    void setUp() throws Exception {
        bugStatusService = new BugStatusServiceImpl(transitionMapper);

        // Inject mock baseMapper using reflection
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(bugStatusService, bugStatusMapper);
    }

    @Test
    void getByProjectId_shouldReturnProjectStatuses_whenExist() {
        // Arrange
        String projectId = "PRJ_001";
        List<BugStatus> projectStatuses = Arrays.asList(
                createStatus(1L, projectId, "OPEN", "Open", "待办", "#EF4444", 1),
                createStatus(2L, projectId, "IN_PROGRESS", "In Progress", "修复中", "#F59E0B", 2)
        );

        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(projectStatuses);

        // Act
        List<BugStatus> result = bugStatusService.getByProjectId(projectId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("OPEN", result.get(0).getCode());
    }

    @Test
    void getByProjectId_shouldReturnDefaultStatuses_whenNoProjectStatuses() {
        String projectId = "PRJ_001";

        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<BugStatus> result = bugStatusService.getByProjectId(projectId);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(5, result.size());
    }

    @Test
    void getDefaultStatuses_shouldReturnFromDb_whenDbHasData() {
        // Arrange
        BugStatus dbStatus = createStatus(1L, null, "CUSTOM", "Custom", "自定义", "#FFFFFF", 1);
        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(dbStatus));

        // Act
        List<BugStatus> result = bugStatusService.getDefaultStatuses();

        // Assert
        assertEquals(1, result.size());
        assertEquals("CUSTOM", result.get(0).getCode());
    }

    @Test
    void getDefaultStatuses_shouldReturnHardcodedDefaults_whenDbEmpty() {
        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<BugStatus> result = bugStatusService.getDefaultStatuses();

        // Assert
        assertEquals(5, result.size());
        assertEquals("OPEN", result.get(0).getCode());
        assertEquals("IN_PROGRESS", result.get(1).getCode());
    }

    @Test
    void canTransition_shouldReturnTrue_whenProjectTransitionExists() {
        String projectId = "PRJ_001";

        when(transitionMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        // Act
        boolean result = bugStatusService.canTransition(projectId, "OPEN", "IN_PROGRESS");

        // Assert
        assertTrue(result);
    }

    @Test
    void canTransition_shouldReturnTrue_whenDefaultTransitionExists() {
        String projectId = "PRJ_001";

        // First call returns false (no project transition), second call returns true (default transition)
        when(transitionMapper.exists(any(LambdaQueryWrapper.class)))
                .thenReturn(false)
                .thenReturn(true);

        // Act
        boolean result = bugStatusService.canTransition(projectId, "OPEN", "CLOSED");

        // Assert - "OPEN" -> "CLOSED" is a valid default transition
        assertTrue(result);
    }

    @Test
    void canTransition_shouldReturnFalse_whenInvalidTransition() {
        String projectId = "PRJ_001";

        when(transitionMapper.exists(any(LambdaQueryWrapper.class)))
                .thenReturn(false)
                .thenReturn(false);

        // Act - "CLOSED" -> "OPEN" is not a valid transition
        boolean result = bugStatusService.canTransition(projectId, "CLOSED", "OPEN");

        // Assert
        assertFalse(result);
    }

    @Test
    void getAllowedTransitions_shouldReturnProjectTransitions_whenExist() {
        String projectId = "PRJ_001";
        List<BugStatusTransition> transitions = Arrays.asList(
                createTransition(1L, projectId, "OPEN", "IN_PROGRESS"),
                createTransition(2L, projectId, "OPEN", "CLOSED")
        );

        when(transitionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(transitions);

        // Act
        List<String> result = bugStatusService.getAllowedTransitions(projectId, "OPEN");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("IN_PROGRESS"));
        assertTrue(result.contains("CLOSED"));
    }

    @Test
    void getAllowedTransitions_shouldReturnDefaultTransitions_whenNoProjectTransitions() {
        String projectId = "PRJ_001";

        when(transitionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<String> result = bugStatusService.getAllowedTransitions(projectId, "OPEN");

        // Assert - Default transitions from OPEN are IN_PROGRESS and CLOSED
        assertEquals(2, result.size());
        assertTrue(result.contains("IN_PROGRESS"));
        assertTrue(result.contains("CLOSED"));
    }

    @Test
    void initializeForProject_shouldNotInitialize_whenAlreadyExists() {
        String projectId = "PRJ_001";

        when(bugStatusMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act
        bugStatusService.initializeForProject(projectId);

        // Assert
        verify(bugStatusMapper, never()).insert(any(BugStatus.class));
        verify(transitionMapper, never()).insert(any(BugStatusTransition.class));
    }

    @Test
    void initializeForProject_shouldCreateStatusesAndTransitions() {
        String projectId = "PRJ_NEW";

        when(bugStatusMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(bugStatusMapper.insert(any(BugStatus.class))).thenReturn(1);
        when(transitionMapper.insert(any(BugStatusTransition.class))).thenReturn(1);

        // Act
        bugStatusService.initializeForProject(projectId);

        // Assert - Should insert 5 statuses
        verify(bugStatusMapper, times(5)).insert(any(BugStatus.class));
        // Should insert 8 transitions
        verify(transitionMapper, times(8)).insert(any(BugStatusTransition.class));
    }

    private BugStatus createStatus(Long id, String projectId, String code, String nameEn, String nameZh, String color, int sortOrder) {
        BugStatus status = new BugStatus();
        status.setId(id);
        status.setProjectId(projectId);
        status.setCode(code);
        status.setNameEn(nameEn);
        status.setNameZh(nameZh);
        status.setColor(color);
        status.setSortOrder(sortOrder);
        return status;
    }

    private BugStatusTransition createTransition(Long id, String projectId, String fromStatus, String toStatus) {
        BugStatusTransition transition = new BugStatusTransition();
        transition.setId(id);
        transition.setProjectId(projectId);
        transition.setFromStatus(fromStatus);
        transition.setToStatus(toStatus);
        return transition;
    }
}
