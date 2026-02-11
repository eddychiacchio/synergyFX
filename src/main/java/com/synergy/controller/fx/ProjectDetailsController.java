package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

public class ProjectDetailsController {

    @FXML private Label projectNameLabel;
    
    // Le tre colonne della lavagna (collegate all'FXML)
    @FXML private VBox todoColumn;
    @FXML private VBox doingColumn;
    @FXML private VBox doneColumn;

    private Project currentProject;

    /**
     * Questo metodo viene chiamato dalla Dashboard per passare il progetto cliccato.
     */
    public void setProject(Project project) {
        this.currentProject = project;
        projectNameLabel.setText(project.getName());
        refreshKanban();
    }

    private void refreshKanban() {
        // 1. Pulisce le colonne (per non duplicare le card se ricarichiamo)
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        // 2. Distribuisce le attività nelle colonne giuste
        for (Activity a : currentProject.getActivities()) {
            VBox card = createActivityCard(a);
            
            switch (a.getStatus()) {
                case DA_FARE: 
                    todoColumn.getChildren().add(card); 
                    break;
                case IN_CORSO: 
                    doingColumn.getChildren().add(card); 
                    break;
                case COMPLETATO: 
                    doneColumn.getChildren().add(card); 
                    break;
            }
        }
    }

    // Metodo helper grafico: Crea il rettangolino colorato per l'attività
    private VBox createActivityCard(Activity a) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        // Stile CSS inline per fare il bordo arrotondato e l'ombra
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        
        // Titolo
        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Etichetta Priorità colorata
        Label priority = new Label(a.getPriority().toString());
        String color = "green"; // Default BASSA
        if (a.getPriority() == PriorityLevel.MEDIA) color = "orange";
        if (a.getPriority() == PriorityLevel.ALTA) color = "red";
        
        priority.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        // Aggiungo elementi alla card
        card.getChildren().addAll(priority, title);
        
        // Se è un GRUPPO, mostro quante sotto-task ha
        if (a instanceof TaskGroup) {
            TaskGroup g = (TaskGroup) a;
            Label subCount = new Label("Sotto-attività: " + g.getChildren().size());
            subCount.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
            card.getChildren().add(subCount);
        }

        return card;
    }

    @FXML
    private void handleBack() throws java.io.IOException {
        // Torna alla Dashboard
        App.setRoot("dashboard");
    }
}