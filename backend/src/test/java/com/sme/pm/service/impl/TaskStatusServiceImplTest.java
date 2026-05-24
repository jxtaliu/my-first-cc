package com.sme.pm.service.impl;

import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.TaskStatus;
import com.sme.pm.mapper.DictCodeMapper;
import com.sme.pm.mapper.TaskStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStatusServiceImplTest {

    @Mock
    private TaskStatusMapper taskStatusMapper;

    @Mock
    private DictCodeMapper dictCodeMapper;

    private TaskStatusServiceImpl taskStatusService;

    @BeforeEach
    void setUp() {
        taskStatusService = new TaskStatusServiceImpl(taskStatusMapper, dictCodeMapper);
    }

    @Test
    void initializeFromDict_shouldCreateStatusesFromDict() {
        String projectId = "PRJ_001";

        DictCode dict1 = new DictCode();
        dict1.setCode("TODO");
        dict1.setName("Todo");
        dict1.setNameEn("Todo");
        dict1.setNameZh("待办");
        dict1.setSortOrder(1);
        dict1.setExtra("{\"color\": \"#909399\"}");

        DictCode dict2 = new DictCode();
        dict2.setCode("IN_PROGRESS");
        dict2.setName("In Progress");
        dict2.setNameEn("In Progress");
        dict2.setNameZh("进行中");
        dict2.setSortOrder(2);
        dict2.setExtra("{\"color\": \"#409EFF\"}");

        when(dictCodeMapper.findByTypeCode("task_status")).thenReturn(Arrays.asList(dict1, dict2));
        when(taskStatusMapper.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        taskStatusService.initializeFromDict(projectId);

        ArgumentCaptor<TaskStatus> captor = ArgumentCaptor.forClass(TaskStatus.class);
        verify(taskStatusMapper, times(2)).save(captor.capture());

        List<TaskStatus> savedStatuses = captor.getAllValues();

        assertEquals(2, savedStatuses.size());

        TaskStatus first = savedStatuses.get(0);
        assertEquals(projectId, first.getProjectId());
        assertEquals("TODO", first.getCode());
        assertEquals("Todo", first.getName());
        assertEquals("#909399", first.getColor());
        assertEquals(1, first.getSortOrder());

        TaskStatus second = savedStatuses.get(1);
        assertEquals(projectId, second.getProjectId());
        assertEquals("IN_PROGRESS", second.getCode());
        assertEquals("#409EFF", second.getColor());
    }

    @Test
    void initializeFromDict_shouldSkipWhenAlreadyInitialized() {
        String projectId = "PRJ_001";

        TaskStatus existing = new TaskStatus();
        existing.setProjectId(projectId);
        existing.setCode("TODO");

        when(taskStatusMapper.findByProjectId(projectId)).thenReturn(Arrays.asList(existing));

        taskStatusService.initializeFromDict(projectId);

        verify(taskStatusMapper, never()).save(any(TaskStatus.class));
    }

    @Test
    void initializeFromDict_shouldMapCategoryCorrectly() {
        String projectId = "PRJ_002";

        DictCode dict1 = new DictCode();
        dict1.setCode("DONE");
        dict1.setName("Done");
        dict1.setNameEn("Done");
        dict1.setNameZh("已完成");
        dict1.setSortOrder(4);
        dict1.setExtra("{\"color\": \"#67C23A\"}");

        when(dictCodeMapper.findByTypeCode("task_status")).thenReturn(Arrays.asList(dict1));
        when(taskStatusMapper.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        taskStatusService.initializeFromDict(projectId);

        ArgumentCaptor<TaskStatus> captor = ArgumentCaptor.forClass(TaskStatus.class);
        verify(taskStatusMapper).save(captor.capture());

        assertEquals("done", captor.getValue().getCategory());
    }

    @Test
    void reorder_shouldUpdateSortOrder() {
        when(taskStatusMapper.updateById(any(TaskStatus.class))).thenReturn(true);

        List<Long> statusIds = Arrays.asList(3L, 1L, 2L);

        taskStatusService.reorder(statusIds);

        verify(taskStatusMapper, times(3)).updateById(any(TaskStatus.class));
    }

    @Test
    void reorder_shouldHandleEmptyList() {
        taskStatusService.reorder(Collections.emptyList());

        verify(taskStatusMapper, never()).updateById(any(TaskStatus.class));
    }

    @Test
    void reorder_shouldHandleNull() {
        taskStatusService.reorder(null);

        verify(taskStatusMapper, never()).updateById(any(TaskStatus.class));
    }
}
