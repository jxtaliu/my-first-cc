package com.sme.pm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.pm.dto.DictCodeDTO;
import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.DictType;
import com.sme.pm.mapper.DictCodeMapper;
import com.sme.pm.mapper.DictTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictServiceImplTest {

    @Mock
    private DictTypeMapper dictTypeMapper;

    @Mock
    private DictCodeMapper dictCodeMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private DictServiceImpl dictService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dictService = new DictServiceImpl(dictTypeMapper, dictCodeMapper, redisTemplate, objectMapper);
    }

    // ==================== getAllTypes Tests ====================

    @Test
    void getAllTypes_shouldReturnAllTypes() {
        DictType type1 = new DictType();
        type1.setId(1L);
        type1.setCode("task_status");

        DictType type2 = new DictType();
        type2.setId(2L);
        type2.setCode("priority");

        when(dictTypeMapper.selectList(null)).thenReturn(Arrays.asList(type1, type2));

        List<DictType> result = dictService.getAllTypes();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllTypes_shouldReturnEmptyList_whenNoTypes() {
        when(dictTypeMapper.selectList(null)).thenReturn(Collections.emptyList());

        List<DictType> result = dictService.getAllTypes();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getCodesByType Tests ====================

    @Test
    void getCodesByType_shouldReturnFromDb_whenCacheMiss() throws Exception {
        String type = "task_status";
        DictCode code1 = new DictCode();
        code1.setCode("TODO");
        code1.setName("To Do");
        code1.setNameEn("To Do");
        code1.setNameZh("待办");
        code1.setSortOrder(1);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(dictCodeMapper.findByTypeCode(type)).thenReturn(Collections.singletonList(code1));

        List<DictCodeDTO> result = dictService.getCodesByType(type);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TODO", result.get(0).getCode());
    }

    @Test
    void getCodesByType_shouldReturnFromCache_whenCacheHit() throws Exception {
        String type = "task_status";
        String cachedJson = "[{\"code\":\"TODO\",\"name\":\"To Do\",\"nameEn\":\"To Do\",\"nameZh\":\"待办\",\"sortOrder\":1}]";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:" + type)).thenReturn(cachedJson);

        List<DictCodeDTO> result = dictService.getCodesByType(type);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TODO", result.get(0).getCode());
        verify(dictCodeMapper, never()).findByTypeCode(anyString());
    }

    @Test
    void getCodesByType_shouldHandleJsonParseError_andFallbackToDb() throws Exception {
        String type = "task_status";
        String corruptedJson = "{ invalid json }";

        DictCode code1 = new DictCode();
        code1.setCode("TODO");
        code1.setName("To Do");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:" + type)).thenReturn(corruptedJson);
        when(dictCodeMapper.findByTypeCode(type)).thenReturn(Collections.singletonList(code1));

        List<DictCodeDTO> result = dictService.getCodesByType(type);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCodesByType_shouldReturnEmptyList_whenNoCodes() {
        String type = "empty_type";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(dictCodeMapper.findByTypeCode(type)).thenReturn(Collections.emptyList());

        List<DictCodeDTO> result = dictService.getCodesByType(type);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getCode Tests ====================

    @Test
    void getCode_shouldReturnDictCodeDTO_whenFound() {
        String type = "task_status";
        String code = "TODO";
        DictCode dictCode = new DictCode();
        dictCode.setCode(code);
        dictCode.setName("To Do");
        dictCode.setNameEn("To Do");
        dictCode.setNameZh("待办");

        when(dictCodeMapper.findByTypeCodeAndCode(type, code)).thenReturn(dictCode);

        DictCodeDTO result = dictService.getCode(type, code);

        assertNotNull(result);
        assertEquals(code, result.getCode());
    }

    @Test
    void getCode_shouldReturnNull_whenNotFound() {
        when(dictCodeMapper.findByTypeCodeAndCode(anyString(), anyString())).thenReturn(null);

        DictCodeDTO result = dictService.getCode("task_status", "NONEXISTENT");

        assertNull(result);
    }

    // ==================== refreshCache Tests ====================

    @Test
    void refreshCache_shouldClearCacheForAllTypes() {
        DictType type1 = new DictType();
        type1.setId(1L);
        type1.setCode("type1");

        DictType type2 = new DictType();
        type2.setId(2L);
        type2.setCode("type2");

        when(dictTypeMapper.selectList(null)).thenReturn(Arrays.asList(type1, type2));
        when(redisTemplate.delete(anyString())).thenReturn(true);

        dictService.refreshCache();

        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    void refreshCache_shouldHandleEmptyTypes() {
        when(dictTypeMapper.selectList(null)).thenReturn(Collections.emptyList());

        dictService.refreshCache();

        verify(redisTemplate, never()).delete(anyString());
    }

    // ==================== DictType CRUD Tests ====================

    @Test
    void getTypeById_shouldReturnType() {
        DictType type = new DictType();
        type.setId(1L);
        type.setCode("task_status");

        when(dictTypeMapper.selectById(1L)).thenReturn(type);

        DictType result = dictService.getTypeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createType_shouldInsertAndReturn() {
        DictType type = new DictType();
        type.setCode("new_type");
        type.setName("New Type");

        when(dictTypeMapper.insert(type)).thenReturn(1);

        DictType result = dictService.createType(type);

        assertNotNull(result);
        verify(dictTypeMapper).insert(type);
    }

    @Test
    void updateType_shouldPreserveCode_whenNotProvided() {
        DictType type = new DictType();
        type.setId(1L);
        type.setName("Updated Name");

        DictType existing = new DictType();
        existing.setId(1L);
        existing.setCode("original_code");

        when(dictTypeMapper.selectById(1L)).thenReturn(existing);
        when(dictTypeMapper.updateDictType(type)).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        DictType result = dictService.updateType(type);

        assertNotNull(result);
        assertEquals("original_code", result.getCode());
        verify(dictTypeMapper).updateDictType(type);
    }

    @Test
    void updateType_shouldUseProvidedCode_whenGiven() {
        DictType type = new DictType();
        type.setId(1L);
        type.setCode("new_code");
        type.setName("Updated Name");

        when(dictTypeMapper.updateDictType(type)).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        DictType result = dictService.updateType(type);

        assertNotNull(result);
        assertEquals("new_code", result.getCode());
    }

    @Test
    void deleteType_shouldDelete_whenNoAssociatedItems() {
        Long id = 1L;
        DictType type = new DictType();
        type.setId(id);
        type.setCode("to_delete");

        when(dictTypeMapper.selectById(id)).thenReturn(type);
        when(dictTypeMapper.countItemsByTypeId(id)).thenReturn(0);
        when(dictTypeMapper.deleteById(id)).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> dictService.deleteType(id));

        verify(dictTypeMapper).deleteById(id);
    }

    @Test
    void deleteType_shouldThrow_whenTypeNotFound() {
        Long id = 999L;

        when(dictTypeMapper.selectById(id)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dictService.deleteType(id));
    }

    @Test
    void deleteType_shouldThrow_whenHasAssociatedItems() {
        Long id = 1L;
        DictType type = new DictType();
        type.setId(id);
        type.setCode("has_items");

        when(dictTypeMapper.selectById(id)).thenReturn(type);
        when(dictTypeMapper.countItemsByTypeId(id)).thenReturn(5);

        assertThrows(IllegalArgumentException.class, () -> dictService.deleteType(id));
        verify(dictTypeMapper, never()).deleteById(id);
    }

    // ==================== DictCode CRUD Tests ====================

    @Test
    void getItemsByTypeId_shouldReturnItems() {
        Long dictTypeId = 1L;
        DictCode item1 = new DictCode();
        item1.setId(1L);
        item1.setDictTypeId(dictTypeId);

        DictCode item2 = new DictCode();
        item2.setId(2L);
        item2.setDictTypeId(dictTypeId);

        when(dictCodeMapper.findByDictTypeId(dictTypeId)).thenReturn(Arrays.asList(item1, item2));

        List<DictCode> result = dictService.getItemsByTypeId(dictTypeId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllItems_shouldReturnAll() {
        when(dictCodeMapper.selectList(null)).thenReturn(Collections.emptyList());

        List<DictCode> result = dictService.getAllItems();

        assertNotNull(result);
    }

    @Test
    void getItemById_shouldReturnItem() {
        DictCode item = new DictCode();
        item.setId(1L);

        when(dictCodeMapper.selectById(1L)).thenReturn(item);

        DictCode result = dictService.getItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createItem_shouldInsertAndRefreshCache() {
        DictCode item = new DictCode();
        item.setDictTypeId(1L);
        item.setCode("NEW");

        DictType type = new DictType();
        type.setId(1L);
        type.setCode("task_status");

        when(dictCodeMapper.insertDictCode(item)).thenReturn(1);
        when(dictTypeMapper.selectById(1L)).thenReturn(type);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        DictCode result = dictService.createItem(item);

        assertNotNull(result);
        verify(dictCodeMapper).insertDictCode(item);
    }

    @Test
    void updateItem_shouldUpdateAndRefreshCache() {
        DictCode item = new DictCode();
        item.setId(1L);
        item.setDictTypeId(1L);
        item.setCode("UPDATED");

        DictType type = new DictType();
        type.setId(1L);
        type.setCode("task_status");

        when(dictCodeMapper.updateDictCode(item)).thenReturn(1);
        when(dictTypeMapper.selectById(1L)).thenReturn(type);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        DictCode result = dictService.updateItem(item);

        assertNotNull(result);
        verify(dictCodeMapper).updateDictCode(item);
    }

    @Test
    void deleteItem_shouldDelete_whenItemExists() {
        Long id = 1L;
        DictCode item = new DictCode();
        item.setId(id);
        item.setDictTypeId(1L);

        DictType type = new DictType();
        type.setId(1L);
        type.setCode("task_status");

        when(dictCodeMapper.selectById(id)).thenReturn(item);
        when(dictCodeMapper.deleteById(id)).thenReturn(1);
        when(dictTypeMapper.selectById(1L)).thenReturn(type);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> dictService.deleteItem(id));

        verify(dictCodeMapper).deleteById(id);
    }

    @Test
    void deleteItem_shouldThrow_whenItemNotFound() {
        Long id = 999L;

        when(dictCodeMapper.selectById(id)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dictService.deleteItem(id));
    }

    @Test
    void deleteItem_shouldHandleNullDictTypeId() {
        Long id = 1L;
        DictCode item = new DictCode();
        item.setId(id);
        item.setDictTypeId(null);

        when(dictCodeMapper.selectById(id)).thenReturn(item);
        when(dictCodeMapper.deleteById(id)).thenReturn(1);
        when(dictTypeMapper.selectById(null)).thenReturn(null);

        assertDoesNotThrow(() -> dictService.deleteItem(id));

        verify(dictCodeMapper).deleteById(id);
        verify(dictTypeMapper).selectById(null);
    }
}
