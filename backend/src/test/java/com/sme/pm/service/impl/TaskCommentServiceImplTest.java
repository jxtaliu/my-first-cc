package com.sme.pm.service.impl;

import com.sme.pm.entity.TaskComment;
import com.sme.pm.mapper.TaskCommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class TaskCommentServiceImplTest {

    @Mock
    private TaskCommentMapper taskCommentMapper;

    @InjectMocks
    private TaskCommentServiceImpl taskCommentService;

    private TaskComment testComment1;
    private TaskComment testComment2;
    private TaskComment testReply;

    @BeforeEach
    void setUp() {
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
        when(taskCommentMapper.findByTaskId(taskId)).thenReturn(comments);

        // Act
        List<TaskComment> result = taskCommentService.findByTaskId(taskId);

        // Assert
        assertEquals(2, result.size());
        verify(taskCommentMapper).findByTaskId(taskId);
    }

    /**
     * 测试场景：根据任务ID查询评论列表 - 任务无评论
     * 预期：返回空列表
     */
    @Test
    void findByTaskId_shouldReturnEmptyList_whenNoComments() {
        // Arrange
        Long taskId = 999L;
        when(taskCommentMapper.findByTaskId(taskId)).thenReturn(Collections.emptyList());

        // Act
        List<TaskComment> result = taskCommentService.findByTaskId(taskId);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskCommentMapper).findByTaskId(taskId);
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
        when(taskCommentMapper.findByTaskId(taskId)).thenReturn(comments);

        // Act
        List<TaskComment> result = taskCommentService.findByTaskId(taskId);

        // Assert
        assertEquals(3, result.size());
        verify(taskCommentMapper).findByTaskId(taskId);
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
        when(taskCommentMapper.findByParentId(parentId)).thenReturn(replies);

        // Act
        List<TaskComment> result = taskCommentService.findByParentId(parentId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(parentId, result.get(0).getParentCommentId());
        verify(taskCommentMapper).findByParentId(parentId);
    }

    /**
     * 测试场景：根据父评论ID查询回复列表 - 无回复
     * 预期：返回空列表
     */
    @Test
    void findByParentId_shouldReturnEmptyList_whenNoReplies() {
        // Arrange
        Long parentId = 2L;
        when(taskCommentMapper.findByParentId(parentId)).thenReturn(Collections.emptyList());

        // Act
        List<TaskComment> result = taskCommentService.findByParentId(parentId);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskCommentMapper).findByParentId(parentId);
    }

    /**
     * 测试场景：根据父评论ID查询回复列表 - 顶级评论（parentId 为 null 的评论）
     * 预期：返回顶级评论（实际上这是错误的用法，但服务层没有限制）
     */
    @Test
    void findByParentId_shouldReturnEmptyList_whenParentIsNull() {
        // Arrange
        Long parentId = null;
        when(taskCommentMapper.findByParentId(parentId)).thenReturn(Collections.emptyList());

        // Act
        List<TaskComment> result = taskCommentService.findByParentId(parentId);

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
        when(taskCommentMapper.findById(commentId)).thenReturn(testComment1);

        // Act
        TaskComment result = taskCommentService.findById(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("This is the first comment", result.getContent());
        verify(taskCommentMapper).findById(commentId);
    }

    /**
     * 测试场景：根据ID查询评论 - 评论不存在
     * 预期：返回 null
     */
    @Test
    void findById_shouldReturnNull_whenNotFound() {
        // Arrange
        Long commentId = 999L;
        when(taskCommentMapper.findById(commentId)).thenReturn(null);

        // Act
        TaskComment result = taskCommentService.findById(commentId);

        // Assert
        assertNull(result);
        verify(taskCommentMapper).findById(commentId);
    }

    /**
     * 测试场景：根据ID查询评论 - 查询回复
     * 预期：返回回复详情，包含父评论ID
     */
    @Test
    void findById_shouldReturnReply() {
        // Arrange
        Long commentId = 3L;
        when(taskCommentMapper.findById(commentId)).thenReturn(testReply);

        // Act
        TaskComment result = taskCommentService.findById(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(1L, result.getParentCommentId());
        assertEquals("This is a reply to comment 1", result.getContent());
        verify(taskCommentMapper).findById(commentId);
    }
}
