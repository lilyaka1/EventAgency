package ru.eventagency.ui;

import ru.eventagency.exception.BusinessException;
import ru.eventagency.model.Client;
import ru.eventagency.model.Event;
import ru.eventagency.model.EventStatus;
import ru.eventagency.model.EventType;
import ru.eventagency.repository.ClientRepository;
import ru.eventagency.repository.EventRepository;
import ru.eventagency.service.ClientService;
import ru.eventagency.service.EventService;
import ru.eventagency.util.DatabaseManager;
import ru.eventagency.util.ExcelExporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/** Console presentation layer. It only reads commands and delegates work to services. */
public class ConsoleApplication {
    private final Scanner scanner = new Scanner(System.in);
    private final ClientService clients;
    private final EventService events;

    public ConsoleApplication() {
        DatabaseManager database = new DatabaseManager();
        clients = new ClientService(new ClientRepository(database));
        events = new EventService(new EventRepository(database), clients);
    }

    public void run() {
        while (true) {
            try {
                printMainMenu();
                switch (prompt("Выберите действие: ")) {
                    case "1" -> clientMenu();
                    case "2" -> eventMenu();
                    case "3" -> searchMenu();
                    case "4" -> filterMenu();
                    case "5" -> showStatistics();
                    case "6" -> exportEvents();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Неизвестный пункт меню");
                }
            } catch (RuntimeException | IOException exception) {
                System.out.println("Ошибка: " + exception.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n========== ЗАКАЗЫ НА ОРГАНИЗАЦИЮ МЕРОПРИЯТИЙ ==========");
        System.out.println("1. Клиенты");
        System.out.println("2. Заказы на организацию мероприятий");
        System.out.println("3. Поиск");
        System.out.println("4. Фильтрация и сортировка");
        System.out.println("5. Статистика");
        System.out.println("6. Экспорт заказов в Excel");
        System.out.println("0. Выход");
    }

    private void clientMenu() {
        while (true) {
            System.out.println("\n--- КЛИЕНТЫ ---");
            System.out.println("1. Добавить\n2. Показать всех\n3. Найти по ID");
            System.out.println("4. Изменить\n5. Удалить\n6. Поиск\n0. Назад");

            try {
                switch (prompt("Действие: ")) {
                    case "1" -> createClient();
                    case "2" -> clients.all().forEach(System.out::println);
                    case "3" -> System.out.println(clients.byId(readInt("ID: ")));
                    case "4" -> updateClient();
                    case "5" -> clients.delete(readInt("ID: "));
                    case "6" -> clients.search(prompt("Текст: ")).forEach(System.out::println);
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Неизвестный пункт");
                }
            } catch (RuntimeException exception) {
                printError(exception);
            }
        }
    }

    private void createClient() {
        clients.create(new Client(prompt("ФИО: "), prompt("Email: "), prompt("Телефон: ")));
        System.out.println("Клиент создан");
    }

    private void updateClient() {
        int id = readInt("ID: ");
        clients.update(new Client(id, prompt("ФИО: "), prompt("Email: "), prompt("Телефон: ")));
        System.out.println("Клиент изменен");
    }

    private void eventMenu() {
        while (true) {
            System.out.println("\n--- ЗАКАЗЫ НА ОРГАНИЗАЦИЮ МЕРОПРИЯТИЙ ---");
            System.out.println("1. Добавить заказ\n2. Показать все заказы\n3. Найти заказ по ID");
            System.out.println("4. Изменить заказ\n5. Удалить заказ\n0. Назад");

            try {
                switch (prompt("Действие: ")) {
                    case "1" -> createEvent();
                    case "2" -> events.all().forEach(System.out::println);
                    case "3" -> System.out.println(events.byId(readInt("ID: ")));
                    case "4" -> updateEvent();
                    case "5" -> events.delete(readInt("ID: "));
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Неизвестный пункт");
                }
            } catch (RuntimeException exception) {
                printError(exception);
            }
        }
    }

    private void createEvent() {
        events.create(readEvent(null));
        System.out.println("Заказ создан");
    }

    private void updateEvent() {
        events.update(readEvent(readInt("ID: ")));
        System.out.println("Заказ изменен");
    }

    private Event readEvent(Integer id) {
        int clientId = readInt("ID клиента: ");
        String title = prompt("Название: ");
        EventType type = readEnum(EventType.class, "Тип (WEDDING, CONFERENCE, BIRTHDAY, CORPORATE, OTHER): ");
        EventStatus status = readEnum(EventStatus.class, "Статус (PLANNED, CONFIRMED, COMPLETED, CANCELLED): ");
        LocalDate date = LocalDate.parse(prompt("Дата (ГГГГ-ММ-ДД): "));
        BigDecimal budget = new BigDecimal(prompt("Бюджет: "));
        String description = prompt("Описание: ");
        return new Event(id, clientId, title, type, status, date, budget, description);
    }

    private void searchMenu() {
        System.out.println("1. Поиск заказов по названию/описанию");
        System.out.println("2. Поиск заказов по клиенту");
        System.out.println("3. Поиск клиентов по ФИО/email");
        switch (prompt("Действие: ")) {
            case "1" -> events.search(prompt("Текст: ")).forEach(System.out::println);
            case "2" -> events.searchByClient(prompt("ФИО клиента: ")).forEach(System.out::println);
            case "3" -> clients.search(prompt("Текст: ")).forEach(System.out::println);
            default -> System.out.println("Неизвестный пункт");
        }
    }

    private void filterMenu() {
        System.out.println("1. Фильтр по статусу\n2. Фильтр по типу");
        System.out.println("3. Сортировка по бюджету\n4. Сортировка по дате");
        switch (prompt("Действие: ")) {
            case "1" -> events.filter(readEnum(EventStatus.class, "Статус: "), null).forEach(System.out::println);
            case "2" -> events.filter(null, readEnum(EventType.class, "Тип: ")).forEach(System.out::println);
            case "3" -> events.sortedByBudget().forEach(System.out::println);
            case "4" -> events.sortedByDate().forEach(System.out::println);
            default -> System.out.println("Неизвестный пункт");
        }
    }

    private void showStatistics() {
        List<Event> allEvents = events.all();
        BigDecimal totalBudget = allEvents.stream()
                .map(Event::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Всего клиентов: " + clients.all().size());
        System.out.println("Всего мероприятий: " + allEvents.size());
        System.out.println("Общий бюджет: " + totalBudget);
        System.out.println("Статусы: " + events.statusStats());
    }

    private void exportEvents() throws IOException {
        String fileName = prompt("Имя файла [event_orders.xlsx]: ");
        if (fileName.isBlank()) {
            fileName = "event_orders.xlsx";
        } else if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName += ".xlsx";
        }
        new ExcelExporter().exportEvents(events.all(), fileName);
        System.out.println("Экспорт заказов выполнен: " + fileName);
    }

    private String prompt(String text) {
        System.out.print(text);
        return scanner.nextLine().trim();
    }

    private int readInt(String text) {
        try {
            return Integer.parseInt(prompt(text));
        } catch (NumberFormatException exception) {
            throw new BusinessException("ID должен быть целым числом");
        }
    }

    private <T extends Enum<T>> T readEnum(Class<T> type, String text) {
        try {
            return Enum.valueOf(type, prompt(text).toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Недопустимое значение");
        }
    }

    private void printError(RuntimeException exception) {
        System.out.println("Ошибка: " + exception.getMessage());
    }
}
