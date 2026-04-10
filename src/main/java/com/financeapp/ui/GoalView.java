package com.financeapp.ui;

import com.financeapp.model.Goal;
import com.financeapp.model.Notification;
import com.financeapp.repository.NotificationRepository;
import com.financeapp.service.GoalService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.context.ApplicationContext;
import javafx.scene.Parent;
import java.util.List;


public class GoalView {

    private final GoalService goalService;
    private final NotificationRepository notificationRepository;

    private Long USER_ID;

    private VBox root;
    private TableView<Goal> goalTable;
    private TextField nameField, targetField, savedField, addAmountField;
    private Label statusLabel;
    private VBox notificationBox;

    public GoalView(ApplicationContext context, Long userId) {
        this.USER_ID = userId;
        this.goalService = context.getBean(GoalService.class);
        this.notificationRepository = context.getBean(NotificationRepository.class);
        buildUI();
        loadGoals();
        loadNotifications();
    }

    // Keep old no-arg constructor so existing JavaFXApp still compiles temporarily
    public GoalView() {
        this.goalService = null;
        this.notificationRepository = null;
        root = new VBox(10);
        root.getChildren().add(new Label("GoalView not initialized - pass ApplicationContext"));
    }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        // Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 16 24;");
        Label title = new Label("Smart Finance - Goals & Savings");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        header.getChildren().add(title);

        HBox content = new HBox(16);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        // ── Left: Create Goal form ──
        VBox formCard = createCard("Create Savings Goal");
        formCard.setPrefWidth(300);

        nameField = new TextField();
        nameField.setPromptText("e.g. Emergency Fund");

        targetField = new TextField();
        targetField.setPromptText("e.g. 50000");

        savedField = new TextField();
        savedField.setPromptText("Initial saved amount (0 if new)");

        Button createBtn = styledButton("Create Goal", "#0d6efd");
        createBtn.setOnAction(e -> handleCreate());

        statusLabel = new Label();
        statusLabel.setWrapText(true);

        formCard.getChildren().addAll(
                formRow("Goal Name:", nameField),
                formRow("Target (Rs.):", targetField),
                formRow("Already Saved:", savedField),
                createBtn,
                statusLabel
        );

        // ── Right panel ──
        VBox rightPanel = new VBox(16);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Goals table card
        VBox tableCard = createCard("Your Savings Goals");
        goalTable = buildTable();
        VBox.setVgrow(goalTable, Priority.ALWAYS);

        // Add savings row
        addAmountField = new TextField();
        addAmountField.setPromptText("Amount to add");
        addAmountField.setPrefWidth(180);
        Button addBtn = styledButton("Add Savings", "#198754");
        addBtn.setOnAction(e -> handleAddSavings());
        Button deleteBtn = styledButton("Delete Goal", "#dc3545");
        deleteBtn.setOnAction(e -> handleDelete());
        Button refreshBtn = styledButton("Refresh", "#6c757d");
        refreshBtn.setOnAction(e -> { loadGoals(); loadNotifications(); });

        HBox tableActions = new HBox(8, addAmountField, addBtn, deleteBtn, refreshBtn);
        tableActions.setAlignment(Pos.CENTER_LEFT);

        tableCard.getChildren().addAll(goalTable, tableActions);

        // Notifications card
        VBox notifCard = createCard("Notifications");
        notificationBox = new VBox(6);
        ScrollPane notifScroll = new ScrollPane(notificationBox);
        notifScroll.setFitToWidth(true);
        notifScroll.setPrefHeight(150);
        notifScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button clearNotifsBtn = styledButton("Mark All Read", "#6c757d");
        clearNotifsBtn.setOnAction(e -> {
            notificationRepository.deleteAll();
            loadNotifications();
        });

        notifCard.getChildren().addAll(notifScroll, clearNotifsBtn);

        rightPanel.getChildren().addAll(tableCard, notifCard);
        content.getChildren().addAll(formCard, rightPanel);
        root.getChildren().addAll(header, content);
        VBox.setVgrow(content, Priority.ALWAYS);
    }

    @SuppressWarnings("unchecked")
    private TableView<Goal> buildTable() {
        TableView<Goal> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(300);

        TableColumn<Goal, String> nameCol = new TableColumn<>("Goal");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        TableColumn<Goal, Double> targetCol = new TableColumn<>("Target (Rs.)");
        targetCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", getTableRow().getItem().getTargetAmount()));
                }
            }
        });

        TableColumn<Goal, Double> savedCol = new TableColumn<>("Saved (Rs.)");
        savedCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", getTableRow().getItem().getSavedAmount()));
                }
            }
        });

        TableColumn<Goal, Double> progressCol = new TableColumn<>("Progress");
        progressCol.setPrefWidth(160);
        progressCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Goal g = getTableRow().getItem();
                double pct = g.getTargetAmount() == 0 ? 0
                        : Math.min(g.getSavedAmount() / g.getTargetAmount(), 1.0);
                ProgressBar bar = new ProgressBar(pct);
                bar.setPrefWidth(100);
                String color = pct >= 1.0 ? "#198754" : pct >= 0.5 ? "#ffc107" : "#0d6efd";
                bar.setStyle("-fx-accent: " + color + ";");
                Label lbl = new Label(String.format("  %.1f%%", pct * 100));
                lbl.setStyle(pct >= 1.0
                        ? "-fx-text-fill: #198754; -fx-font-weight: bold;"
                        : "-fx-text-fill: #333;");
                HBox box = new HBox(4, bar, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        TableColumn<Goal, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    Goal g = getTableRow().getItem();
                    boolean achieved = g.getSavedAmount() >= g.getTargetAmount();
                    setText(achieved ? "✅ Achieved!" : "In Progress");
                    setStyle("-fx-text-fill: " + (achieved ? "#198754" : "#856404") +
                            "; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(nameCol, targetCol, savedCol, progressCol, statusCol);
        return table;
    }

    private void handleCreate() {
        try {
            String name = nameField.getText().trim();
            if (name.isBlank()) { setStatus("Enter a goal name", "#ffc107"); return; }
            double target = Double.parseDouble(targetField.getText());
            double saved  = savedField.getText().isBlank() ? 0
                    : Double.parseDouble(savedField.getText());

            com.financeapp.model.User user = new com.financeapp.model.User();
            user.setUserId(USER_ID);

            Goal goal = Goal.builder()
                    .name(name)
                    .targetAmount(target)
                    .savedAmount(saved)
                    .user(user)
                    .build();

            goalService.createGoal(goal);
            setStatus("Goal created!", "#198754");
            nameField.clear(); targetField.clear(); savedField.clear();
            loadGoals();
        } catch (NumberFormatException e) {
            setStatus("Enter valid amounts", "#dc3545");
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "#dc3545");
        }
    }

    private void handleAddSavings() {
        Goal selected = goalTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a goal first", "#ffc107"); return; }
        try {
            double amount = Double.parseDouble(addAmountField.getText());
            goalService.addSavings(selected.getId(), amount);
            addAmountField.clear();
            setStatus("Savings added!", "#198754");
            loadGoals();
            loadNotifications();
        } catch (NumberFormatException e) {
            setStatus("Enter a valid amount", "#dc3545");
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "#dc3545");
        }
    }

    private void handleDelete() {
        Goal selected = goalTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a goal first", "#ffc107"); return; }
        goalService.deleteGoal(selected.getId());
        setStatus("Goal deleted", "#6c757d");
        loadGoals();
    }

    private void loadGoals() {
        List<Goal> goals = goalService.getGoalsByUser(USER_ID);
        Platform.runLater(() ->
                goalTable.setItems(FXCollections.observableArrayList(goals)));
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        Platform.runLater(() -> {
            notificationBox.getChildren().clear();
            if (notifications.isEmpty()) {
                Label empty = new Label("No notifications");
                empty.setStyle("-fx-text-fill: #888;");
                notificationBox.getChildren().add(empty);
            } else {
                for (Notification n : notifications) {
                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: #d1e7dd; -fx-background-radius: 6;" +
                            "-fx-padding: 6 10;");
                    Label icon = new Label("🔔");
                    Label msg = new Label(n.getMessage());
                    msg.setStyle("-fx-text-fill: #0f5132; -fx-font-weight: bold;");
                    Label time = new Label(n.getTimestamp() != null
                            ? "  " + n.getTimestamp().toLocalTime().toString().substring(0, 8)
                            : "");
                    time.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11;");
                    row.getChildren().addAll(icon, msg, time);
                    notificationBox.getChildren().add(row);
                }
            }
        });
    }

    private VBox createCard(String title) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-padding: 16;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        card.getChildren().add(lbl);
        return card;
    }

    private HBox formRow(String labelText, javafx.scene.Node input) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(labelText);
        lbl.setMinWidth(120);
        lbl.setStyle("-fx-text-fill: #555;");
        if (input instanceof Control) ((Control) input).setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(input, Priority.ALWAYS);
        row.getChildren().addAll(lbl, input);
        return row;
    }

    private Button styledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;" +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 13;");
        return btn;
    }

    private void setStatus(String msg, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        });
    }

    public VBox getRoot() { return root; }

    public Parent getView() 
    {
        return root;
    }
}