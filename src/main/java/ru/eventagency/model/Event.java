package ru.eventagency.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Event {
    private Integer id;
    private Integer clientId;
    private String clientName;
    private String title;
    private EventType type;
    private EventStatus status;
    private LocalDate eventDate;
    private BigDecimal budget;
    private String description;

    public Event(Integer id, Integer clientId, String clientName, String title, EventType type, EventStatus status, LocalDate eventDate, BigDecimal budget, String description) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.title = title;
        this.type = type;
        this.status = status;
        this.eventDate = eventDate;
        this.budget = budget;
        this.description = description;
    }

    public Event(Integer id, Integer clientId, String title, EventType type, EventStatus status, LocalDate eventDate, BigDecimal budget, String description) {
        this(id, clientId, null, title, type, status, eventDate, budget, description);
    }

    public Event(Integer clientId, String title, EventType type, EventStatus status, LocalDate eventDate, BigDecimal budget, String description) {
        this(null, clientId, null, title, type, status, eventDate, budget, description);
    }

    public Integer getId() { return id; }
    public Integer getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getTitle() { return title; }
    public EventType getType() { return type; }
    public EventStatus getStatus() { return status; }
    public LocalDate getEventDate() { return eventDate; }
    public BigDecimal getBudget() { return budget; }
    public String getDescription() { return description; }

    public boolean isCompleted() {
        return status == EventStatus.COMPLETED;
    }

    public boolean canChangeStatusTo(EventStatus newStatus) {
        return !isCompleted() || newStatus == EventStatus.COMPLETED;
    }

    public boolean hasPositiveBudget() {
        return budget != null && budget.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isScheduledBefore(LocalDate date) {
        return eventDate != null && eventDate.isBefore(date);
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", title='" + title + "', type=" + type
                + ", status=" + status + ", date=" + eventDate + ", budget=" + budget
                + ", client='" + clientName + "'}";
    }
}
