package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.model.*;
import com.synergy.controller.DocumentController;
import com.synergy.util.DataManager;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ComboBox;

import java.io.File;

public class ProjectDetailsController {

    @FXML private Label projectNameLabel;
    @FXML private TableView<ProjectDocument> documentsTable;
    @FXML private TableColumn<ProjectDocument, String> docNameColumn;
    @FXML private ComboBox<String> sortComboBox;
    
    // Le tre colonne della lavagna (collegate all'FXML)
    @FXML private VBox todoColumn;
    @FXML private VBox doingColumn;
    @FXML private VBox doneColumn;

    private Project currentProject;
    private DocumentController documentController = new DocumentController();

    public void setProject(Project project) {
        this.currentProject = project;
        projectNameLabel.setText(project.getName());
        
        if (sortComboBox.getItems().isEmpty()) {
            sortComboBox.getItems().addAll("Nessuno", "Priorità", "Scadenza");
            sortComboBox.getSelectionModel().selectFirst(); // Seleziona "Nessuno" di default
        }
        
        refreshKanban();
        
        docNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        refreshDocuments();
        
        documentsTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                ProjectDocument selectedDoc = documentsTable.getSelectionModel().getSelectedItem();
                if (selectedDoc != null) {
                    openDocument(selectedDoc);
                }
            }
        });
    }

    private void refreshKanban() {
        // 1. Pulisce le colonne
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        // 2. Recupero la lista delle attività
        List<Activity> activities = currentProject.getActivities();

        // 3. Controllo l'ordinamento selezionato e applico lo Strategy
        String sortSelection = sortComboBox != null ? sortComboBox.getValue() : "Nessuno";
        SortStrategy strategy = null;

        if ("Priorità".equals(sortSelection)) {
            strategy = new SortByPriority();
        } else if ("Scadenza".equals(sortSelection)) {
            strategy = new SortByDeadline();
        }

        if (strategy != null) {
            strategy.sort(activities); // Il tuo Pattern entra in azione qui!
        }

        // 4. Distribuisce le attività ordinate nelle colonne giuste
        for (Activity a : activities) {
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
    
    private void refreshDocuments() {
        if (currentProject != null) {
            documentsTable.getItems().clear();
            documentsTable.getItems().addAll(currentProject.getDocuments());
        }
    }

    @FXML
    private void handleUploadDocument() {
        // Apre la finestra di dialogo del sistema operativo per scegliere un file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona il documento da caricare");
        
        // Puoi aggiungere filtri per le estensioni se vuoi (es. solo PDF, DOCX)
        // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(documentsTable.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                // ATTENZIONE: Verifica che il metodo nel tuo DocumentController si chiami esattamente così.
                // Nella versione Web probabilmente prendeva un oggetto Part, ora deve prendere un File.
                documentController.uploadFile(currentProject.getId(), selectedFile);
                
                // Salvataggio tramite DataManager (in modo che la modifica persista)
                DataManager.getInstance().saveData();
                
                // Aggiorna la vista
                refreshDocuments();
                
            } catch (Exception e) {
                e.printStackTrace();
                // Qui potresti mostrare un Alert di errore all'utente
            }
        }
    }
    
 // Metodo per aprire il file con il programma predefinito di Windows/Mac
    private void openDocument(ProjectDocument doc) {
        try {
            File fileToOpen = documentController.getDocumentFile(doc);
            if (fileToOpen.exists()) {
                Desktop.getDesktop().open(fileToOpen);
            } else {
                System.out.println("Errore: Il file non è più presente nella cartella.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo per il bottone rosso "Elimina"
    @FXML
    private void handleDeleteDocument() {
        ProjectDocument selectedDoc = documentsTable.getSelectionModel().getSelectedItem();
        if (selectedDoc != null) {
            // documentController ha già il metodo deleteDocument scritto da te, usiamolo!
            documentController.deleteDocument(currentProject.getId(), selectedDoc.getId());
            refreshDocuments(); // Ricarica la tabella per far sparire la riga
        }
    }
    
    @FXML
    private void handleSortChange() {
        refreshKanban(); // Ricarica la lavagna con il nuovo ordinamento
    }
}