package com.synergy.controller.fx;

import com.synergy.model.Project;
import com.synergy.model.User;
import com.synergy.util.DataManager;
import com.synergy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Date;
import java.util.List;

public class ProjectFormController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private DatePicker deadlinePicker;

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String description = descField.getText();

        if (name.isEmpty()) {
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        // 1. Recupera l'utente corrente (sarà il proprietario)
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // 2. Genera un ID univoco per il progetto
        int newId = (int) (System.currentTimeMillis() & 0xfffffff);

        // 3. Gestione Data (se selezionata, altrimenti data odierna + 30gg)
        Date deadline = new Date();
        if (deadlinePicker.getValue() != null) {
            deadline = java.sql.Date.valueOf(deadlinePicker.getValue());
        }

        // 4. Crea il progetto
        Project newProject = new Project(newId, name, description, new Date(), deadline, currentUser);

        // 5. Salva nel DataManager
        List<Project> projects = DataManager.getInstance().getProjects();
        projects.add(newProject);
        DataManager.getInstance().saveData();

        // 6. Chiudi la finestra
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}