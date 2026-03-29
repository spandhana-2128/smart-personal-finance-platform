package com.financeapp.ui;

import com.financeapp.model.Budget;
import com.financeapp.service.AlertService;
import com.financeapp.service.BudgetService;
import com.financeapp.service.ReportService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.nio.file.*;
import java.time.Year;
import java.util.List;

public class BudgetView {

    private final BudgetService budgetService;
    private final AlertService alertService;
    private final ReportService reportService;

    private static final Long USER_ID = 1L;

    private VBox root;
    private TableView<Budget> budgetTable;
    private Label statusLabel;
    private ComboBox<String> monthBox, strategyBox;
    private TextField limitField, yearField, spendField, incomeField;
    private Label suggestedLabel;
    private VBox alertBox;

    public BudgetView(ApplicationContext context) {
        this.budgetService = context.getBean(BudgetService.class);
        this.alertService = context.getBean(AlertService.class);
        this.reportService = context.getBean(ReportService.class);
        buildUI();
        loadBudgets();
        loadAlerts();
    }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 16 24;");
        Label title = new Label("Smart Finance - Budget Planner");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        header.getChildren().add(title);

        alertBox = new VBox(4);
        alertBox.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10 16;");
        alertBox.setVisible(false);
        alertBox.setManaged(false);

        HBox content = new HBox(16);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        VBox formCard = createCard("Create Budget");
        formCard.setPrefWidth(320);

        monthBox = new ComboBox<>(FXCollections.observableArrayList(
                "JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE",
                "JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"));
        monthBox.setValue("JANUARY");

        yearField = new TextField(String.valueOf(Year.now().getValue()));
        limitField = new TextField();
        limitField.setPromptText("e.g. 15000");

        strategyBox = new ComboBox<>(FXCollections.observableArrayList("CONSERVATIVE","AGGRESSIVE"));
        strategyBox.setValue("CONSERVATIVE");

        incomeField = new TextField();
        incomeField.setPromptText("Monthly income Rs.");
        suggestedLabel = new Label();
        suggestedLabel.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");

        Button suggestBtn = styledButton("Get Suggested Limit", "#6c757d");
        suggestBtn.setOnAction(e -> handleSuggest());

        Button createBtn = styledButton("Create Budget", "#0d6efd");
        createBtn.setOnAction(e -> handleCreate());

        statusLabel = new Label();
        statusLabel.setWrapText(true);

        formCard.getChildren().addAll(
                formRow("Month:", monthBox),
                formRow("Year:", yearField),
                formRow("Strategy:", strategyBox),
                formRow("Budget Limit:", limitField),
                new Separator(),
                new Label("Suggest limit from income:"),
                formRow("Income:", incomeField),
                suggestBtn, suggestedLabel,
                new Separator(),
                createBtn, statusLabel
        );

        VBox rightPanel = new VBox(16);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox tableCard = createCard("Your Budgets");
        budgetTable = buildTable();
        VBox.setVgrow(budgetTable, Priority.ALWAYS);

        HBox spendRow = new HBox(8);
        spendRow.setAlignment(Pos.CENTER_LEFT);
        spendField = new TextField();
        spendField.setPromptText("Add expense amount");
        spendField.setPrefWidth(200);
        Button spendBtn = styledButton("Add Spending", "#198754");
        spendBtn.setOnAction(e -> handleAddSpending());
        Button deleteBtn = styledButton("Delete Budget", "#dc3545");
        deleteBtn.setOnAction(e -> handleDelete());
        spendRow.getChildren().addAll(spendField, spendBtn, deleteBtn);

        tableCard.getChildren().addAll(budgetTable, spendRow);

        VBox exportCard = createCard("Export Report");
        Button pdfBtn = styledButton("Download PDF", "#6f42c1");
        Button csvBtn = styledButton("Download CSV", "#0dcaf0");
        pdfBtn.setOnAction(e -> handleExport("pdf"));
        csvBtn.setOnAction(e -> handleExport("csv"));
        HBox exportBtns = new HBox(12, pdfBtn, csvBtn);
        exportCard.getChildren().add(exportBtns);

        rightPanel.getChildren().addAll(tableCard, exportCard);
        content.getChildren().addAll(formCard, rightPanel);
        root.getChildren().addAll(header, alertBox, content);
        VBox.setVgrow(content, Priority.ALWAYS);
    }

    @SuppressWarnings("unchecked")
    private TableView<Budget> buildTable() {
        TableView<Budget> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);

        TableColumn<Budget, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(new PropertyValueFactory<>("month"));

        TableColumn<Budget, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Budget, Double> limitCol = new TableColumn<>("Limit");
        limitCol.setCellValueFactory(new PropertyValueFactory<>("monthlyLimit"));

        TableColumn<Budget, Double> spentCol = new TableColumn<>("Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("currentSpending"));

        TableColumn<Budget, String> stratCol = new TableColumn<>("Strategy");
        stratCol.setCellValueFactory(new PropertyValueFactory<>("strategyType"));

        TableColumn<Budget, Double> pctCol = new TableColumn<>("% Used");
        pctCol.setCellValueFactory(new PropertyValueFactory<>("currentSpending"));
        pctCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Budget b = (Budget) getTableRow().getItem();
                double pct = b.getSpendingPercentage();
                ProgressBar bar = new ProgressBar(pct / 100);
                bar.setPrefWidth(100);
                String color = pct >= 100 ? "#dc3545" : pct >= 70 ? "#ffc107" : "#198754";
                bar.setStyle("-fx-accent: " + color + ";");
                Label lbl = new Label(String.format("  %.1f%%", pct));
                HBox box = new HBox(4, bar, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(monthCol, yearCol, limitCol, spentCol, stratCol, pctCol);
        return table;
    }

    private void handleCreate() {
        try {
            budgetService.createBudget(
                    USER_ID,
                    Double.parseDouble(limitField.getText()),
                    monthBox.getValue(),
                    Integer.parseInt(yearField.getText()),
                    strategyBox.getValue()
            );
            setStatus("Budget created!", "#198754");
            limitField.clear();
            loadBudgets();
            loadAlerts();
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), "#dc3545");
        }
    }

    private void handleSuggest() {
        try {
            double income = Double.parseDouble(incomeField.getText());
            double suggested = budgetService.getSuggestedLimit(strategyBox.getValue(), income);
            suggestedLabel.setText("Suggested: Rs." + String.format("%.2f", suggested));
            limitField.setText(String.format("%.2f", suggested));
        } catch (Exception e) {
            suggestedLabel.setText("Enter valid income");
        }
    }

    private void handleAddSpending() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a budget first", "#ffc107"); return; }
        try {
            budgetService.addSpending(selected.getBudgetId(),
                    Double.parseDouble(spendField.getText()));
            spendField.clear();
            setStatus("Spending added!", "#198754");
            loadBudgets();
            loadAlerts();
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "#dc3545");
        }
    }

    private void handleDelete() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a budget first", "#ffc107"); return; }
        budgetService.deleteBudget(selected.getBudgetId());
        setStatus("Budget deleted", "#6c757d");
        loadBudgets();
    }

    private void handleExport(String type) {
        try {
            byte[] data = type.equals("pdf")
                    ? reportService.exportPDF(USER_ID)
                    : reportService.exportCSV(USER_ID);
            String filename = "budget_report." + type;
            Path path = Paths.get(System.getProperty("user.home"), "Downloads", filename);
            Files.write(path, data);
            setStatus("Saved to Downloads/" + filename, "#198754");
        } catch (IOException e) {
            setStatus("Export failed: " + e.getMessage(), "#dc3545");
        }
    }

    private void loadBudgets() {
        List<Budget> budgets = budgetService.getBudgetsByUser(USER_ID);
        Platform.runLater(() ->
                budgetTable.setItems(FXCollections.observableArrayList(budgets)));
    }

    private void loadAlerts() {
        // Use full class name to avoid conflict with javafx.scene.control.Alert
        List<com.financeapp.model.Alert> alerts = alertService.getUnseenAlerts(USER_ID);
        Platform.runLater(() -> {
            alertBox.getChildren().clear();
            if (alerts.isEmpty()) {
                alertBox.setVisible(false);
                alertBox.setManaged(false);
            } else {
                alertBox.setVisible(true);
                alertBox.setManaged(true);
                for (com.financeapp.model.Alert a : alerts) {
                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    Label msg = new Label(a.getMessage());
                    msg.setStyle("-fx-text-fill: " +
                            (a.getAlertType().equals("EXCEEDED") ? "#dc3545" : "#856404") + ";");
                    Button dismiss = new Button("X");
                    dismiss.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    dismiss.setOnAction(e -> {
                        alertService.markSeen(a.getAlertId());
                        loadAlerts();
                    });
                    row.getChildren().addAll(msg, dismiss);
                    alertBox.getChildren().add(row);
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
}