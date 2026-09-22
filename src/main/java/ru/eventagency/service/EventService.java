package ru.eventagency.service;

import ru.eventagency.exception.*;
import ru.eventagency.model.*;
import ru.eventagency.repository.EventRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class EventService {
    private final EventRepository repository; private final ClientService clients;
    public EventService(EventRepository repository, ClientService clients){this.repository=repository;this.clients=clients;}
    public Event create(Event e){validate(e);return repository.create(e);}
    public List<Event> all(){return repository.findAll();}
    public Event byId(int id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Заказ с ID "+id+" не найден"));}
    public void update(Event e){Event old=byId(e.getId());if(old.getStatus()==EventStatus.COMPLETED&&e.getStatus()!=EventStatus.COMPLETED)throw new BusinessException("Завершенный заказ нельзя вернуть в другой статус");validate(e, old.getStatus()==EventStatus.COMPLETED);repository.update(e);}
    public void delete(int id){Event e=byId(id);if(e.getStatus()==EventStatus.COMPLETED)throw new BusinessException("Завершенный заказ нельзя удалить");repository.delete(id);}
    public List<Event> search(String text){return repository.search(text);}
    public List<Event> searchByClient(String text){return repository.searchByClient(text);}
    public List<Event> filter(EventStatus s, EventType t){return repository.filter(s,t);}
    public List<Event> sortedByBudget(){return all().stream().sorted(Comparator.comparing(Event::getBudget).reversed()).collect(Collectors.toList());}
    public List<Event> sortedByDate(){return all().stream().sorted(Comparator.comparing(Event::getEventDate)).collect(Collectors.toList());}
    public Map<EventStatus,Long> statusStats(){return all().stream().collect(Collectors.groupingBy(Event::getStatus,Collectors.counting()));}
    private void validate(Event e){validate(e, false);}
    private void validate(Event e, boolean allowPastDate){if(e.getClientId()==null||clients.byId(e.getClientId())==null)throw new BusinessException("Клиент не указан");if(e.getTitle()==null||e.getTitle().isBlank())throw new BusinessException("Название заказа обязательно");if(e.getEventDate()==null||(!allowPastDate&&e.getEventDate().isBefore(java.time.LocalDate.now())))throw new BusinessException("Дата мероприятия не может быть в прошлом");if(e.getBudget()==null||e.getBudget().compareTo(BigDecimal.ZERO)<=0)throw new BusinessException("Бюджет должен быть положительным");if(e.getType()==null||e.getStatus()==null)throw new BusinessException("Тип и статус обязательны");}
}
