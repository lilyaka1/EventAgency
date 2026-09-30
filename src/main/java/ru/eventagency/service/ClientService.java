package ru.eventagency.service;

import ru.eventagency.exception.BusinessException;
import ru.eventagency.exception.EntityNotFoundException;
import ru.eventagency.model.Client;
import ru.eventagency.repository.ClientStore;

import java.util.List;

/** Applies business rules for clients before delegating persistence to a store. */
public class ClientService {
    private final ClientStore repository;

    public ClientService(ClientStore repository) {
        this.repository = repository;
    }

    public Client create(Client client) {
        validate(client);
        return repository.create(client);
    }

    public List<Client> all() {
        return repository.findAll();
    }

    public Client byId(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден"));
    }

    public void update(Client client) {
        byId(client.getId());
        validate(client);
        repository.update(client);
    }

    public void delete(int id) {
        byId(id);
        repository.delete(id);
    }

    public List<Client> search(String text) {
        return repository.search(text);
    }

    private void validate(Client client) {
        if (!client.hasValidContactDetails()) {
            throw new BusinessException("Укажите ФИО, корректный email и телефон клиента");
        }
    }
}
