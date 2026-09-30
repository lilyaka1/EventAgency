package ru.eventagency.repository;

import ru.eventagency.model.Client;
import ru.eventagency.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PostgreSQL implementation of the client store. */
public class ClientRepository implements ClientStore {
    private final DatabaseManager database;

    public ClientRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public Client create(Client client) {
        String sql = "INSERT INTO clients(full_name, email, phone) VALUES(?, ?, ?) RETURNING id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getEmail());
            statement.setString(3, client.getPhone());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                client.setId(result.getInt(1));
                return client;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка создания клиента: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Client> findAll() {
        return query("SELECT * FROM clients ORDER BY id", List.of());
    }

    @Override
    public Optional<Client> findById(int id) {
        return query("SELECT * FROM clients WHERE id = ?", List.of(id)).stream().findFirst();
    }

    @Override
    public void update(Client client) {
        String sql = "UPDATE clients SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getEmail());
            statement.setString(3, client.getPhone());
            statement.setInt(4, client.getId());
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Клиент не найден");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка изменения клиента: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM clients WHERE id = ?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Клиент не найден");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка удаления клиента: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Client> search(String text) {
        String sql = "SELECT * FROM clients WHERE LOWER(full_name) LIKE LOWER(?) "
                + "OR LOWER(email) LIKE LOWER(?) ORDER BY full_name";
        String pattern = "%" + text + "%";
        return query(sql, List.of(pattern, pattern));
    }

    private List<Client> query(String sql, List<Object> parameters) {
        List<Client> clients = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.size(); index++) {
                statement.setObject(index + 1, parameters.get(index));
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    clients.add(map(result));
                }
            }
            return clients;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения клиентов: " + exception.getMessage(), exception);
        }
    }

    private Client map(ResultSet result) throws SQLException {
        return new Client(
                result.getInt("id"),
                result.getString("full_name"),
                result.getString("email"),
                result.getString("phone")
        );
    }
}
