package com.synergy.util;

import com.synergy.model.User;

//classe che gestisce la sessione di un utente
public class SessionManager {
    
    // Istanza statica unica (Singleton Pattern)
    private static SessionManager instance;
    
    // variabile che manterrà in memoria l'utente loggato
    private User currentUser;

    // Costruttore privato per impedire 'new SessionManager()'
    private SessionManager() {}

    // Metodo per ottenere l'istanza
    public static SessionManager getInstance() {
    	// Se non è mai stata chiesta prima (è null), allora la crea
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

    //restituisce l'utente attualmente loggato
    public User getCurrentUser() {
        return currentUser;
    }
}