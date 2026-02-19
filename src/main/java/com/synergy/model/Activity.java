package com.synergy.model;

import java.io.Serializable;// Serializable è necessari per permettere il salvataggio dell'oggetto su file
import java.time.LocalDate;

//definisco una classe astratta(non instanziabile con new, ma solo ereditatà
//può essere salvata su file grazie a Serializable
public abstract class Activity implements Serializable {
	
	// identificatore univoco per la serializzazione.
    // Serve a garantire che la versione salvata e quella letta dal file siano compatibili.
    private static final long serialVersionUID = 1L;

    //attributi protetti, in modo che possano essere visti da ST GT
    protected int id;
    protected String title;
    protected PriorityLevel priority;
    protected ActivityStatus status;
    protected LocalDate deadline;

    //costruttore di activity
    public Activity(int id, String title, PriorityLevel priority) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = ActivityStatus.DA_FARE;
        this.deadline = LocalDate.now().plusDays(7);
    }

    //metodi per leggere i valori delle variabili protette
    public int getId() { return id; }
    public String getTitle() { return title; }
    public PriorityLevel getPriority() { return priority; }
    public ActivityStatus getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }

    //metodi per modificare i valori delle variabili
    public void setTitle(String title) { this.title = title; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }
    public void setStatus(ActivityStatus status) { this.status = status; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}