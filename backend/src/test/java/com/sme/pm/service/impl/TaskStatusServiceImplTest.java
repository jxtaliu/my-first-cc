package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
class TaskStatusServiceImplTest {

    @Mock
    private TaskStatusMapper taskStatusMapper;

    @Mock
    private DictCodeMapper dictCodeMapper;

    private TaskStatusServiceImpl taskStatusService;

    @BeforeEach
    void setUp() throws Exception {
        taskStatusService = new TaskStatusServiceImpl();

        // Inject mock baseMapper using reflection (ServiceImpl uses @Autowired)
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(taskStatusService, taskStatusMapper);

        // Inject mock dictCodeMapper
        Field dictCodeMapperField = TaskStatusServiceImpl.class.getDeclaredField("dictCodeMapper");
        dictCodeMapperField.setAccessible(true);
        dictCodeMapperField.set(taskStatusService, dictCodeMapper);
    }

    @Test
    void findByProjectId_shouldReturnStatuses() {
        String projectId = "PRJ_001";
        TaskStatus status1 = new TaskStatus();
        status1.setId(1L);
        status1.setProjectId(projectId);
        status1.setCode("TODO");
        status1.setNameEn("To Do");

        TaskStatus status2 = new TaskStatus();
        status2.setId(2L);
        status2.setProjectId(projectId);
        status2.setCode("DONE");
        status2.setNameEn("Done");

        when(taskStatusMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(status1, status2));

        List<TaskStatus> result = taskStatusService.findByProjectId(projectId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TODO", result.get(0).getCode());
        assertEquals("DONE", result.get(1).getCode());
    }

    @Test
    void findByProjectId_shouldReturnEmptyList_whenNoStatuses() {
        when(taskStatusMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        List<TaskStatus> result = taskStatusService.findByProjectId("PRJ_999");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findSystemDefaults_shouldReturnStatusesWithNullProjectId() {
        TaskStatus status1 = new TaskStatus();
        status1.setId(1L);
        status1.setProjectId(null);
        status1.setCode("TODO");
        status1.setNameEn("To Do");

        when(taskStatusMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(status1));

        List<TaskStatus> result = taskStatusService.findSystemDefaults();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getProjectId());
    }

    @Test
    void findByCode_shouldReturnStatus() {
        String projectId = "PRJ_001";
        String code = "TODO";
        TaskStatus status = new TaskStatus();
        status.setId(1L);
        status.setProjectId(projectId);
        status.setCode(code);
        status.setNameEn("To Do");

        // Use doReturn for selectOne to avoid argument mismatch with boolean second param
        doReturn(status).when(taskStatusMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());

        TaskStatus result = taskStatusService.findByCode(projectId, code);

        assertNotNull(result);
        assertEquals("TODO", result.getCode());
    }

    @Test
    void findByCode_shouldReturnNull_whenNotFound() {
        doReturn(null).when(taskStatusMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());

        TaskStatus result = taskStatusService.findByCode("PRJ_999", "NONEXISTENT");

        assertNull(result);
    }

    @Test
    void initializeFromDict_shouldNotInitialize_whenStatusesAlreadyExist() {
        String projectId = "PRJ_001";
        TaskStatus existingStatus = new TaskStatus();
        existingStatus.setId(1L);
        existingStatus.setProjectId(projectId);

        when(taskStatusMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(existingStatus));

        taskStatusService.initializeFromDict(projectId);

        verify(dictCodeMapper, never()).findByTypeCode(any());
        verify(taskStatusMapper, never()).insert(any(TaskStatus.class));
    }

    @Test
    void reorder_shouldUpdateSortOrder() {
        List<Long> statusIds = Arrays.asList(3L, 1L, 2L);

        when(taskStatusMapper.updateById(any(TaskStatus.class))).thenReturn(1);

        taskStatusService.reorder(statusIds);

        verify(taskStatusMapper, times(3)).updateById(any(TaskStatus.class));

        ArgumentCaptor<TaskStatus> captor = ArgumentCaptor.forClass(TaskStatus.class);
        verify(taskStatusMapper, times(3)).updateById(captor.capture());

        List<TaskStatus> capturedStatuses = captor.getAllValues();
        assertEquals(3, capturedStatuses.size());
        // First item in list (id=3) should have sortOrder=1
        assertEquals(3L, capturedStatuses.get(0).getId());
        assertEquals(1, capturedStatuses.get(0).getSortOrder());
        // Second item (id=1) should have sortOrder=2
        assertEquals(1L, capturedStatuses.get(1).getId());
        assertEquals(2, capturedStatuses.get(1).getSortOrder());
    }

    @Test
    void reorder_shouldDoNothing_whenEmptyList() {
        taskStatusService.reorder(Collections.emptyList());

        verify(taskStatusMapper, never()).updateById(any());
    }

    @Test
    void reorder_shouldDoNothing_whenNullList() {
        taskStatusService.reorder(null);

        verify(taskStatusMapper, never()).updateById(any());
    }

    @Test
    void initializeFromDict_shouldCreateStatusesFromDict() {
        when(taskStatusMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        DictCode dictCode = new DictCode();
        dictCode.setCode("TODO");
        dictCode.setName("To Do");
        dictCode.setNameEn("To Do");
        dictCode.setNameZh("待办");
        dictCode.setSortOrder(1);

        when(dictCodeMapper.findByTypeCode("task_status"))
            .thenReturn(Collections.singletonList(dictCode));

        taskStatusService.initializeFromDict("PRJ_TEST");

        ArgumentCaptor<TaskStatus> captor = ArgumentCaptor.forClass(TaskStatus.class);
        verify(taskStatusMapper).insert(captor.capture());

        TaskStatus savedStatus = captor.getValue();
        assertEquals("PRJ_TEST", savedStatus.getProjectId());
    }
}
