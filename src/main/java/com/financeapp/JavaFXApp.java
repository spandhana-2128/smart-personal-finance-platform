package com.financeapp;

import com.financeapp.ui.BudgetView;
import com.financeapp.ui.TransactionView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class JavaFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BudgetView budgetView = new BudgetView(BudgetModuleApplication.context);
        TransactionView transactionView = new TransactionView(BudgetModuleApplication.context);

        Tab transactionTab = new Tab("Income & Expenses", transactionView.getRoot());
        Tab budgetTab = new Tab("Budget Planner", budgetView.getRoot());

        tabPane.getTabs().addAll(transactionTab, budgetTab);

        budgetTab.setOnSelectionChanged(e -> {
            if (budgetTab.isSelected()) {
                budgetView.refresh();
            }
        });

        Scene scene = new Scene(tabPane, 1100, 700);
        primaryStage.setTitle("Smart Personal Finance Platform");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}