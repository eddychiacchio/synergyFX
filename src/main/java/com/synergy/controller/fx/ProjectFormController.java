package com.synergy.controller.fx;

import com.synergy.controller.ProjectController;
import com.synergy.model.Project;
import com.synergy.model.User;
import com.synergy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProjectFormController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    
    // Richiamiamo il tuo backend
    private ProjectController projectController = new ProjectController();

    private Project projectToEdit = null;
    
    public void setProjectToEdit(Project project) {
        this.projectToEdit = project;
        if (nameField != null) nameField.setText(project.getName());
        if (descField != null) descField.setText(project.getDescription());
    }
    
    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String description = descField.getText();

        if (name == null || name.trim().isEmpty()) {
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        // IL BIVIO MAGICO: È una Creazione o una Modifica?
        if (projectToEdit == null) {
            // 1. CREAZIONE (Il progetto non esiste, deleghiamo al ProjectController)
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                projectController.createProject(name, description, currentUser);
            }
        } else {
            // 2. MODIFICA (Il progetto esiste già, aggiorniamo solo i testi)
            projectController.updateProjectDetails(projectToEdit.getId(), name, description);
        }

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