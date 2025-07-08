package ru.advantum.commons.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AggregatorTest {

    private List<TestEmployee> employees;

    @BeforeEach
    void setUp() {
        employees = Arrays.asList(
                new TestEmployee(20, new BigDecimal("100.00")), // Duplicate age
                new TestEmployee(20, new BigDecimal("200.00")),
                new TestEmployee(30, new BigDecimal("300.00")),
                new TestEmployee(40, new BigDecimal("400.00")),
                new TestEmployee(50, new BigDecimal("500.00"))
        );
    }

    @Test
    void testEmptyCollection() {
        AggregationResult result = Aggregator.of(Collections.<TestEmployee>emptyList())
                .count("count")
                .sum("sum", TestEmployee::getSalary)
                .distinct("distinct", TestEmployee::getAge)
                .min("min", TestEmployee::getAge)
                .max("max", TestEmployee::getAge)
                .median("median", TestEmployee::getSalary)
                .aggregate();

        assertEquals(0L, result.getCount("count"));
        assertEquals(0, result.getSum("sumAge", Integer.class));
        assertEquals(BigDecimal.ZERO, result.getSum("sum"));
        assertEquals(Collections.emptySet(), result.getDistinct("distinct"));
        assertNull(result.getMin("min"), "Min should be null for empty collection");
        assertNull(result.getMax("max"), "Max should be null for empty collection");
        assertNull(result.getMedian("median"), "Median should be null for empty collection");
    }

    @Test
    void testAllAggregationsSequentially() {
        AggregationResult result = Aggregator.of(employees)
                .count("count")
                .distinct("distinctAges", TestEmployee::getAge)
                .sum("sum", TestEmployee::getSalary)
                .sum("sumAge", TestEmployee::getAge)
                .average("avgAge", TestEmployee::getAge)
                .min("minSalary", TestEmployee::getSalary)
                .max("maxAge", TestEmployee::getAge)
                .min("minAge", TestEmployee::getAge)
                .median("medianSalary", TestEmployee::getSalary)
                .aggregate();

        assertEquals(5L, result.getCount("count"));
        assertEquals(Set.of(20, 30, 40, 50), result.getDistinct("distinctAges"));
        assertEquals(new BigDecimal("1500.00"), result.getSum("sum"));
        assertEquals(new BigDecimal("160"), result.getSum("sumAge"));
        assertEquals(160, result.getSum("sumAge", Integer.class));
        assertEquals(32.0, result.getAverage("avgAge"));
        assertEquals(new BigDecimal("100.00"), result.getMin("minSalary"));
        assertEquals("50", result.getMax("maxAge").toString());
        assertEquals(50, Optional.of(result.getMax("maxAge")).get());
        assertEquals(20, Optional.of(result.getMax("minAge")).get());
        assertEquals(new BigDecimal("300.00"), result.getMedian("medianSalary"));
    }

    @Test
    void testAllAggregationsParallel() {
        AggregationResult result = Aggregator.of(employees)
                .parallel()
                .count("count")
                .distinct("distinctAges", TestEmployee::getAge)
                .sum("sum", TestEmployee::getSalary)
                .average("avgAge", TestEmployee::getAge)
                .min("minSalary", TestEmployee::getSalary)
                .max("maxAge", TestEmployee::getAge)
                .median("medianSalary", TestEmployee::getSalary)
                .aggregate();

        assertEquals(5L, result.getCount("count"));
        assertEquals(Set.of(20, 30, 40, 50), result.getDistinct("distinctAges"));
        assertEquals(new BigDecimal("1500.00"), result.getSum("sum"));
        assertEquals(32.0, result.getAverage("avgAge"));
        assertEquals(new BigDecimal("100.00"), result.getMin("minSalary"));
        assertEquals("50", result.getMax("maxAge").toString());
        assertEquals(new BigDecimal("300.00"), result.getMedian("medianSalary"));
    }

    // Вспомогательный класс для тестов
    private static class TestEmployee {
        private final int age;
        private final BigDecimal salary;

        public TestEmployee(int age, BigDecimal salary) {
            this.age = age;
            this.salary = salary;
        }

        public int getAge() {
            return age;
        }

        public BigDecimal getSalary() {
            return salary;
        }
    }
}
