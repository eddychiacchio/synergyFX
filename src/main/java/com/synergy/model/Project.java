package com.synergy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Implementa ISubject
public class Project implements Serializable, ISubject {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    
    private List<ProjectMembership> memberships = new ArrayList<>();
    private List<Activity> activities = new ArrayList<>();
    private List<ProjectDocument> documents = new ArrayList<>();

    public Project(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // --- METODI OBSERVER (PATTERN) ---

    @Override
    public void attach(IObserver o) {
        // La logica è gestita tramite ProjectMembership nel Controller, 
        // ma qui formalmente rispettiamo l'interfaccia.
    }

    @Override
    public void detach(IObserver o) {
        // Idem come sopra
    }

    @Override
    public void notifyObservers(String message) {
        for (ProjectMembership pm : memberships) {
            User u = pm.getUser();
            // Chiama il metodo update() dell'utente
            u.update("[Progetto: " + this.name + "] " + message);
        }
    }

    // --- GETTERS & SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ProjectMembership> getMemberships() {
        if (memberships == null) memberships = new ArrayList<>();
        return memberships;
    }

    public List<Activity> getActivities() {
        if (activities == null) activities = new ArrayList<>();
        return activities;
    }

    public List<ProjectDocument> getDocuments() {
        if (documents == null) documents = new ArrayList<>();
        return documents;
    }
}