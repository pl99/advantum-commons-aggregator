package ru.advantum.commons.aggregator;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GroupingAggregator<T, K> extends AbstractAggregator<T, Map<K, AggregationResult>> {

    private final Function<? super T, ? extends K> classifier;

    GroupingAggregator(Collection<T> collection, Function<? super T, ? extends K> classifier, boolean parallel, List<AggregationTask<T>> tasks) {
        super(collection);
        this.classifier = classifier;
        this.parallel = parallel;
        this.tasks.addAll(tasks);
    }

    @Override
    protected AbstractAggregator<T, Map<K, AggregationResult>> newInstance(Collection<T> collection, boolean parallel, List<AggregationTask<T>> tasks) {
        return new GroupingAggregator<>(collection, this.classifier, parallel, tasks);
    }

    @Override
    public Map<K, AggregationResult> aggregate() {
        Stream<T> stream = parallel ? collection.parallelStream() : collection.stream();
        return stream.collect(Collectors.groupingBy(classifier, buildCompositeCollector()));
    }
}
