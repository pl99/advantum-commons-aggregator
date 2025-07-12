package ru.advantum.commons.aggregator;

import ru.advantum.commons.aggregator.collectors.MedianCollector;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Основной класс библиотеки для построения и выполнения запросов на агрегацию.
 * Использует Fluent API для декларативного описания задач.
 *
 * @param <T> Тип объектов в коллекции.
 */
//public class Aggregator<T> {
public final class Aggregator<T> extends AbstractAggregator<T, AggregationResult> {

    private final Collection<T> collection;
    private final boolean parallel;
    private final List<AggregationTask<T>> tasks;

    public Aggregator(Collection<T> collection, boolean parallel, List<AggregationTask<T>> tasks) {
        super(collection);
        this.collection = collection;
        this.parallel = parallel;
        this.tasks = tasks;
    }

    /**
     * Точка входа для создания экземпляра Aggregator.
     * @param collection Коллекция для агрегации.
     * @param <T> Тип элементов.
     * @return Новый экземпляр Aggregator.
     */
    public static <T> Aggregator<T> of(Collection<T> collection) {
        return new Aggregator<>(collection, false, new ArrayList<>());
    }

    /**
     * Включает параллельную обработку коллекции.
     * @return Текущий экземпляр Aggregator.
     */
    public Aggregator<T> parallel() {
        return new Aggregator<>(this.collection, true, this.tasks);
    }

    /**
     * Добавляет задачу на подсчет количества элементов.
     * @param key Ключ для результата.
     * @return Текущий экземпляр Aggregator.
     */
    public Aggregator<T> count(String key) {
        tasks.add(new AggregationTask<>(key, Collectors.counting()));
        return this;
    }

    /**
     * Добавляет задачу на вычисление суммы.
     * @param key Ключ для результата.
     * @param mapper Функция для извлечения числового значения из объекта.
     * @param <N> Тип числового значения.
     * @return Текущий экземпляр Aggregator.
     */
    public <N extends Number> Aggregator<T> sum(String key, Function<T, N> mapper) {
        Collector<T, ?, BigDecimal> collector = Collectors.mapping(
                mapper,
                Collectors.reducing(BigDecimal.ZERO, n -> new BigDecimal(n.toString()), BigDecimal::add)
        );
        tasks.add(new AggregationTask<>(key, collector));
        return this;
    }

    /**
     * Добавляет задачу на вычисление среднего значения.
     * @param key Ключ для результата.
     * @param mapper Функция для извлечения числового значения из объекта.
     * @param <N> Тип числового значения.
     * @return Текущий экземпляр Aggregator.
     */
    public <N extends Number> Aggregator<T> average(String key, Function<T, N> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.averagingDouble(n -> mapper.apply(n).doubleValue())));
        return this;
    }

    /**
     * Добавляет задачу на поиск минимального значения.
     * @param key Ключ для результата.
     * @param mapper Функция для извлечения значения для сравнения.
     * @param <U> Тип сравниваемого значения.
     * @return Текущий экземпляр Aggregator.
     */
    public <U extends Comparable<? super U>> Aggregator<T> min(String key, Function<T, U> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, Collectors.minBy(Comparator.naturalOrder()))));
        return this;
    }

    /**
     * Добавляет задачу на поиск максимального значения.
     * @param key Ключ для результата.
     * @param mapper Функция для извлечения значения для сравнения.
     * @param <U> Тип сравниваемого значения.
     * @return Текущий экземпляр Aggregator.
     */
    public <U extends Comparable<? super U>> Aggregator<T> max(String key, Function<T, U> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, Collectors.maxBy(Comparator.naturalOrder()))));
        return this;
    }

    /**
     * Добавляет задачу на вычисление медианы.
     * @param key Ключ для результата.
     * @param mapper Функция для извлечения числового значения.
     * @param <N> Тип числового значения.
     * @return Текущий экземпляр Aggregator.
     */
    public <N extends Number & Comparable<N>> Aggregator<T> median(String key, Function<T, N> mapper) {
        tasks.add(new AggregationTask<>(key, Collectors.mapping(mapper, new MedianCollector<>())));
        return this;
    }


    /**
     * Добавляет задачу на сбор уникальных значений.
     * @param key Ключ для результата.
     * @param mapper Функция для извлечения значения из объекта.
     * @return Текущий экземпляр Aggregator.
     */
    public Aggregator<T> distinct(String key, Function<T, ?> mapper) {
        tasks.add(new AggregationTask<>(key,
                Collectors.mapping(mapper, Collectors.toSet())
        ));
        return this;
    }

    /**
     * Запускает процесс агрегации.
     * @return {@link AggregationResult} с результатами всех вычислений.
     */
    public AggregationResult aggregate() {
        if (tasks.isEmpty()) {
            return new AggregationResult();
        }

        // Создаем список нижестоящих коллекторов из каждой задачи, стирая типы для единообразной обработки
        @SuppressWarnings("unchecked")
        List<Collector<T, Object, Object>> collectors = tasks.stream()
                .map(task -> (Collector<T, Object, Object>) task.getCollector())
                .collect(Collectors.toList());

        // Supplier для списка промежуточных аккумуляторов
        Supplier<List<Object>> supplier = () -> collectors.stream()
                .map(c -> c.supplier().get())
                .collect(Collectors.toList());

        // Accumulator, который передает элемент каждому нижестоящему аккумулятору
        BiConsumer<List<Object>, T> accumulator = (accs, item) -> {
            for (int i = 0; i < collectors.size(); i++) {
                collectors.get(i).accumulator().accept(accs.get(i), item);
            }
        };

        // Combiner для параллельной обработки
        BinaryOperator<List<Object>> combiner = (accs1, accs2) -> {
            List<Object> combined = new ArrayList<>(accs1.size());
            for (int i = 0; i < collectors.size(); i++) {
                combined.add(collectors.get(i).combiner().apply(accs1.get(i), accs2.get(i)));
            }
            return combined;
        };

        // Finisher, который применяет финальное преобразование к каждому результату и помещает его в AggregationResult
        Function<List<Object>, AggregationResult> finisher = accs -> {
            AggregationResult result = new AggregationResult();
            for (int i = 0; i < collectors.size(); i++) {
                Object finalValue = collectors.get(i).finisher().apply(accs.get(i));
                String key = tasks.get(i).getAlias();

                // Распаковываем Optional значения от коллекторов вроде min/max
                if (finalValue instanceof Optional) {
                    ((Optional<?>) finalValue).ifPresent(v -> result.put(key, v));
                } else {
                    result.put(key, finalValue);
                }
            }
            return result;
        };

        // Собираем финальный, композитный коллектор
        Collector<T, List<Object>, AggregationResult> finalCollector = Collector.of(
                supplier,
                accumulator,
                combiner,
                finisher
        );

        Stream<T> stream = parallel ? collection.parallelStream() : collection.stream();
        return stream.collect(finalCollector);
    }
    @SafeVarargs
    public static <T> GroupingAggregator<T, List<Object>> groupBy(Collection<T> collection, Function<? super T, ?>... classifiers) {
        Function<T, List<Object>> compositeClassifier = t ->
                Arrays.stream(classifiers)
                        .map(c -> c.apply(t))
                        .collect(Collectors.toList());
        return new GroupingAggregator<>(collection, compositeClassifier, false, List.of());
    }
    @Override
    protected AbstractAggregator<T, AggregationResult> newInstance(Collection<T> collection, boolean parallel, List<AggregationTask<T>> aggregationTasks) {
        return new Aggregator<>(collection, parallel, tasks);
    }

}
