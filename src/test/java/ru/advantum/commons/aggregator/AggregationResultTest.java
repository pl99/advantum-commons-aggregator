package ru.advantum.commons.aggregator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AggregationResultTest {

    @Test
    void testPutAndGet() {
        AggregationResult result = new AggregationResult();
        result.put("testKey", "testValue");
        assertEquals("testValue", result.get("testKey"));
    }

    @Test
    void testGetSum() {
        AggregationResult result = new AggregationResult();
        result.put("sum", 100.0);
        assertEquals(100.0, result.get("sum"), 0.001);
    }

    @Test
    void testGetAverage() {
        AggregationResult result = new AggregationResult();
        result.put("average", 50.0);
        assertEquals(50.0, result.get("average"), 0.001);
    }

    @Test
    void testGetMin() {
        AggregationResult result = new AggregationResult();
        result.put("min", 10.0);
        assertEquals(10.0, result.get("min"), 0.001);
    }

    @Test
    void testGetMax() {
        AggregationResult result = new AggregationResult();
        result.put("max", 90.0);
        assertEquals(90.0, result.get("max"), 0.001);
    }

    @Test
    void testGetCount() {
        AggregationResult result = new AggregationResult();
        result.put("count", 5L);
        assertEquals(5L, (Long) result.get("count"));
    }

    @Test
    void testGetMedian() {
        AggregationResult result = new AggregationResult();
        result.put("median", 25.0);
        assertEquals(25.0, result.get("median"), 0.001);
    }

    @Test
    void testNonExistentKey() {
        AggregationResult result = new AggregationResult();
        assertNull(result.get("nonExistentKey"));
    }
}


