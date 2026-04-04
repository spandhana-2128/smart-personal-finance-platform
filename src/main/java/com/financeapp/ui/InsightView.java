package com.financeapp.ui;

import com.financeapp.model.SpendingInsight;
import com.financeapp.model.Transaction;
import com.financeapp.service.InsightService;
import com.financeapp.service.TransactionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * InsightView — JavaFX UI for "Auto-Generated Spending Insights" use case.
 *
 * Layout:
 *  ┌──────────────────────────────────────────┐
 *  │  Header bar                              │
 *  ├──────────────────────────────────────────┤
 *  │  [Generate Insights]  Status label       │
 *  ├────────────────────┬─────────────────────┤
 *  │  Bar Chart         │  Insight Cards      │
 *  │  (category spend)  │  (text summaries)   │
 *  ├────────────────────┴─────────────────────┤
 *  │  Recurring Expenses Table                │
 *  └──────────────────────────────────────────┘
 */
public class InsightView {

    private static final Long USER_ID = 1L;

    private final InsightService insightService;
    private final TransactionService transactionService;

    private VBox root;
    private VBox insightCardsBox;
    private BarChart<String, Number> barChart;
    private Label statusLabel;
    private TableView<Transaction> recurringTable;

    public InsightView(ApplicationContext context) {
        this.insightService    = context.getBean(InsightService.class);
        this.transactionService = context.getBean(TransactionService.class);
        buildUI();
        loadData(); // load existing insights on open
    }

    // ─────────────────────────────────────────────────────────────────────
    //  UI Construction
    // ─────────────────────────────────────────────────────────────────────

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        root.getChildren().addAll(
                buildHeader(),
                buildToolbar(),
                buildMainContent(),
                buildRecurringSection()
        );
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 16 24;");
        Label title = new Label("Smart Finance — Spending Insights");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");
        header.getChildren().add(title);
        return header;
    }

    private HBox buildToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setStyle("-fx-background-color: #ffffff; -fx-padding: 12 24;"
                + "-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button generateBtn = new Button("⚡ Generate Insights");
        generateBtn.setStyle(
                "-fx-background-color: #4361ee; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 6;");

        statusLabel = new Label("Click 'Generate Insights' to analyse your spending.");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");

        generateBtn.setOnAction(e -> generateInsights());

        toolbar.getChildren().addAll(generateBtn, statusLabel);
        return toolbar;
    }

    private HBox buildMainContent() {
        HBox content = new HBox(16);
        content.setPadding(new Insets(16));
        content.setPrefHeight(340);

        // Left — Bar Chart
        VBox chartBox = new VBox(8);
        chartBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-padding: 16; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        HBox.setHgrow(chartBox, Priority.ALWAYS);

        Label chartTitle = new Label("Spending by Category");
        chartTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis   = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount (₹)");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        barChart.setPrefHeight(260);
        barChart.setStyle("-fx-bar-fill: #4361ee;");

        chartBox.getChildren().addAll(chartTitle, barChart);

        // Right — Insight Cards
        VBox cardsPanel = new VBox(8);
        cardsPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-padding: 16; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        cardsPanel.setPrefWidth(340);

        Label cardsTitle = new Label("AI Insights");
        cardsTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        insightCardsBox = new VBox(10);
        insightCardsBox.setPadding(new Insets(4, 0, 4, 0));
        scrollPane.setContent(insightCardsBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        cardsPanel.getChildren().addAll(cardsTitle, scrollPane);

        content.getChildren().addAll(chartBox, cardsPanel);
        return content;
    }

    private VBox buildRecurringSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(0, 16, 16, 16));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-padding: 16; -fx-margin: 0 16 16 16;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        Label title = new Label("🔁 Recurring Expenses");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        recurringTable = new TableView<>();
        recurringTable.setPrefHeight(160);
        recurringTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recurringTable.setPlaceholder(new Label("No recurring expenses found."));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDescription()));

        TableColumn<Transaction, String> amtCol = new TableColumn<>("Amount (₹)");
        amtCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f", c.getValue().getAmount())));

        TableColumn<Transaction, String> freqCol = new TableColumn<>("Frequency");
        freqCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFrequency() != null
                                ? c.getValue().getFrequency() : "-"));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDate() != null
                                ? c.getValue().getDate().toString() : "-"));

        recurringTable.getColumns().addAll(descCol, amtCol, freqCol, dateCol);

        section.getChildren().addAll(title, recurringTable);
        return section;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Data Loading
    // ─────────────────────────────────────────────────────────────────────

    /** Called on tab open — loads any previously generated insights. */
    private void loadData() {
        new Thread(() -> {
            try {
                List<SpendingInsight> insights = insightService.getInsights(USER_ID);
                Map<String, Double>   categoryMap =
                        transactionService.getExpenseByCategory(USER_ID);
                List<Transaction> recurring =
                        transactionService.getByUser(USER_ID).stream()
                                .filter(Transaction::isRecurring)
                                .toList();

                Platform.runLater(() -> {
                    renderInsightCards(insights);
                    renderBarChart(categoryMap);
                    renderRecurringTable(recurring);
                    if (!insights.isEmpty()) {
                        statusLabel.setText("Insights loaded. Last generated: "
                                + insights.get(0).getGeneratedAt()
                                          .toLocalDate());
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        statusLabel.setText("Could not load data: " + ex.getMessage()));
            }
        }).start();
    }

    /** Called when the user clicks 'Generate Insights'. */
    private void generateInsights() {
        statusLabel.setText("Analysing your spending…");
        statusLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-size: 13;");

        new Thread(() -> {
            try {
                List<SpendingInsight> insights = insightService.generateInsights(USER_ID);
                Map<String, Double>   categoryMap =
                        transactionService.getExpenseByCategory(USER_ID);
                List<Transaction> recurring =
                        transactionService.getByUser(USER_ID).stream()
                                .filter(Transaction::isRecurring)
                                .toList();

                Platform.runLater(() -> {
                    renderInsightCards(insights);
                    renderBarChart(categoryMap);
                    renderRecurringTable(recurring);
                    statusLabel.setText("✅ " + insights.size()
                            + " insight(s) generated for this month.");
                    statusLabel.setStyle("-fx-text-fill: #2d6a4f; -fx-font-size: 13;");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 13;");
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Rendering helpers
    // ─────────────────────────────────────────────────────────────────────

    private void renderInsightCards(List<SpendingInsight> insights) {
        insightCardsBox.getChildren().clear();

        if (insights.isEmpty()) {
            Label empty = new Label("No insights yet. Add transactions and click Generate.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
            empty.setWrapText(true);
            insightCardsBox.getChildren().add(empty);
            return;
        }

        for (SpendingInsight insight : insights) {
            insightCardsBox.getChildren().add(buildInsightCard(insight));
        }
    }

    private VBox buildInsightCard(SpendingInsight insight) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setStyle(cardStyle(insight.getInsightType()));

        String emoji = switch (insight.getInsightType()) {
            case "TOP_CATEGORY"      -> "🏆";
            case "SAVINGS_RATE"      -> "💰";
            case "SPENDING_DIVERSITY"-> "📊";
            default                  -> "💡";
        };

        Label typeLabel = new Label(emoji + "  " + friendlyType(insight.getInsightType()));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #1a1a2e;");

        Label msg = new Label(insight.getMessage());
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");

        card.getChildren().addAll(typeLabel, msg);
        return card;
    }

    private String cardStyle(String type) {
        String color = switch (type) {
            case "TOP_CATEGORY"       -> "#e8f4fd";
            case "SAVINGS_RATE"       -> "#e8f8f0";
            case "SPENDING_DIVERSITY" -> "#fef9e7";
            default                   -> "#f5f5f5";
        };
        return "-fx-background-color: " + color + "; -fx-background-radius: 8;"
                + "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-border-width: 1;";
    }

    private String friendlyType(String type) {
        return switch (type) {
            case "TOP_CATEGORY"       -> "Top Spending Category";
            case "SAVINGS_RATE"       -> "Savings Rate";
            case "SPENDING_DIVERSITY" -> "Spending Diversity";
            default                   -> type;
        };
    }

    private void renderBarChart(Map<String, Double> categoryMap) {
        barChart.getData().clear();
        if (categoryMap == null || categoryMap.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spending");
        categoryMap.forEach((cat, amt) ->
                series.getData().add(new XYChart.Data<>(cat, amt)));

        barChart.getData().add(series);

        // Style bars after render
        Platform.runLater(() ->
            series.getData().forEach(d -> {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill: #4361ee;");
                }
            })
        );
    }

    private void renderRecurringTable(List<Transaction> recurring) {
        recurringTable.setItems(FXCollections.observableArrayList(recurring));
    }

    public VBox getRoot() {
        return root;
    }
}
