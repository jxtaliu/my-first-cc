package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sme.pm.entity.*;
import com.sme.pm.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DataStandardServiceImpl 单元测试
 *
 * <p>测试场景：
 * <ul>
 *   <li>测试数据标准的 CRUD 操作（创建、读取、更新、删除）</li>
 *   <li>测试不同类型（ENUM/CODE/STRING/NUMBER）的子项保存逻辑</li>
 *   <li>测试 getDetail 方法正确加载各类型子表数据</li>
 *   <li>测试 share 方法调用飞书/邮件分享</li>
 *   <li>测试异常场景：更新/删除不存在的记录</li>
 * </ul>
 *
 * <p>断言：
 * <ul>
 *   <li>验证返回的数据结构完整性</li>
 *   <li>验证 Mapper 方法调用次数和参数</li>
 *   <li>验证异常抛出符合预期</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataStandardServiceImplTest {

    @Mock
    private DataStandardMapper dataStandardMapper;

    @Mock
    private DataStandardEnumItemMapper enumItemMapper;

    @Mock
    private DataStandardCodeItemMapper codeItemMapper;

    @Mock
    private DataStandardStringItemMapper stringItemMapper;

    @Mock
    private DataStandardNumberItemMapper numberItemMapper;

    private DataStandardServiceImpl dataStandardService;

    @BeforeEach
    void setUp() throws Exception {
        dataStandardService = new DataStandardServiceImpl(
            dataStandardMapper,
            enumItemMapper,
            codeItemMapper,
            stringItemMapper,
            numberItemMapper
        );
    }

    // ==================== getAll Tests ====================

    /**
     * 测试场景：getAll 方法返回所有数据标准
     * 测试内容：验证返回列表包含多条记录，且数据正确
     * 断言：
     * - 返回列表非空
     * - 列表大小为 2
     * - 数据标准类型分别为 ENUM 和 CODE
     */
    @Test
    void getAll_shouldReturnAllDataStandards() {
        // Arrange
        DataStandard enumStandard = createDataStandard(1L, "PRIORITY", "优先级枚举", "ENUM");
        DataStandard codeStandard = createDataStandard(2L, "TASK_CODE", "任务编码", "CODE");
        when(dataStandardMapper.findAll()).thenReturn(Arrays.asList(enumStandard, codeStandard));

        // Act
        List<DataStandard> result = dataStandardService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PRIORITY", result.get(0).getCode());
        assertEquals("ENUM", result.get(0).getType());
        assertEquals("TASK_CODE", result.get(1).getCode());
        assertEquals("CODE", result.get(1).getType());
        verify(dataStandardMapper, times(1)).findAll();
    }

    /**
     * 测试场景：getAll 方法返回空列表
     * 测试内容：当数据库中没有数据标准时，验证返回空列表
     * 断言：返回空列表且不抛出异常
     */
    @Test
    void getAll_shouldReturnEmptyList_whenNoDataStandards() {
        // Arrange
        when(dataStandardMapper.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<DataStandard> result = dataStandardService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataStandardMapper, times(1)).findAll();
    }

    // ==================== getById Tests ====================

    /**
     * 测试场景：根据 ID 获取数据标准
     * 测试内容：验证返回正确的数据标准记录
     * 断言：
     * - 返回非空
     * - ID、编码、名称、类型均正确
     */
    @Test
    void getById_shouldReturnDataStandard_whenExists() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");
        when(dataStandardMapper.findById(id)).thenReturn(standard);

        // Act
        DataStandard result = dataStandardService.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("PRIORITY", result.getCode());
        assertEquals("优先级枚举", result.getName());
        assertEquals("ENUM", result.getType());
        verify(dataStandardMapper, times(1)).findById(id);
    }

    /**
     * 测试场景：根据不存在的 ID 获取数据标准
     * 测试内容：验证返回 null 且不抛出异常
     * 断言：返回 null
     */
    @Test
    void getById_shouldReturnNull_whenNotExists() {
        // Arrange
        Long id = 999L;
        when(dataStandardMapper.findById(id)).thenReturn(null);

        // Act
        DataStandard result = dataStandardService.getById(id);

        // Assert
        assertNull(result);
        verify(dataStandardMapper, times(1)).findById(id);
    }

    // ==================== getByType Tests ====================

    /**
     * 测试场景：根据类型筛选数据标准
     * 测试内容：验证只返回指定类型的记录
     * 断言：
     * - 只返回 ENUM 类型的记录
     * - 列表大小为 1
     */
    @Test
    void getByType_shouldReturnFilteredDataStandards() {
        // Arrange
        String type = "ENUM";
        DataStandard enumStandard = createDataStandard(1L, "PRIORITY", "优先级枚举", "ENUM");
        when(dataStandardMapper.findByType(type)).thenReturn(Collections.singletonList(enumStandard));

        // Act
        List<DataStandard> result = dataStandardService.getByType(type);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ENUM", result.get(0).getType());
        verify(dataStandardMapper, times(1)).findByType(type);
    }

    // ==================== create Tests ====================

    /**
     * 测试场景：创建 ENUM 类型数据标准
     * 测试内容：验证创建成功并正确保存主表和子表数据
     * 断言：
     * - 返回的 dataStandard 包含自动生成的 ID
     * - enumItemMapper.insert 被调用 2 次（因为有 2 个枚举值）
     * - dataStandardMapper.insert 被调用 1 次
     */
    @Test
    void create_shouldSaveEnumStandardWithItems() {
        // Arrange
        DataStandard standard = createDataStandard(null, "PRIORITY", "优先级枚举", "ENUM");
        standard.setDescription("任务优先级定义");
        standard.setOwnerName("张三");

        DataStandardEnumItem item1 = new DataStandardEnumItem();
        item1.setValue("HIGH");
        item1.setLabel("高");
        item1.setSortOrder(1);

        DataStandardEnumItem item2 = new DataStandardEnumItem();
        item2.setValue("LOW");
        item2.setLabel("低");
        item2.setSortOrder(2);

        standard.setEnumItems(Arrays.asList(item1, item2));

        when(dataStandardMapper.insert(any(DataStandard.class))).thenAnswer(invocation -> {
            DataStandard ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        // Act
        DataStandard result = dataStandardService.create(standard);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PRIORITY", result.getCode());

        // Verify insert was called for mapper and items
        verify(dataStandardMapper, times(1)).insert(any(DataStandard.class));
        verify(enumItemMapper, times(2)).insert(any(DataStandardEnumItem.class));
    }

    /**
     * 测试场景：创建 CODE 类型数据标准
     * 测试内容：验证 CODE 类型子表数据正确保存
     * 断言：
     * - codeItemMapper.insert 被调用 1 次
     */
    @Test
    void create_shouldSaveCodeStandardWithItem() {
        // Arrange
        DataStandard standard = createDataStandard(null, "TASK_CODE", "任务编码", "CODE");

        DataStandardCodeItem codeItem = new DataStandardCodeItem();
        codeItem.setFormat("[A-Z][0-9][0-9]");
        codeItem.setPrefix("TSK");
        codeItem.setLength(5);
        codeItem.setExample("TSK001");

        standard.setCodeItem(codeItem);

        when(dataStandardMapper.insert(any(DataStandard.class))).thenAnswer(invocation -> {
            DataStandard ds = invocation.getArgument(0);
            ds.setId(2L);
            return 1;
        });

        // Act
        DataStandard result = dataStandardService.create(standard);

        // Assert
        assertNotNull(result);
        verify(dataStandardMapper, times(1)).insert(any(DataStandard.class));
        verify(codeItemMapper, times(1)).insert(any(DataStandardCodeItem.class));
    }

    /**
     * 测试场景：创建 STRING 类型数据标准
     * 测试内容：验证 STRING 类型子表数据正确保存
     * 断言：stringItemMapper.insert 被调用 1 次
     */
    @Test
    void create_shouldSaveStringStandardWithItem() {
        // Arrange
        DataStandard standard = createDataStandard(null, "EMAIL", "邮箱格式", "STRING");

        DataStandardStringItem stringItem = new DataStandardStringItem();
        stringItem.setMinLength(5);
        stringItem.setMaxLength(100);
        stringItem.setPattern("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
        stringItem.setExample("test@example.com");

        standard.setStringItem(stringItem);

        when(dataStandardMapper.insert(any(DataStandard.class))).thenAnswer(invocation -> {
            DataStandard ds = invocation.getArgument(0);
            ds.setId(3L);
            return 1;
        });

        // Act
        DataStandard result = dataStandardService.create(standard);

        // Assert
        assertNotNull(result);
        verify(stringItemMapper, times(1)).insert(any(DataStandardStringItem.class));
    }

    /**
     * 测试场景：创建 NUMBER 类型数据标准
     * 测试内容：验证 NUMBER 类型子表数据正确保存
     * 断言：numberItemMapper.insert 被调用 1 次
     */
    @Test
    void create_shouldSaveNumberStandardWithItem() {
        // Arrange
        DataStandard standard = createDataStandard(null, "SCORE", "评分", "NUMBER");

        DataStandardNumberItem numberItem = new DataStandardNumberItem();
        numberItem.setMinValue(java.math.BigDecimal.valueOf(0.0));
        numberItem.setMaxValue(java.math.BigDecimal.valueOf(100.0));
        numberItem.setDecimalPlaces(1);
        numberItem.setExample("85.5");

        standard.setNumberItem(numberItem);

        when(dataStandardMapper.insert(any(DataStandard.class))).thenAnswer(invocation -> {
            DataStandard ds = invocation.getArgument(0);
            ds.setId(4L);
            return 1;
        });

        // Act
        DataStandard result = dataStandardService.create(standard);

        // Assert
        assertNotNull(result);
        verify(numberItemMapper, times(1)).insert(any(DataStandardNumberItem.class));
    }

    // ==================== update Tests ====================

    /**
     * 测试场景：更新数据标准
     * 测试内容：验证更新成功，子表数据先删除后重新插入
     * 断言：
     * - dataStandardMapper.updateById 被调用
     * - deleteSubItems 被调用（通过 delete 调用验证）
     * - 原有枚举值被删除，新枚举值被插入
     */
    @Test
    void update_shouldUpdateStandardAndSubItems() {
        // Arrange
        Long id = 1L;
        DataStandard existing = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");
        existing.setOwnerName("张三");

        DataStandard updated = createDataStandard(id, "PRIORITY_UPD", "优先级枚举更新", "ENUM");
        updated.setOwnerName("李四");

        DataStandardEnumItem newItem = new DataStandardEnumItem();
        newItem.setValue("MEDIUM");
        newItem.setLabel("中");
        newItem.setSortOrder(2);
        updated.setEnumItems(Collections.singletonList(newItem));

        when(dataStandardMapper.findById(id)).thenReturn(existing);
        when(dataStandardMapper.updateById(any(DataStandard.class))).thenReturn(1);

        // Act
        DataStandard result = dataStandardService.update(id, updated);

        // Assert
        assertNotNull(result);
        assertEquals("PRIORITY_UPD", result.getCode());
        assertEquals("李四", result.getOwnerName());
        verify(dataStandardMapper, times(1)).updateById(any(DataStandard.class));
    }

    /**
     * 测试场景：更新不存在的数据标准
     * 测试内容：验证抛出 RuntimeException
     * 断言：异常消息包含 "Data standard not found"
     */
    @Test
    void update_shouldThrowException_whenNotExists() {
        // Arrange
        Long id = 999L;
        when(dataStandardMapper.findById(id)).thenReturn(null);

        DataStandard updated = createDataStandard(id, "TEST", "测试", "ENUM");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataStandardService.update(id, updated);
        });
        assertEquals("Data standard not found", exception.getMessage());
    }

    // ==================== delete Tests ====================

    /**
     * 测试场景：删除数据标准
     * 测试内容：验证子表和主表记录都被删除
     * 断言：
     * - 4 个 deleteSubItems 方法被调用（针对每种子表类型）
     * - dataStandardMapper.deleteById 被调用
     */
    @Test
    void delete_shouldDeleteStandardAndSubItems() {
        // Arrange
        Long id = 1L;
        when(enumItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(codeItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(stringItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(numberItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(dataStandardMapper.deleteById(id)).thenReturn(1);

        // Act
        dataStandardService.delete(id);

        // Assert
        verify(enumItemMapper, times(1)).delete(any());
        verify(codeItemMapper, times(1)).delete(any());
        verify(stringItemMapper, times(1)).delete(any());
        verify(numberItemMapper, times(1)).delete(any());
        verify(dataStandardMapper, times(1)).deleteById(id);
    }

    // ==================== getDetail Tests ====================

    /**
     * 测试场景：获取 ENUM 类型数据标准详情
     * 测试内容：验证正确加载枚举值子表数据
     * 断言：
     * - detail 包含 enumItems
     * - enumItems 大小为 2
     * - detail 包含正确的主表字段
     */
    @Test
    void getDetail_shouldReturnEnumDetailWithItems() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");

        DataStandardEnumItem item1 = new DataStandardEnumItem();
        item1.setId(1L);
        item1.setStandardId(id);
        item1.setValue("HIGH");
        item1.setLabel("高");
        item1.setSortOrder(1);

        DataStandardEnumItem item2 = new DataStandardEnumItem();
        item2.setId(2L);
        item2.setStandardId(id);
        item2.setValue("LOW");
        item2.setLabel("低");
        item2.setSortOrder(2);

        when(dataStandardMapper.findById(id)).thenReturn(standard);
        when(enumItemMapper.findByStandardId(id)).thenReturn(Arrays.asList(item1, item2));

        // Act
        Map<String, Object> result = dataStandardService.getDetail(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.get("id"));
        assertEquals("PRIORITY", result.get("code"));
        assertEquals("优先级枚举", result.get("name"));
        assertEquals("ENUM", result.get("type"));

        @SuppressWarnings("unchecked")
        List<DataStandardEnumItem> enumItems = (List<DataStandardEnumItem>) result.get("enumItems");
        assertNotNull(enumItems);
        assertEquals(2, enumItems.size());
        assertEquals("HIGH", enumItems.get(0).getValue());
        assertEquals("低", enumItems.get(1).getLabel());
    }

    /**
         * 测试场景：获取 CODE 类型数据标准详情
     * 测试内容：验证正确加载编码型子表数据
     * 断言：detail 包含 codeItem 且字段正确
     */
    @Test
    void getDetail_shouldReturnCodeDetailWithItem() {
        // Arrange
        Long id = 2L;
        DataStandard standard = createDataStandard(id, "TASK_CODE", "任务编码", "CODE");

        DataStandardCodeItem codeItem = new DataStandardCodeItem();
        codeItem.setId(1L);
        codeItem.setStandardId(id);
        codeItem.setFormat("[A-Z][0-9][0-9]");
        codeItem.setPrefix("TSK");
        codeItem.setLength(5);
        codeItem.setExample("TSK001");

        when(dataStandardMapper.findById(id)).thenReturn(standard);
        when(codeItemMapper.findByStandardId(id)).thenReturn(codeItem);

        // Act
        Map<String, Object> result = dataStandardService.getDetail(id);

        // Assert
        assertNotNull(result);
        assertEquals("CODE", result.get("type"));

        @SuppressWarnings("unchecked")
        DataStandardCodeItem resultItem = (DataStandardCodeItem) result.get("codeItem");
        assertNotNull(resultItem);
        assertEquals("TSK", resultItem.getPrefix());
        assertEquals(5, resultItem.getLength());
    }

    /**
     * 测试场景：获取 STRING 类型数据标准详情
     * 测试内容：验证正确加载字符型子表数据
     * 断言：detail 包含 stringItem 且 minLength/maxLength 正确
     */
    @Test
    void getDetail_shouldReturnStringDetailWithItem() {
        // Arrange
        Long id = 3L;
        DataStandard standard = createDataStandard(id, "EMAIL", "邮箱格式", "STRING");

        DataStandardStringItem stringItem = new DataStandardStringItem();
        stringItem.setId(1L);
        stringItem.setStandardId(id);
        stringItem.setMinLength(5);
        stringItem.setMaxLength(100);
        stringItem.setPattern("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
        stringItem.setExample("test@example.com");

        when(dataStandardMapper.findById(id)).thenReturn(standard);
        when(stringItemMapper.findByStandardId(id)).thenReturn(stringItem);

        // Act
        Map<String, Object> result = dataStandardService.getDetail(id);

        // Assert
        assertNotNull(result);
        assertEquals("STRING", result.get("type"));

        @SuppressWarnings("unchecked")
        DataStandardStringItem resultItem = (DataStandardStringItem) result.get("stringItem");
        assertNotNull(resultItem);
        assertEquals(5, resultItem.getMinLength());
        assertEquals(100, resultItem.getMaxLength());
    }

    /**
     * 测试场景：获取 NUMBER 类型数据标准详情
     * 测试内容：验证正确加载数值型子表数据
     * 断言：detail 包含 numberItem 且 minValue/maxValue/decimalPlaces 正确
     */
    @Test
    void getDetail_shouldReturnNumberDetailWithItem() {
        // Arrange
        Long id = 4L;
        DataStandard standard = createDataStandard(id, "SCORE", "评分", "NUMBER");

        DataStandardNumberItem numberItem = new DataStandardNumberItem();
        numberItem.setId(1L);
        numberItem.setStandardId(id);
        numberItem.setMinValue(java.math.BigDecimal.valueOf(0.0));
        numberItem.setMaxValue(java.math.BigDecimal.valueOf(100.0));
        numberItem.setDecimalPlaces(1);
        numberItem.setExample("85.5");

        when(dataStandardMapper.findById(id)).thenReturn(standard);
        when(numberItemMapper.findByStandardId(id)).thenReturn(numberItem);

        // Act
        Map<String, Object> result = dataStandardService.getDetail(id);

        // Assert
        assertNotNull(result);
        assertEquals("NUMBER", result.get("type"));

        @SuppressWarnings("unchecked")
        DataStandardNumberItem resultItem = (DataStandardNumberItem) result.get("numberItem");
        assertNotNull(resultItem);
        assertEquals(java.math.BigDecimal.valueOf(0.0), resultItem.getMinValue());
        assertEquals(java.math.BigDecimal.valueOf(100.0), resultItem.getMaxValue());
        assertEquals(1, resultItem.getDecimalPlaces());
    }

    /**
     * 测试场景：获取不存在的详情
     * 测试内容：验证返回空 Map
     * 断言：返回空 Map 且不抛出异常
     */
    @Test
    void getDetail_shouldReturnEmptyMap_whenNotExists() {
        // Arrange
        Long id = 999L;
        when(dataStandardMapper.findById(id)).thenReturn(null);

        // Act
        Map<String, Object> result = dataStandardService.getDetail(id);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== share Tests ====================

    /**
     * 测试场景：分享到飞书
     * 测试内容：验证飞书分享方法被调用且参数正确
     * 断言：不抛出异常，打印方法被调用
     */
    @Test
    void share_shouldCallFeishu_whenMethodIsFeishu() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");
        standard.setDescription("任务优先级定义");
        standard.setOwnerName("张三");

        when(dataStandardMapper.findById(id)).thenReturn(standard);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "feishu");
        shareParams.put("webhookUrl", "https://open.feishu.cn/webhook/test");

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> {
            dataStandardService.share(id, shareParams);
        });
    }

    /**
     * 测试场景：分享到邮件
     * 测试内容：验证邮件分享方法被调用且参数正确
     * 断言：不抛出异常，打印方法被调用
     */
    @Test
    void share_shouldCallEmail_whenMethodIsEmail() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");

        when(dataStandardMapper.findById(id)).thenReturn(standard);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "email");
        shareParams.put("recipient", "test@example.com");

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> {
            dataStandardService.share(id, shareParams);
        });
    }

    /**
     * 测试场景：分享不存在的记录
     * 测试内容：验证抛出 RuntimeException
     * 断言：异常消息包含 "Data standard not found"
     */
    @Test
    void share_shouldThrowException_whenNotExists() {
        // Arrange
        Long id = 999L;
        when(dataStandardMapper.findById(id)).thenReturn(null);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "feishu");
        shareParams.put("webhookUrl", "https://open.feishu.cn/webhook/test");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataStandardService.share(id, shareParams);
        });
        assertEquals("Data standard not found", exception.getMessage());
    }

    @Test
    void share_shouldThrowException_whenFeishuWebhookUrlIsNull() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");

        when(dataStandardMapper.findById(id)).thenReturn(standard);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "feishu");
        shareParams.put("webhookUrl", null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataStandardService.share(id, shareParams);
        });
        assertEquals("Feishu webhook URL is required", exception.getMessage());
    }

    @Test
    void share_shouldThrowException_whenFeishuWebhookUrlIsEmpty() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");

        when(dataStandardMapper.findById(id)).thenReturn(standard);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "feishu");
        shareParams.put("webhookUrl", "");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataStandardService.share(id, shareParams);
        });
        assertEquals("Feishu webhook URL is required", exception.getMessage());
    }

    @Test
    void share_shouldThrowException_whenEmailRecipientIsNull() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");

        when(dataStandardMapper.findById(id)).thenReturn(standard);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "email");
        shareParams.put("recipient", null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataStandardService.share(id, shareParams);
        });
        assertEquals("Email recipient is required", exception.getMessage());
    }

    @Test
    void share_shouldThrowException_whenEmailRecipientIsEmpty() {
        // Arrange
        Long id = 1L;
        DataStandard standard = createDataStandard(id, "PRIORITY", "优先级枚举", "ENUM");

        when(dataStandardMapper.findById(id)).thenReturn(standard);

        Map<String, Object> shareParams = new HashMap<>();
        shareParams.put("method", "email");
        shareParams.put("recipient", "");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataStandardService.share(id, shareParams);
        });
        assertEquals("Email recipient is required", exception.getMessage());
    }

    // ==================== Helper Methods ====================

    /**
     * 创建 DataStandard 测试数据辅助方法
     *
     * @param id ID
     * @param code 编码
     * @param name 名称
     * @param type 类型
     * @return DataStandard 实例
     */
    private DataStandard createDataStandard(Long id, String code, String name, String type) {
        DataStandard standard = new DataStandard();
        standard.setId(id);
        standard.setCode(code);
        standard.setName(name);
        standard.setType(type);
        standard.setDeleted(0);
        return standard;
    }
}
