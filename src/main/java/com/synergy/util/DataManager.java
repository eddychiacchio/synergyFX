package com.synergy.util;

import com.synergy.model.Project;
import com.synergy.model.User;
import com.synergy.model.ProjectMembership;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    
    // -- SINGLETON --
    private static DataManager instance;
    
    private List<User> users;
    private List<Project> projects;

    // FILE DI SALVATAGGIO (Percorsi Assoluti nella cartella Utente per evitare errori di permessi)
    private final String FILE_USERS = System.getProperty("user.home") + File.separator + "synergy_users.ser";
    private final String FILE_PROJECTS = System.getProperty("user.home") + File.separator + "synergy_projects.ser";

    private DataManager() {
        // Inizializza liste vuote
        users = new ArrayList<>();
        projects = new ArrayList<>();
        
        loadData(); // Carica i dati
        
        // AUTO-RIPARAZIONE: Se non ci sono utenti, crea Admin
        if (users.isEmpty()) {
            System.out.println("⚠️ ATTENZIONE: Nessun utente trovato. Creo admin di default.");
            User admin = new User(1, "Mario Rossi", "test@synergy.com", "12345");
            users.add(admin);
            saveData();
        }
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    // Getters
    public List<User> getUsers() { return users; }
    public List<Project> getProjects() { return projects; }

    // --- SALVATAGGIO ---
    public void saveData() {
        try {
            // Salva Utenti
            try (ObjectOutputStream outUser = new ObjectOutputStream(new FileOutputStream(FILE_USERS))) {
                outUser.writeObject(users);
            }
            // Salva Progetti
            try (ObjectOutputStream outProj = new ObjectOutputStream(new FileOutputStream(FILE_PROJECTS))) {
                outProj.writeObject(projects);
            }
            System.out.println("✅ Dati salvati in: " + FILE_PROJECTS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CARICAMENTO ---
    private void loadData() {
        // Carica Utenti
        File fUsers = new File(FILE_USERS);
        if (fUsers.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fUsers))) {
                users = (List<User>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Errore lettura utenti, resetto lista.");
                users = new ArrayList<>();
            }
        }

        // Carica Progetti
        File fProjects = new File(FILE_PROJECTS);
        if (fProjects.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fProjects))) {
                projects = (List<Project>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Errore lettura progetti, resetto lista.");
                projects = new ArrayList<>();
            }
        }
        
     // Ricollega i riferimenti (Risolve i cloni della serializzazione)
        if (projects != null && users != null) {
            for (Project p : projects) {
                for (ProjectMembership pm : p.getMemberships()) {
                    for (User globalUser : users) {
                        if (pm.getUser().getId() == globalUser.getId()) {
                            pm.setUser(globalUser); // Sostituisci il clone con l'utente reale!
                            break;
                        }
                    }
                }
            }
        }
    }
}