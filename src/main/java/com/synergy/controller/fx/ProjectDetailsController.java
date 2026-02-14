package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.model.*;
import com.synergy.controller.DocumentController;
import com.synergy.controller.ProjectController;
import com.synergy.util.DataManager;
import com.synergy.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.awt.Desktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProjectDetailsController {

    @FXML private Label projectNameLabel;
    @FXML private TableView<ProjectDocument> documentsTable;
    @FXML private TableColumn<ProjectDocument, String> docNameColumn;
    @FXML private ComboBox<String> sortComboBox;
    
    // Bottoni Sensibili (Da nascondere se l'utente non è Admin)
    @FXML private Button inviteMemberBtn;
    @FXML private Button deleteDocBtn;
    
    // Le tre colonne della lavagna
    @FXML private VBox todoColumn;
    @FXML private VBox doingColumn;
    @FXML private VBox doneColumn;

    private Project currentProject;
    private ProjectController projectController = new ProjectController();
    private DocumentController documentController = new DocumentController();
    
    // Variabile globale per sapere se l'utente corrente comanda su questo progetto
    private boolean isAdmin = false;

    @FXML
    public void initialize() {
        todoColumn.setMinHeight(600);
        doingColumn.setMinHeight(600);
        doneColumn.setMinHeight(600);
        
        setupDropTarget(todoColumn, ActivityStatus.DA_FARE, "#f1f5f9");
        setupDropTarget(doingColumn, ActivityStatus.IN_CORSO, "#fff7ed");
        setupDropTarget(doneColumn, ActivityStatus.COMPLETATO, "#f0fdf4");
    }

    private void setupDropTarget(VBox column, ActivityStatus targetStatus, String originalColor) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragEntered(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 8;");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle("-fx-background-color: " + originalColor + "; -fx-background-radius: 8;");
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                int activityId = Integer.parseInt(db.getString());
                projectController.updateActivityStatus(currentProject.getId(), activityId, targetStatus.name());
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
        
        // --- 1. CONTROLLO PERMESSI ---
        User currentUser = SessionManager.getInstance().getCurrentUser();
        this.isAdmin = false;
        if (currentUser != null) {
            for (ProjectMembership pm : project.getMemberships()) {
                if (pm.getUser() != null && pm.getUser().getId() == currentUser.getId()) {
                    this.isAdmin = pm.getIsAdmin();
                    break;
                }
            }
        }
        
        // --- 2. APPLICA I PERMESSI ALLA UI ---
        // Se non è admin, scompare il bottone Invita e il bottone Elimina Documento!
        if (inviteMemberBtn != null) inviteMemberBtn.setVisible(isAdmin);
        if (deleteDocBtn != null) deleteDocBtn.setVisible(isAdmin);
        
        if (sortComboBox.getItems().isEmpty()) {
            sortComboBox.getItems().addAll("Nessuno", "Priorità", "Scadenza");
            sortComboBox.getSelectionModel().selectFirst();
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
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        List<Activity> activities = currentProject.getActivities();
        String sortSelection = sortComboBox != null ? sortComboBox.getValue() : "Nessuno";
        SortStrategy strategy = null;

        if ("Priorità".equals(sortSelection)) {
            strategy = new SortByPriority();
        } else if ("Scadenza".equals(sortSelection)) {
            strategy = new SortByDeadline();
        }

        if (strategy != null) {
            strategy.sort(activities);
        }

        for (Activity a : activities) {
            VBox card = createActivityCard(a);
            switch (a.getStatus()) {
                case DA_FARE: todoColumn.getChildren().add(card); break;
                case IN_CORSO: doingColumn.getChildren().add(card); break;
                case COMPLETATO: doneColumn.getChildren().add(card); break;
            }
        }
    }
    
    private VBox createActivityCard(Activity a) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        DropShadow normalShadow = new DropShadow(5, Color.rgb(0, 0, 0, 0.05));
        normalShadow.setOffsetY(2);
        DropShadow hoverShadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.10));
        hoverShadow.setOffsetY(4);
        
        card.setEffect(normalShadow);

        card.setOnMouseEntered(e -> {
            card.setEffect(hoverShadow);
            card.setTranslateY(-2);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(normalShadow);
            card.setTranslateY(0);
        });

        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        title.setWrapText(true);
        
        Label priority = new Label(a.getPriority().toString());
        String basePriorityStyle = "-fx-padding: 3 8 3 8; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 10px; ";
        
        if (a.getPriority() == PriorityLevel.BASSA) {
            priority.setStyle(basePriorityStyle + "-fx-text-fill: #10b981; -fx-background-color: #d1fae5;");
        } else if (a.getPriority() == PriorityLevel.MEDIA) {
            priority.setStyle(basePriorityStyle + "-fx-text-fill: #f59e0b; -fx-background-color: #fef3c7;");
        } else if (a.getPriority() == PriorityLevel.ALTA) {
            priority.setStyle(basePriorityStyle + "-fx-text-fill: #ef4444; -fx-background-color: #fee2e2;");
        }

        card.getChildren().addAll(priority, title);
        
        if (a instanceof TaskGroup) {
            TaskGroup g = (TaskGroup) a;
            Label subCount = new Label("📎 " + g.getChildren().size() + " sotto-task");
            subCount.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
            card.getChildren().add(subCount);
        }

        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(a.getId()));
            db.setContent(content);
            event.consume();
        });

        ContextMenu contextMenu = new ContextMenu();
        
        // Tutti possono modificare un'attività
        MenuItem editItem = new MenuItem("✏️ Modifica Attività");
        editItem.setOnAction(e -> openEditActivityModal(a));
        contextMenu.getItems().add(editItem);

        // Solo il Project Manager può usare il tasto destro per ELIMINARE!
        if (isAdmin) {
            MenuItem deleteItem = new MenuItem("🗑️ Elimina Attività");
            deleteItem.setStyle("-fx-text-fill: red;");
            deleteItem.setOnAction(e -> {
                projectController.deleteActivity(currentProject.getId(), a.getId());
                DataManager.getInstance().saveData();
                refreshKanban();
            });
            contextMenu.getItems().add(deleteItem);
        }

        card.setOnContextMenuRequested(event -> {
            contextMenu.show(card, event.getScreenX(), event.getScreenY());
        });

        return card;
    }

    @FXML
    private void handleBack() throws IOException {
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona il documento da caricare");
        File selectedFile = fileChooser.showOpenDialog(documentsTable.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                documentController.uploadFile(currentProject.getId(), selectedFile);
                DataManager.getInstance().saveData();
                refreshDocuments();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
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

    @FXML
    private void handleDeleteDocument() {
        if (!isAdmin) return; // Doppia sicurezza backend
        
        ProjectDocument selectedDoc = documentsTable.getSelectionModel().getSelectedItem();
        if (selectedDoc != null) {
            documentController.deleteDocument(currentProject.getId(), selectedDoc.getId());
            refreshDocuments();
        }
    }
    
    @FXML
    private void handleSortChange() {
        refreshKanban();
    }
    
    private void openEditActivityModal(Activity a) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/activity_form.fxml"));
            Parent root = loader.load();
            ActivityFormController controller = loader.getController();
            
            controller.setProject(currentProject);
            // La magia: passiamo la card al form per farlo pre-compilare!
            controller.setActivityToEdit(a); 

            Stage stage = new Stage();
            stage.setTitle("Modifica: " + a.getTitle());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectNameLabel.getScene().getWindow());
            stage.showAndWait();
            
            refreshKanban(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleNewActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/activity_form.fxml"));
            Parent root = loader.load();
            ActivityFormController controller = loader.getController();
            controller.setProject(currentProject);

            Stage stage = new Stage();
            stage.setTitle("Crea Nuova Attività");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectNameLabel.getScene().getWindow());
            stage.showAndWait();
            refreshKanban(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleInviteMember() {
        if (!isAdmin) return; // Doppia sicurezza
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invita Membro");
        dialog.setHeaderText("Invita un nuovo collaboratore in " + currentProject.getName());
        dialog.setContentText("Inserisci l'email dell'utente da invitare:");

        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String email = result.get().trim();
            if (!email.isEmpty()) {
                boolean success = projectController.inviteUserToProject(currentProject.getId(), email);
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