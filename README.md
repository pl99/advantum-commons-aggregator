# Библиотека агрегации данных

## Обзор

Библиотека предоставляет гибкий и эффективный способ агрегирования данных из коллекций с использованием Java Stream API. 
Позволяет выполнять несколько агрегаций за один проход по коллекции.

## Особенности

*   **Fluent API:** Интуитивно понятный и цепочечный интерфейс для определения агрегаций.
*   **Поддержка различных агрегаций:** Включает стандартные агрегации (сумма, среднее, мин, макс, количество) и пользовательские (медиана).
*   **Агрегация за один проход:** Эффективная обработка данных, позволяющая выполнять несколько агрегаций за один проход по коллекции, что минимизирует накладные расходы.
*   **Параллельная обработка:** Возможность использования параллельных потоков для повышения производительности на больших наборах данных.
*   **Расширяемость:** Легко добавлять новые типы агрегаций путем реализации интерфейса `Collector`.

## Начало работы

### Требования

*   Java 11 или выше
*   Maven 3.x

### Установка

1.  Клонируйте репозиторий:

    ```bash
    git clone <URL_репозитория>
    cd advantum-commons-aggregator
    ```

2.  Соберите проект с помощью Maven:

    ```bash
    mvn clean install
    ```

    Это установит библиотеку в ваш локальный репозиторий Maven, и вы сможете использовать ее в своих проектах.

### Использование

Добавьте следующую зависимость в ваш `pom.xml`:

```xml
<dependency>
    <groupId>ru.advantum.commons</groupId>
    <artifactId>aggregator</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Примеры использования

```java
public class Main {
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", 30, new BigDecimal("60000.50")),
                new Employee(2, "Bob", 45, new BigDecimal("80000.00")),
                new Employee(3, "Charlie", 25, new BigDecimal("55000.75")),
                new Employee(4, "David", 50, new BigDecimal("120000.00")),
                new Employee(5, "Eve", 35, new BigDecimal("75000.00")),
                new Employee(6, "Frank", 30, new BigDecimal("62000.00")) // Duplicate age
        );

        System.out.println("--- Выполнение агрегации в одном потоке ---");
        AggregationResult result = Aggregator.of(employees)
                .count("employeeCount")
                .distinct("distinctAges", Employee::getAge)
                .sum("totalSalary", Employee::getSalary)
                .average("averageAge", Employee::getAge)
                .min("minSalary", Employee::getSalary)
                .max("maxAge", Employee::getAge)
                .median("medianSalary", Employee::getSalary)
                .aggregate();

        System.out.println(result);
        System.out.println("Количество сотрудников: " + result.getCount("employeeCount"));
        System.out.println("Уникальные возрасты: " + result.getDistinct("distinctAges"));
        System.out.println("Общая зарплата: " + result.getSum("totalSalary"));
        System.out.println("Средний возраст: " + result.getAverage("averageAge"));
        System.out.println("Минимальная зарплата: " + result.getMin("minSalary"));
        System.out.println("Максимальный возраст: " + result.getMax("maxAge"));
        System.out.println("Медианная зарплата: " + result.getMedian("medianSalary"));

        System.out.println("\n--- Выполнение агрегации в параллельном режиме ---");
        AggregationResult parallelResult = Aggregator.of(employees)
                .parallel()
                .count("employeeCount")
                .distinct("distinctAges", Employee::getAge)
                .sum("totalSalary", Employee::getSalary)
                .average("averageAge", Employee::getAge)
                .aggregate();

        System.out.println(parallelResult);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class Employee {
        int id;
        String name;
        int age;
        BigDecimal salary;
    }
}
```

### Выполнение нескольких агрегаций

### Параллельная агрегация

Для использования параллельных потоков просто вызовите метод `parallel()`:

```java
AggregationResult parallelResult = Aggregator.of(items)
                .parallel()
                .sum("totalValue", Item::getValue)
                .aggregate();
```

## Структура проекта

```
data_aggregator_library/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── ru/
│   │           └──advantum/
│   │              └──commons/
│   │                 └── aggregator/
│   │                     ├── AggregationResult.java
│   │                     ├── AggregationTask.java
│   │                     ├── Aggregator.java
│   │                     └── collectors/
│   │                         ├── CompositeCollector.java
│   │                         ├── MedianCollector.java
│   │                         └── TeeingCollector.java
│   └── test/
│       └── java/
│           └── ru/
│               └──advantum/
│                  └──commons/
│                     └── aggregator/
│                         ├── AggregationResultTest.java
│                         └── AggregatorTest.java
└── README.md
└── LICENSE
```

## Вклад

Приветствуются любые вклады! Пожалуйста, создавайте pull requests или сообщайте о проблемах (issues) на GitHub.

## Лицензия

Этот проект лицензируется в соответствии с лицензией MIT. См. файл `LICENSE` для получения более подробной информации.

## Автор
PL99


