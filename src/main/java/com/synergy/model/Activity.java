package com.synergy.model;

import java.io.Serializable;
import java.time.LocalDate; // Importante per le date

public abstract class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String title;
    protected PriorityLevel priority;
    protected ActivityStatus status;
    protected LocalDate deadline; // Il campo data

    public Activity(int id, String title, PriorityLevel priority) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = ActivityStatus.DA_FARE;
        this.deadline = LocalDate.now().plusDays(7); // Default: scadenza tra 7 giorni
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public PriorityLevel getPriority() { return priority; }
    public ActivityStatus getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }

    // --- SETTERS (Quelli che mancavano e causavano l'errore) ---
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }
    
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}