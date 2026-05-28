package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.DataStandard;
import com.sme.pm.service.IDataStandardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataStandardController.
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *   <li>List all data standards without filter</li>
 *   <li>List data standards filtered by type</li>
 *   <li>Get data standard detail by ID</li>
 *   <li>Create a new data standard</li>
 *   <li>Update an existing data standard</li>
 *   <li>Delete a data standard</li>
 *   <li>Share a data standard</li>
 * </ul>
 *
 * <p>Assertions verify:</p>
 * <ul>
 *   <li>Result wrapper structure (success flag, data payload)</li>
 *   <li>Service method invocation with correct parameters</li>
 *   <li>Return type correctness for each endpoint</li>
 *   <li>Edge cases (empty list, null type parameter)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class DataStandardControllerTest {

    @Mock
    private IDataStandardService dataStandardService;

    private DataStandardController dataStandardController;

    @BeforeEach
    void setUp() {
        dataStandardController = new DataStandardController(dataStandardService);
    }

    /**
     * Scenario: List all data standards without type filter.
     * Tests that GET /api/v1/data-standards returns all data standards.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method getAll() is called</li>
     *   <li>Result wraps the list of data standards</li>
     *   <li>Result indicates success</li>
     * </ul>
     */
    @Test
    void list_shouldReturnAllDataStandards_whenNoTypeProvided() {
        // Arrange
        DataStandard ds1 = createDataStandard(1L, "CODE_001", "Code Standard", "CODE");
        DataStandard ds2 = createDataStandard(2L, "ENUM_001", "Enum Standard", "ENUM");
        when(dataStandardService.getAll()).thenReturn(Arrays.asList(ds1, ds2));

        // Act
        Result<List<DataStandard>> result = dataStandardController.list(null);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        verify(dataStandardService).getAll();
    }

    /**
     * Scenario: List data standards filtered by type parameter.
     * Tests that GET /api/v1/data-standards?type=CODE returns filtered results.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method getByType("CODE") is called</li>
     *   <li>Result wraps only CODE type data standards</li>
     *   <li>Result indicates success</li>
     * </ul>
     */
    @Test
    void list_shouldReturnFilteredDataStandards_whenTypeProvided() {
        // Arrange
        DataStandard codeStandard = createDataStandard(1L, "CODE_001", "Code Standard", "CODE");
        when(dataStandardService.getByType("CODE")).thenReturn(Collections.singletonList(codeStandard));

        // Act
        Result<List<DataStandard>> result = dataStandardController.list("CODE");

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("CODE", result.getData().get(0).getType());
        verify(dataStandardService).getByType("CODE");
    }

    /**
     * Scenario: List data standards with empty type string.
     * Tests that an empty string type parameter triggers getAll() instead of getByType().
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method getAll() is called (not getByType)</li>
     *   <li>Result wraps empty list</li>
     * </ul>
     */
    @Test
    void list_shouldReturnAllDataStandards_whenTypeIsEmptyString() {
        // Arrange
        when(dataStandardService.getAll()).thenReturn(Collections.emptyList());

        // Act
        Result<List<DataStandard>> result = dataStandardController.list("");

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
        verify(dataStandardService).getAll();
        verify(dataStandardService, never()).getByType(any());
    }

    /**
     * Scenario: Get data standard detail by ID.
     * Tests that GET /api/v1/data-standards/{id} returns detailed information.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method getDetail(1L) is called</li>
     *   <li>Result wraps the detail map</li>
     *   <li>Result indicates success</li>
     * </ul>
     */
    @Test
    void getById_shouldReturnDataStandardDetail_whenIdExists() {
        // Arrange
        Long id = 1L;
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("id", id);
        detailMap.put("code", "CODE_001");
        detailMap.put("name", "Code Standard");
        when(dataStandardService.getDetail(id)).thenReturn(detailMap);

        // Act
        Result<Map<String, Object>> result = dataStandardController.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(id, result.getData().get("id"));
        assertEquals("CODE_001", result.getData().get("code"));
        verify(dataStandardService).getDetail(id);
    }

    /**
     * Scenario: Create a new data standard.
     * Tests that POST /api/v1/data-standards creates and returns the new standard.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method create() is called with correct data</li>
     *   <li>Result wraps the created data standard with generated ID</li>
     *   <li>Result indicates success</li>
     * </ul>
     */
    @Test
    void create_shouldReturnCreatedDataStandard_whenValidInput() {
        // Arrange
        DataStandard inputStandard = createDataStandard(null, "NEW_CODE", "New Standard", "CODE");
        DataStandard createdStandard = createDataStandard(10L, "NEW_CODE", "New Standard", "CODE");
        when(dataStandardService.create(any(DataStandard.class))).thenReturn(createdStandard);

        // Act
        Result<DataStandard> result = dataStandardController.create(inputStandard);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(10L, result.getData().getId());
        assertEquals("NEW_CODE", result.getData().getCode());
        verify(dataStandardService).create(any(DataStandard.class));
    }

    /**
     * Scenario: Update an existing data standard.
     * Tests that PUT /api/v1/data-standards/{id} updates and returns the standard.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method update(id, dataStandard) is called with correct parameters</li>
     *   <li>Result wraps the updated data standard</li>
     *   <li>Result indicates success</li>
     * </ul>
     */
    @Test
    void update_shouldReturnUpdatedDataStandard_whenIdExists() {
        // Arrange
        Long id = 1L;
        DataStandard updateData = createDataStandard(null, "UPDATED_CODE", "Updated Standard", "ENUM");
        DataStandard updatedStandard = createDataStandard(id, "UPDATED_CODE", "Updated Standard", "ENUM");
        when(dataStandardService.update(eq(id), any(DataStandard.class))).thenReturn(updatedStandard);

        // Act
        Result<DataStandard> result = dataStandardController.update(id, updateData);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(id, result.getData().getId());
        assertEquals("UPDATED_CODE", result.getData().getCode());
        verify(dataStandardService).update(eq(id), any(DataStandard.class));
    }

    /**
     * Scenario: Delete a data standard.
     * Tests that DELETE /api/v1/data-standards/{id} removes the standard.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method delete(id) is called</li>
     *   <li>Result indicates success with no data payload</li>
     * </ul>
     */
    @Test
    void delete_shouldReturnSuccess_whenIdExists() {
        // Arrange
        Long id = 1L;
        doNothing().when(dataStandardService).delete(id);

        // Act
        Result<Void> result = dataStandardController.delete(id);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNull(result.getData());
        verify(dataStandardService).delete(id);
    }

    /**
     * Scenario: Share a data standard with share parameters.
     * Tests that POST /api/v1/data-standards/{id}/share executes sharing logic.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *   <li>Service method share(id, shareParams) is called with correct parameters</li>
     *   <li>Result indicates success with no data payload</li>
     * </ul>
     */
    @Test
    void share_shouldReturnSuccess_whenValidInput() {
        // Arrange
        Long id = 1L;
        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("targetUsers", Arrays.asList("user1", "user2"));
        shareParams.put("permission", "READ");
        doNothing().when(dataStandardService).share(eq(id), any(Map.class));

        // Act
        Result<Void> result = dataStandardController.share(id, shareParams);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNull(result.getData());
        verify(dataStandardService).share(eq(id), any(Map.class));
    }

    /**
     * Helper method to create a DataStandard instance for testing.
     *
     * @param id          the data standard ID
     * @param code        the data standard code
     * @param name        the data standard name
     * @param type        the data standard type
     * @return a configured DataStandard instance
     */
    private DataStandard createDataStandard(Long id, String code, String name, String type) {
        DataStandard ds = new DataStandard();
        ds.setId(id);
        ds.setCode(code);
        ds.setName(name);
        ds.setType(type);
        ds.setCreatedAt(LocalDateTime.now());
        ds.setUpdatedAt(LocalDateTime.now());
        return ds;
    }
}
