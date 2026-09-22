package ru.eventagency.service;

import ru.eventagency.exception.*;
import ru.eventagency.model.Client;
import ru.eventagency.repository.ClientRepository;
import java.util.List;

public class ClientService {
    private final ClientRepository repository;
    public ClientService(ClientRepository repository){this.repository=repository;}
    public Client create(Client c){validate(c);return repository.create(c);}
    public List<Client> all(){return repository.findAll();}
    public Client byId(int id){return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Клиент с ID "+id+" не найден"));}
    public void update(Client c){byId(c.getId());validate(c);repository.update(c);}
    public void delete(int id){byId(id);repository.delete(id);}
    public List<Client> search(String text){return repository.search(text);}
    private void validate(Client c){if(c.getFullName()==null||c.getFullName().isBlank())throw new BusinessException("Имя клиента обязательно");if(c.getEmail()==null||!c.getEmail().contains("@"))throw new BusinessException("Некорректный email");if(c.getPhone()==null||c.getPhone().isBlank())throw new BusinessException("Телефон обязателен");}
}
