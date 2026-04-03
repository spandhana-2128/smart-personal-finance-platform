package com.financeapp.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class GoalView {

    public VBox getView() {

        Label title = new Label("Goal Management");

        TextField name = new TextField();
        name.setPromptText("Goal Name");

        TextField target = new TextField();
        target.setPromptText("Target Amount");

        TextField saved = new TextField();
        saved.setPromptText("Saved Amount");

        TextField userId = new TextField();
        userId.setPromptText("User ID");

        Button create = new Button("Create Goal");

        create.setOnAction(e -> {
            String json = "{ \"name\":\"" + name.getText() +
                    "\", \"targetAmount\":" + target.getText() +
                    ", \"savedAmount\":" + saved.getText() +
                    ", \"user\":{\"userId\":" + userId.getText() + "} }";

            ApiClient.post("/goals", json);
        });

        TextField goalId = new TextField();
        goalId.setPromptText("Goal ID");

        TextField amount = new TextField();
        amount.setPromptText("Add Amount");

        Button add = new Button("Add Savings");

        add.setOnAction(e -> {
            ApiClient.put("/goals/" + goalId.getText() + "/add?amount=" + amount.getText());
        });

        Label note = new Label("Check notifications via API (GET /notifications)");

        VBox layout = new VBox(10,
                title,
                name, target, saved, userId, create,
                goalId, amount, add,
                note
        );

        layout.setPadding(new Insets(20));
        return layout;
    }
}