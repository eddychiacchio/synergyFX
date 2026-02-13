package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.controller.ProjectController;
import com.synergy.model.Project;
import com.synergy.model.User;
import com.synergy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Project> projectListView;

    private ProjectController projectController = new ProjectController();

    @FXML
    public void initialize() {
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            welcomeLabel.setText("Benvenuto, " + currentUser.getName());
            
            refreshProjectList();
            
            /* 2. Carico i progetti dell'utente
            List<Project> projects = projectController.getProjectsByUser(currentUser);
            projectListView.getItems().addAll(projects);
            */
            
            // 3. Gestisco il click sulla lista per aprire i dettagli
            projectListView.setOnMouseClicked(event -> {
                Project selected = projectListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openProjectDetails(selected);
                }
            });
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.getInstance().logout();
        App.setRoot("login"); // Torna alla schermata di login
    }

    @FXML
    private void handleNewProject() {
        try {
            // 1. Carica la finestra del form
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_form.fxml"));
            Parent root = loader.load();

            // 2. Crea uno Stage (una nuova finestra)
            Stage stage = new Stage();
            stage.setTitle("Nuovo Progetto");
            stage.setScene(new Scene(root));
            
            // 3. Blocca la finestra sotto finché questa non viene chiusa (Modale)
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projectListView.getScene().getWindow());
            
            // 4. Mostra e aspetta
            stage.showAndWait();

            // 5. Quando si chiude, ricarica la lista per vedere il nuovo progetto!
            refreshProjectList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo helper per ricaricare la lista (utile per evitare codice duplicato)
    private void refreshProjectList() {
        projectListView.getItems().clear();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<Project> projects = projectController.getProjectsByUser(currentUser);
            projectListView.getItems().addAll(projects);
        }
    }

    // Metodo helper per aprire la schermata di dettaglio
    private void openProjectDetails(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/project_details.fxml"));
            Parent root = loader.load();
            
            // Passo il progetto selezionato al controller del dettaglio
            ProjectDetailsController controller = loader.getController();
            controller.setProject(project);
            
            // Cambio la scena
            Stage stage = (Stage) projectListView.getScene().getWindow();
            stage.getScene().setRoot(root);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}