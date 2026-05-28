package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.StatusTransition;
import com.sme.pm.mapper.StatusTransitionMapper;
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
class StatusTransitionServiceImplTest {

    @Mock
    private StatusTransitionMapper statusTransitionMapper;

    private StatusTransitionServiceImpl statusTransitionService;

    @BeforeEach
    void setUp() throws Exception {
        statusTransitionService = new StatusTransitionServiceImpl();

        // Inject mock baseMapper using reflection
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(statusTransitionService, statusTransitionMapper);
    }

    @Test
    void findByProjectId_shouldReturnTransitions() {
        // Arrange
        String projectId = "PRJ_001";
        StatusTransition transition1 = new StatusTransition();
        transition1.setId(1L);
        transition1.setProjectId(projectId);
        transition1.setFromStatusId(1L);
        transition1.setToStatusId(2L);

        StatusTransition transition2 = new StatusTransition();
        transition2.setId(2L);
        transition2.setProjectId(projectId);
        transition2.setFromStatusId(2L);
        transition2.setToStatusId(3L);

        when(statusTransitionMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(transition1, transition2));

        // Act
        List<StatusTransition> result = statusTransitionService.findByProjectId(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void findByProjectId_shouldReturnEmptyList_whenNoTransitions() {
        // Arrange
        when(statusTransitionMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // Act
        List<StatusTransition> result = statusTransitionService.findByProjectId("PRJ_EMPTY");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByFromStatus_shouldReturnTransitions() {
        // Arrange
        Long fromStatusId = 1L;
        StatusTransition transition = new StatusTransition();
        transition.setId(1L);
        transition.setFromStatusId(fromStatusId);
        transition.setToStatusId(2L);

        when(statusTransitionMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(transition));

        // Act
        List<StatusTransition> result = statusTransitionService.findByFromStatus(fromStatusId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(fromStatusId, result.get(0).getFromStatusId());
    }

    @Test
    void findByFromStatus_shouldReturnEmptyList_whenNoTransitions() {
        // Arrange
        when(statusTransitionMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // Act
        List<StatusTransition> result = statusTransitionService.findByFromStatus(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void existsTransition_shouldReturnTrue_whenTransitionExists() {
        // Arrange
        when(statusTransitionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act
        boolean result = statusTransitionService.existsTransition(1L, 2L);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsTransition_shouldReturnFalse_whenTransitionDoesNotExist() {
        // Arrange
        when(statusTransitionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Act
        boolean result = statusTransitionService.existsTransition(1L, 999L);

        // Assert
        assertFalse(result);
    }
}
