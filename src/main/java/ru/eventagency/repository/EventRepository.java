package ru.eventagency.repository;

import ru.eventagency.model.Event;
import ru.eventagency.model.EventStatus;
import ru.eventagency.model.EventType;
import ru.eventagency.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PostgreSQL implementation of the event-order store. */
public class EventRepository implements EventStore {
    private final DatabaseManager database;

    public EventRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public Event create(Event event) {
        String sql = "INSERT INTO event_orders(client_id, title, event_type, status, event_date, budget, description) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setEventFields(statement, event);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return new Event(result.getInt(1), event.getClientId(), event.getTitle(), event.getType(),
                        event.getStatus(), event.getEventDate(), event.getBudget(), event.getDescription());
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка создания заказа: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Event> findAll() {
        return query(baseQuery() + "ORDER BY e.event_date", List.of());
    }

    @Override
    public Optional<Event> findById(int id) {
        return query(baseQuery() + "WHERE e.id = ?", List.of(id)).stream().findFirst();
    }

    @Override
    public void update(Event event) {
        String sql = "UPDATE event_orders SET client_id = ?, title = ?, event_type = ?, status = ?, "
                + "event_date = ?, budget = ?, description = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setEventFields(statement, event);
            statement.setInt(8, event.getId());
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Заказ не найден");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка изменения заказа: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM event_orders WHERE id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Заказ не найден");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка удаления заказа: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Event> search(String text) {
        String sql = baseQuery() + "WHERE LOWER(e.title) LIKE LOWER(?) OR LOWER(e.description) LIKE LOWER(?) "
                + "ORDER BY e.event_date";
        String pattern = "%" + text + "%";
        return query(sql, List.of(pattern, pattern));
    }

    @Override
    public List<Event> searchByClient(String text) {
        String sql = baseQuery() + "WHERE LOWER(c.full_name) LIKE LOWER(?) ORDER BY e.event_date";
        return query(sql, List.of("%" + text + "%"));
    }

    @Override
    public List<Event> filter(EventStatus status, EventType type) {
        StringBuilder sql = new StringBuilder(baseQuery() + "WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (status != null) {
            sql.append(" AND e.status = ?");
            parameters.add(status.name());
        }
        if (type != null) {
            sql.append(" AND e.event_type = ?");
            parameters.add(type.name());
        }
        sql.append(" ORDER BY e.event_date");
        return query(sql.toString(), parameters);
    }

    private String baseQuery() {
        return "SELECT e.*, c.full_name AS client_name FROM event_orders e "
                + "JOIN clients c ON c.id = e.client_id ";
    }

    private void setEventFields(PreparedStatement statement, Event event) throws SQLException {
        statement.setInt(1, event.getClientId());
        statement.setString(2, event.getTitle());
        statement.setString(3, event.getType().name());
        statement.setString(4, event.getStatus().name());
        statement.setDate(5, java.sql.Date.valueOf(event.getEventDate()));
        statement.setBigDecimal(6, event.getBudget());
        statement.setString(7, event.getDescription());
    }

    private List<Event> query(String sql, List<Object> parameters) {
        List<Event> events = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    events.add(map(result));
                }
            }
            return events;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения заказов: " + exception.getMessage(), exception);
        }
    }

    private Event map(ResultSet result) throws SQLException {
        return new Event(
                result.getInt("id"),
                result.getInt("client_id"),
                result.getString("client_name"),
                result.getString("title"),
                EventType.valueOf(result.getString("event_type")),
                EventStatus.valueOf(result.getString("status")),
                result.getDate("event_date").toLocalDate(),
                result.getBigDecimal("budget"),
                result.getString("description")
        );
    }
}
