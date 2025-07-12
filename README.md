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
    git clone https://github.com/pl99/advantum-commons-aggregator
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
    <version>1.1-SNAPSHOT</version>
</dependency>
```

## Использование

Библиотека имеет две основные точки входа: Aggregator.of() для агрегации по всей коллекции и Aggregator.groupBy() для агрегации с группировкой.

```java
import ru.advantum.commons.aggregator.Aggregator;
import ru.advantum.commons.aggregator.AggregationResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", 30, new BigDecimal("60000.50"), "IT"),
                new Employee(2, "Bob", 45, new BigDecimal("80000.00"), "HR"),
                new Employee(3, "Charlie", 25, new BigDecimal("55000.75"), "IT"),
                new Employee(4, "David", 50, new BigDecimal("120000.00"), "Management"),
                new Employee(5, "Eve", 35, new BigDecimal("75000.00"), "HR"),
                new Employee(6, "Frank", 30, new BigDecimal("62000.00"), "IT")
        );

        System.out.println("--- 1. Простая агрегация по всей коллекции ---");
        AggregationResult totalResult = Aggregator.of(employees)
                .count("totalEmployees")
                .sum("totalSalaries", Employee::getSalary)
                .aggregate();
        System.out.println("Общие результаты: " + totalResult);


        System.out.println("\n--- 2. Агрегация с группировкой по одному полю (отдел) ---");
        Map<List<Object>, AggregationResult> byDepartment = Aggregator.groupBy(employees, Employee::getDepartment)
                .count("employeeCount")
                .average("averageSalary", Employee::getSalary)
                .aggregate();

        byDepartment.forEach((key, result) -> {
            System.out.println("\nОтдел: " + key.get(0));
            System.out.println("  - Количество сотрудников: " + result.getCount("employeeCount"));
            System.out.println("  - Средняя зарплата: " + result.getAverage("averageSalary"));
        });

        System.out.println("\n--- 3. Агрегация с группировкой по нескольким полям (отдел и возраст) ---");
        Map<List<Object>, AggregationResult> byDeptAndAge = Aggregator.groupBy(employees, Employee::getDepartment, Employee::getAge)
                .count("count")
                .sum("salarySum", Employee::getSalary)
                .aggregate();

        byDeptAndAge.forEach((key, result) -> {
            String department = (String) key.get(0);
            Integer age = (Integer) key.get(1);
            System.out.println("\nГруппа: Отдел=" + department + ", Возраст=" + age);
            System.out.println("  - Количество сотрудников в группе: " + result.getCount("count"));
            System.out.println("  - Суммарная зарплата в группе: " + result.getSum("salarySum"));
        });
    }

    private static class Employee {
        private int id;
        private String name;
        private int age;
        private BigDecimal salary;
        private String department;

        public Employee(int id, String name, int age, BigDecimal salary, String department) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.salary = salary;
            this.department = department;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    ", salary=" + salary +
                    ", department='" + department + '\'' +
                    '}';
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public BigDecimal getSalary() {
            return salary;
        }

        public String getDepartment() {
            return department;
        }
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

## Обзор API


| Метод                               | Описание                                |
|-------------------------------------|-----------------------------------------|
| Aggregator.of(collection)           | Создает агрегатор для всей коллекции.   |
| Aggregator.groupBy(collection, ...) |Создает агрегатор с группировкой по одному или нескольким полям.|
| .parallel()                         |Включает параллельный режим вычислений.|
| .count(key)                         |Считает общее количество элементов.|
| .distinct(key, mapper)              |Собирает уникальные значения в Set.|
| .sum(key, mapper)                   |Считает сумму (BigDecimal).|
| .average(key, mapper)               |Считает среднее значение (Double).|
| .min(key, mapper)                   |Находит минимальное значение.|
| .max(key, mapper)                   |Находит максимальное значение.|
| .median(key, mapper)                |Вычисляет медиану (BigDecimal).|
| .aggregate()                        |Запускает процесс агрегации и возвращает результат.|

## Структура проекта

```
advantum-commons-aggregator/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── ru/
│   │           └──advantum/
│   │              └──commons/
│   │                 └── aggregator/
│   │                     ├── AbstractAggregator.java
│   │                     ├── AggregationResult.java
│   │                     ├── AggregationTask.java
│   │                     ├── Aggregator.java
│   │                     ├── GroupingAggregator.java
│   │                     └── collectors/
│   │                         ├── MedianCollector.java
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


