package com.sme.pm.service.impl;

import com.sme.pm.entity.Department;
import com.sme.pm.mapper.DepartmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;

    private DepartmentServiceImpl departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(departmentMapper);
    }

    // ==================== getAllDepartments Tests ====================

    @Test
    void getAllDepartments_shouldReturnDepartmentsWithMemberCount() {
        Department dept1 = new Department();
        dept1.setId(1L);
        dept1.setDepartmentId("DEPT001");
        dept1.setName("Engineering");

        Department dept2 = new Department();
        dept2.setId(2L);
        dept2.setDepartmentId("DEPT002");
        dept2.setName("Sales");

        when(departmentMapper.selectList(null)).thenReturn(Arrays.asList(dept1, dept2));
        when(departmentMapper.countUsersByDepartmentId(1L)).thenReturn(5);
        when(departmentMapper.countUsersByDepartmentId(2L)).thenReturn(3);

        List<Department> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5, result.get(0).getMemberCount());
        assertEquals(3, result.get(1).getMemberCount());
    }

    @Test
    void getAllDepartments_shouldReturnEmptyList_whenNoDepartments() {
        when(departmentMapper.selectList(null)).thenReturn(Collections.emptyList());

        List<Department> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(departmentMapper, never()).countUsersByDepartmentId(any());
    }

    // ==================== getDepartmentTree Tests ====================

    @Test
    void getDepartmentTree_shouldBuildTreeWithRootDepartments() {
        Department root = new Department();
        root.setId(1L);
        root.setDepartmentId("DEPT001");
        root.setName("Root");
        root.setParentId(null);

        Department child = new Department();
        child.setId(2L);
        child.setDepartmentId("DEPT002");
        child.setName("Child");
        child.setParentId(1L);

        when(departmentMapper.selectList(null)).thenReturn(Arrays.asList(root, child));
        when(departmentMapper.countUsersByDepartmentId(1L)).thenReturn(5);
        when(departmentMapper.countUsersByDepartmentId(2L)).thenReturn(2);

        List<Department> result = departmentService.getDepartmentTree();

        assertNotNull(result);
        assertEquals(1, result.size()); // Only root
        assertEquals("Root", result.get(0).getName());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("Child", result.get(0).getChildren().get(0).getName());
    }

    @Test
    void getDepartmentTree_shouldReturnEmptyList_whenNoDepartments() {
        when(departmentMapper.selectList(null)).thenReturn(Collections.emptyList());

        List<Department> result = departmentService.getDepartmentTree();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDepartmentTree_shouldHandleMultipleRootDepartments() {
        Department root1 = new Department();
        root1.setId(1L);
        root1.setDepartmentId("DEPT001");
        root1.setName("Root1");
        root1.setParentId(null);

        Department root2 = new Department();
        root2.setId(2L);
        root2.setDepartmentId("DEPT002");
        root2.setName("Root2");
        root2.setParentId(null);

        when(departmentMapper.selectList(null)).thenReturn(Arrays.asList(root1, root2));
        when(departmentMapper.countUsersByDepartmentId(1L)).thenReturn(5);
        when(departmentMapper.countUsersByDepartmentId(2L)).thenReturn(3);

        List<Department> result = departmentService.getDepartmentTree();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== getById Tests ====================

    @Test
    void getById_shouldReturnDepartment() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setDepartmentId("DEPT001");

        when(departmentMapper.selectById(1L)).thenReturn(dept);

        Department result = departmentService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(departmentMapper.selectById(999L)).thenReturn(null);

        Department result = departmentService.getById(999L);

        assertNull(result);
    }

    // ==================== create Tests ====================

    @Test
    void create_shouldGenerateDepartmentId() {
        Department dept = new Department();
        dept.setName("New Department");

        when(departmentMapper.getMaxDepartmentIdNumber()).thenReturn(5L);
        when(departmentMapper.insert(any(Department.class))).thenReturn(1);

        Department result = departmentService.create(dept);

        assertNotNull(result);
        assertEquals("DEPT006", result.getDepartmentId());
        verify(departmentMapper).insert(dept);
    }

    // ==================== canDelete Tests ====================

    @Test
    void canDelete_shouldReturnTrue_whenNoUsersAndNoChildren() {
        Long id = 1L;
        when(departmentMapper.countUsersByDepartmentId(id)).thenReturn(0);
        when(departmentMapper.countChildrenByDepartmentId(id)).thenReturn(0);

        boolean result = departmentService.canDelete(id);

        assertTrue(result);
    }

    @Test
    void canDelete_shouldReturnFalse_whenUsersExist() {
        Long id = 1L;
        when(departmentMapper.countUsersByDepartmentId(id)).thenReturn(5);
        when(departmentMapper.countChildrenByDepartmentId(id)).thenReturn(0);

        boolean result = departmentService.canDelete(id);

        assertFalse(result);
    }

    @Test
    void canDelete_shouldReturnFalse_whenChildrenExist() {
        Long id = 1L;
        when(departmentMapper.countUsersByDepartmentId(id)).thenReturn(0);
        when(departmentMapper.countChildrenByDepartmentId(id)).thenReturn(3);

        boolean result = departmentService.canDelete(id);

        assertFalse(result);
    }

    @Test
    void canDelete_shouldReturnFalse_whenBothUsersAndChildrenExist() {
        Long id = 1L;
        when(departmentMapper.countUsersByDepartmentId(id)).thenReturn(2);
        when(departmentMapper.countChildrenByDepartmentId(id)).thenReturn(3);

        boolean result = departmentService.canDelete(id);

        assertFalse(result);
    }

    // ==================== getChildren Tests ====================

    @Test
    void getChildren_shouldReturnChildDepartments() {
        Long parentId = 1L;
        Department child1 = new Department();
        child1.setId(2L);
        child1.setParentId(parentId);

        Department child2 = new Department();
        child2.setId(3L);
        child2.setParentId(parentId);

        when(departmentMapper.findByParentId(parentId)).thenReturn(Arrays.asList(child1, child2));

        List<Department> result = departmentService.getChildren(parentId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getChildren_shouldReturnEmptyList_whenNoChildren() {
        Long parentId = 1L;
        when(departmentMapper.findByParentId(parentId)).thenReturn(Collections.emptyList());

        List<Department> result = departmentService.getChildren(parentId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== delete Tests ====================

    @Test
    void delete_shouldCallMapper() {
        Long id = 1L;

        departmentService.delete(id);

        verify(departmentMapper).deleteById(id);
    }
}
