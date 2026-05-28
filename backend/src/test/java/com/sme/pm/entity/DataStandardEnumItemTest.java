package com.sme.pm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStandardEnumItem 实体单元测试
 *
 * 测试场景：验证 DataStandardEnumItem 实体的所有字段的 getter/setter、equals/hashCode/toString 方法
 * 测试内容：
 * - Long 类型 id 字段的存取
 * - Long 类型 standardId 关联字段的存取
 * - String 类型 value、label、description 字段的存取
 * - Integer 类型 sortOrder 字段的存取（带默认值）
 *
 * 断言：
 * - 所有字段的 getter 返回预期值
 * - setter 后 getter 能正确返回设置的值
 * - sortOrder 默认值为 0
 * - equals 和 hashCode 基于所有字段（由 Lombok @Data 生成）
 * - toString 包含所有字段的字符串表示
 */
class DataStandardEnumItemTest {

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        /**
         * 场景：使用无参构造函数创建实例
         * 测试内容：验证默认值为 null 或 0
         * 断言：所有字段为 null，sortOrder 为默认值 0
         */
        DataStandardEnumItem item = new DataStandardEnumItem();

        assertNull(item.getId());
        assertNull(item.getStandardId());
        assertNull(item.getValue());
        assertNull(item.getLabel());
        assertEquals(0, item.getSortOrder());
        assertNull(item.getDescription());
    }

    @Test
    void setter_and_getter_shouldWorkForAllFields() {
        /**
         * 场景：使用 setter 设置所有字段值后，通过 getter 获取
         * 测试内容：验证所有字段的存取功能
         * 断言：getter 返回与 setter 设置一致的值
         */
        DataStandardEnumItem item = new DataStandardEnumItem();

        item.setId(1L);
        item.setStandardId(100L);
        item.setValue("ACTIVE");
        item.setLabel("激活");
        item.setSortOrder(1);
        item.setDescription("表示激活状态");

        assertEquals(1L, item.getId());
        assertEquals(100L, item.getStandardId());
        assertEquals("ACTIVE", item.getValue());
        assertEquals("激活", item.getLabel());
        assertEquals(1, item.getSortOrder());
        assertEquals("表示激活状态", item.getDescription());
    }

    @Test
    void sortOrder_shouldHaveDefaultValueOfZero() {
        /**
         * 场景：验证 sortOrder 字段的默认值
         * 测试内容：检查未设置 sortOrder 时的默认值
         * 断言：sortOrder 默认为 0
         */
        DataStandardEnumItem item = new DataStandardEnumItem();

        assertEquals(0, item.getSortOrder());
    }

    @Test
    void sortOrder_shouldAcceptCustomValue() {
        /**
         * 场景：设置 sortOrder 为非默认值
         * 测试内容：验证 sortOrder 可被设置为任意整数值
         * 断言：设置后 getter 返回设置的值
         */
        DataStandardEnumItem item = new DataStandardEnumItem();
        item.setSortOrder(99);

        assertEquals(99, item.getSortOrder());
    }

    @Test
    void equals_shouldReturnTrueForIdenticalObjects() {
        /**
         * 场景：两个 DataStandardEnumItem 对象所有字段值相同
         * 测试内容：验证 equals 方法正确性（由 Lombok @Data 生成）
         * 断言：两个对象 equals 返回 true，hashCode 相等
         */
        DataStandardEnumItem item1 = new DataStandardEnumItem();
        item1.setId(1L);
        item1.setStandardId(100L);
        item1.setValue("STATUS_A");
        item1.setLabel("状态A");
        item1.setSortOrder(1);
        item1.setDescription("描述");

        DataStandardEnumItem item2 = new DataStandardEnumItem();
        item2.setId(1L);
        item2.setStandardId(100L);
        item2.setValue("STATUS_A");
        item2.setLabel("状态A");
        item2.setSortOrder(1);
        item2.setDescription("描述");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        /**
         * 场景：两个 DataStandardEnumItem 对象字段值不同
         * 测试内容：验证 equals 方法区分不同对象
         * 断言：两个对象 equals 返回 false
         */
        DataStandardEnumItem item1 = new DataStandardEnumItem();
        item1.setId(1L);
        item1.setValue("VALUE1");

        DataStandardEnumItem item2 = new DataStandardEnumItem();
        item2.setId(2L);
        item2.setValue("VALUE2");

        assertNotEquals(item1, item2);
    }

    @Test
    void toString_shouldContainAllFields() {
        /**
         * 场景：调用 toString 方法
         * 测试内容：验证 toString 包含所有字段信息
         * 断言：toString 字符串包含各字段名称和值
         */
        DataStandardEnumItem item = new DataStandardEnumItem();
        item.setId(1L);
        item.setStandardId(100L);
        item.setValue("ENUM_VALUE");
        item.setLabel("枚举值");
        item.setSortOrder(5);
        item.setDescription("枚举值描述");

        String str = item.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("standardId=100"));
        assertTrue(str.contains("value=ENUM_VALUE"));
        assertTrue(str.contains("label=枚举值"));
        assertTrue(str.contains("sortOrder=5"));
        assertTrue(str.contains("description=枚举值描述"));
    }
}
