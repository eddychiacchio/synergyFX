package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.model.*;
import com.synergy.controller.DocumentController;
import com.synergy.util.DataManager;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
//import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
//import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ComboBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
//import javafx.scene.layout.Region;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import com.synergy.controller.ProjectController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import java.util.Optional;
import javafx.scene.layout.Region;

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
    private ProjectController projectController = new ProjectController();
    private DocumentController documentController = new DocumentController();

    @FXML
    public void initialize() {
        // --- AGGIUNTA FONDAMENTALE PER IL DRAG&DROP ---
        // Diamo un'altezza minima per poter sganciare le card anche se la colonna è vuota!
        todoColumn.setMinHeight(600);
        doingColumn.setMinHeight(600);
        doneColumn.setMinHeight(600);
        
        // Configuriamo le tre colonne per accettare il Drag & Drop
        setupDropTarget(todoColumn, ActivityStatus.DA_FARE, "#f1f5f9");
        setupDropTarget(doingColumn, ActivityStatus.IN_CORSO, "#fff7ed");
        setupDropTarget(doneColumn, ActivityStatus.COMPLETATO, "#f0fdf4");
    }

    // Metodo helper per configurare il rilascio su una colonna
    private void setupDropTarget(VBox column, ActivityStatus targetStatus, String originalColor) {
        // 1. Quando il mouse "passa sopra" la colonna con un oggetto trascinato
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // 2. Quando l'oggetto entra nella colonna, la scurisci leggermente (Feedback visivo)
        column.setOnDragEntered(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 8;");
            }
            event.consume();
        });

        // 3. Quando l'oggetto esce senza essere stato rilasciato, ripristini il colore
        column.setOnDragExited(event -> {
            column.setStyle("-fx-background-color: " + originalColor + "; -fx-background-radius: 8;");
            event.consume();
        });

        // 4. Quando LASCIO il tasto del mouse sulla colonna (Il Drop!)
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                // Leggo l'ID della card che ho trascinato
                int activityId = Integer.parseInt(db.getString());
                
                // Aggiorno il Database! (Utilizza il tuo metodo già pronto in ProjectController)
                projectController.updateActivityStatus(currentProject.getId(), activityId, targetStatus.name());
                
                // Ricarico la lavagna per far apparire la card nella nuova colonna
                refreshKanban();
                success = true;
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-cursor: hand;");
        
        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);
        
        Label priority = new Label(a.getPriority().toString());
        String color = "green"; 
        if (a.getPriority() == PriorityLevel.MEDIA) color = "orange";
        if (a.getPriority() == PriorityLevel.ALTA) color = "red";
        
        priority.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;");

        card.getChildren().addAll(priority, title);
        
        if (a instanceof TaskGroup) {
            TaskGroup g = (TaskGroup) a;
            Label subCount = new Label("Sotto-attività: " + g.getChildren().size());
            subCount.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
            card.getChildren().add(subCount);
        }

        // --- INIZIO CODICE NUOVO: ABILITARE IL DRAG ---
        card.setOnDragDetected(event -> {
            // Inizia un'operazione di spostamento
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            
            // Mettiamo l'ID della card "negli appunti" della Dragboard come Stringa
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(a.getId()));
            db.setContent(content);
            
            event.consume();
        });
        
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Elimina Attività");
        
        // Cosa succede se clicco su "Elimina"
        deleteItem.setOnAction(e -> {
            // Usa il metodo già esistente nel tuo ProjectController
            projectController.deleteActivity(currentProject.getId(), a.getId());
            // Salva e ricarica la grafica
            DataManager.getInstance().saveData();
            refreshKanban();
        });
        
        contextMenu.getItems().add(deleteItem);

        // Associa il menu al click destro sulla card
        card.setOnContextMenuRequested(event -> {
            contextMenu.show(card, event.getScreenX(), event.getScreenY());
        });

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
    
    @FXML
    private void handleNewActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/activity_form.fxml"));
            Parent root = loader.load();

            // Passiamo il progetto attuale alla nuova finestra
            ActivityFormController controller = loader.getController();
            controller.setProject(currentProject);

            Stage stage = new Stage();
            stage.setTitle("Crea Nuova Attività");
            stage.setScene(new Scene(root));
            
            // Blocca la finestra sotto finché questa non viene chiusa
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectNameLabel.getScene().getWindow());
            
            stage.showAndWait(); // <-- Il codice si ferma qui finché non chiudi la modale

            // Appena la modale viene chiusa, ricarichiamo la lavagna per far apparire il nuovo task!
            refreshKanban(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // Metodo per gestire l'invito di un nuovo membro
    @FXML
    private void handleInviteMember() {
        // 1. Crea la finestra di dialogo preimpostata di JavaFX
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invita Membro");
        dialog.setHeaderText("Invita un nuovo collaboratore in " + currentProject.getName());
        dialog.setContentText("Inserisci l'email dell'utente da invitare:");

        // 2. Mostra la finestra e aspetta che l'utente inserisca il testo
        Optional<String> result = dialog.showAndWait();
        
        // 3. Se l'utente ha premuto "OK" e ha inserito qualcosa...
        if (result.isPresent()) {
            String email = result.get().trim();
            
            if (!email.isEmpty()) {
                // Richiama il tuo metodo nel backend
                boolean success = projectController.inviteUserToProject(currentProject.getId(), email);
                
                // 4. Mostra un popup di Successo o Errore
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Successo");
                    alert.setHeaderText(null);
                    alert.setContentText("L'utente con email " + email + " è stato aggiunto al progetto!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossibile invitare l'utente.\nVerifica che l'email sia corretta e che l'utente non sia già nel progetto.");
                    alert.showAndWait();
                }
            }
        }
    }
}