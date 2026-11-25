package ru.eventagency.service;

import org.junit.jupiter.api.Test;
import ru.eventagency.exception.BusinessException;
import ru.eventagency.model.Client;
import ru.eventagency.repository.ClientStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientServiceTest {
    @Test
    void rejectsInvalidEmail() {
        ClientService service = new ClientService(new InMemoryClientStore());

        assertThrows(BusinessException.class,
                () -> service.create(new Client("Иван Иванов", "invalid", "+79990000000")));
    }

    @Test
    void rejectsTooLongPhone() {
        ClientService service = new ClientService(new InMemoryClientStore());

        assertThrows(BusinessException.class,
                () -> service.create(new Client("Иван Иванов", "ivan@example.com", "1".repeat(31))));
    }

    private static class InMemoryClientStore implements ClientStore {
        private final List<Client> clients = new ArrayList<>();

        @Override public Client create(Client client) { clients.add(client); return client; }
        @Override public List<Client> findAll() { return List.copyOf(clients); }
        @Override public Optional<Client> findById(int id) { return Optional.empty(); }
        @Override public void update(Client client) {}
        @Override public void delete(int id) {}
        @Override public List<Client> search(String text) { return List.of(); }
    }
}
