package com.sme.pm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStandardStringItem 实体单元测试
 *
 * 测试场景：验证 DataStandardStringItem 实体的所有字段的 getter/setter、equals/hashCode/toString 方法
 * 测试内容：
 * - Long 类型 id 字段的存取
 * - Long 类型 standardId 关联字段的存取
 * - Integer 类型 minLength、maxLength 字段的存取（带默认值）
 * - String 类型 pattern、example 字段的存取
 *
 * 断言：
 * - 所有字段的 getter 返回预期值
 * - setter 后 getter 能正确返回设置的值
 * - minLength 默认值为 1，maxLength 默认值为 255
 * - equals 和 hashCode 基于所有字段（由 Lombok @Data 生成）
 * - toString 包含所有字段的字符串表示
 */
class DataStandardStringItemTest {

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        /**
         * 场景：使用无参构造函数创建实例
         * 测试内容：验证默认值为 null 或默认值字段的值
         * 断言：所有字段为 null，minLength 为 1，maxLength 为 255
         */
        DataStandardStringItem item = new DataStandardStringItem();

        assertNull(item.getId());
        assertNull(item.getStandardId());
        assertEquals(1, item.getMinLength());
        assertEquals(255, item.getMaxLength());
        assertNull(item.getPattern());
        assertNull(item.getExample());
    }

    @Test
    void setter_and_getter_shouldWorkForAllFields() {
        /**
         * 场景：使用 setter 设置所有字段值后，通过 getter 获取
         * 测试内容：验证所有字段的存取功能
         * 断言：getter 返回与 setter 设置一致的值
         */
        DataStandardStringItem item = new DataStandardStringItem();

        item.setId(1L);
        item.setStandardId(100L);
        item.setMinLength(5);
        item.setMaxLength(100);
        item.setPattern("^[a-zA-Z]+$");
        item.setExample("HelloWorld");

        assertEquals(1L, item.getId());
        assertEquals(100L, item.getStandardId());
        assertEquals(5, item.getMinLength());
        assertEquals(100, item.getMaxLength());
        assertEquals("^[a-zA-Z]+$", item.getPattern());
        assertEquals("HelloWorld", item.getExample());
    }

    @Test
    void minLength_shouldHaveDefaultValueOfOne() {
        /**
         * 场景：验证 minLength 字段的默认值
         * 测试内容：检查未设置 minLength 时的默认值
         * 断言：minLength 默认为 1
         */
        DataStandardStringItem item = new DataStandardStringItem();

        assertEquals(1, item.getMinLength());
    }

    @Test
    void maxLength_shouldHaveDefaultValueOf255() {
        /**
         * 场景：验证 maxLength 字段的默认值
         * 测试内容：检查未设置 maxLength 时的默认值
         * 断言：maxLength 默认为 255
         */
        DataStandardStringItem item = new DataStandardStringItem();

        assertEquals(255, item.getMaxLength());
    }

    @Test
    void minLength_shouldAcceptZeroValue() {
        /**
         * 场景：设置 minLength 为 0
         * 测试内容：验证 minLength 可接受 0 值（表示无最小长度限制）
         * 断言：设置后 getter 返回 0
         */
        DataStandardStringItem item = new DataStandardStringItem();
        item.setMinLength(0);

        assertEquals(0, item.getMinLength());
    }

    @Test
    void maxLength_shouldAcceptLargeValue() {
        /**
         * 场景：设置 maxLength 为较大值
         * 测试内容：验证 maxLength 可接受较大整数值
         * 断言：设置后 getter 返回设置的值
         */
        DataStandardStringItem item = new DataStandardStringItem();
        item.setMaxLength(10000);

        assertEquals(10000, item.getMaxLength());
    }

    @Test
    void pattern_shouldAcceptNull() {
        /**
         * 场景：设置 pattern 为 null
         * 测试内容：验证 pattern 可接受 null 值（表示无正则限制）
         * 断言：设置后 getter 返回 null
         */
        DataStandardStringItem item = new DataStandardStringItem();
        item.setPattern(null);

        assertNull(item.getPattern());
    }

    @Test
    void example_shouldAcceptNull() {
        /**
         * 场景：设置 example 为 null
         * 测试内容：验证 example 可接受 null 值
         * 断言：设置后 getter 返回 null
         */
        DataStandardStringItem item = new DataStandardStringItem();
        item.setExample(null);

        assertNull(item.getExample());
    }

    @Test
    void equals_shouldReturnTrueForIdenticalObjects() {
        /**
         * 场景：两个 DataStandardStringItem 对象所有字段值相同
         * 测试内容：验证 equals 方法正确性（由 Lombok @Data 生成）
         * 断言：两个对象 equals 返回 true，hashCode 相等
         */
        DataStandardStringItem item1 = new DataStandardStringItem();
        item1.setId(1L);
        item1.setStandardId(100L);
        item1.setMinLength(1);
        item1.setMaxLength(255);
        item1.setPattern("^[a-z]+$");
        item1.setExample("test");

        DataStandardStringItem item2 = new DataStandardStringItem();
        item2.setId(1L);
        item2.setStandardId(100L);
        item2.setMinLength(1);
        item2.setMaxLength(255);
        item2.setPattern("^[a-z]+$");
        item2.setExample("test");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        /**
         * 场景：两个 DataStandardStringItem 对象字段值不同
         * 测试内容：验证 equals 方法区分不同对象
         * 断言：两个对象 equals 返回 false
         */
        DataStandardStringItem item1 = new DataStandardStringItem();
        item1.setId(1L);
        item1.setMinLength(1);

        DataStandardStringItem item2 = new DataStandardStringItem();
        item2.setId(2L);
        item2.setMinLength(5);

        assertNotEquals(item1, item2);
    }

    @Test
    void toString_shouldContainAllFields() {
        /**
         * 场景：调用 toString 方法
         * 测试内容：验证 toString 包含所有字段信息
         * 断言：toString 字符串包含各字段名称和值
         */
        DataStandardStringItem item = new DataStandardStringItem();
        item.setId(1L);
        item.setStandardId(100L);
        item.setMinLength(2);
        item.setMaxLength(50);
        item.setPattern("[A-Z]+");
        item.setExample("EXAMPLE");

        String str = item.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("standardId=100"));
        assertTrue(str.contains("minLength=2"));
        assertTrue(str.contains("maxLength=50"));
        assertTrue(str.contains("pattern=[A-Z]+"));
        assertTrue(str.contains("example=EXAMPLE"));
    }
}
