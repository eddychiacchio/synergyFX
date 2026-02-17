package com.synergy.controller;

import com.synergy.model.User;
import com.synergy.util.DataManager;
import java.util.List;

public class AuthenticationController {

    // Metodo LOGIN
    public User login(String email, String password) {
        List<User> users = DataManager.getInstance().getUsers();
        
        for (User user : users) {
            // Confronto case-insensitive per l'email
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // --- METODO REGISTRAZIONE CORRETTO ---
    public boolean register(String name, String email, String password) {
        List<User> users = DataManager.getInstance().getUsers();

        // 1. Controllo se l'email esiste già
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return false; // Email già usata
            }
        }

        // 2. Genero un ID univoco (int)
        // Cerco l'ID più alto esistente e aggiungo 1 (Auto-incremento manuale)
        int newId = 1;
        for (User u : users) {
            if (u.getId() >= newId) {
                newId = u.getId() + 1;
            }
        }

        // 3. Creo il nuovo utente usando il TUO costruttore
        User newUser = new User(newId, name, email, password);

        // 4. Aggiungo alla lista e salvo su file
        users.add(newUser);
        DataManager.getInstance().saveData();

        return true; // Registrazione OK
    }
}