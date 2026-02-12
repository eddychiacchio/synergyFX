package com.synergy.controller.fx;

import com.synergy.model.Project;
import com.synergy.model.ProjectMembership;
import com.synergy.model.User;
import com.synergy.util.DataManager;
import com.synergy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class ProjectFormController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    // Rimosso DatePicker

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String description = descField.getText();

        if (name.isEmpty()) {
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        // Genera ID
        int newId = (int) (System.currentTimeMillis() & 0xfffffff);

        // --- CORREZIONE: Usiamo il costruttore ESATTO del tuo Project.java ---
        Project newProject = new Project(newId, name, description);
        
        // --- PASSAGGIO FONDAMENTALE: Aggiungiamo l'utente al progetto ---
        // Se non facciamo questo, il progetto esiste ma non è "tuo", quindi non lo vedrai
        ProjectMembership membership = new ProjectMembership(newProject, currentUser);
        // Nota: Se ProjectMembership ha un costruttore diverso (es. User, Project), inverti i parametri.
        // Se ProjectMembership richiede ruolo, usa: new ProjectMembership(newProject, currentUser, "ADMIN");
        
        newProject.getMemberships().add(membership);
        // ---------------------------------------------------------------

        // Salva
        List<Project> projects = DataManager.getInstance().getProjects();
        projects.add(newProject);
        DataManager.getInstance().saveData();

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