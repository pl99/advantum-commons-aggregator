package ru.advantum.commons.aggregator;

import ru.advantum.commons.aggregator.collectors.MedianCollector;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractAggregator<T, R> {

    protected final Collection<T> collection;
    protected boolean parallel = false;
    protected final List<AggregationTask<T>> tasks = new ArrayList<>();

    protected AbstractAggregator(Collection<T> collection) {
        this.collection = collection;
    }

    public abstract R aggregate();

    protected abstract AbstractAggregator<T, R> newInstance(Collection<T> collection, boolean parallel, List<AggregationTask<T>> tasks);

    public AbstractAggregator<T, R> parallel() {
        this.parallel = true;
        return this;
    }

    public AbstractAggregator<T, R> count(String key) {
        tasks.add(new AggregationTask<>(key, Collectors.counting()));
        return this;
    }

    public AbstractAggregator<T, R> distinct(String key, Function<T, ?> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, Collectors.toSet())));
        return this;
    }

    public <N extends Number> AbstractAggregator<T, R> sum(String key, Function<T, N> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, Collectors.reducing(BigDecimal.ZERO, n -> new BigDecimal(n.toString()), BigDecimal::add))));
        return this;
    }

    public <N extends Number> AbstractAggregator<T, R> average(String key, Function<T, N> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.averagingDouble(n -> mapper.apply(n).doubleValue())));
        return this;
    }

    public <U extends Comparable<? super U>> AbstractAggregator<T, R> min(String key, Function<T, U> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, Collectors.minBy(Comparator.naturalOrder()))));
        return this;
    }

    public <U extends Comparable<? super U>> AbstractAggregator<T, R> max(String key, Function<T, U> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, Collectors.maxBy(Comparator.naturalOrder()))));
        return this;
    }

    public <N extends Number & Comparable<N>> AbstractAggregator<T, R> median(String key, Function<T, N> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, new MedianCollector<>())));
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Collector<T, ?, AggregationResult> buildCompositeCollector() {
        if (tasks.isEmpty()) {
            return Collectors.collectingAndThen(Collectors.counting(), count -> new AggregationResult());
        }

        List<Collector<T, Object, Object>> collectors = tasks.stream()
                .map(task -> (Collector<T, Object, Object>) task.getCollector())
                .collect(Collectors.toList());

        Supplier<List<Object>> supplier = () -> collectors.stream()
                .map(c -> c.supplier().get())
                .collect(Collectors.toList());

        BiConsumer<List<Object>, T> accumulator = (accs, item) -> {
            for (int i = 0; i < collectors.size(); i++) {
                collectors.get(i).accumulator().accept(accs.get(i), item);
            }
        };

        BinaryOperator<List<Object>> combiner = (accs1, accs2) -> {
            List<Object> combined = new ArrayList<>(accs1.size());
            for (int i = 0; i < collectors.size(); i++) {
                combined.add(collectors.get(i).combiner().apply(accs1.get(i), accs2.get(i)));
            }
            return combined;
        };

        Function<List<Object>, AggregationResult> finisher = accs -> {
            AggregationResult result = new AggregationResult();
            for (int i = 0; i < collectors.size(); i++) {
                Object finalValue = collectors.get(i).finisher().apply(accs.get(i));
                String key = tasks.get(i).getAlias();
                if (finalValue instanceof Optional) {
                    ((Optional<?>) finalValue).ifPresent(v -> result.put(key, v));
                } else {
                    result.put(key, finalValue);
                }
            }
            return result;
        };
        return Collector.of(supplier, accumulator, combiner, finisher);
    }
}
