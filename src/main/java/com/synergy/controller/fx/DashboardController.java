package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.controller.ProjectController;
import com.synergy.model.Activity;
import com.synergy.model.ActivityStatus;
import com.synergy.model.Project;
import com.synergy.model.ProjectMembership;
import com.synergy.model.User;
import com.synergy.util.DataManager;
import com.synergy.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Project> projectListView;
    @FXML private ListView<String> notificationsListView;
    @FXML private Label notificationBadge;
    @FXML private VBox notificationPanel;

    private ProjectController projectController = new ProjectController();

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            welcomeLabel.setText(currentUser.getName()); // Stile Tailwind: Mettiamo solo il nome
            
            notificationBadge.setVisible(false);
            
            // --- MAGIA: CREAZIONE DELLE CARD PERSONALIZZATE PER I PROGETTI ---
            projectListView.setCellFactory(param -> new ListCell<Project>() {
                @Override
                protected void updateItem(Project project, boolean empty) {
                    super.updateItem(project, empty);

                    if (empty || project == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("-fx-background-color: transparent;"); // Sfondo trasparente per le celle vuote
                        setContextMenu(null);
                    } else {
                        // 1. Controlla se l'utente è Amministratore di questo progetto
                        boolean isAdmin = false;
                        for (ProjectMembership pm : project.getMemberships()) {
                            if (pm.getUser() != null && pm.getUser().getId() == currentUser.getId()) {
                                isAdmin = pm.getIsAdmin();
                                break;
                            }
                        }

                        // 2. Calcola la percentuale di completamento
                        int totalTasks = project.getActivities().size();
                        int completedTasks = 0;
                        for (Activity a : project.getActivities()) {
                            if (a.getStatus() == ActivityStatus.COMPLETATO) {
                                completedTasks++;
                            }
                        }
                        int progress = (totalTasks == 0) ? 0 : (completedTasks * 100) / totalTasks;

                        // 3. COSTRUISCE LA CARD GRAFICA
                        VBox card = new VBox(12); // Spaziatura interna
                        card.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-cursor: hand;");
                        
                        // Effetto Hover (come in Tailwind)
                        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 18; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-cursor: hand;"));
                        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-cursor: hand;"));

                        // --- RIGA SUPERIORE: Icona e Badge Ruolo ---
                        HBox topRow = new HBox(10);
                        topRow.setAlignment(Pos.CENTER_LEFT);
                        
                        // Cerchietto Ciano (stile Tailwind)
                        Circle icon = new Circle(12, Color.web("#cffafe"));
                        icon.setStroke(Color.web("#06b6d4"));
                        icon.setStrokeWidth(2);
                        
                        // Badge "Project Manager" o "Team Member"
                        Label roleBadge = new Label(isAdmin ? "PROJECT MANAGER" : "TEAM MEMBER");
                        roleBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");
                        
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS); // Spinge il badge tutto a destra
                        
                        topRow.getChildren().addAll(icon, spacer, roleBadge);

                        // --- CENTRO: Titolo e Descrizione ---
                        Label title = new Label(project.getName());
                        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                        
                        Label desc = new Label(project.getDescription() != null && !project.getDescription().isEmpty() ? project.getDescription() : "Nessuna descrizione disponibile per questo progetto.");
                        desc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                        desc.setWrapText(true);
                        desc.setMaxHeight(40); // Tronca le descrizioni troppo lunghe

                        // --- INFERIORE: Barra di Progresso ---
                        HBox progressInfo = new HBox();
                        Label progressLabel = new Label("Progresso");
                        progressLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
                        Region progSpacer = new Region();
                        HBox.setHgrow(progSpacer, Priority.ALWAYS);
                        Label progressPercent = new Label(progress + "%");
                        progressPercent.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0891b2;");
                        progressInfo.getChildren().addAll(progressLabel, progSpacer, progressPercent);

                        // Costruisce la barra vettoriale (Sfondo Grigio + Riempimento Ciano)
                        StackPane barContainer = new StackPane();
                        barContainer.setAlignment(Pos.CENTER_LEFT);
                        
                        Rectangle bgBar = new Rectangle(0, 6, Color.web("#f1f5f9"));
                        bgBar.setArcWidth(6); bgBar.setArcHeight(6);
                        bgBar.widthProperty().bind(projectListView.widthProperty().subtract(70)); // Responsive
                        
                        Rectangle fillBar = new Rectangle(0, 6, Color.web("#06b6d4"));
                        fillBar.setArcWidth(6); fillBar.setArcHeight(6);
                        fillBar.widthProperty().bind(projectListView.widthProperty().subtract(70).multiply(progress / 100.0));

                        barContainer.getChildren().addAll(bgBar, fillBar);

                        // Assembla tutta la card
                        card.getChildren().addAll(topRow, title, desc, progressInfo, barContainer);
                        
                     // --- 4. MENU CONTESTUALE (Click Destro per Eliminare il Progetto) ---
                        // Solo il Project Manager può eliminare il progetto!
                     // --- 4. MENU CONTESTUALE (Click Destro per Eliminare il Progetto) ---
                        if (isAdmin) {
                            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                            javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("🗑️ Elimina Progetto");
                            deleteItem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            
                            deleteItem.setOnAction(e -> {
                                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Conferma Eliminazione");
                                alert.setHeaderText("Stai per eliminare '" + project.getName() + "'");
                                alert.setContentText("Sei sicuro? Tutte le attività, i documenti e i membri verranno persi per sempre.");
                                
                                java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                                if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                                    projectController.deleteProject(project.getId());
                                    refreshProjectList(); 
                                }
                            });
                            
                            contextMenu.getItems().add(deleteItem);
                            
                            // Applica il menu DIRETTAMENTE alla Cella (modo nativo JavaFX)
                            setContextMenu(contextMenu);
                        } else {
                            // Se non è admin, disattiva il menu
                            setContextMenu(null);
                        }

                        // Imposta la card come grafica della cella
                        setGraphic(card);
                        // Rende la cella trasparente e mette un po' di margine sotto
                        setStyle("-fx-background-color: transparent; -fx-padding: 0 0 15 0;");
                    }
                }
            });

            // Gestione Notifiche (Diamo uno stile minimale anche a quelle)
            notificationsListView.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        setText("• " + item);
                        setWrapText(true);
                        setStyle("-fx-background-color: transparent; -fx-padding: 8 0; -fx-text-fill: #475569; -fx-font-size: 13px; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                    }
                }
            });
            
            refreshProjectList();
            refreshNotifications();
            
            projectListView.setOnMouseClicked(event -> {
                // MAGIA: Apre il progetto SOLO se l'utente usa il Click Sinistro!
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    Project selected = projectListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        openProjectDetails(selected);
                    }
                }
            });
        }
    }
    
    @FXML
    private void toggleNotifications() {
        // Appare/Scompare quando clicchi il bottone nella barra laterale
        notificationPanel.setVisible(!notificationPanel.isVisible());
    }

    @FXML
    private void hideNotificationsIfClickOutside() {
        // Se l'utente clicca fuori nella zona grigia, il menu a tendina si chiude da solo
        if (notificationPanel.isVisible()) {
            notificationPanel.setVisible(false);
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.getInstance().logout();
        App.setRoot("login");
    }

    @FXML
    private void handleNewProject() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_form.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nuovo Progetto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectListView.getScene().getWindow());
            stage.showAndWait();
            refreshProjectList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshProjectList() {
        projectListView.getItems().clear();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<Project> projects = projectController.getProjectsByUser(currentUser);
            projectListView.getItems().addAll(projects);
        }
    }

    private void openProjectDetails(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_details.fxml"));
            Parent root = loader.load();
            ProjectDetailsController controller = loader.getController();
            controller.setProject(project);
            Stage stage = (Stage) projectListView.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void refreshNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<String> notifs = currentUser.getNotifications();
            notificationsListView.getItems().clear();
            
            if (notifs != null && !notifs.isEmpty()) {
                notificationsListView.getItems().addAll(notifs);
                
                // Mostra il badge e aggiorna il numero
                notificationBadge.setText(String.valueOf(notifs.size()));
                notificationBadge.setVisible(true);
            } else {
                // Nessuna notifica: nascondi il pallino
                notificationBadge.setVisible(false);
            }
        }
    }

    @FXML
    private void handleClearNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.clearNotifications();
            DataManager.getInstance().saveData(); 
            refreshNotifications();
            notificationPanel.setVisible(false);
        }
    }
}