package com.synergy.model;

import java.io.Serializable;

public class ProjectMembership implements Serializable {
    private static final long serialVersionUID = 1L;

    private Project project;
    private User user;
    private boolean isAdmin;

    // Costruttore vuoto (utile per serializzazione)
    public ProjectMembership() {}

    // Getters e Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
}