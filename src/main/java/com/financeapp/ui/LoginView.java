package com.financeapp.ui;

import com.financeapp.model.User;
import com.financeapp.service.AuthService;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import java.util.Optional;
import java.util.function.Consumer;

public class LoginView {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    private VBox root;
    private TextField emailField, nameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private boolean isRegisterMode = false;

    public LoginView(ApplicationContext context, Consumer<User> onLoginSuccess) {
        this.authService = context.getBean(AuthService.class);
        this.onLoginSuccess = onLoginSuccess;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setMaxWidth(400);

        Label title = new Label("Smart Personal Finance");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");

        Label subtitle = new Label("Login to your account");
        subtitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14;");

        nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setVisible(false);
        nameField.setManaged(false);
        styleInput(nameField);

        emailField = new TextField();
        emailField.setPromptText("Email");
        styleInput(emailField);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);

        Button actionBtn = new Button("Login");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white;" +
                "-fx-background-radius: 6; -fx-padding: 10; -fx-font-size: 14; -fx-cursor: hand;");
        actionBtn.setOnAction(e -> handleAction());

        Button toggleBtn = new Button("Don't have an account? Register");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6ea8fe;" +
                "-fx-cursor: hand; -fx-border-color: transparent;");
        toggleBtn.setOnAction(e -> {
            isRegisterMode = !isRegisterMode;
            nameField.setVisible(isRegisterMode);
            nameField.setManaged(isRegisterMode);
            actionBtn.setText(isRegisterMode ? "Register" : "Login");
            subtitle.setText(isRegisterMode ? "Create an account" : "Login to your account");
            toggleBtn.setText(isRegisterMode
                    ? "Already have an account? Login"
                    : "Don't have an account? Register");
        });

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #dc3545;");
        statusLabel.setWrapText(true);

        root.getChildren().addAll(title, subtitle, nameField,
                emailField, passwordField, actionBtn, toggleBtn, statusLabel);
    }

    private void handleAction() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Email and password are required");
            return;
        }

        try {
            if (isRegisterMode) {
                String name = nameField.getText().trim();
                if (name.isBlank()) { statusLabel.setText("Name is required"); return; }
                User user = authService.register(name, email, password);
                onLoginSuccess.accept(user);
            } else {
                Optional<User> user = authService.login(email, password);
                if (user.isPresent()) {
                    onLoginSuccess.accept(user.get());
                } else {
                    statusLabel.setText("Invalid email or password");
                }
            }
        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    private void styleInput(Control input) {
        input.setMaxWidth(Double.MAX_VALUE);
        input.setStyle("-fx-background-color: #2a2a4a; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #888; -fx-background-radius: 6; -fx-padding: 10;");
    }

    public VBox getRoot() { return root; }
}