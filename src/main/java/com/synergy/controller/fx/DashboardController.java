package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.controller.ProjectController;
import com.synergy.model.*;
import com.synergy.util.DataManager;
import com.synergy.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    // --- CLASSE WRAPPER PER LA TABELLA (Come nel vecchio JSP) ---
    public static class ActivityView {
        private Activity activity;
        private Project project;

        public ActivityView(Activity activity, Project project) {
            this.activity = activity;
            this.project = project;
        }

        public Activity getActivity() { return activity; }
        
        // Metodi usati automaticamente dal PropertyValueFactory della Tabella
        public String getName() { return activity.getTitle(); }
        public String getProjectName() { return project.getName(); }
        public LocalDate getDeadline() { return activity.getDeadline(); }
        public PriorityLevel getPriority() { return activity.getPriority(); }
        public ActivityStatus getStatus() { return activity.getStatus(); }
    }

    // --- COMPONENTI UI ---
    @FXML private Label usernameLabel;
    @FXML private Label userInitialsLabel;
    @FXML private GridPane projectsGrid;
    @FXML private VBox recentProjectsList;
    
    // TABELLA ORA USA LA CLASSE "ActivityView" e "PriorityLevel" !
    @FXML private TableView<ActivityView> activitiesTable;
    @FXML private TableColumn<ActivityView, String> activityNameColumn;
    @FXML private TableColumn<ActivityView, String> activityProjectColumn;
    @FXML private TableColumn<ActivityView, LocalDate> activityDeadlineColumn;
    @FXML private TableColumn<ActivityView, PriorityLevel> activityPriorityColumn;
    @FXML private TableColumn<ActivityView, ActivityStatus> activityStatusColumn;
    
    @FXML private ListView<String> notificationsListView;
    @FXML private Label notificationBadge;
    @FXML private VBox notificationPanel;

    private ProjectController projectController = new ProjectController();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            if (usernameLabel != null) usernameLabel.setText(currentUser.getName());
            if (userInitialsLabel != null) {
                String initials = getInitials(currentUser.getName());
                userInitialsLabel.setText(initials);
            }
            
            if (notificationBadge != null) {
                notificationBadge.setVisible(false);
            }
            
            setupActivitiesTable();
            
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
                        setStyle("-fx-background-color: transparent; -fx-padding: 10 12; -fx-text-fill: #475569; -fx-font-size: 13px; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                    }
                }
            });
            
            loadProjects();
            loadRecentProjects();
            loadActivities();
            refreshNotifications();
        }
    }

    private void setupActivitiesTable() {
        
        activityNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        activityNameColumn.setCellFactory(column -> new TableCell<ActivityView, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    ActivityView av = getTableView().getItems().get(getIndex());
                    VBox container = new VBox(4);
                    
                    Label nameLabel = new Label(item);
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                    
                    // Mostra se è un gruppo o una singola task
                    String desc = (av.getActivity() instanceof TaskGroup) ? "Gruppo di Task" : "Task Singola";
                    Label descLabel = new Label(desc);
                    descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                    
                    container.getChildren().addAll(nameLabel, descLabel);
                    setGraphic(container);
                }
            }
        });
        
        activityProjectColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        activityProjectColumn.setCellFactory(column -> new TableCell<ActivityView, String>() {
            @Override
            protected void updateItem(String projectName, boolean empty) {
                super.updateItem(projectName, empty);
                if (empty || projectName == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(projectName);
                    badge.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;");
                    setGraphic(badge);
                }
            }
        });
        
        activityDeadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        activityDeadlineColumn.setCellFactory(column -> new TableCell<ActivityView, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(date.format(dateFormatter));
                    LocalDate today = LocalDate.now();
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, date);
                    
                    if (daysUntil < 0) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;"); 
                    } else if (daysUntil == 0) {
                        setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold;"); 
                    } else if (daysUntil <= 3) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 600;"); 
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });
        
        activityPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        activityPriorityColumn.setCellFactory(column -> new TableCell<ActivityView, PriorityLevel>() {
            @Override
            protected void updateItem(PriorityLevel priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(priority.toString());
                    switch (priority) {
                        case ALTA: badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case MEDIA: badge.setStyle("-fx-background-color: #fed7aa; -fx-text-fill: #ea580c; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case BASSA: badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        activityStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        activityStatusColumn.setCellFactory(column -> new TableCell<ActivityView, ActivityStatus>() {
            @Override
            protected void updateItem(ActivityStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status.toString().replace("_", " ")); // Fix del nome
                    switch (status) {
                        case DA_FARE: badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case IN_CORSO: badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case COMPLETATO: badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadProjects() {
        if (projectsGrid == null) return;
        
        projectsGrid.getChildren().clear();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            List<Project> projects = projectController.getProjectsByUser(currentUser);
            
            int col = 0;
            int row = 0;
            
            for (Project project : projects) {
                VBox projectCard = createProjectCard(project, currentUser);
                projectsGrid.add(projectCard, col, row);
                
                col++;
                if (col > 1) { 
                    col = 0;
                    row++;
                }
            }
        }
    }

    private VBox createProjectCard(Project project, User currentUser) {
        boolean isAdmin = false;
        for (ProjectMembership pm : project.getMemberships()) {
            if (pm.getUser() != null && pm.getUser().getId() == currentUser.getId()) {
                isAdmin = pm.getIsAdmin();
                break;
            }
        }
        
        int totalTasks = project.getActivities().size();
        int completedTasks = 0;
        for (Activity a : project.getActivities()) {
            if (a.getStatus() == ActivityStatus.COMPLETATO) {
                completedTasks++;
            }
        }
        int progress = (totalTasks == 0) ? 0 : (completedTasks * 100) / totalTasks;
        
        String borderColor = progress < 33 ? "#ef4444" : progress < 66 ? "#f59e0b" : "#10b981";
        String progressColor = progress < 33 ? "#ef4444" : progress < 66 ? "#f59e0b" : "#10b981";
        
        VBox card = new VBox(14);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-cursor: hand;");
        card.setPrefWidth(480);
        card.setMinHeight(160);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 4); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-cursor: hand;"));
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("</>");
        icon.setStyle("-fx-font-size: 24px; -fx-text-fill: " + borderColor + "; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label role = new Label(isAdmin ? "Project Manager" : "Team Member");
        role.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;");
        header.getChildren().addAll(icon, spacer, role);
        
        Label name = new Label(project.getName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        name.setWrapText(true);
        
        Label description = new Label(project.getDescription() != null && !project.getDescription().isEmpty() ? project.getDescription() : "Nessuna descrizione disponibile");
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        description.setWrapText(true);
        description.setMaxHeight(40);
        
        VBox progressSection = new VBox(8);
        StackPane progressBar = new StackPane();
        progressBar.setAlignment(Pos.CENTER_LEFT);
        progressBar.setMaxHeight(6);
        Region bgBar = new Region();
        bgBar.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 4;");
        bgBar.setPrefHeight(6);
        bgBar.setMaxWidth(Double.MAX_VALUE);
        Region fillBar = new Region();
        fillBar.setStyle("-fx-background-color: " + progressColor + "; -fx-background-radius: 4;");
        fillBar.setPrefHeight(6);
        fillBar.prefWidthProperty().bind(bgBar.widthProperty().multiply(progress / 100.0));
        progressBar.getChildren().addAll(bgBar, fillBar);
        
        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        Label percentage = new Label(progress + "% Completato");
        percentage.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        HBox avatars = new HBox(-8);
        List<ProjectMembership> members = project.getMemberships();
        int maxAvatars = Math.min(3, members.size());
        for (int i = 0; i < maxAvatars; i++) {
            User member = members.get(i).getUser();
            if (member != null) {
                StackPane avatar = new StackPane();
                Circle circle = new Circle(12);
                circle.setFill(Color.web(getColorForUser(member.getId())));
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(2);
                Label initials = new Label(getInitials(member.getName()));
                initials.setStyle("-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;");
                avatar.getChildren().addAll(circle, initials);
                avatars.getChildren().add(avatar);
            }
        }
        progressInfo.getChildren().addAll(percentage, spacer2, avatars);
        progressSection.getChildren().addAll(progressBar, progressInfo);
        
        card.getChildren().addAll(header, name, description, progressSection);
        
        // CLICK SINISTRO PER APRIRE
        card.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                openProjectDetails(project);
            }
        });
        
        // CLICK DESTRO PER ELIMINARE
        if (isAdmin) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("🗑️ Elimina Progetto");
            deleteItem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            deleteItem.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Eliminazione");
                alert.setHeaderText("Stai per eliminare '" + project.getName() + "'");
                alert.setContentText("Sei sicuro? Tutte le attività e i documenti verranno persi.");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        projectController.deleteProject(project.getId());
                        loadProjects();
                        loadRecentProjects();
                        loadActivities();
                    }
                });
            });
            contextMenu.getItems().add(deleteItem);
            card.setOnContextMenuRequested(e -> {
                contextMenu.show(card, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }
        
        return card;
    }

    private void loadActivities() {
        if (activitiesTable == null) return;
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        List<ActivityView> allActivities = new ArrayList<>();
        List<Project> projects = projectController.getProjectsByUser(currentUser);
        
        for (Project project : projects) {
            // Aggiungiamo tutte le attività dei progetti dell'utente nella view
            for (Activity activity : project.getActivities()) {
                allActivities.add(new ActivityView(activity, project));
            }
        }
        
        // Ordina per scadenza (più vicine prima)
        allActivities.sort((a1, a2) -> {
            if (a1.getDeadline() == null) return 1;
            if (a2.getDeadline() == null) return -1;
            return a1.getDeadline().compareTo(a2.getDeadline());
        });
        
        activitiesTable.getItems().clear();
        activitiesTable.getItems().addAll(allActivities);
    }

    private void loadRecentProjects() {
        if (recentProjectsList == null) return;
        
        recentProjectsList.getChildren().clear();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            List<Project> projects = projectController.getProjectsByUser(currentUser);
            
            for (Project project : projects) {
                HBox projectItem = new HBox(8);
                projectItem.setAlignment(Pos.CENTER_LEFT);
                projectItem.setStyle("-fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand;");
                
                Circle indicator = new Circle(4);
                indicator.setFill(Color.web("#10b981"));
                
                Label projectName = new Label(project.getName());
                projectName.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: 600;");
                
                projectItem.getChildren().addAll(indicator, projectName);
                
                projectItem.setOnMouseEntered(e -> {
                    projectItem.setStyle("-fx-padding: 8 12; -fx-background-color: #1e293b; -fx-background-radius: 6; -fx-cursor: hand;");
                    projectName.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600;");
                });
                
                projectItem.setOnMouseExited(e -> {
                    projectItem.setStyle("-fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand;");
                    projectName.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: 600;");
                });
                
                projectItem.setOnMouseClicked(e -> openProjectDetails(project));
                
                recentProjectsList.getChildren().add(projectItem);
            }
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }

    private String getColorForUser(int userId) {
        String[] colors = {"#0891b2", "#6366f1", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#3b82f6", "#ef4444"};
        return colors[userId % colors.length];
    }

    @FXML
    private void toggleNotifications() {
        if (notificationPanel != null) {
            notificationPanel.setVisible(!notificationPanel.isVisible());
        }
    }

    @FXML
    private void hideNotificationsIfClickOutside() {
        if (notificationPanel != null && notificationPanel.isVisible()) {
            notificationPanel.setVisible(false);
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
                if (notificationBadge != null) {
                    notificationBadge.setVisible(false);
                }
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
            if (notificationPanel != null) notificationPanel.setVisible(false);
        }
    }

    @FXML
    private void handleChat() {
        System.out.println("Apertura chat...");
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
            stage.initOwner(projectsGrid.getScene().getWindow());
            stage.showAndWait();
            loadProjects();
            loadActivities();
            loadRecentProjects();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openProjectDetails(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_details.fxml"));
            Parent root = loader.load();
            ProjectDetailsController controller = loader.getController();
            controller.setProject(project);
            Stage stage = (Stage) projectsGrid.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}