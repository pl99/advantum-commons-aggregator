package ru.advantum.commons.aggregator.collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Коллектор для вычисления медианы.
 * ВНИМАНИЕ: Хранит все элементы в памяти, что может привести к OutOfMemoryError на больших коллекциях.
 *
 * @param <T> Тип элементов, должен быть {@link Number} и {@link Comparable}.
 */
public class MedianCollector<T extends Number & Comparable<T>> implements Collector<T, List<T>, BigDecimal> {

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

    @Override
    public Function<List<T>, BigDecimal> finisher() {
        return list -> {
            if (list.isEmpty()) {
                return null;
            }
            Collections.sort(list);
            int size = list.size();
            int mid = size / 2;

            if (size % 2 == 0) {
                // Для четного числа элементов - среднее двух центральных
                BigDecimal val1 = toBigDecimal(list.get(mid - 1));
                BigDecimal val2 = toBigDecimal(list.get(mid));
                return val1.add(val2).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
            } else {
                // Для нечетного - центральный элемент
                return toBigDecimal(list.get(mid));
            }
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    private BigDecimal toBigDecimal(T number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        return new BigDecimal(number.toString());
    }
}
