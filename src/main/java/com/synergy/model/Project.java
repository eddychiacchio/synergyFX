package com.synergy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.synergy.pattern.observer.IObserver;
import com.synergy.pattern.observer.ISubject;

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
    	// Verifichiamo che l'Observer passato sia effettivamente un User
        if (o instanceof User) {
            User user = (User) o;
            
            // Controlliamo tramite Stream API se l'utente è già tra i membri (evita duplicati)
            boolean alreadyMember = getMemberships().stream()
                .anyMatch(pm -> pm.getUser().getId() == user.getId());
                
            if (!alreadyMember) {
                // Crea il legame fisico e lo aggiunge alla lista
                ProjectMembership newMembership = new ProjectMembership(this, user);
                memberships.add(newMembership);
            }
        }
    }

    @Override
    public void detach(IObserver o) {
    	if (o instanceof User) {
            User user = (User) o;
            
            // Rimuove la ProjectMembership in cui l'ID dell'utente corrisponde a quello passato
            getMemberships().removeIf(pm -> pm.getUser().getId() == user.getId());
        }
    }

    @Override
    public void notifyObservers(String message) {
        for (ProjectMembership pm : memberships) {
            User u = pm.getUser();
            // Chiama il metodo update() dell'utente
            u.update(message);
        }
    }
    
    @Override
    public String toString() {
        return this.name;
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