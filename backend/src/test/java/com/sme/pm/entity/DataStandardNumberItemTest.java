package com.sme.pm.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStandardNumberItem 实体单元测试
 *
 * 测试场景：验证 DataStandardNumberItem 实体的所有字段的 getter/setter、equals/hashCode/toString 方法
 * 测试内容：
 * - Long 类型 id 字段的存取
 * - Long 类型 standardId 关联字段的存取
 * - BigDecimal 类型 minValue、maxValue、example 字段的存取
 * - Integer 类型 decimalPlaces 字段的存取（带默认值）
 *
 * 断言：
 * - 所有字段的 getter 返回预期值
 * - setter 后 getter 能正确返回设置的值
 * - decimalPlaces 默认值为 0
 * - equals 和 hashCode 基于所有字段（由 Lombok @Data 生成）
 * - toString 包含所有字段的字符串表示
 */
class DataStandardNumberItemTest {

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        /**
         * 场景：使用无参构造函数创建实例
         * 测试内容：验证默认值为 null 或默认值字段的值
         * 断言：所有字段为 null，decimalPlaces 为 0
         */
        DataStandardNumberItem item = new DataStandardNumberItem();

        assertNull(item.getId());
        assertNull(item.getStandardId());
        assertNull(item.getMinValue());
        assertNull(item.getMaxValue());
        assertEquals(0, item.getDecimalPlaces());
        assertNull(item.getExample());
    }

    @Test
    void setter_and_getter_shouldWorkForAllFields() {
        /**
         * 场景：使用 setter 设置所有字段值后，通过 getter 获取
         * 测试内容：验证所有字段的存取功能
         * 断言：getter 返回与 setter 设置一致的值
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        BigDecimal minVal = new BigDecimal("0.00");
        BigDecimal maxVal = new BigDecimal("9999.99");

        item.setId(1L);
        item.setStandardId(100L);
        item.setMinValue(minVal);
        item.setMaxValue(maxVal);
        item.setDecimalPlaces(2);
        item.setExample("123.45");

        assertEquals(1L, item.getId());
        assertEquals(100L, item.getStandardId());
        assertEquals(minVal, item.getMinValue());
        assertEquals(maxVal, item.getMaxValue());
        assertEquals(2, item.getDecimalPlaces());
        assertEquals("123.45", item.getExample());
    }

    @Test
    void decimalPlaces_shouldHaveDefaultValueOfZero() {
        /**
         * 场景：验证 decimalPlaces 字段的默认值
         * 测试内容：检查未设置 decimalPlaces 时的默认值
         * 断言：decimalPlaces 默认为 0
         */
        DataStandardNumberItem item = new DataStandardNumberItem();

        assertEquals(0, item.getDecimalPlaces());
    }

    @Test
    void decimalPlaces_shouldAcceptCustomValue() {
        /**
         * 场景：设置 decimalPlaces 为非默认值
         * 测试内容：验证 decimalPlaces 可被设置为任意整数值
         * 断言：设置后 getter 返回设置的值
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        item.setDecimalPlaces(4);

        assertEquals(4, item.getDecimalPlaces());
    }

    @Test
    void minValue_shouldAcceptZero() {
        /**
         * 场景：设置 minValue 为零
         * 测试内容：验证 BigDecimal 类型 minValue 可接受零值
         * 断言：设置后 getter 返回 BigDecimal.ZERO
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        item.setMinValue(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, item.getMinValue());
    }

    @Test
    void minValue_shouldAcceptNegative() {
        /**
         * 场景：设置 minValue 为负数
         * 测试内容：验证 BigDecimal 类型 minValue 可接受负值
         * 断言：设置后 getter 返回负的 BigDecimal 值
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        BigDecimal negativeMin = new BigDecimal("-1000.50");
        item.setMinValue(negativeMin);

        assertEquals(negativeMin, item.getMinValue());
    }

    @Test
    void maxValue_shouldAcceptLargeValue() {
        /**
         * 场景：设置 maxValue 为较大值
         * 测试内容：验证 BigDecimal 类型 maxValue 可接受大数值
         * 断言：设置后 getter 返回正确的 BigDecimal 值
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        BigDecimal largeMax = new BigDecimal("999999999.99");
        item.setMaxValue(largeMax);

        assertEquals(largeMax, item.getMaxValue());
    }

    @Test
    void minValue_and_maxValue_shouldSupportPrecision() {
        /**
         * 场景：验证高精度数值的存取
         * 测试内容：BigDecimal 类型支持高精度数值
         * 断言：高精度数值正确存取
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        BigDecimal preciseMin = new BigDecimal("0.0000001");
        BigDecimal preciseMax = new BigDecimal("0.9999999");

        item.setMinValue(preciseMin);
        item.setMaxValue(preciseMax);

        assertEquals(preciseMin, item.getMinValue());
        assertEquals(preciseMax, item.getMaxValue());
    }

    @Test
    void example_shouldHandleNumericString() {
        /**
         * 场景：设置 example 为数字字符串
         * 测试内容：验证 example 字段可接受数字格式字符串
         * 断言：example 正确存取
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        item.setExample("3.14159");

        assertEquals("3.14159", item.getExample());
    }

    @Test
    void equals_shouldReturnTrueForIdenticalObjects() {
        /**
         * 场景：两个 DataStandardNumberItem 对象所有字段值相同
         * 测试内容：验证 equals 方法正确性（由 Lombok @Data 生成）
         * 断言：两个对象 equals 返回 true，hashCode 相等
         */
        DataStandardNumberItem item1 = new DataStandardNumberItem();
        item1.setId(1L);
        item1.setStandardId(100L);
        item1.setMinValue(new BigDecimal("0"));
        item1.setMaxValue(new BigDecimal("100"));
        item1.setDecimalPlaces(0);
        item1.setExample("50");

        DataStandardNumberItem item2 = new DataStandardNumberItem();
        item2.setId(1L);
        item2.setStandardId(100L);
        item2.setMinValue(new BigDecimal("0"));
        item2.setMaxValue(new BigDecimal("100"));
        item2.setDecimalPlaces(0);
        item2.setExample("50");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentObjects() {
        /**
         * 场景：两个 DataStandardNumberItem 对象字段值不同
         * 测试内容：验证 equals 方法区分不同对象
         * 断言：两个对象 equals 返回 false
         */
        DataStandardNumberItem item1 = new DataStandardNumberItem();
        item1.setId(1L);
        item1.setDecimalPlaces(2);

        DataStandardNumberItem item2 = new DataStandardNumberItem();
        item2.setId(2L);
        item2.setDecimalPlaces(4);

        assertNotEquals(item1, item2);
    }

    @Test
    void toString_shouldContainAllFields() {
        /**
         * 场景：调用 toString 方法
         * 测试内容：验证 toString 包含所有字段信息
         * 断言：toString 字符串包含各字段名称和值
         */
        DataStandardNumberItem item = new DataStandardNumberItem();
        item.setId(1L);
        item.setStandardId(100L);
        item.setMinValue(new BigDecimal("0"));
        item.setMaxValue(new BigDecimal("999.99"));
        item.setDecimalPlaces(2);
        item.setExample("123.45");

        String str = item.toString();

        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("standardId=100"));
        assertTrue(str.contains("minValue=0"));
        assertTrue(str.contains("maxValue=999.99"));
        assertTrue(str.contains("decimalPlaces=2"));
        assertTrue(str.contains("example=123.45"));
    }
}
