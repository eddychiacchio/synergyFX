package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.controller.ProjectController;
import com.synergy.model.*;
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

// EREDITA DAL BASE CONTROLLER!
public class DashboardController extends BaseController {

    public static class ActivityView {
        private Activity activity;
        private Project project;

        public ActivityView(Activity activity, Project project) {
            this.activity = activity;
            this.project = project;
        }
        public Activity getActivity() { return activity; }
        public String getName() { return activity.getTitle(); }
        public String getProjectName() { return project.getName(); }
        public LocalDate getDeadline() { return activity.getDeadline(); }
        public PriorityLevel getPriority() { return activity.getPriority(); }
        public ActivityStatus getStatus() { return activity.getStatus(); }
    }

    @FXML private GridPane projectsGrid;
    @FXML private VBox recentProjectsList;
    @FXML private TableView<ActivityView> activitiesTable;
    @FXML private TableColumn<ActivityView, String> activityNameColumn;
    @FXML private TableColumn<ActivityView, String> activityProjectColumn;
    @FXML private TableColumn<ActivityView, LocalDate> activityDeadlineColumn;
    @FXML private TableColumn<ActivityView, PriorityLevel> activityPriorityColumn;
    @FXML private TableColumn<ActivityView, ActivityStatus> activityStatusColumn;

    private ProjectController projectController = new ProjectController();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            // Chiama il metodo della superclasse per impostare nomi e iniziali!
            initUserHeader(currentUser);
            
            if (notificationBadge != null) notificationBadge.setVisible(false);
            
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
            refreshNotifications(); // Metodo ereditato
        }
    }

    private void setupActivitiesTable() {
        activityNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        activityNameColumn.setCellFactory(column -> new TableCell<ActivityView, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    ActivityView av = getTableView().getItems().get(getIndex());
                    VBox container = new VBox(4);
                    Label nameLabel = new Label(item);
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
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
                if (empty || projectName == null) setGraphic(null);
                else {
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
                if (empty || date == null) { setText(null); setStyle(""); } 
                else {
                    setText(date.format(dateFormatter));
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
                    if (daysUntil < 0) setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;"); 
                    else if (daysUntil == 0) setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold;"); 
                    else if (daysUntil <= 3) setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 600;"); 
                    else setStyle("-fx-text-fill: #64748b;");
                }
            }
        });
        
        activityPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        activityPriorityColumn.setCellFactory(column -> new TableCell<ActivityView, PriorityLevel>() {
            @Override
            protected void updateItem(PriorityLevel priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) setGraphic(null);
                else {
                    Label badge = new Label(priority.toString());
                    switch (priority) {
                        case ALTA: badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case MEDIA: badge.setStyle("-fx-background-color: #fed7aa; -fx-text-fill: #ea580c; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case BASSA: badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                    }
                    setGraphic(badge); setAlignment(Pos.CENTER);
                }
            }
        });
        
        activityStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        activityStatusColumn.setCellFactory(column -> new TableCell<ActivityView, ActivityStatus>() {
            @Override
            protected void updateItem(ActivityStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) setGraphic(null);
                else {
                    Label badge = new Label(status.toString().replace("_", " "));
                    switch (status) {
                        case DA_FARE: badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case IN_CORSO: badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                        case COMPLETATO: badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;"); break;
                    }
                    setGraphic(badge); setAlignment(Pos.CENTER);
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
            int col = 0, row = 0;
            
            for (Project project : projects) {
                try {
                    // 1. Carichiamo la Card dal nuovo file FXML
                    FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_card.fxml"));
                    VBox projectCard = loader.load();
                    ProjectCardController cardController = loader.getController();
                    
                    // 2. Passiamo i dati e spieghiamo al controller cosa fare se l'utente clicca i tasti!
                    cardController.setProjectData(
                        project, 
                        currentUser, 
                        // Azione Click Sinistro:
                        p -> openProjectDetails(p),
                        // Azione Modifica:
                        p -> openEditProjectModal(p),
                        // Azione Elimina:
                        p -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Conferma Eliminazione");
                            alert.setHeaderText("Stai per eliminare '" + p.getName() + "'");
                            alert.setContentText("Sei sicuro? Tutte le attività verranno perse.");
                            alert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    projectController.deleteProject(p.getId());
                                    loadProjects(); loadRecentProjects(); loadActivities();
                                }
                            });
                        }
                    );
                    
                    // 3. Aggiungiamo la Card completata alla griglia
                    projectsGrid.add(projectCard, col, row);
                    
                    col++;
                    if (col > 1) { col = 0; row++; }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadActivities() {
        if (activitiesTable == null) return;
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        List<ActivityView> allActivities = new ArrayList<>();
        List<Project> projects = projectController.getProjectsByUser(currentUser);
        
        for (Project project : projects) {
            for (Activity activity : project.getActivities()) {
                allActivities.add(new ActivityView(activity, project));
            }
        }
        
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
                HBox projectItem = new HBox(8); projectItem.setAlignment(Pos.CENTER_LEFT);
                projectItem.setStyle("-fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand;");
                Circle indicator = new Circle(4); indicator.setFill(Color.web("#0891b2"));
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
            loadProjects(); loadActivities(); loadRecentProjects();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void openEditProjectModal(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_form.fxml"));
            Parent root = loader.load();
            ProjectFormController controller = (ProjectFormController) loader.getController();
            controller.setProjectToEdit(project);
            Stage stage = new Stage();
            stage.setTitle("Modifica Progetto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectsGrid.getScene().getWindow());
            stage.showAndWait();
            loadProjects(); loadRecentProjects(); loadActivities();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openProjectDetails(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_details.fxml"));
            Parent root = loader.load();
            ProjectDetailsController controller = loader.getController();
            controller.setProject(project);
            Stage stage = (Stage) projectsGrid.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}