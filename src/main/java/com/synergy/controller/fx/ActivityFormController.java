package com.synergy.controller.fx;

import com.synergy.controller.ProjectController;
import com.synergy.model.Activity;
import com.synergy.model.Project;
import com.synergy.model.TaskGroup;
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
    
    // NUOVA VARIABILE: Tiene traccia se stiamo modificando un'attività esistente
    private Activity activityToEdit = null; 

    @FXML
    public void initialize() {
        priorityBox.getItems().addAll("BASSA", "MEDIA", "ALTA");
        priorityBox.getSelectionModel().select("MEDIA");
    }

    public void setProject(Project project) {
        this.currentProject = project;
    }

    // --- NUOVO METODO: Pre-compila il form se stiamo modificando ---
    public void setActivityToEdit(Activity a) {
        this.activityToEdit = a;
        
        // Riempi i campi base
        titleField.setText(a.getTitle());
        priorityBox.getSelectionModel().select(a.getPriority().toString());
        
        if (a.getDeadline() != null) {
            deadlinePicker.setValue(a.getDeadline());
        }

        // Se è un gruppo, estrai le sotto-task e mettile nell'area di testo (una per riga)
        if (a instanceof TaskGroup) {
            TaskGroup group = (TaskGroup) a;
            StringBuilder sb = new StringBuilder();
            for (Activity child : group.getChildren()) {
                sb.append(child.getTitle()).append("\n");
            }
            subtasksArea.setText(sb.toString().trim());
        }
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

        String subtasksText = subtasksArea.getText();
        String[] subTasks = null;
        if (subtasksText != null && !subtasksText.trim().isEmpty()) {
            subTasks = subtasksText.split("\\n");
        }

        // BIVIO: Creazione o Modifica?
        if (activityToEdit == null) {
            // Creiamo una Nuova Attività
            projectController.addActivityToProject(currentProject.getId(), title, priority, dateStr, subTasks);
        } else {
            // Modifichiamo quella esistente sfruttando il metodo che avevi già scritto!
            projectController.updateActivityContent(currentProject.getId(), activityToEdit.getId(), title, priority, dateStr, subTasks);
        }

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