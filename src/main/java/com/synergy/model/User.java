package com.synergy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.synergy.pattern.observer.IObserver;

// PATTERN OBSERVER: La classe User rappresenta un "Osservatore" (Observer).
// implementa IObserver, il che significa che può "ascoltare" e ricevere notifiche dai Progetti.

public class User implements Serializable, IObserver {

    // ID di serializzazione
    private static final long serialVersionUID = 1L;

    // attributi base dell'utente
    private int id;
    private String name;
    private String email;
    private String password;
    
    // lista privata per memorizzare lo storico delle notifiche ricevute dall'utente
    private List<String> notifications = new ArrayList<>();

    // costruttore per creare un nuovo utente
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // METODO OBSERVER 
    // questo è il metodo richiesto dall'interfaccia IObserver.
    // viene chiamato in automatico (ad esempio dal ProjectController) quando c'è una novità.
    @Override
    public void update(String message) {
        
        // aggiunge la stringa di notifica ricevuta in cima alla lista (indice 0), 
        // così la più recente sarà la prima ad essere mostrata nell'interfaccia.
        notifications.add(0, message);
    }
    
    // metodo per recuperare e leggere tutte le notifiche dell'utente
    public List<String> getNotifications() {

        // controllo di sicurezza: se per via della deserializzazione la lista è null, ne crea una vuota
        if(notifications == null) notifications = new ArrayList<>();
        return notifications;
    }
    
    // metodo per svuotare la lista delle notifiche
    public void clearNotifications() {
        notifications.clear();
    }

    // getter e setter 
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
