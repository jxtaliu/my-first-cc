package com.sme.pm.service.impl;

import com.sme.pm.entity.Milestone;
import com.sme.pm.mapper.MilestoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MilestoneServiceImpl 单元测试
 *
 * 测试场景：里程碑服务的各项功能
 * - 查询所有里程碑
 * - 查询跨项目里程碑
 * - 根据项目ID查询里程碑
 * - 查询即将到期的里程碑
 * - 完成里程碑
 */
@ExtendWith(MockitoExtension.class)
class MilestoneServiceImplTest {

    @Mock
    private MilestoneMapper milestoneMapper;

    @InjectMocks
    private MilestoneServiceImpl milestoneService;

    private Milestone testMilestone1;
    private Milestone testMilestone2;
    private Milestone crossProjectMilestone;

    @BeforeEach
    void setUp() {
        // Inject the mock mapper into the service's baseMapper field
        ReflectionTestUtils.setField(milestoneService, "baseMapper", milestoneMapper);

        testMilestone1 = new Milestone();
        testMilestone1.setId(1L);
        testMilestone1.setName("Milestone 1");
        testMilestone1.setDescription("First milestone");
        testMilestone1.setTargetDate(LocalDate.now().plusDays(7));
        testMilestone1.setIsCrossProject(0);
        testMilestone1.setProjectId("PRJ_001");
        testMilestone1.setStatus("ACTIVE");
        testMilestone1.setCreatedAt(LocalDateTime.now());

        testMilestone2 = new Milestone();
        testMilestone2.setId(2L);
        testMilestone2.setName("Milestone 2");
        testMilestone2.setDescription("Second milestone");
        testMilestone2.setTargetDate(LocalDate.now().plusDays(14));
        testMilestone2.setIsCrossProject(0);
        testMilestone2.setProjectId("PRJ_001");
        testMilestone2.setStatus("PLANNING");
        testMilestone2.setCreatedAt(LocalDateTime.now());

        crossProjectMilestone = new Milestone();
        crossProjectMilestone.setId(3L);
        crossProjectMilestone.setName("Cross-Project Milestone");
        crossProjectMilestone.setDescription("Affects multiple projects");
        crossProjectMilestone.setTargetDate(LocalDate.now().plusDays(21));
        crossProjectMilestone.setIsCrossProject(1);
        crossProjectMilestone.setProjectId(null);
        crossProjectMilestone.setStatus("ACTIVE");
        crossProjectMilestone.setCreatedAt(LocalDateTime.now());
    }

    // ==================== findAll Tests ====================

    /**
     * 测试场景：查询所有里程碑 - 存在多条里程碑
     * 预期：返回所有未删除的里程碑，按目标日期升序排列
     */
    @Test
    void findAll_shouldReturnAllMilestones() {
        // Arrange
        List<Milestone> milestones = Arrays.asList(testMilestone1, testMilestone2);
        when(milestoneMapper.findAll()).thenReturn(milestones);

        // Act
        List<Milestone> result = milestoneService.findAll();

        // Assert
        assertEquals(2, result.size());
        verify(milestoneMapper).findAll();
    }

    /**
     * 测试场景：查询所有里程碑 - 无里程碑记录
     * 预期：返回空列表
     */
    @Test
    void findAll_shouldReturnEmptyList_whenNoMilestones() {
        // Arrange
        when(milestoneMapper.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Milestone> result = milestoneService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(milestoneMapper).findAll();
    }

    // ==================== findCrossProject Tests ====================

    /**
     * 测试场景：查询跨项目里程碑 - 存在跨项目里程碑
     * 预期：返回所有跨项目里程碑
     */
    @Test
    void findCrossProject_shouldReturnCrossProjectMilestones() {
        // Arrange
        List<Milestone> milestones = Collections.singletonList(crossProjectMilestone);
        when(milestoneMapper.findCrossProject()).thenReturn(milestones);

        // Act
        List<Milestone> result = milestoneService.findCrossProject();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getIsCrossProject());
        verify(milestoneMapper).findCrossProject();
    }

    /**
     * 测试场景：查询跨项目里程碑 - 无跨项目里程碑
     * 预期：返回空列表
     */
    @Test
    void findCrossProject_shouldReturnEmptyList_whenNoneExist() {
        // Arrange
        when(milestoneMapper.findCrossProject()).thenReturn(Collections.emptyList());

        // Act
        List<Milestone> result = milestoneService.findCrossProject();

        // Assert
        assertTrue(result.isEmpty());
        verify(milestoneMapper).findCrossProject();
    }

    // ==================== findByProjectId Tests ====================

    /**
     * 测试场景：根据项目ID查询里程碑 - 存在多个里程碑
     * 预期：返回指定项目的所有里程碑
     */
    @Test
    void findByProjectId_shouldReturnProjectMilestones() {
        // Arrange
        String projectId = "PRJ_001";
        List<Milestone> milestones = Arrays.asList(testMilestone1, testMilestone2);
        when(milestoneMapper.findByProjectId(projectId)).thenReturn(milestones);

        // Act
        List<Milestone> result = milestoneService.findByProjectId(projectId);

        // Assert
        assertEquals(2, result.size());
        verify(milestoneMapper).findByProjectId(projectId);
    }

    /**
     * 测试场景：根据项目ID查询里程碑 - 项目下无里程碑
     * 预期：返回空列表
     */
    @Test
    void findByProjectId_shouldReturnEmptyList_whenNoMilestones() {
        // Arrange
        String projectId = "PRJ_999";
        when(milestoneMapper.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        // Act
        List<Milestone> result = milestoneService.findByProjectId(projectId);

        // Assert
        assertTrue(result.isEmpty());
        verify(milestoneMapper).findByProjectId(projectId);
    }

    // ==================== findDueSoon Tests ====================

    /**
     * 测试场景：查询即将到期的里程碑 - 在指定天数内存在里程碑
     * 预期：返回目标日期在 today 到 today+days 范围内的里程碑
     */
    @Test
    void findDueSoon_shouldReturnMilestonesWithinDays() {
        // Arrange
        int days = 7;
        List<Milestone> milestones = Collections.singletonList(testMilestone1);
        when(milestoneMapper.findDueSoon(any(LocalDate.class))).thenReturn(milestones);

        // Act
        List<Milestone> result = milestoneService.findDueSoon(days);

        // Assert
        assertEquals(1, result.size());
        verify(milestoneMapper).findDueSoon(any(LocalDate.class));
    }

    /**
     * 测试场景：查询即将到期的里程碑 - 无即将到期的里程碑
     * 预期：返回空列表
     */
    @Test
    void findDueSoon_shouldReturnEmptyList_whenNoMilestonesDue() {
        // Arrange
        int days = 3;
        when(milestoneMapper.findDueSoon(any(LocalDate.class))).thenReturn(Collections.emptyList());

        // Act
        List<Milestone> result = milestoneService.findDueSoon(days);

        // Assert
        assertTrue(result.isEmpty());
        verify(milestoneMapper).findDueSoon(any(LocalDate.class));
    }

    /**
     * 测试场景：查询即将到期的里程碑 - days 参数为 0
     * 预期：只返回今天到期的里程碑
     */
    @Test
    void findDueSoon_shouldReturnTodayOnly_whenDaysIsZero() {
        // Arrange
        int days = 0;
        when(milestoneMapper.findDueSoon(any(LocalDate.class))).thenReturn(Collections.emptyList());

        // Act
        milestoneService.findDueSoon(days);

        // Assert
        verify(milestoneMapper).findDueSoon(any(LocalDate.class));
    }

    // ==================== completeMilestone Tests ====================

    /**
     * 测试场景：完成里程碑 - 里程碑存在
     * 预期：里程碑状态设置为 COMPLETED，调用 updateById
     */
    @Test
    void completeMilestone_shouldSetStatusToCompleted() {
        // Arrange
        Long milestoneId = 1L;
        when(milestoneMapper.findById(milestoneId)).thenReturn(testMilestone1);
        when(milestoneMapper.updateById(testMilestone1)).thenReturn(1);

        // Act
        milestoneService.completeMilestone(milestoneId);

        // Assert
        assertEquals("COMPLETED", testMilestone1.getStatus());
        verify(milestoneMapper).findById(milestoneId);
        verify(milestoneMapper).updateById(testMilestone1);
    }

    /**
     * 测试场景：完成里程碑 - 里程碑不存在
     * 预期：抛出 IllegalArgumentException
     */
    @Test
    void completeMilestone_shouldThrowException_whenNotFound() {
        // Arrange
        Long milestoneId = 999L;
        when(milestoneMapper.findById(milestoneId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> milestoneService.completeMilestone(milestoneId)
        );
        assertTrue(exception.getMessage().contains("Milestone not found"));
        verify(milestoneMapper, never()).updateById(any(Milestone.class));
    }

    /**
     * 测试场景：完成里程碑 - 里程碑已处于 COMPLETED 状态
     * 预期：仍然调用 updateById，状态保持 COMPLETED
     */
    @Test
    void completeMilestone_shouldUpdateEvenIfAlreadyCompleted() {
        // Arrange
        Long milestoneId = 1L;
        testMilestone1.setStatus("COMPLETED");
        when(milestoneMapper.findById(milestoneId)).thenReturn(testMilestone1);
        when(milestoneMapper.updateById(testMilestone1)).thenReturn(1);

        // Act
        milestoneService.completeMilestone(milestoneId);

        // Assert
        assertEquals("COMPLETED", testMilestone1.getStatus());
        verify(milestoneMapper).updateById(testMilestone1);
    }
}
