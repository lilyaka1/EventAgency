package ru.eventagency.repository;

import ru.eventagency.model.Client;

import java.util.List;

/** Contract for storing and searching clients. */
public interface ClientStore extends Repository<Client> {
    List<Client> search(String text);
}
