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
    
    private ProjectFormController projectController = new ProjectFormController();

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

        if (name.isEmpty()) {
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();

        int newId = (int) (System.currentTimeMillis() & 0xfffffff);

        Project newProject = new Project(newId, name, description);
        
        ProjectMembership membership = new ProjectMembership();
        membership.setProject(newProject);
        membership.setUser(currentUser);
        membership.setIsAdmin(true);
        
        newProject.getMemberships().add(membership);

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