package com.synergy.controller.fx;

import com.synergy.controller.ProjectController;
import com.synergy.model.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class ActivityFormController {

    @FXML private TextField titleField;
    @FXML private ComboBox<String> priorityBox;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextArea subtasksArea;

    private Project currentProject;
    private ProjectController projectController = new ProjectController();

    @FXML
    public void initialize() {
        // Riempiamo il menu a tendina con le priorità
        priorityBox.getItems().addAll("BASSA", "MEDIA", "ALTA");
        priorityBox.getSelectionModel().select("MEDIA"); // Valore di default
    }

    public void setProject(Project project) {
        this.currentProject = project;
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            titleField.setStyle("-fx-border-color: red;");
            return;
        }

        String priority = priorityBox.getValue();
        LocalDate date = deadlinePicker.getValue();
        String dateStr = (date != null) ? date.toString() : "";

        // Suddivide le sotto-task per riga
        String subtasksText = subtasksArea.getText();
        String[] subTasks = null;
        if (subtasksText != null && !subtasksText.trim().isEmpty()) {
            subTasks = subtasksText.split("\\n"); // Divide usando "A capo"
        }

        // Passa tutto al controller principale (esattamente come faceva la Servlet!)
        projectController.addActivityToProject(currentProject.getId(), title, priority, dateStr, subTasks);

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}