package ru.advantum.commons.aggregator;

import java.util.stream.Collector;

/**
 * Внутренний класс для описания задачи агрегации.
 * @param <T> Тип элемента коллекции
 */
public class AggregationTask<T> {
    private final String alias;
//    private final Function<T, ?> fieldExtractor;
    private final Collector<T, ?, ?> collector;

    public AggregationTask(String alias, Collector<T, ?, ?> collector) {
        this.alias = alias;
        this.collector = collector;
    }

    public String getAlias() {
        return alias;
    }


    public Collector<T, ?, ?> getCollector() {
        return collector;
    }
}


