# Event Agency: заказы на организацию мероприятий

Настольная информационная система агентства мероприятий на JavaFX, PostgreSQL и JDBC.

## Запуск PostgreSQL

Через Docker:

```bash
docker compose up -d --wait
```

PostgreSQL будет доступен на порту `5433`. Для остановки сохранения данных:

```bash
docker compose down
```

Параметры подключения находятся в `src/main/resources/database.properties`.
Для запуска вне Docker при необходимости измените переменные окружения:

```text
DB_URL=jdbc:postgresql://localhost:5432/event_agency
DB_USER=postgres
DB_PASSWORD=postgres
```

## Запуск приложения

```bash
mvn clean package
mvn javafx:run
```

Для запуска через собранный JAR используйте:

```bash
java -jar target/event-agency-1.0-SNAPSHOT.jar
```

Если Maven сообщает, что `JAVA_HOME` не настроен, на macOS используйте команду:

```bash
cd "/Users/lilyaka/Desktop/раб.тетради/event agency"
JAVA_HOME=$(/usr/libexec/java_home) java -jar target/event-agency-1.0-SNAPSHOT.jar
```

Система поддерживает клиентов и заказы на организацию мероприятий, CRUD, поиск, фильтрацию, сортировку, статистику и экспорт заказов в Excel.
