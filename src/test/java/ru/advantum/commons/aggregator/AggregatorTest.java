package ru.advantum.commons.aggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class AggregatorTest {

    private List<TestEmployee> employees;

    @BeforeEach
    void setUp() {
        employees = Arrays.asList(
                new TestEmployee(20, new BigDecimal("100.00"), "A"),
                new TestEmployee(20, new BigDecimal("200.00"), "B"), // same age, different group
                new TestEmployee(30, new BigDecimal("300.00"), "A"),
                new TestEmployee(40, new BigDecimal("400.00"), "B"),
                new TestEmployee(50, new BigDecimal("500.00"), "A"),
                new TestEmployee(20, new BigDecimal("150.00"), "A") // same age and group as first
        );
    }

    @Test
    void testSimpleAggregation() {
        AggregationResult result = Aggregator.of(employees)
                .count("count")
                .distinct("distinctGroups", TestEmployee::getGroup)
                .aggregate();

        assertEquals(6L, result.getCount("count"));
        assertEquals(Set.of("A", "B"), result.getDistinct("distinctGroups"));
    }

    @Test
    void testGroupingBySingleField() {
        Map<List<Object>, AggregationResult> result = Aggregator.groupBy(employees, TestEmployee::getGroup)
                .count("count")
                .sum("sumSalary", TestEmployee::getSalary)
                .max("maxAge", TestEmployee::getAge)
                .aggregate();

        assertNotNull(result);
        assertEquals(2, result.size());

        AggregationResult groupA = result.get(List.of("A"));
        assertNotNull(groupA);
        assertEquals(4L, groupA.getCount("count"));
        assertEquals(new BigDecimal("1050.00"),(groupA.getSum("sumSalary")));
        assertEquals(50, (Integer) groupA.getMax("maxAge"));

        AggregationResult groupB = result.get(List.of("B"));
        assertNotNull(groupB);
        assertEquals(2L, groupB.getCount("count"));
        assertEquals(new BigDecimal("600.00"),groupB.getSum("sumSalary"));
        assertEquals(40, (Integer) groupB.getMax("maxAge"));
    }

    @Test
    void testGroupingByMultipleFields() {
        Map<List<Object>, AggregationResult> result = Aggregator.groupBy(employees, TestEmployee::getGroup, TestEmployee::getAge)
                .count("count")
                .sum("sumSalary", TestEmployee::getSalary)
                .aggregate();

        assertNotNull(result);
        assertEquals(5, result.size()); // (A, 20), (A, 30), (A, 50), (B, 20), (B, 40) -> wait, 5 groups

        // Check group (A, 20)
        AggregationResult groupA20 = result.get(List.of("A", 20));
        assertNotNull(groupA20);
        assertEquals(2L, groupA20.getCount("count"));
        assertEquals(new BigDecimal("250.00"),(groupA20.getSum("sumSalary")));

        // Check group (A, 30)
        AggregationResult groupA30 = result.get(List.of("A", 30));
        assertNotNull(groupA30);
        assertEquals(1L, groupA30.getCount("count"));
    }


    @Test
    void testGroupingByObjectIdentity() {
        Map<List<Object>, AggregationResult> result = Aggregator.groupBy(employees, Function.identity())
                .count("count")
                .sum("sumSalary", TestEmployee::getSalary)
                .aggregate();

        assertNotNull(result);
        assertEquals(employees.size(), result.size());

        TestEmployee employeeToTest = new TestEmployee(30, new BigDecimal("300.00"), "A");
        AggregationResult employeeResult = result.get(List.of(employeeToTest));
        assertNotNull(employeeResult);
        assertEquals(1L, employeeResult.getCount("count"));
        assertEquals(new BigDecimal("300.00"),(employeeResult.getSum("sumSalary")));
    }

    private static class TestEmployee {
        private final int age;
        private final BigDecimal salary;
        private final String group;

        public TestEmployee(int age, BigDecimal salary, String group) {
            this.age = age; this.salary = salary; this.group = group;
        }
        public int getAge() { return age; }
        public BigDecimal getSalary() { return salary; }
        public String getGroup() { return group; }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            TestEmployee that = (TestEmployee) o;
            return age == that.age && salary.equals(that.salary) && group.equals(that.group);
        }

        @Override
        public int hashCode() {
            int result = age;
            result = 31 * result + salary.hashCode();
            result = 31 * result + group.hashCode();
            return result;
        }
    }
}
