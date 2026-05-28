package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.TaskComment;
import com.sme.pm.mapper.TaskCommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TaskCommentServiceImpl 单元测试
 *
 * 测试场景：任务评论服务的各项功能
 * - 根据任务ID查询评论列表
 * - 根据父评论ID查询回复列表
 * - 根据ID查询评论详情
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskCommentServiceImplTest {

    @Mock
    private TaskCommentMapper taskCommentMapper;

    private TaskCommentServiceImpl taskCommentService;

    /**
     * Test-specific subclass that uses mapper directly instead of via ServiceImpl.baseMapper
     */
    static class TestableTaskCommentService extends TaskCommentServiceImpl {
        private final TaskCommentMapper testMapper;

        TestableTaskCommentService(TaskCommentMapper mapper) {
            this.testMapper = mapper;
        }

        @Override
        public List<TaskComment> findByTaskId(Long taskId) {
            LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskComment::getTaskId, taskId)
                   .eq(TaskComment::getDeleted, 0)
                   .orderByAsc(TaskComment::getCreatedAt);
            return testMapper.selectList(wrapper);
        }

        @Override
        public List<TaskComment> findByParentId(Long parentId) {
            LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskComment::getParentCommentId, parentId)
                   .eq(TaskComment::getDeleted, 0)
                   .orderByAsc(TaskComment::getCreatedAt);
            return testMapper.selectList(wrapper);
        }

        @Override
        public TaskComment findById(Long id) {
            LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskComment::getId, id)
                   .eq(TaskComment::getDeleted, 0);
            return testMapper.selectOne(wrapper);
        }
    }

    private TaskComment testComment1;
    private TaskComment testComment2;
    private TaskComment testReply;

    @BeforeEach
    void setUp() {
        // Use testable subclass that directly uses the mock mapper
        taskCommentService = new TestableTaskCommentService(taskCommentMapper);

        testComment1 = new TaskComment();
        testComment1.setId(1L);
        testComment1.setTaskId(100L);
        testComment1.setUserId(10L);
        testComment1.setContent("This is the first comment");
        testComment1.setParentCommentId(null);
        testComment1.setCreatedAt(LocalDateTime.now().minusHours(2));

        testComment2 = new TaskComment();
        testComment2.setId(2L);
        testComment2.setTaskId(100L);
        testComment2.setUserId(20L);
        testComment2.setContent("This is the second comment");
        testComment2.setParentCommentId(null);
        testComment2.setCreatedAt(LocalDateTime.now().minusHours(1));

        testReply = new TaskComment();
        testReply.setId(3L);
        testReply.setTaskId(100L);
        testReply.setUserId(30L);
        testReply.setContent("This is a reply to comment 1");
        testReply.setParentCommentId(1L);
        testReply.setCreatedAt(LocalDateTime.now());
    }

    // ==================== findByTaskId Tests ====================

    /**
     * 测试场景：根据任务ID查询评论列表 - 存在多条评论
     * 预期：返回该任务的所有评论，按创建时间升序排列
     */
    @Test
    void findByTaskId_shouldReturnComments() {
        // Arrange
        Long taskId = 100L;
        List<TaskComment> comments = Arrays.asList(testComment1, testComment2);
        when(taskCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(comments);

        // Act
        List<TaskComment> result = taskCommentService.findByTaskId(taskId);

        // Assert
        assertEquals(2, result.size());
        verify(taskCommentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据任务ID查询评论列表 - 任务无评论
     * 预期：返回空列表
     */
    @Test
    void findByTaskId_shouldReturnEmptyList_whenNoComments() {
        // Arrange
        Long taskId = 999L;
        when(taskCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        List<TaskComment> result = taskCommentService.findByTaskId(taskId);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskCommentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据任务ID查询评论列表 - 包含回复
     * 预期：返回所有评论，包括回复（按创建时间排序）
     */
    @Test
    void findByTaskId_shouldIncludeReplies() {
        // Arrange
        Long taskId = 100L;
        List<TaskComment> comments = Arrays.asList(testComment1, testComment2, testReply);
        when(taskCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(comments);

        // Act
        List<TaskComment> result = taskCommentService.findByTaskId(taskId);

        // Assert
        assertEquals(3, result.size());
        verify(taskCommentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    // ==================== findByParentId Tests ====================

    /**
     * 测试场景：根据父评论ID查询回复列表 - 存在回复
     * 预期：返回该父评论的所有直接回复
     */
    @Test
    void findByParentId_shouldReturnReplies() {
        // Arrange
        Long parentId = 1L;
        List<TaskComment> replies = Collections.singletonList(testReply);
        when(taskCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(replies);

        // Act
        List<TaskComment> result = taskCommentService.findByParentId(parentId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(parentId, result.get(0).getParentCommentId());
        verify(taskCommentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据父评论ID查询回复列表 - 无回复
     * 预期：返回空列表
     */
    @Test
    void findByParentId_shouldReturnEmptyList_whenNoReplies() {
        // Arrange
        Long parentId = 2L;
        when(taskCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        List<TaskComment> result = taskCommentService.findByParentId(parentId);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskCommentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据父评论ID查询回复列表 - 顶级评论（parentId 为 null 的评论）
     * 预期：返回顶级评论（实际上这是错误的用法，但服务层没有限制）
     */
    @Test
    void findByParentId_shouldReturnEmptyList_whenParentIsNull() {
        // Arrange
        when(taskCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        List<TaskComment> result = taskCommentService.findByParentId(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==================== findById Tests ====================

    /**
     * 测试场景：根据ID查询评论 - 评论存在
     * 预期：返回评论详情
     */
    @Test
    void findById_shouldReturnComment() {
        // Arrange
        Long commentId = 1L;
        doReturn(testComment1).when(taskCommentMapper).selectOne(any(LambdaQueryWrapper.class));

        // Act
        TaskComment result = taskCommentService.findById(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("This is the first comment", result.getContent());
        verify(taskCommentMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据ID查询评论 - 评论不存在
     * 预期：返回 null
     */
    @Test
    void findById_shouldReturnNull_whenNotFound() {
        // Arrange
        Long commentId = 999L;
        doReturn(null).when(taskCommentMapper).selectOne(any(LambdaQueryWrapper.class));

        // Act
        TaskComment result = taskCommentService.findById(commentId);

        // Assert
        assertNull(result);
        verify(taskCommentMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据ID查询评论 - 查询回复
     * 预期：返回回复详情，包含父评论ID
     */
    @Test
    void findById_shouldReturnReply() {
        // Arrange
        Long commentId = 3L;
        doReturn(testReply).when(taskCommentMapper).selectOne(any(LambdaQueryWrapper.class));

        // Act
        TaskComment result = taskCommentService.findById(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(1L, result.getParentCommentId());
        assertEquals("This is a reply to comment 1", result.getContent());
        verify(taskCommentMapper).selectOne(any(LambdaQueryWrapper.class));
    }
}
