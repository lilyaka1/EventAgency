package ru.eventagency.service;

import ru.eventagency.exception.BusinessException;
import ru.eventagency.exception.EntityNotFoundException;
import ru.eventagency.model.Event;
import ru.eventagency.model.EventStatus;
import ru.eventagency.model.EventType;
import ru.eventagency.repository.EventStore;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Applies business rules for event orders. */
public class EventService {
    private final EventStore repository;
    private final ClientService clients;

    public EventService(EventStore repository, ClientService clients) {
        this.repository = repository;
        this.clients = clients;
    }

    public Event create(Event event) {
        validate(event, false);
        return repository.create(event);
    }

    public List<Event> all() {
        return repository.findAll();
    }

    public Event byId(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с ID " + id + " не найден"));
    }

    public void update(Event event) {
        Event existing = byId(event.getId());
        if (!existing.canChangeStatusTo(event.getStatus())) {
            throw new BusinessException("Завершенный заказ нельзя вернуть в другой статус");
        }
        validate(event, existing.isCompleted());
        repository.update(event);
    }

    public void delete(int id) {
        Event event = byId(id);
        if (event.isCompleted()) {
            throw new BusinessException("Завершенный заказ нельзя удалить");
        }
        repository.delete(id);
    }

    public List<Event> search(String text) {
        return repository.search(text);
    }

    public List<Event> searchByClient(String text) {
        return repository.searchByClient(text);
    }

    public List<Event> filter(EventStatus status, EventType type) {
        return repository.filter(status, type);
    }

    public List<Event> sortedByBudget() {
        return all().stream()
                .sorted(Comparator.comparing(Event::getBudget).reversed())
                .toList();
    }

    public List<Event> sortedByDate() {
        return all().stream()
                .sorted(Comparator.comparing(Event::getEventDate))
                .toList();
    }

    public Map<EventStatus, Long> statusStats() {
        return all().stream()
                .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));
    }

    private void validate(Event event, boolean allowPastDate) {
        if (event.getClientId() == null) {
            throw new BusinessException("Клиент не указан");
        }
        clients.byId(event.getClientId());

        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new BusinessException("Название заказа обязательно");
        }
        if (event.getTitle().length() > 160) {
            throw new BusinessException("Название заказа не может быть длиннее 160 символов");
        }
        if (event.getDescription() != null && event.getDescription().length() > 10000) {
            throw new BusinessException("Описание не может быть длиннее 10000 символов");
        }
        if (event.getType() == null || event.getStatus() == null) {
            throw new BusinessException("Тип и статус обязательны");
        }
        if (!event.hasPositiveBudget()) {
            throw new BusinessException("Бюджет должен быть положительным");
        }
        if (!allowPastDate && event.isScheduledBefore(LocalDate.now())) {
            throw new BusinessException("Дата мероприятия не может быть в прошлом");
        }
    }
}
