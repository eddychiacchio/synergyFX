package com.synergy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Implementa IObserver
public class User implements Serializable, IObserver {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String password;
    
    // Lista delle notifiche ricevute
    private List<String> notifications = new ArrayList<>();

    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // --- METODO OBSERVER ---
    @Override
    public void update(String message) {
        // Aggiunge la notifica in cima alla lista
        notifications.add(0, message);
    }
    
    // Metodo per leggere le notifiche
    public List<String> getNotifications() {
        if(notifications == null) notifications = new ArrayList<>();
        return notifications;
    }
    
    // Metodo per pulire le notifiche (opzionale)
    public void clearNotifications() {
        notifications.clear();
    }

    // --- GETTERS & SETTERS STANDARD ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}