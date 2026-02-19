package com.synergy.controller;

import com.synergy.model.User;
import com.synergy.util.DataManager;
import java.util.List;

//gestisce il modello Utente e i dati
public class AuthenticationController {

    //verifica le credenziali, restituisce l'utente se corrette altrimenti null
    public User login(String email, String password) {
    	
        List<User> users = DataManager.getInstance().getUsers();//recupera gli utenti da DM
        
        //scorre la lista di tutti gli utenti 
        for (User user : users) {
            // verica che la mail sia correta (esclude il case-sensitive) e anche la passsword
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    // metodo per registrare un nuovo utente, true = successo, false = email già esistente
    public boolean register(String name, String email, String password) {
    	
        List<User> users = DataManager.getInstance().getUsers();//recupera gli utenti da DM

        // 1. Controllo se l'email esiste già
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return false; // Email già usata
            }
        }

        // 2. Genero un ID univoco
        int newId = 1;// inizializzo a uno l'ID
        // Cerco l'ID più alto esistente e aggiungo 1 per ogni utente
        for (User u : users) {
            if (u.getId() >= newId) {
                newId = u.getId() + 1;
            }
        }

        // 3. Creo il nuovo utente
        User newUser = new User(newId, name, email, password);

        // 4. Aggiungo alla lista e salvo su file
        users.add(newUser);
        DataManager.getInstance().saveData();

        return true;
    }
}