package ru.eventagency.repository;

import ru.eventagency.model.Event;
import ru.eventagency.model.EventStatus;
import ru.eventagency.model.EventType;

import java.util.List;

/** Contract for storing and searching event orders. */
public interface EventStore extends Repository<Event> {
    List<Event> search(String text);

    List<Event> searchByClient(String text);

    List<Event> filter(EventStatus status, EventType type);
}
