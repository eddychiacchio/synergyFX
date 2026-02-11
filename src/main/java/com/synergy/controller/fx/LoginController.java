package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.controller.AuthenticationController;
import com.synergy.model.User;
import com.synergy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AuthenticationController auth = new AuthenticationController();

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText();
        String pass = passwordField.getText();

        User user = auth.login(email, pass);

        if (user != null) {
            SessionManager.getInstance().login(user);
            App.setRoot("dashboard");
        } else {
            errorLabel.setText("Email o password errati");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }
}