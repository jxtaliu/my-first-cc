package com.sme.pm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStandardCodeItem 实体单元测试
 *
 * 测试场景：验证 DataStandardCodeItem 实体的所有字段的 getter/setter、equals/hashCode/toString 方法
 * 测试内容：
 * - Long 类型 id 字段的存取
 * - Long 类型 standardId 关联字段的存取
 * - String 类型 format、prefix、example 字段的存取
 * - Integer 类型 length 字段的存取
 *
 * 断言：
 * - 所有字段的 getter 返回预期值
 * - setter 后 getter 能正确返回设置的值
 * - equals 和 hashCode 基于所有字段（由 Lombok @Data 生成）
 * - toString 包含所有字段的字符串表示
 */
class DataStandardCodeItemTest {

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        /**
         * 场景：使用无参构造函数创建实例
         * 测试内容：验证默认值为 null 或 0
         * 断言：所有字段为 null
         */
        DataStandardCodeItem item = new DataStandardCodeItem();

        assertNull(item.getId());
        assertNull(item.getStandardId());
        assertNull(item.getFormat());
        assertNull(item.getPrefix());
        assertNull(item.getLength());
        assertNull(item.getExample());
    }

    @Test
    void setter_and_getter_shouldWorkForAllFields() {
        /**
         * 场景：使用 setter 设置所有字段值后，通过 getter 获取
         * 测试内容：验证所有字段的存取功能
         * 断言：getter 返回与 setter 设置一致的值
         */
        DataStandardCodeItem item = new DataStandardCodeItem();

        item.setId(1L);
        item.setStandardId(100L);
        item.setFormat("[A-Z0000]");
        item.setPrefix("PRJ");
        item.setLength(10);
        item.setExample("PRJ20240001");

        assertEquals(1L, item.getId());
        assertEquals(100L, item.getStandardId());
        assertEquals("[A-Z0000]", item.getFormat());
        assertEquals("PRJ", item.getPrefix());
        assertEquals(10, item.getLength());
        assertEquals("PRJ20240001", item.getExample());
    }

    @Test
    void setter_and_getter_shouldWorkForId() {
        /**
         * 场景：设置 id 字段
         * 测试内容：验证 Long 类型 id 的存取
         * 断言：id 正确存取
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setId(999L);

        assertEquals(999L, item.getId());
    }

    @Test
    void setter_and_getter_shouldWorkForStandardId() {
        /**
         * 场景：设置 standardId 关联字段
         * 测试内容：验证 Long 类型 standardId 的存取
         * 断言：standardId 正确存取
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setStandardId(200L);

        assertEquals(200L, item.getStandardId());
    }

    @Test
    void setter_and_getter_shouldWorkForFormat() {
        /**
         * 场景：设置 format 字段
         * 测试内容：验证 String 类型 format 的存取（如 [A-Z0000]）
         * 断言：format 正确存取
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setFormat("[A-Z]{3}[0-9]{4}");

        assertEquals("[A-Z]{3}[0-9]{4}", item.getFormat());
    }

    @Test
    void setter_and_getter_shouldWorkForPrefix() {
        /**
         * 场景：设置 prefix 字段
         * 测试内容：验证 String 类型 prefix 的存取
         * 断言：prefix 正确存取
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setPrefix("CODE");

        assertEquals("CODE", item.getPrefix());
    }

    @Test
    void setter_and_getter_shouldWorkForLength() {
        /**
         * 场景：设置 length 字段
         * 测试内容：验证 Integer 类型 length 的存取
         * 断言：length 正确存取
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setLength(8);

        assertEquals(8, item.getLength());
    }

    @Test
    void setter_and_getter_shouldWorkForExample() {
        /**
         * 场景：设置 example 字段
         * 测试内容：验证 String 类型 example 的存取
         * 断言：example 正确存取
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setExample("ABC12345");

        assertEquals("ABC12345", item.getExample());
    }

    @Test
    void equals_shouldReturnTrueForIdenticalObjects() {
        /**
         * 场景：两个 DataStandardCodeItem 对象所有字段值相同
         * 测试内容：验证 equals 方法正确性（由 Lombok @Data 生成）
         * 断言：两个对象 equals 返回 true，hashCode 相等
         */
        DataStandardCodeItem item1 = new DataStandardCodeItem();
        item1.setId(1L);
        item1.setStandardId(100L);
        item1.setFormat("[A-Z0000]");
        item1.setPrefix("P");
        item1.setLength(6);
        item1.setExample("A12345");

        DataStandardCodeItem item2 = new DataStandardCodeItem();
        item2.setId(1L);
        item2.setStandardId(100L);
        item2.setFormat("[A-Z0000]");
        item2.setPrefix("P");
        item2.setLength(6);
        item2.setExample("A12345");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        /**
         * 场景：两个 DataStandardCodeItem 对象字段值不同
         * 测试内容：验证 equals 方法区分不同对象
         * 断言：两个对象 equals 返回 false
         */
        DataStandardCodeItem item1 = new DataStandardCodeItem();
        item1.setId(1L);
        item1.setFormat("[A-Z0000]");

        DataStandardCodeItem item2 = new DataStandardCodeItem();
        item2.setId(2L);
        item2.setFormat("[0-9]{6}");

        assertNotEquals(item1, item2);
    }

    @Test
    void toString_shouldContainAllFields() {
        /**
         * 场景：调用 toString 方法
         * 测试内容：验证 toString 包含所有字段信息
         * 断言：toString 字符串包含各字段名称和值
         */
        DataStandardCodeItem item = new DataStandardCodeItem();
        item.setId(1L);
        item.setStandardId(100L);
        item.setFormat("[A-Z0000]");
        item.setPrefix("TEST");
        item.setLength(8);
        item.setExample("TEST1234");

        String str = item.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("standardId=100"));
        assertTrue(str.contains("format=[A-Z0000]"));
        assertTrue(str.contains("prefix=TEST"));
        assertTrue(str.contains("length=8"));
        assertTrue(str.contains("example=TEST1234"));
    }
}
