package com.financeapp;

import com.financeapp.ui.BudgetView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        BudgetView view = new BudgetView(BudgetModuleApplication.context);
        Scene scene = new Scene(view.getRoot(), 900, 650);
        primaryStage.setTitle("💰 Smart Finance - Budget Planner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}