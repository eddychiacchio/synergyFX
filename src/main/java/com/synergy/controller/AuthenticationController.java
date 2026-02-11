package com.synergy.controller;

import com.synergy.model.User;
import com.synergy.util.DataManager;
import java.util.List;

public class AuthenticationController {

    // Metodo che cerca l'utente nella lista caricata dal file
    public User login(String email, String password) {
        List<User> users = DataManager.getInstance().getUsers();
        
        for (User u : users) {
            // Confronto email e password (nella realtà la password sarebbe hashata)
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                return u; // Trovato!
            }
        }
        return null; // Nessun utente trovato con queste credenziali
    }
}
