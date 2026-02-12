package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.controller.AuthenticationController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;

    private AuthenticationController auth = new AuthenticationController();

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            msgLabel.setText("Compila tutti i campi!");
            msgLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = auth.register(name, email, pass);

        if (success) {
            msgLabel.setText("Registrazione avvenuta! Torna al login.");
            msgLabel.setStyle("-fx-text-fill: green;");
        } else {
            msgLabel.setText("Email già esistente o errore.");
            msgLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void backToLogin() throws IOException {
        App.setRoot("login");
    }
}