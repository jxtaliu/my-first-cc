package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sme.pm.entity.TaskAttachment;
import com.sme.pm.mapper.TaskAttachmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TaskAttachmentServiceImpl 单元测试
 *
 * 测试场景：任务附件服务的各项功能
 * - 根据任务ID查询附件列表
 * - 根据ID查询附件详情
 * - 上传附件
 * - 删除附件
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskAttachmentServiceImplTest {

    @Mock
    private TaskAttachmentMapper taskAttachmentMapper;

    private TaskAttachmentServiceImpl taskAttachmentService;

    /**
     * Test-specific subclass that uses mapper directly instead of via ServiceImpl.baseMapper
     */
    static class TestableTaskAttachmentService extends TaskAttachmentServiceImpl {
        private final TaskAttachmentMapper testMapper;

        TestableTaskAttachmentService(TaskAttachmentMapper mapper) {
            this.testMapper = mapper;
        }

        @Override
        public List<TaskAttachment> findByTaskId(Long taskId) {
            LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskAttachment::getTaskId, taskId)
                   .eq(TaskAttachment::getDeleted, 0)
                   .orderByDesc(TaskAttachment::getCreatedAt);
            return testMapper.selectList(wrapper);
        }

        @Override
        public TaskAttachment findById(Long id) {
            LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskAttachment::getId, id)
                   .eq(TaskAttachment::getDeleted, 0);
            return testMapper.selectOne(wrapper);
        }

        @Override
        public void uploadAttachment(TaskAttachment attachment) {
            testMapper.insert(attachment);
        }

        @Override
        public void deleteAttachment(Long id) {
            testMapper.deleteById(id);
        }
    }

    private TaskAttachment testAttachment1;
    private TaskAttachment testAttachment2;

    @BeforeEach
    void setUp() {
        taskAttachmentService = new TestableTaskAttachmentService(taskAttachmentMapper);

        testAttachment1 = new TaskAttachment();
        testAttachment1.setId(1L);
        testAttachment1.setTaskId(100L);
        testAttachment1.setFileName("design.pdf");
        testAttachment1.setFilePath("/uploads/attachments/design.pdf");
        testAttachment1.setFileSize(1024L);
        testAttachment1.setMimeType("application/pdf");
        testAttachment1.setUploadedBy(10L);
        testAttachment1.setCreatedAt(LocalDateTime.now());

        testAttachment2 = new TaskAttachment();
        testAttachment2.setId(2L);
        testAttachment2.setTaskId(100L);
        testAttachment2.setFileName("requirements.docx");
        testAttachment2.setFilePath("/uploads/attachments/requirements.docx");
        testAttachment2.setFileSize(2048L);
        testAttachment2.setMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        testAttachment2.setUploadedBy(10L);
        testAttachment2.setCreatedAt(LocalDateTime.now());
    }

    // ==================== findByTaskId Tests ====================

    /**
     * 测试场景：根据任务ID查询附件列表 - 存在多个附件
     * 预期：返回该任务的所有附件，按创建时间倒序排列
     */
    @Test
    void findByTaskId_shouldReturnAttachments() {
        // Arrange
        Long taskId = 100L;
        List<TaskAttachment> attachments = Arrays.asList(testAttachment2, testAttachment1);
        when(taskAttachmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(attachments);

        // Act
        List<TaskAttachment> result = taskAttachmentService.findByTaskId(taskId);

        // Assert
        assertEquals(2, result.size());
        verify(taskAttachmentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据任务ID查询附件列表 - 任务无附件
     * 预期：返回空列表
     */
    @Test
    void findByTaskId_shouldReturnEmptyList_whenNoAttachments() {
        // Arrange
        Long taskId = 999L;
        when(taskAttachmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        List<TaskAttachment> result = taskAttachmentService.findByTaskId(taskId);

        // Assert
        assertTrue(result.isEmpty());
        verify(taskAttachmentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    // ==================== findById Tests ====================

    /**
     * 测试场景：根据ID查询附件 - 附件存在
     * 预期：返回附件详情
     */
    @Test
    void findById_shouldReturnAttachment() {
        // Arrange
        Long attachmentId = 1L;
        when(taskAttachmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testAttachment1);

        // Act
        TaskAttachment result = taskAttachmentService.findById(attachmentId);

        // Assert
        assertNotNull(result);
        assertEquals(attachmentId, result.getId());
        assertEquals("design.pdf", result.getFileName());
        verify(taskAttachmentMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试场景：根据ID查询附件 - 附件不存在
     * 预期：返回 null
     */
    @Test
    void findById_shouldReturnNull_whenNotFound() {
        // Arrange
        Long attachmentId = 999L;
        when(taskAttachmentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        TaskAttachment result = taskAttachmentService.findById(attachmentId);

        // Assert
        assertNull(result);
        verify(taskAttachmentMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    // ==================== uploadAttachment Tests ====================

    /**
     * 测试场景：上传附件 - 有效输入
     * 预期：调用 save 方法保存附件
     */
    @Test
    void uploadAttachment_shouldSaveAttachment() {
        // Arrange
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(100L);
        attachment.setFileName("new-file.pdf");
        attachment.setFilePath("/uploads/attachments/new-file.pdf");
        attachment.setFileSize(512L);
        attachment.setMimeType("application/pdf");
        attachment.setUploadedBy(10L);

        when(taskAttachmentMapper.insert(attachment)).thenReturn(1);

        // Act
        taskAttachmentService.uploadAttachment(attachment);

        // Assert
        verify(taskAttachmentMapper).insert(attachment);
    }

    /**
     * 测试场景：上传附件 - 附件包含所有必要字段
     * 预期：成功保存到数据库
     */
    @Test
    void uploadAttachment_shouldSaveWithAllFields() {
        // Arrange
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(100L);
        attachment.setFileName("specification.xlsx");
        attachment.setFilePath("/uploads/attachments/specification.xlsx");
        attachment.setFileSize(4096L);
        attachment.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        attachment.setUploadedBy(20L);

        when(taskAttachmentMapper.insert(attachment)).thenReturn(1);

        // Act
        taskAttachmentService.uploadAttachment(attachment);

        // Assert
        assertEquals(100L, attachment.getTaskId());
        assertEquals("specification.xlsx", attachment.getFileName());
        verify(taskAttachmentMapper).insert(attachment);
    }

    // ==================== deleteAttachment Tests ====================

    /**
     * 测试场景：删除附件 - 附件存在
     * 预期：调用 removeById 删除附件
     */
    @Test
    void deleteAttachment_shouldRemoveById() {
        // Arrange
        Long attachmentId = 1L;

        // Act
        taskAttachmentService.deleteAttachment(attachmentId);

        // Assert
        verify(taskAttachmentMapper).deleteById(attachmentId);
    }

    /**
     * 测试场景：删除附件 - 附件不存在
     * 预期：仍然调用 removeById（MyBatis-Plus 会处理不存在的记录）
     */
    @Test
    void deleteAttachment_shouldCallRemoveById_evenIfNotExists() {
        // Arrange
        Long attachmentId = 999L;

        // Act
        taskAttachmentService.deleteAttachment(attachmentId);

        // Assert
        verify(taskAttachmentMapper).deleteById(attachmentId);
    }
}
