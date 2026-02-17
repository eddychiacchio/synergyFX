package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.model.*;
import com.synergy.controller.DocumentController;
import com.synergy.controller.ProjectController;
import com.synergy.util.SessionManager;
import com.synergy.util.DataManager; // <-- IMPORT AGGIUNTO!

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
import javafx.scene.control.ListView; // <-- IMPORT AGGIUNTO!
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

    @FXML private Label usernameLabel;
    @FXML private Label userInitialsLabel;
    @FXML private Label projectDescriptionLabel;
    @FXML private Label todoCountLabel;
    @FXML private Label doingCountLabel;
    @FXML private Label doneCountLabel;
    @FXML private VBox recentProjectsList;

    @FXML private Label projectNameLabel;
    @FXML private TableView<ProjectDocument> documentsTable;
    @FXML private TableColumn<ProjectDocument, String> docNameColumn;
    @FXML private TableColumn<ProjectDocument, String> docTypeColumn;
    @FXML private TableColumn<ProjectDocument, java.time.LocalDate> docDateColumn;
    @FXML private ComboBox<String> sortComboBox;
    
    @FXML private Button inviteMemberBtn;
    @FXML private Button deleteDocBtn;
    
    @FXML private VBox todoColumn;
    @FXML private VBox doingColumn;
    @FXML private VBox doneColumn;

    private Project currentProject;
    private ProjectController projectController = new ProjectController();
    private DocumentController documentController = new DocumentController();
    private boolean isAdmin = false;

    @FXML
    public void initialize() {
        todoColumn.setMinHeight(600);
        doingColumn.setMinHeight(600);
        doneColumn.setMinHeight(600);
        
        setupDropTarget(todoColumn, ActivityStatus.DA_FARE, "#f8fafc");
        setupDropTarget(doingColumn, ActivityStatus.IN_CORSO, "#fffbeb");
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
                column.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 12;");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle("-fx-background-color: transparent;"); 
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int activityId = Integer.parseInt(db.getString());
                
                projectController.updateActivityStatus(currentProject.getId(), activityId, targetStatus.name());
                currentProject = projectController.getProjectById(currentProject.getId());
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
        
        if (projectDescriptionLabel != null) {
            projectDescriptionLabel.setText(project.getDescription() != null && !project.getDescription().isEmpty() 
                    ? project.getDescription() : "Nessuna descrizione.");
        }
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        this.isAdmin = false;
        if (currentUser != null) {
            if (usernameLabel != null) usernameLabel.setText(currentUser.getName());
            if (userInitialsLabel != null) {
                String initials = currentUser.getName().length() >= 2 ? currentUser.getName().substring(0, 2).toUpperCase() : "U";
                userInitialsLabel.setText(initials);
            }
            
            for (ProjectMembership pm : project.getMemberships()) {
                if (pm.getUser() != null && pm.getUser().getId() == currentUser.getId()) {
                    this.isAdmin = pm.getIsAdmin();
                    break;
                }
            }
        }
        
        if (inviteMemberBtn != null) inviteMemberBtn.setVisible(isAdmin);
        if (deleteDocBtn != null) deleteDocBtn.setVisible(isAdmin);
        
        if (sortComboBox.getItems().isEmpty()) {
            sortComboBox.getItems().addAll("Nessuno", "Priorità", "Scadenza");
            sortComboBox.getSelectionModel().selectFirst();
        }
        
        refreshKanban();
        
        docNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (docTypeColumn != null) docTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (docDateColumn != null) docDateColumn.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));
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
        
        if (todoCountLabel != null) todoCountLabel.setText(String.valueOf(todoColumn.getChildren().size()));
        if (doingCountLabel != null) doingCountLabel.setText(String.valueOf(doingColumn.getChildren().size()));
        if (doneCountLabel != null) doneCountLabel.setText(String.valueOf(doneColumn.getChildren().size()));
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
            Label subCount = new Label("📎 " + g.getChildren().size() + " sub-task");
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
        
        MenuItem editItem = new MenuItem("✏️ Modifica Attività");
        editItem.setOnAction(e -> openEditActivityModal(a));
        contextMenu.getItems().add(editItem);

        if (isAdmin) {
            MenuItem deleteItem = new MenuItem("🗑️ Elimina Attività");
            deleteItem.setStyle("-fx-text-fill: red;");
            deleteItem.setOnAction(e -> {
                projectController.deleteActivity(currentProject.getId(), a.getId());
                currentProject = projectController.getProjectById(currentProject.getId());
                refreshKanban();
            });
            contextMenu.getItems().add(deleteItem);
        }

        card.setOnContextMenuRequested(event -> {
            contextMenu.show(card, event.getScreenX(), event.getScreenY());
        });

        return card;
    }

    private void openEditActivityModal(Activity a) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/activity_form.fxml"));
            Parent root = loader.load();
            ActivityFormController controller = loader.getController();
            
            controller.setProject(currentProject);
            controller.setActivityToEdit(a); 

            Stage stage = new Stage();
            stage.setTitle("Modifica: " + a.getTitle());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectNameLabel.getScene().getWindow());
            stage.showAndWait();
            
            currentProject = projectController.getProjectById(currentProject.getId());
            refreshKanban(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void handleNotifications() {
        System.out.println("Notifiche cliccate dalla Sidebar");
    }

    @FXML
    private void handleChat() {
        System.out.println("Chat cliccata dalla Sidebar");
    }
    
    @FXML
    private void handleBack() throws IOException {
        handleDashboard();
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
                currentProject = projectController.getProjectById(currentProject.getId());
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
                System.out.println("Errore: Il file non è più presente in " + fileToOpen.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteDocument() {
        if (!isAdmin) return;
        ProjectDocument selectedDoc = documentsTable.getSelectionModel().getSelectedItem();
        if (selectedDoc != null) {
            documentController.deleteDocument(currentProject.getId(), selectedDoc.getId());
            currentProject = projectController.getProjectById(currentProject.getId());
            refreshDocuments();
        }
    }
    
    @FXML
    private void handleSortChange() {
        refreshKanban();
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
            
            currentProject = projectController.getProjectById(currentProject.getId());
            refreshKanban(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleInviteMember() {
        if (!isAdmin) return;
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
                    alert.setContentText("L'utente è stato aggiunto al progetto!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossibile invitare l'utente.");
                    alert.showAndWait();
                }
            }
        }
    }
    
    @FXML private VBox notificationPanel;
    @FXML private ListView<String> notificationsListView;
    @FXML private Label notificationBadge;

    @FXML
    private void toggleNotifications() {
        if (notificationPanel != null) {
            notificationPanel.setVisible(!notificationPanel.isVisible());
            if (notificationPanel.isVisible()) refreshNotifications();
        }
    }

    @FXML
    private void hideNotificationsIfClickOutside() {
        if (notificationPanel != null && notificationPanel.isVisible()) {
            notificationPanel.setVisible(false);
        }
    }

    @FXML
    private void handleClearNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.clearNotifications();
            DataManager.getInstance().saveData();
            refreshNotifications();
            if (notificationPanel != null) notificationPanel.setVisible(false);
        }
    }

    private void refreshNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && notificationsListView != null) {
            List<String> notifs = currentUser.getNotifications();
            notificationsListView.getItems().clear();
            if (notifs != null && !notifs.isEmpty()) {
                notificationsListView.getItems().addAll(notifs);
                if (notificationBadge != null) {
                    notificationBadge.setText(String.valueOf(notifs.size()));
                    notificationBadge.setVisible(true);
                }
            } else {
                if (notificationBadge != null) notificationBadge.setVisible(false);
            }
        }
    }

    @FXML
    private void handleLogout() throws java.io.IOException {
        SessionManager.getInstance().logout();
        App.setRoot("login");
    }
}