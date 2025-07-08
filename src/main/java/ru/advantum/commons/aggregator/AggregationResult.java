package ru.advantum.commons.aggregator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Потокобезопасный контейнер для хранения и доступа к результатам агрегации.
 * Предоставляет типизированные геттеры для удобства.
 */
public final class AggregationResult {

    private final Map<String, Object> results = new ConcurrentHashMap<>();

    // Конструктор с видимостью в пределах пакета, чтобы его можно было создать только внутри библиотеки.
    AggregationResult() {}

    /**
     * Добавляет результат агрегации в контейнер.
     * @param key Ключ, идентифицирующий агрегацию.
     * @param value Результат вычисления.
     */
    void put(String key, Object value) {
        if (value != null) {
            results.put(key, value);
        }
    }

    /**
     * Объединяет данный результат с другим.
     * @param other Другой объект AggregationResult для слияния.
     * @return Текущий экземпляр после слияния.
     */
    AggregationResult merge(AggregationResult other) {
        this.results.putAll(other.results);
        return this;
    }

    /**
     * Получает результат по ключу, приводя его к указанному типу.
     * @param key Ключ агрегации.
     * @param <T> Тип результата.
     * @return Результат агрегации или null, если ключ не найден.
     */
    @SuppressWarnings("unchecked")
    <T> T get(String key) {
        return (T) results.get(key);
    }

    public BigDecimal getSum(String key) {
        BigDecimal bigDecimal = get(key);
        if (bigDecimal==null) return BigDecimal.ZERO;
        return bigDecimal;
    }
    public <T extends Number> T getSum(String key, Class<T> targetType) {
        BigDecimal sum = getSum(key);
        if (targetType == Integer.class) {
            return targetType.cast(sum.intValue());
        } else if (targetType == Long.class) {
            return targetType.cast(sum.longValue());
        } else if (targetType == Double.class) {
            return targetType.cast(sum.doubleValue());
        } else if (targetType == Float.class) {
            return targetType.cast(sum.floatValue());
        } else if (targetType == Short.class) {
            return targetType.cast(sum.shortValue());
        } else if (targetType == Byte.class) {
            return targetType.cast(sum.byteValue());
        } else if (targetType == BigDecimal.class) {
            return targetType.cast(sum);
        } else if (targetType == java.math.BigInteger.class) {
            return targetType.cast(sum.toBigInteger());
        }
        // Добавьте обработку других типов Number (Atomic...), если необходимо
        throw new IllegalArgumentException("Unsupported Number type: " + targetType.getName());
    }

    public Double getAverage(String key) {
        return get(key);
    }

    public <T> T getMin(String key) {
        return get(key);
    }

    public <T> T getMax(String key) {
        return get(key);
    }

    public Long getCount(String key) {
        return get(key);
    }

    public <T> T getMedian(String key) {
        return get(key);
    }

    /**
     * Возвращает коллекцию уникальных значений.
     * @param key Ключ агрегации.
     * @param <U> Тип элементов в коллекции.
     * @return Set с уникальными значениями.
     */
    public <U> Set<U> getDistinct(String key) {
        return get(key);
    }

    @Override
    public String toString() {
        return "AggregationResult{" +
                "results=" + results +
                '}';
    }
}
