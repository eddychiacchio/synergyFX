package com.synergy.controller.fx;

import com.synergy.App;
import com.synergy.model.User;
import com.synergy.util.DataManager;
import com.synergy.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

// Classe astratta: non può essere istanziata direttamente, serve solo per far ereditare il codice!
public abstract class BaseController {

    // Usiamo "protected" invece di "private" affinché le classi figlie possano vederli
    @FXML protected Label usernameLabel;
    @FXML protected Label userInitialsLabel;
    @FXML protected VBox notificationPanel;
    @FXML protected ListView<String> notificationsListView;
    @FXML protected Label notificationBadge;

    // --- METODI COMUNI DELLA UI ---

    protected void initUserHeader(User currentUser) {
        if (currentUser != null) {
            if (usernameLabel != null) usernameLabel.setText(currentUser.getName());
            if (userInitialsLabel != null) {
                userInitialsLabel.setText(getInitials(currentUser.getName()));
            }
        }
    }

    protected String getInitials(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }

    @FXML
    protected void toggleNotifications() {
        if (notificationPanel != null) {
            notificationPanel.setVisible(!notificationPanel.isVisible());
            if (notificationPanel.isVisible()) refreshNotifications();
        }
    }

    @FXML
    protected void hideNotificationsIfClickOutside() {
        if (notificationPanel != null && notificationPanel.isVisible()) {
            notificationPanel.setVisible(false);
        }
    }

    protected void refreshNotifications() {
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
    protected void handleClearNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.clearNotifications();
            DataManager.getInstance().saveData(); 
            refreshNotifications();
            if (notificationPanel != null) {
                notificationPanel.setVisible(false);
            }
        }
    }

    @FXML
    protected void handleChat() {
        System.out.println("Apertura chat...");
    }

    @FXML
    protected void handleLogout() throws IOException {
        SessionManager.getInstance().logout();
        App.setRoot("login");
    }
    
    protected String getColorForUser(int userId) {
        String[] colors = {"#0891b2", "#6366f1", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#3b82f6", "#ef4444"};
        return colors[userId % colors.length];
    }
}