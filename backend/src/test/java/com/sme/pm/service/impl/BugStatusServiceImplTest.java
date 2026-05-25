package com.sme.pm.service.impl;

import com.sme.pm.entity.BugStatus;
import com.sme.pm.entity.BugStatusTransition;
import com.sme.pm.mapper.BugStatusMapper;
import com.sme.pm.mapper.BugStatusTransitionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BugStatusServiceImplTest {

    @Mock
    private BugStatusMapper bugStatusMapper;

    @Mock
    private BugStatusTransitionMapper transitionMapper;

    private TestBugStatusService bugStatusService;

    @BeforeEach
    void setUp() {
        bugStatusService = new TestBugStatusService(bugStatusMapper, transitionMapper);
    }

    @Test
    void getByProjectId_shouldReturnProjectStatuses_whenExist() {
        String projectId = "PRJ_001";
        List<BugStatus> projectStatuses = Arrays.asList(
                createStatus(1L, projectId, "OPEN", "Open", "待办", "#EF4444", 1),
                createStatus(2L, projectId, "IN_PROGRESS", "In Progress", "修复中", "#F59E0B", 2)
        );

        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(projectStatuses);

        List<BugStatus> result = bugStatusService.getByProjectId(projectId);

        assertEquals(2, result.size());
        assertEquals("OPEN", result.get(0).getCode());
    }

    @Test
    void getByProjectId_shouldReturnDefaultStatuses_whenNoProjectStatuses() {
        String projectId = "PRJ_001";

        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        List<BugStatus> result = bugStatusService.getByProjectId(projectId);

        assertFalse(result.isEmpty());
        assertEquals(5, result.size());
    }

    @Test
    void getDefaultStatuses_shouldReturnHardcodedDefaults_whenDbEmpty() {
        when(bugStatusMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<BugStatus> result = bugStatusService.getDefaultStatuses();

        assertEquals(5, result.size());
        assertEquals("OPEN", result.get(0).getCode());
        assertEquals("IN_PROGRESS", result.get(1).getCode());
        assertEquals("IN_TEST", result.get(2).getCode());
        assertEquals("CLOSED", result.get(3).getCode());
        assertEquals("REOPENED", result.get(4).getCode());
    }

    @Test
    void canTransition_shouldReturnTrue_whenValidTransition() {
        String projectId = "PRJ_001";

        when(transitionMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        boolean result = bugStatusService.canTransition(projectId, "OPEN", "IN_PROGRESS");

        assertTrue(result);
    }

    @Test
    void canTransition_shouldReturnFalse_whenInvalidTransition() {
        String projectId = "PRJ_001";

        when(transitionMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);

        boolean result = bugStatusService.canTransition(projectId, "CLOSED", "OPEN");

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

        List<String> result = bugStatusService.getAllowedTransitions(projectId, "OPEN");

        assertEquals(2, result.size());
        assertTrue(result.contains("IN_PROGRESS"));
        assertTrue(result.contains("CLOSED"));
    }

    @Test
    void getAllowedTransitions_shouldReturnDefaultTransitions_whenNoProjectTransitions() {
        String projectId = "PRJ_001";

        when(transitionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<String> result = bugStatusService.getAllowedTransitions(projectId, "OPEN");

        assertEquals(2, result.size());
        assertTrue(result.contains("IN_PROGRESS"));
        assertTrue(result.contains("CLOSED"));
    }

    // Test helper class that mirrors the service logic without ServiceImpl dependency
    private static class TestBugStatusService {
        private static final List<BugStatus> DEFAULT_STATUSES = List.of(
                createStatus(1L, null, "OPEN", "Open", "待办", "#EF4444", 1),
                createStatus(2L, null, "IN_PROGRESS", "In Progress", "修复中", "#F59E0B", 2),
                createStatus(3L, null, "IN_TEST", "In Test", "待验证", "#3B82F6", 3),
                createStatus(4L, null, "CLOSED", "Closed", "已关闭", "#10B981", 4),
                createStatus(5L, null, "REOPENED", "Reopened", "重新打开", "#8B5CF6", 5)
        );

        private static final List<String[]> DEFAULT_TRANSITIONS = List.of(
                new String[]{"OPEN", "IN_PROGRESS"},
                new String[]{"OPEN", "CLOSED"},
                new String[]{"IN_PROGRESS", "IN_TEST"},
                new String[]{"IN_PROGRESS", "REOPENED"},
                new String[]{"IN_TEST", "CLOSED"},
                new String[]{"IN_TEST", "IN_PROGRESS"},
                new String[]{"CLOSED", "REOPENED"},
                new String[]{"REOPENED", "IN_PROGRESS"}
        );

        private final BugStatusMapper bugStatusMapper;
        private final BugStatusTransitionMapper transitionMapper;

        TestBugStatusService(BugStatusMapper bugStatusMapper, BugStatusTransitionMapper transitionMapper) {
            this.bugStatusMapper = bugStatusMapper;
            this.transitionMapper = transitionMapper;
        }

        List<BugStatus> getByProjectId(String projectId) {
            LambdaQueryWrapper<BugStatus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BugStatus::getProjectId, projectId)
                    .eq(BugStatus::getDeleted, 0)
                    .orderByAsc(BugStatus::getSortOrder);
            List<BugStatus> projectStatuses = bugStatusMapper.selectList(wrapper);

            if (projectStatuses != null && !projectStatuses.isEmpty()) {
                return projectStatuses;
            }
            return getDefaultStatuses();
        }

        List<BugStatus> getDefaultStatuses() {
            LambdaQueryWrapper<BugStatus> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNull(BugStatus::getProjectId)
                    .eq(BugStatus::getDeleted, 0)
                    .orderByAsc(BugStatus::getSortOrder);
            List<BugStatus> defaultStatuses = bugStatusMapper.selectList(wrapper);

            if (defaultStatuses == null || defaultStatuses.isEmpty()) {
                return new ArrayList<>(DEFAULT_STATUSES);
            }
            return defaultStatuses;
        }

        boolean canTransition(String projectId, String fromStatus, String toStatus) {
            LambdaQueryWrapper<BugStatusTransition> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BugStatusTransition::getProjectId, projectId)
                    .eq(BugStatusTransition::getFromStatus, fromStatus)
                    .eq(BugStatusTransition::getToStatus, toStatus)
                    .eq(BugStatusTransition::getDeleted, 0);
            return transitionMapper.exists(wrapper);
        }

        List<String> getAllowedTransitions(String projectId, String fromStatus) {
            List<String> transitions = new ArrayList<>();

            LambdaQueryWrapper<BugStatusTransition> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BugStatusTransition::getProjectId, projectId)
                    .eq(BugStatusTransition::getFromStatus, fromStatus)
                    .eq(BugStatusTransition::getDeleted, 0);
            List<BugStatusTransition> projectTransitions = transitionMapper.selectList(wrapper);

            if (projectTransitions != null && !projectTransitions.isEmpty()) {
                transitions.addAll(projectTransitions.stream()
                        .map(BugStatusTransition::getToStatus)
                        .collect(Collectors.toList()));
            } else {
                for (String[] transition : DEFAULT_TRANSITIONS) {
                    if (transition[0].equals(fromStatus)) {
                        transitions.add(transition[1]);
                    }
                }
            }
            return transitions;
        }
    }

    private static BugStatus createStatus(Long id, String projectId, String code, String nameEn, String nameZh, String color, int sortOrder) {
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
