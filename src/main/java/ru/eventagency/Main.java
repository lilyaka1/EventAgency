package ru.eventagency;

import ru.eventagency.exception.*;
import ru.eventagency.model.*;
import ru.eventagency.repository.*;
import ru.eventagency.service.*;
import ru.eventagency.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class Main {
    private final Scanner scanner=new Scanner(System.in); private final ClientService clients; private final EventService events;
    public Main(){DatabaseManager db=new DatabaseManager();ClientRepository cr=new ClientRepository(db);clients=new ClientService(cr);events=new EventService(new EventRepository(db),clients);}
    public static void main(String[] args){new Main().run();}
    private void run(){while(true){try{System.out.println("\n========== ЗАКАЗЫ НА ОРГАНИЗАЦИЮ МЕРОПРИЯТИЙ ==========");System.out.println("1. Клиенты\n2. Заказы на организацию мероприятий\n3. Поиск\n4. Фильтрация и сортировка\n5. Статистика\n6. Экспорт заказов в Excel\n0. Выход");String choice=prompt("Выберите действие: ");if(choice.equals("0"))return;switch(choice){case "1"->clientMenu();case "2"->eventMenu();case "3"->searchMenu();case "4"->filterMenu();case "5"->statistics();case "6"->export();default->System.out.println("Неизвестный пункт меню");}}catch(RuntimeException|java.io.IOException e){System.out.println("Ошибка: "+e.getMessage());}}}
    private void clientMenu(){while(true){System.out.println("\n--- КЛИЕНТЫ ---\n1. Добавить\n2. Показать всех\n3. Найти по ID\n4. Изменить\n5. Удалить\n6. Поиск\n0. Назад");String c=prompt("Действие: ");try{switch(c){case "1"->createClient();case "2"->clients.all().forEach(System.out::println);case "3"->System.out.println(clients.byId(readInt("ID: ")));case "4"->updateClient();case "5"->clients.delete(readInt("ID: "));case "6"->clients.search(prompt("Текст: ")).forEach(System.out::println);case "0"-> {return;}default->System.out.println("Неизвестный пункт");}}catch(RuntimeException e){System.out.println("Ошибка: "+e.getMessage());}}}
    private void createClient(){clients.create(new Client(prompt("ФИО: "),prompt("Email: "),prompt("Телефон: ")));System.out.println("Клиент создан");}
    private void updateClient(){int id=readInt("ID: ");clients.update(new Client(id,prompt("ФИО: "),prompt("Email: "),prompt("Телефон: ")));System.out.println("Клиент изменен");}
    private void eventMenu(){while(true){System.out.println("\n--- ЗАКАЗЫ НА ОРГАНИЗАЦИЮ МЕРОПРИЯТИЙ ---\n1. Добавить заказ\n2. Показать все заказы\n3. Найти заказ по ID\n4. Изменить заказ\n5. Удалить заказ\n0. Назад");String c=prompt("Действие: ");try{switch(c){case "1"->createEvent();case "2"->events.all().forEach(System.out::println);case "3"->System.out.println(events.byId(readInt("ID: ")));case "4"->updateEvent();case "5"->events.delete(readInt("ID: "));case "0"-> {return;}default->System.out.println("Неизвестный пункт");}}catch(RuntimeException e){System.out.println("Ошибка: "+e.getMessage());}}}
    private Event readEvent(Integer id){int client=readInt("ID клиента: ");String title=prompt("Название: ");EventType type=readEnum(EventType.class,"Тип (WEDDING, CONFERENCE, BIRTHDAY, CORPORATE, OTHER): ");EventStatus status=readEnum(EventStatus.class,"Статус (PLANNED, CONFIRMED, COMPLETED, CANCELLED): ");LocalDate date=LocalDate.parse(prompt("Дата (ГГГГ-ММ-ДД): "));BigDecimal budget=new BigDecimal(prompt("Бюджет: "));String description=prompt("Описание: ");return new Event(id,client,title,type,status,date,budget,description);}
    private void createEvent(){events.create(readEvent(null));System.out.println("Заказ создан");}
    private void updateEvent(){int id=readInt("ID: ");events.update(readEvent(id));System.out.println("Заказ изменен");}
    private void searchMenu(){System.out.println("1. Поиск заказов по названию/описанию\n2. Поиск заказов по клиенту\n3. Поиск клиентов по ФИО/email");String c=prompt("Действие: ");if(c.equals("1"))events.search(prompt("Текст: ")).forEach(System.out::println);else if(c.equals("2"))events.searchByClient(prompt("ФИО клиента: ")).forEach(System.out::println);else if(c.equals("3"))clients.search(prompt("Текст: ")).forEach(System.out::println);}
    private void filterMenu(){System.out.println("1. Фильтр по статусу\n2. Фильтр по типу\n3. Сортировка по бюджету\n4. Сортировка по дате");String c=prompt("Действие: ");if(c.equals("1"))events.filter(readEnum(EventStatus.class,"Статус: "),null).forEach(System.out::println);else if(c.equals("2"))events.filter(null,readEnum(EventType.class,"Тип: ")).forEach(System.out::println);else if(c.equals("3"))events.sortedByBudget().forEach(System.out::println);else if(c.equals("4"))events.sortedByDate().forEach(System.out::println);}
    private void statistics(){List<Event> a=events.all();long budget=a.stream().map(Event::getBudget).mapToLong(BigDecimal::longValue).sum();System.out.println("Всего клиентов: "+clients.all().size());System.out.println("Всего мероприятий: "+a.size());System.out.println("Запланированных: "+a.stream().filter(e->e.getStatus()==EventStatus.PLANNED).count());System.out.println("Подтвержденных: "+a.stream().filter(e->e.getStatus()==EventStatus.CONFIRMED).count());System.out.println("Завершенных: "+a.stream().filter(e->e.getStatus()==EventStatus.COMPLETED).count());System.out.println("Общий бюджет: "+budget);System.out.println("Статусы: "+events.statusStats());}
    private void export() throws java.io.IOException{String file=prompt("Имя файла [event_orders.xlsx]: ");if(file.isBlank())file="event_orders.xlsx";new ExcelExporter().exportEvents(events.all(),file);System.out.println("Экспорт заказов выполнен: "+file);}
    private String prompt(String text){System.out.print(text);return scanner.nextLine().trim();}
    private int readInt(String text){try{return Integer.parseInt(prompt(text));}catch(NumberFormatException e){throw new BusinessException("ID должен быть целым числом");}}
    private <T extends Enum<T>> T readEnum(Class<T> type,String text){try{return Enum.valueOf(type,prompt(text).toUpperCase());}catch(IllegalArgumentException e){throw new BusinessException("Недопустимое значение");}}
}
