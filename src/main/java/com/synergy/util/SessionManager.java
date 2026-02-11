package com.synergy.util;

import com.synergy.model.User;

public class SessionManager {
    
    // Istanza statica unica (Singleton Pattern)
    private static SessionManager instance;
    
    // L'utente attualmente loggato
    private User currentUser;

    // Costruttore privato per impedire 'new SessionManager()'
    private SessionManager() {}

    // Metodo per ottenere l'istanza
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Salva l'utente dopo il login
    public void login(User user) {
        this.currentUser = user;
    }

    // Rimuove l'utente al logout
    public void logout() {
        this.currentUser = null;
    }

    // Recupera l'utente corrente
    public User getCurrentUser() {
        return currentUser;
    }
}