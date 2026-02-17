package com.synergy.controller.fx;

import com.synergy.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.function.Consumer;

public class ProjectCardController {

    @FXML private VBox cardRoot;
    @FXML private Label roleLabel;
    @FXML private Label projectName;
    @FXML private Label projectDescription;
    @FXML private Region bgBar;
    @FXML private Region fillBar;
    @FXML private Label percentageLabel;
    @FXML private HBox avatarsBox;

    private Project project;
    private User currentUser;
    private boolean isAdmin = false;

    // Questi Consumer permettono al componente di "avvisare" la Dashboard quando viene cliccato
    private Consumer<Project> onProjectClicked;
    private Consumer<Project> onEditClicked;
    private Consumer<Project> onDeleteClicked;

    public void setProjectData(Project project, User currentUser, Consumer<Project> onProjectClicked, Consumer<Project> onEditClicked, Consumer<Project> onDeleteClicked) {
        this.project = project;
        this.currentUser = currentUser;
        this.onProjectClicked = onProjectClicked;
        this.onEditClicked = onEditClicked;
        this.onDeleteClicked = onDeleteClicked;

        setupData();
        setupInteractions();
    }

    private void setupData() {
        for (ProjectMembership pm : project.getMemberships()) {
            if (pm.getUser() != null && pm.getUser().getId() == currentUser.getId()) {
                isAdmin = pm.getIsAdmin(); break;
            }
        }

        projectName.setText(project.getName());
        projectName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        projectDescription.setText(project.getDescription() != null && !project.getDescription().isEmpty() ? project.getDescription() : "Nessuna descrizione disponibile");
        projectDescription.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        roleLabel.setText(isAdmin ? "Project Manager" : "Team Member");
        roleLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6;");

        String mainColor = "#0891b2";
        cardRoot.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + mainColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-cursor: hand;");
        
        bgBar.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 4;");
        fillBar.setStyle("-fx-background-color: " + mainColor + "; -fx-background-radius: 4;");

        int totalTasks = project.getActivities().size();
        int completedTasks = 0;
        for (Activity a : project.getActivities()) if (a.getStatus() == ActivityStatus.COMPLETATO) completedTasks++;
        int progress = (totalTasks == 0) ? 0 : (completedTasks * 100) / totalTasks;
        
        percentageLabel.setText(progress + "% Completato");
        percentageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        fillBar.setMaxWidth(Region.USE_PREF_SIZE);
        fillBar.prefWidthProperty().bind(bgBar.widthProperty().multiply(progress / 100.0));

        avatarsBox.getChildren().clear();
        int maxAvatars = Math.min(3, project.getMemberships().size());
        for (int i = 0; i < maxAvatars; i++) {
            User member = project.getMemberships().get(i).getUser();
            if (member != null) {
                StackPane avatar = new StackPane();
                Circle circle = new Circle(12, Color.web(getColorForUser(member.getId())));
                circle.setStroke(Color.WHITE); circle.setStrokeWidth(2);
                Label initials = new Label(getInitials(member.getName()));
                initials.setStyle("-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;");
                avatar.getChildren().addAll(circle, initials); 
                avatarsBox.getChildren().add(avatar);
            }
        }
    }

    private void setupInteractions() {
        String borderColor = "#0891b2";
        cardRoot.setOnMouseEntered(e -> cardRoot.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 4); -fx-cursor: hand;"));
        cardRoot.setOnMouseExited(e -> cardRoot.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-cursor: hand;"));

        cardRoot.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && onProjectClicked != null) onProjectClicked.accept(project);
        });

        if (isAdmin) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("✏️ Modifica Progetto");
            editItem.setStyle("-fx-font-weight: bold;");
            editItem.setOnAction(e -> { if(onEditClicked != null) onEditClicked.accept(project); });
            
            MenuItem deleteItem = new MenuItem("🗑️ Elimina Progetto");
            deleteItem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            deleteItem.setOnAction(e -> { if(onDeleteClicked != null) onDeleteClicked.accept(project); });
            
            contextMenu.getItems().addAll(editItem, deleteItem);
            cardRoot.setOnContextMenuRequested(e -> { contextMenu.show(cardRoot, e.getScreenX(), e.getScreenY()); e.consume(); });
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        return (parts.length >= 2) ? (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase() : name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getColorForUser(int userId) {
        String[] colors = {"#0891b2", "#6366f1", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#3b82f6", "#ef4444"};
        return colors[userId % colors.length];
    }
}