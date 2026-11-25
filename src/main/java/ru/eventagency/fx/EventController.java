package ru.eventagency.fx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
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

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class EventController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> sortField;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, Integer> idColumn;
    @FXML private TableColumn<Event, String> titleColumn;
    @FXML private TableColumn<Event, String> clientColumn;
    @FXML private TableColumn<Event, EventType> typeColumn;
    @FXML private TableColumn<Event, EventStatus> statusColumn;
    @FXML private TableColumn<Event, LocalDate> dateColumn;
    @FXML private TableColumn<Event, BigDecimal> budgetColumn;
    @FXML private Label totalLabel;
    @FXML private Label plannedLabel;
    @FXML private Label completedLabel;
    @FXML private Label budgetLabel;
    @FXML private Label averageLabel;

    private EventService eventService;
    private ClientService clientService;
    private final ObservableList<Event> events = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        DatabaseManager database = new DatabaseManager();
        clientService = new ClientService(new ClientRepository(database));
        eventService = new EventService(new EventRepository(database), clientService);

        statusFilter.setItems(FXCollections.observableArrayList(
                "Все", "PLANNED", "CONFIRMED", "COMPLETED", "CANCELLED"));
        typeFilter.setItems(FXCollections.observableArrayList(
                "Все", "WEDDING", "CONFERENCE", "BIRTHDAY", "CORPORATE", "OTHER"));
        sortField.setItems(FXCollections.observableArrayList(
                "По умолчанию", "По бюджету", "По дате"));

        idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        clientColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClientName()));
        typeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getType()));
        statusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEventDate()));
        budgetColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBudget()));
        budgetColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.toPlainString() + " ₽");
            }
        });

        sortField.valueProperty().addListener((observable, oldValue, newValue) -> applyView());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyView());
        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyView());
        eventTable.setItems(events);
        refresh();
    }

    @FXML
    private void onSearch() {
        applyView();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        statusFilter.getSelectionModel().clearSelection();
        typeFilter.getSelectionModel().clearSelection();
        sortField.getSelectionModel().select("По умолчанию");
        refresh();
    }

    @FXML
    private void onAdd() {
        showEventDialog(null);
    }

    @FXML
    private void onEdit() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите заказ", "Для редактирования выберите запись в таблице.");
            return;
        }
        showEventDialog(selected);
    }

    @FXML
    private void onDelete() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите заказ", "Для удаления выберите запись в таблице.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удалить заказ «" + selected.getTitle() + "»?");
        confirmation.setContentText("Это действие нельзя отменить.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                eventService.delete(selected.getId());
                refresh();
            } catch (RuntimeException exception) {
                showError(exception);
            }
        }
    }

    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт заказов");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File file = chooser.showSaveDialog(eventTable.getScene().getWindow());
        if (file == null) {
            return;
        }
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".xlsx")) {
            path += ".xlsx";
        }
        try {
            new ExcelExporter().exportEvents(List.copyOf(events), path);
            showInfo("Экспорт выполнен", "Файл сохранён:\n" + path);
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void refresh() {
        try {
            events.setAll(eventService.all());
            updateStatistics();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void applyView() {
        try {
            String text = searchField.getText() == null ? "" : searchField.getText().trim();
            List<Event> result = eventService.all();
            if (!text.isBlank()) {
                String lower = text.toLowerCase();
                result = result.stream()
                        .filter(event -> event.getTitle().toLowerCase().contains(lower)
                                || (event.getDescription() != null
                                && event.getDescription().toLowerCase().contains(lower))
                                || (event.getClientName() != null
                                && event.getClientName().toLowerCase().contains(lower)))
                        .toList();
            }

            String status = statusFilter.getValue();
            if (status != null && !"Все".equals(status)) {
                EventStatus selectedStatus = EventStatus.valueOf(status);
                result = result.stream().filter(event -> event.getStatus() == selectedStatus).toList();
            }
            String type = typeFilter.getValue();
            if (type != null && !"Все".equals(type)) {
                EventType selectedType = EventType.valueOf(type);
                result = result.stream().filter(event -> event.getType() == selectedType).toList();
            }

            String sort = sortField.getValue();
            if ("По бюджету".equals(sort)) {
                result = result.stream()
                        .sorted(Comparator.comparing(Event::getBudget).reversed()).toList();
            } else if ("По дате".equals(sort)) {
                result = result.stream()
                        .sorted(Comparator.comparing(Event::getEventDate)).toList();
            }
            events.setAll(result);
            updateStatistics();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void updateStatistics() {
        List<Event> source = eventService.all();
        long total = source.size();
        long planned = source.stream().filter(event -> event.getStatus() == EventStatus.PLANNED).count();
        long completed = source.stream().filter(event -> event.getStatus() == EventStatus.COMPLETED).count();
        BigDecimal sum = source.stream().map(Event::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total == 0 ? BigDecimal.ZERO
                : sum.divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);
        totalLabel.setText("Всего: " + total);
        plannedLabel.setText("Запланировано: " + planned);
        completedLabel.setText("Завершено: " + completed);
        budgetLabel.setText("Сумма бюджетов: " + sum + " ₽");
        averageLabel.setText("Средний бюджет: " + average + " ₽");
    }

    private void showEventDialog(Event existing) {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новый заказ" : "Редактирование заказа");
        dialog.setHeaderText(existing == null ? "Введите данные заказа" : "Измените данные заказа");
        ButtonType save = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<Client> client = new ComboBox<>();
        client.setItems(FXCollections.observableArrayList(clientService.all()));
        client.setPromptText("Клиент");

        TextField title = new TextField();
        title.setPromptText("Название");
        ComboBox<EventType> type = new ComboBox<>(
                FXCollections.observableArrayList(EventType.values()));
        type.setPromptText("Тип");
        ComboBox<EventStatus> status = new ComboBox<>(
                FXCollections.observableArrayList(EventStatus.values()));
        status.setPromptText("Статус");
        DatePicker date = new DatePicker();
        date.setPromptText("Дата мероприятия");
        TextField budget = new TextField();
        budget.setPromptText("Бюджет");
        TextArea description = new TextArea();
        description.setPromptText("Описание");
        description.setPrefRowCount(3);

        if (existing != null) {
            client.getItems().stream()
                    .filter(item -> item.getId().equals(existing.getClientId()))
                    .findFirst()
                    .ifPresent(client::setValue);
            title.setText(existing.getTitle());
            type.setValue(existing.getType());
            status.setValue(existing.getStatus());
            date.setValue(existing.getEventDate());
            budget.setText(existing.getBudget().toPlainString());
            description.setText(existing.getDescription());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Клиент:"), client);
        grid.addRow(1, new Label("Название:"), title);
        grid.addRow(2, new Label("Тип:"), type);
        grid.addRow(3, new Label("Статус:"), status);
        grid.addRow(4, new Label("Дата:"), date);
        grid.addRow(5, new Label("Бюджет:"), budget);
        grid.addRow(6, new Label("Описание:"), description);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != save) {
                return null;
            }
            if (client.getValue() == null || title.getText().isBlank()
                    || type.getValue() == null || status.getValue() == null
                    || date.getValue() == null) {
                showWarning("Проверьте данные",
                        "Клиент, название, тип, статус и дата обязательны.");
                return null;
            }
            final BigDecimal parsedBudget;
            try {
                parsedBudget = new BigDecimal(budget.getText().trim());
            } catch (Exception exception) {
                showWarning("Некорректный бюджет",
                        "Введите положительное числовое значение бюджета.");
                return null;
            }
            if (parsedBudget.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning("Некорректный бюджет",
                        "Бюджет должен быть больше нуля.");
                return null;
            }
            return new Event(
                    existing == null ? null : existing.getId(),
                    client.getValue().getId(),
                    title.getText().trim(),
                    type.getValue(),
                    status.getValue(),
                    date.getValue(),
                    parsedBudget,
                    description.getText().trim());
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                if (existing == null) {
                    eventService.create(result);
                } else {
                    eventService.update(result);
                }
                refresh();
            } catch (RuntimeException exception) {
                showError(exception);
            }
        });
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Проверка данных");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(RuntimeException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Операция не выполнена");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    private void showError(Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Операция не выполнена");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
