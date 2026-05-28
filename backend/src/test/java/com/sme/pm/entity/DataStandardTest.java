package com.sme.pm.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStandard 实体单元测试
 *
 * 测试场景：验证 DataStandard 实体的所有字段的 getter/setter、equals/hashCode/toString 方法
 * 测试内容：
 * - 所有基本类型字段的存取
 * - LocalDateTime 类型字段的存取
 * - List<DataStandardEnumItem> 子表关联的存取
 * - DataStandardCodeItem 子表关联的存取
 * - DataStandardStringItem 子表关联的存取
 * - DataStandardNumberItem 子表关联的存取
 * - deleted 逻辑删除标记的存取
 *
 * 断言：
 * - 所有字段的 getter 返回预期值
 * - setter 后 getter 能正确返回设置的值
 * - equals 和 hashCode 基于所有字段（由 Lombok @Data 生成）
 * - toString 包含所有字段的字符串表示
 */
class DataStandardTest {

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        /**
         * 场景：使用无参构造函数创建实例
         * 测试内容：验证默认值为 null 或 0
         * 断言：所有字段为 null，deleted 为 0，关联子表为 null
         */
        DataStandard standard = new DataStandard();

        assertNull(standard.getId());
        assertNull(standard.getCode());
        assertNull(standard.getName());
        assertNull(standard.getType());
        assertNull(standard.getDescription());
        assertNull(standard.getOwnerId());
        assertNull(standard.getOwnerName());
        assertNull(standard.getCreatedAt());
        assertNull(standard.getUpdatedAt());
        assertNull(standard.getDeleted());
        assertNull(standard.getEnumItems());
        assertNull(standard.getCodeItem());
        assertNull(standard.getStringItem());
        assertNull(standard.getNumberItem());
    }

    @Test
    void setter_and_getter_shouldWorkForAllFields() {
        /**
         * 场景：使用 setter 设置所有字段值后，通过 getter 获取
         * 测试内容：验证所有字段的存取功能
         * 断言：getter 返回与 setter 设置一致的值
         */
        DataStandard standard = new DataStandard();
        LocalDateTime now = LocalDateTime.now();

        standard.setId(1L);
        standard.setCode("DS001");
        standard.setName("数据标准名称");
        standard.setType("ENUM");
        standard.setDescription("这是描述");
        standard.setOwnerId(100L);
        standard.setOwnerName("张三");
        standard.setCreatedAt(now);
        standard.setUpdatedAt(now);
        standard.setDeleted(1);

        assertEquals(1L, standard.getId());
        assertEquals("DS001", standard.getCode());
        assertEquals("数据标准名称", standard.getName());
        assertEquals("ENUM", standard.getType());
        assertEquals("这是描述", standard.getDescription());
        assertEquals(100L, standard.getOwnerId());
        assertEquals("张三", standard.getOwnerName());
        assertEquals(now, standard.getCreatedAt());
        assertEquals(now, standard.getUpdatedAt());
        assertEquals(1, standard.getDeleted());
    }

    @Test
    void setter_and_getter_shouldWorkForEnumItems() {
        /**
         * 场景：设置子表关联 DataStandardEnumItem 列表
         * 测试内容：验证 List<DataStandardEnumItem> 类型的关联对象存取
         * 断言：子表列表正确存取
         */
        DataStandard standard = new DataStandard();
        List<DataStandardEnumItem> enumItems = new ArrayList<>();

        DataStandardEnumItem item1 = new DataStandardEnumItem();
        item1.setId(1L);
        item1.setValue("VALUE1");
        item1.setLabel("值1");
        enumItems.add(item1);

        DataStandardEnumItem item2 = new DataStandardEnumItem();
        item2.setId(2L);
        item2.setValue("VALUE2");
        item2.setLabel("值2");
        enumItems.add(item2);

        standard.setEnumItems(enumItems);

        assertNotNull(standard.getEnumItems());
        assertEquals(2, standard.getEnumItems().size());
        assertEquals("VALUE1", standard.getEnumItems().get(0).getValue());
        assertEquals("VALUE2", standard.getEnumItems().get(1).getValue());
    }

    @Test
    void setter_and_getter_shouldWorkForCodeItem() {
        /**
         * 场景：设置子表关联 DataStandardCodeItem
         * 测试内容：验证 DataStandardCodeItem 类型的关联对象存取
         * 断言：子表对象正确存取
         */
        DataStandard standard = new DataStandard();
        DataStandardCodeItem codeItem = new DataStandardCodeItem();
        codeItem.setId(1L);
        codeItem.setStandardId(100L);
        codeItem.setFormat("[A-Z0000]");
        codeItem.setPrefix("P");
        codeItem.setLength(6);
        codeItem.setExample("A12345");

        standard.setCodeItem(codeItem);

        assertNotNull(standard.getCodeItem());
        assertEquals(1L, standard.getCodeItem().getId());
        assertEquals(100L, standard.getCodeItem().getStandardId());
        assertEquals("[A-Z0000]", standard.getCodeItem().getFormat());
        assertEquals("P", standard.getCodeItem().getPrefix());
        assertEquals(6, standard.getCodeItem().getLength());
        assertEquals("A12345", standard.getCodeItem().getExample());
    }

    @Test
    void setter_and_getter_shouldWorkForStringItem() {
        /**
         * 场景：设置子表关联 DataStandardStringItem
         * 测试内容：验证 DataStandardStringItem 类型的关联对象存取
         * 断言：子表对象正确存取
         */
        DataStandard standard = new DataStandard();
        DataStandardStringItem stringItem = new DataStandardStringItem();
        stringItem.setId(1L);
        stringItem.setStandardId(100L);
        stringItem.setMinLength(1);
        stringItem.setMaxLength(255);
        stringItem.setPattern("^[a-zA-Z]+$");
        stringItem.setExample("HelloWorld");

        standard.setStringItem(stringItem);

        assertNotNull(standard.getStringItem());
        assertEquals(1L, standard.getStringItem().getId());
        assertEquals(100L, standard.getStringItem().getStandardId());
        assertEquals(1, standard.getStringItem().getMinLength());
        assertEquals(255, standard.getStringItem().getMaxLength());
        assertEquals("^[a-zA-Z]+$", standard.getStringItem().getPattern());
        assertEquals("HelloWorld", standard.getStringItem().getExample());
    }

    @Test
    void setter_and_getter_shouldWorkForNumberItem() {
        /**
         * 场景：设置子表关联 DataStandardNumberItem
         * 测试内容：验证 DataStandardNumberItem 类型的关联对象存取
         * 断言：子表对象正确存取
         */
        DataStandard standard = new DataStandard();
        DataStandardNumberItem numberItem = new DataStandardNumberItem();
        numberItem.setId(1L);
        numberItem.setStandardId(100L);
        numberItem.setMinValue(BigDecimal.ZERO);
        numberItem.setMaxValue(new BigDecimal("999.99"));
        numberItem.setDecimalPlaces(2);
        numberItem.setExample("123.45");

        standard.setNumberItem(numberItem);

        assertNotNull(standard.getNumberItem());
        assertEquals(1L, standard.getNumberItem().getId());
        assertEquals(100L, standard.getNumberItem().getStandardId());
        assertEquals(BigDecimal.ZERO, standard.getNumberItem().getMinValue());
        assertEquals(new BigDecimal("999.99"), standard.getNumberItem().getMaxValue());
        assertEquals(2, standard.getNumberItem().getDecimalPlaces());
        assertEquals("123.45", standard.getNumberItem().getExample());
    }

    @Test
    void equals_shouldReturnTrueForIdenticalObjects() {
        /**
         * 场景：两个 DataStandard 对象所有字段值相同
         * 测试内容：验证 equals 方法正确性（由 Lombok @Data 生成）
         * 断言：两个对象 equals 返回 true，hashCode 相等
         */
        DataStandard standard1 = new DataStandard();
        standard1.setId(1L);
        standard1.setCode("DS001");
        standard1.setName("标准名称");
        standard1.setType("ENUM");
        standard1.setDescription("描述");
        standard1.setOwnerId(100L);
        standard1.setOwnerName("张三");
        standard1.setDeleted(0);

        DataStandard standard2 = new DataStandard();
        standard2.setId(1L);
        standard2.setCode("DS001");
        standard2.setName("标准名称");
        standard2.setType("ENUM");
        standard2.setDescription("描述");
        standard2.setOwnerId(100L);
        standard2.setOwnerName("张三");
        standard2.setDeleted(0);

        assertEquals(standard1, standard2);
        assertEquals(standard1.hashCode(), standard2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        /**
         * 场景：两个 DataStandard 对象字段值不同
         * 测试内容：验证 equals 方法区分不同对象
         * 断言：两个对象 equals 返回 false
         */
        DataStandard standard1 = new DataStandard();
        standard1.setId(1L);
        standard1.setCode("DS001");

        DataStandard standard2 = new DataStandard();
        standard2.setId(2L);
        standard2.setCode("DS002");

        assertNotEquals(standard1, standard2);
    }

    @Test
    void toString_shouldContainAllFields() {
        /**
         * 场景：调用 toString 方法
         * 测试内容：验证 toString 包含所有字段信息
         * 断言：toString 字符串包含各字段名称和值
         */
        DataStandard standard = new DataStandard();
        standard.setId(1L);
        standard.setCode("DS001");
        standard.setName("测试标准");
        standard.setType("STRING");
        standard.setDescription("描述");
        standard.setOwnerId(100L);
        standard.setOwnerName("李四");

        String str = standard.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("code=DS001"));
        assertTrue(str.contains("name=测试标准"));
        assertTrue(str.contains("type=STRING"));
        assertTrue(str.contains("description=描述"));
        assertTrue(str.contains("ownerId=100"));
        assertTrue(str.contains("ownerName=李四"));
    }
}
