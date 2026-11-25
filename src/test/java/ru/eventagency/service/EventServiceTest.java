package ru.eventagency.service;

import org.junit.jupiter.api.Test;
import ru.eventagency.exception.BusinessException;
import ru.eventagency.model.Client;
import ru.eventagency.model.Event;
import ru.eventagency.model.EventStatus;
import ru.eventagency.model.EventType;
import ru.eventagency.repository.ClientStore;
import ru.eventagency.repository.EventStore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EventServiceTest {
    @Test
    void rejectsPastDate() {
        EventService service = eventService();
        Event event = validEvent();
        final Event pastEvent = new Event(event.getClientId(), event.getTitle(), event.getType(), event.getStatus(),
                LocalDate.now().minusDays(1), event.getBudget(), event.getDescription());

        assertThrows(BusinessException.class, () -> service.create(pastEvent));
    }

    @Test
    void rejectsNonPositiveBudget() {
        EventService service = eventService();
        final Event event = new Event(1, "Тестовый заказ", EventType.OTHER, EventStatus.PLANNED,
                LocalDate.now().plusDays(10), BigDecimal.ZERO, "Описание");

        assertThrows(BusinessException.class, () -> service.create(event));
    }

    @Test
    void rejectsTooLongTitle() {
        EventService service = eventService();
        Event event = new Event(1, "а".repeat(161), EventType.OTHER, EventStatus.PLANNED,
                LocalDate.now().plusDays(10), BigDecimal.valueOf(1000), "Описание");

        assertThrows(BusinessException.class, () -> service.create(event));
    }

    private EventService eventService() {
        return new EventService(new InMemoryEventStore(), new ClientService(new InMemoryClientStore()));
    }

    private Event validEvent() {
        return new Event(1, "Тестовый заказ", EventType.OTHER, EventStatus.PLANNED,
                LocalDate.now().plusDays(10), BigDecimal.valueOf(1000), "Описание");
    }

    private static class InMemoryClientStore implements ClientStore {
        @Override public Client create(Client client) { return client; }
        @Override public List<Client> findAll() { return List.of(new Client(1, "Клиент", "client@example.com", "+79990000000")); }
        @Override public Optional<Client> findById(int id) {
            return id == 1 ? Optional.of(new Client(1, "Клиент", "client@example.com", "+79990000000")) : Optional.empty();
        }
        @Override public void update(Client client) {}
        @Override public void delete(int id) {}
        @Override public List<Client> search(String text) { return List.of(); }
    }

    private static class InMemoryEventStore implements EventStore {
        private final List<Event> events = new ArrayList<>();
        @Override public Event create(Event event) { events.add(event); return event; }
        @Override public List<Event> findAll() { return List.copyOf(events); }
        @Override public Optional<Event> findById(int id) { return Optional.empty(); }
        @Override public void update(Event event) {}
        @Override public void delete(int id) {}
        @Override public List<Event> search(String text) { return List.of(); }
        @Override public List<Event> searchByClient(String text) { return List.of(); }
        @Override public List<Event> filter(EventStatus status, EventType type) { return List.of(); }
    }
}
