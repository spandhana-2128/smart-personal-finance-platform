package com.financeapp;

import com.financeapp.model.User;
import com.financeapp.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JavaFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setTitle("Smart Personal Finance Platform");
        primaryStage.setScene(scene);

        LoginView loginView = new LoginView(BudgetModuleApplication.context, user -> {
            Platform.runLater(() -> showMainApp(primaryStage, root, user));
        });

        StackPane loginContainer = new StackPane(loginView.getRoot());
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setStyle("-fx-background-color: #1a1a2e;");
        root.getChildren().add(loginContainer);

        primaryStage.show();
    }

    private void showMainApp(Stage stage, StackPane root, User user) {
        Long userId = user.getUserId();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BudgetView      budgetView      = new BudgetView(BudgetModuleApplication.context, userId);
        TransactionView transactionView = new TransactionView(BudgetModuleApplication.context, userId);
        GoalView        goalView        = new GoalView(BudgetModuleApplication.context, userId);
        InsightView     insightView     = new InsightView(BudgetModuleApplication.context);

        Tab transactionTab = new Tab("Income & Expenses",     transactionView.getRoot());
        Tab budgetTab      = new Tab("Budget Planner",        budgetView.getRoot());
        Tab goalTab        = new Tab("Goals & Notifications", goalView.getRoot());
        Tab insightTab     = new Tab("Spending Insights",     insightView.getRoot());

        tabPane.getTabs().addAll(transactionTab, budgetTab, goalTab, insightTab);

        budgetTab.setOnSelectionChanged(e -> {
            if (budgetTab.isSelected()) budgetView.refresh();
        });

        root.getChildren().clear();
        root.getChildren().add(tabPane);
        stage.setTitle("Smart Finance — Welcome, " + user.getName());
    }
}