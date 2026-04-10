package com.financeapp.ui;

import com.financeapp.model.*;
import com.financeapp.service.CategoryService;
import com.financeapp.service.TransactionService;
import com.financeapp.service.CurrencyService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class TransactionView {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;

    private final Long USER_ID;

    private VBox root;
    private TableView<Transaction> transactionTable;
    private Label statusLabel;

    // Form fields
    private ToggleGroup typeToggle;
    private RadioButton incomeRadio, expenseRadio;
    private TextField amountField, descriptionField, sourceField, paymentField;
    private DatePicker datePicker;
    private ComboBox<Category> categoryBox;
    private ComboBox<String> frequencyBox;
    private CheckBox recurringCheck;
    private VBox incomeFields, expenseFields;

    // Summary
    private VBox summaryBox;

    public TransactionView(ApplicationContext context, Long userId) {
        this.context = context;
        this.USER_ID = userId;
        this.transactionService = context.getBean(TransactionService.class);
        this.categoryService = context.getBean(CategoryService.class);
        this.currencyService = context.getBean(CurrencyService.class);
        buildUI();
        loadTransactions();
        loadSummary();
    }
    @Autowired private ApplicationContext context;

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 16 24;");
        Label title = new Label("Smart Finance - Income & Expenses");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        header.getChildren().add(title);

        HBox content = new HBox(16);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        // ── Left: Form Card ──
        VBox formCard = createCard("Log Transaction");
        formCard.setPrefWidth(340);

        // Type toggle
        typeToggle = new ToggleGroup();
        incomeRadio = new RadioButton("Income");
        expenseRadio = new RadioButton("Expense");
        incomeRadio.setToggleGroup(typeToggle);
        expenseRadio.setToggleGroup(typeToggle);
        incomeRadio.setSelected(true);
        HBox typeRow = new HBox(16, incomeRadio, expenseRadio);
        typeRow.setAlignment(Pos.CENTER_LEFT);

        // Common fields
        amountField = new TextField();
        amountField.setPromptText("e.g. 5000");
        datePicker = new DatePicker(LocalDate.now());
        descriptionField = new TextField();
        descriptionField.setPromptText("Optional note");

        recurringCheck = new CheckBox("Recurring");
        frequencyBox = new ComboBox<>(FXCollections.observableArrayList(
                "MONTHLY", "WEEKLY", "DAILY"));
        frequencyBox.setValue("MONTHLY");
        frequencyBox.setDisable(true);
        recurringCheck.setOnAction(e ->
                frequencyBox.setDisable(!recurringCheck.isSelected()));
        HBox recurringRow = new HBox(8, recurringCheck, frequencyBox);
        recurringRow.setAlignment(Pos.CENTER_LEFT);

        // Income-only fields
        sourceField = new TextField();
        sourceField.setPromptText("e.g. SALARY, FREELANCE");
        incomeFields = new VBox(8, formRow("Source:", sourceField));

        // Expense-only fields
        categoryBox = new ComboBox<>(
                FXCollections.observableArrayList(categoryService.getAllCategories()));
        categoryBox.setPromptText("Select category");
        categoryBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getCategoryName());
            }
        });
        categoryBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getCategoryName());
            }
        });
        paymentField = new TextField();
        paymentField.setPromptText("CASH / UPI / CARD");
        expenseFields = new VBox(8,
                formRow("Category:", categoryBox),
                formRow("Payment:", paymentField));
        expenseFields.setVisible(false);
        expenseFields.setManaged(false);

        // Toggle visibility on type switch
        typeToggle.selectedToggleProperty().addListener((obs, old, nw) -> {
            boolean isExpense = nw == expenseRadio;
            incomeFields.setVisible(!isExpense);
            incomeFields.setManaged(!isExpense);
            expenseFields.setVisible(isExpense);
            expenseFields.setManaged(isExpense);
        });

        Button saveBtn = styledButton("Save Transaction", "#0d6efd");
        saveBtn.setOnAction(e -> handleSave());
        statusLabel = new Label();
        statusLabel.setWrapText(true);

        formCard.getChildren().addAll(
                new Label("Type:"), typeRow,
                new Separator(),
                formRow("Amount (Rs.):", amountField),
                formRow("Date:", datePicker),
                formRow("Description:", descriptionField),
                recurringRow,
                new Separator(),
                incomeFields,
                expenseFields,
                new Separator(),
                saveBtn, statusLabel
        );

        // ── Right: Table + Summary ──
        VBox rightPanel = new VBox(16);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox tableCard = createCard("Transaction History");
        transactionTable = buildTable();
        VBox.setVgrow(transactionTable, Priority.ALWAYS);

        Button deleteBtn = styledButton("Delete Selected", "#dc3545");
        deleteBtn.setOnAction(e -> handleDelete());
        Button refreshBtn = styledButton("Refresh", "#6c757d");
        refreshBtn.setOnAction(e -> { loadTransactions(); loadSummary(); });
        HBox tableActions = new HBox(8, deleteBtn, refreshBtn);
        tableCard.getChildren().addAll(transactionTable, tableActions);

        // Summary card
        summaryBox = createCard("Expense by Category");

        rightPanel.getChildren().addAll(tableCard, summaryBox);
        content.getChildren().addAll(formCard, rightPanel);
        // Currency Converter card
        VBox currencyCard = createCard("Currency Converter");
        TextField convertAmount = new TextField();
        convertAmount.setPromptText("Amount");
        ComboBox<String> fromCurrency = new ComboBox<>(
            FXCollections.observableArrayList(
                currencyService.getSupportedCurrencies()));
        fromCurrency.setValue("INR");
        ComboBox<String> toCurrency = new ComboBox<>(
            FXCollections.observableArrayList(
                currencyService.getSupportedCurrencies()));
        toCurrency.setValue("USD");
        Label convertResult = new Label("Result will appear here");
        convertResult.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
        Button convertBtn = styledButton("Convert", "#6f42c1");
        convertBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(convertAmount.getText());
                double result = context.getBean(CurrencyService.class)
                    .convert(amt, fromCurrency.getValue(), toCurrency.getValue());
                convertResult.setText(String.format("%.2f %s = %.2f %s",
                    amt, fromCurrency.getValue(), result, toCurrency.getValue()));
            } catch (NumberFormatException ex) {
                convertResult.setText("Enter a valid amount");
            }
        });
        HBox currencyRow = new HBox(8, convertAmount, fromCurrency,
            new Label("→"), toCurrency, convertBtn);
        currencyRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        currencyCard.getChildren().addAll(currencyRow, convertResult);
        root.getChildren().addAll(header, content);
        VBox.setVgrow(content, Priority.ALWAYS);
    }

    @SuppressWarnings("unchecked")
    private TableView<Transaction> buildTable() {
        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(350);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    Transaction t = getTableRow().getItem();
                    boolean isIncome = t instanceof Income;
                    setText(isIncome ? "INCOME" : "EXPENSE");
                    setStyle("-fx-text-fill: " + (isIncome ? "#198754" : "#dc3545") +
                             "; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Transaction, Double> amtCol = new TableColumn<>("Amount (Rs.)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        TableColumn<Transaction, String> catCol = new TableColumn<>("Category/Source");
        catCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Transaction t = getTableRow().getItem();
                    if (t instanceof Income i) setText(i.getSource());
                    else if (t instanceof Expense ex)
                        setText(ex.getCategory() != null
                                ? ex.getCategory().getCategoryName() : "-");
                }
            }
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, Boolean> recCol = new TableColumn<>("Recurring");
        recCol.setCellValueFactory(new PropertyValueFactory<>("isRecurring"));
        recCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Yes" : "No"));
            }
        });

        table.getColumns().addAll(typeCol, dateCol, amtCol, catCol, descCol, recCol);
        return table;
    }

    private void handleSave() {
        try {
            boolean isIncome = typeToggle.getSelectedToggle() == incomeRadio;
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = datePicker.getValue();
            String desc = descriptionField.getText();
            boolean recurring = recurringCheck.isSelected();
            String freq = recurring ? frequencyBox.getValue() : null;

            if (isIncome) {
                String source = sourceField.getText().isBlank() ? "OTHER" : sourceField.getText();
                transactionService.addIncome(USER_ID, amount, date, desc,
                        "INR", source, recurring, freq);
            } else {
                if (categoryBox.getValue() == null) {
                    setStatus("Please select a category", "#ffc107");
                    return;
                }
                String payment = paymentField.getText().isBlank() ? "CASH" : paymentField.getText();
                transactionService.addExpense(USER_ID,
                        categoryBox.getValue().getCategoryId(),
                        amount, date, desc, "INR", payment, recurring, freq);
            }

            setStatus("Transaction saved!", "#198754");
            clearForm();
            loadTransactions();
            loadSummary();
        } catch (NumberFormatException e) {
            setStatus("Enter a valid amount", "#dc3545");
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage(), "#dc3545");
        }
    }

    private void handleDelete() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a transaction first", "#ffc107"); return; }
        transactionService.deleteTransaction(selected.getTransactionId());
        setStatus("Deleted", "#6c757d");
        loadTransactions();
        loadSummary();
    }

    private void loadTransactions() {
        List<Transaction> list = transactionService.getByUser(USER_ID);
        Platform.runLater(() ->
                transactionTable.setItems(FXCollections.observableArrayList(list)));
    }

    private void loadSummary() {
        Map<String, Double> summary = transactionService.getExpenseByCategory(USER_ID);
        Platform.runLater(() -> {
            summaryBox.getChildren().removeIf(n -> !(n instanceof Label &&
                    ((Label) n).getText().equals("Expense by Category")));
            if (summary.isEmpty()) {
                summaryBox.getChildren().add(new Label("No expenses yet."));
            } else {
                summary.forEach((cat, total) -> {
                    HBox row = new HBox(8);
                    row.setAlignment(Pos.CENTER_LEFT);
                    Label catLbl = new Label(cat);
                    catLbl.setMinWidth(120);
                    catLbl.setStyle("-fx-font-weight: bold;");
                    Label amtLbl = new Label(String.format("Rs. %.2f", total));
                    amtLbl.setStyle("-fx-text-fill: #dc3545;");
                    row.getChildren().addAll(catLbl, amtLbl);
                    summaryBox.getChildren().add(row);
                });
            }
        });
    }

    private void clearForm() {
        amountField.clear();
        descriptionField.clear();
        sourceField.clear();
        paymentField.clear();
        datePicker.setValue(LocalDate.now());
        categoryBox.setValue(null);
        recurringCheck.setSelected(false);
        frequencyBox.setDisable(true);
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