package com.sme.pm.service.impl;

import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(projectMapper);
    }

    @Test
    void create_shouldInsertProjectAndReturn() {
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setProjectType(1);
        project.setOwnerId(1L);

        when(projectMapper.insert(any(Project.class))).thenReturn(1);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        Project result = projectService.create(project);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectMapper).insert(project);
    }

    @Test
    void update_shouldUpdateProjectAndReturn() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Updated Project");

        when(projectMapper.updateById(project)).thenReturn(1);

        Project result = projectService.update(project);

        assertNotNull(result);
        assertEquals("Updated Project", result.getName());
        verify(projectMapper).updateById(project);
    }

    @Test
    void delete_shouldCallDeleteById() {
        Long projectId = 1L;

        projectService.delete(projectId);

        verify(projectMapper).deleteById(projectId);
    }

    @Test
    void getById_shouldReturnProject() {
        Long projectId = 1L;
        Project expectedProject = new Project();
        expectedProject.setId(projectId);
        expectedProject.setName("Test Project");

        when(projectMapper.findById(projectId)).thenReturn(expectedProject);

        Project result = projectService.getById(projectId);

        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals("Test Project", result.getName());
        verify(projectMapper).findById(projectId);
    }

    @Test
    void list_shouldReturnAllProjects() {
        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("Project 1");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");

        when(projectMapper.findAll()).thenReturn(Arrays.asList(project1, project2));

        List<Project> result = projectService.list();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(projectMapper).findAll();
    }

    @Test
    void archive_shouldSetStatusToArchived() {
        Long projectId = 1L;

        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        projectService.archive(projectId);

        verify(projectMapper).updateById(argThat(project ->
            project.getId().equals(projectId) && project.getStatus() == 3
        ));
    }

    @Test
    void restore_shouldSetStatusToActive() {
        Long projectId = 1L;

        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        projectService.restore(projectId);

        verify(projectMapper).updateById(argThat(project ->
            project.getId().equals(projectId) && project.getStatus() == 2
        ));
    }

    @Test
    void addMember_shouldCallAddMember() {
        Long projectId = 1L;
        Long userId = 2L;

        projectService.addMember(projectId, userId);

        verify(projectMapper).addMember(projectId, userId);
    }

    @Test
    void removeMember_shouldCallRemoveMember() {
        Long projectId = 1L;
        Long userId = 2L;

        projectService.removeMember(projectId, userId);

        verify(projectMapper).removeMember(projectId, userId);
    }
}
