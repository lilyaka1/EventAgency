# Event Agency: заказы на организацию мероприятий

Консольная информационная система агентства мероприятий на Java 17, PostgreSQL и JDBC.

## Запуск PostgreSQL

1. Выполнить `schema.sql` в PostgreSQL.
2. При необходимости изменить переменные окружения:

```text
DB_URL=jdbc:postgresql://localhost:5432/event_agency
DB_USER=postgres
DB_PASSWORD=postgres
```

## Запуск приложения

```bash
mvn clean package
java -jar target/event-agency-1.0-SNAPSHOT.jar
```

Если Maven сообщает, что `JAVA_HOME` не настроен, на macOS используйте команду:

```bash
cd "/Users/lilyaka/Desktop/раб.тетради/event agency"
JAVA_HOME=$(/usr/libexec/java_home) java -jar target/event-agency-1.0-SNAPSHOT.jar
```

Система поддерживает клиентов и заказы на организацию мероприятий, CRUD, поиск, фильтрацию, сортировку, статистику и экспорт заказов в Excel.
