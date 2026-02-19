package com.synergy.util;

import com.synergy.model.Project;
import com.synergy.model.User;
import com.synergy.model.ProjectMembership;
import java.io.*; //classi per la gestione degli I/O
import java.util.ArrayList;
import java.util.List;
//le classi per gestire l'esecuzione multithreading
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataManager { //gestiste i salvataggi e i caricamenti
    
	//la variabile statica serve per mantenere l'unica istanza permessa di questa classe
    private static DataManager instance;
    
    private List<User> users; //lista che conterà gli utenti
    private List<Project> projects; //lista che conterà i progetti
    // crea un gestore di thread per eseguire i salvataggi
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(); 

    // FILE DI SALVATAGGIO (Percorsi Assoluti nella cartella Utente per evitare errori di permessi)
    private final String FILE_USERS = System.getProperty("user.home") + File.separator + "synergy_users.ser";
    private final String FILE_PROJECTS = System.getProperty("user.home") + File.separator + "synergy_projects.ser";

    // cotruttore privato per evitare che altre classi creaino copie di DataManager
    private DataManager() {
        // Inizializza liste vuote
        users = new ArrayList<>();
        projects = new ArrayList<>();
        
        loadData(); // Carica i dati
        
        // AUTO-RIPARAZIONE: Se non ci sono utenti, crea Admin
        if (users.isEmpty()) {
            System.out.println("ATTENZIONE: Nessun utente trovato. Creo admin di default.");
            User admin = new User(1, "Eddy Chiacchio", "test@synergy.com", "123");
            users.add(admin); //aggiunge l'utente alla lista
            saveData();
        }
    }

    //metodo sincronizzato per ottenere l'unica istanza di DataManager
    public static synchronized DataManager getInstance() {
    	//se l'istanza non esiste viene creata
        if (instance == null) 
        	instance = new DataManager();
        return instance; //ritorna l'istanza
    }

    // Getters, per permettere ad altre classi di accedere alla lista utenti e progetti
    public List<User> getUsers() { return users; }
    public List<Project> getProjects() { return projects; }

 // --- SALVATAGGIO ASINCRONO ---
    public void saveData() {
        // Deleghiamo il compito di salvare al thread in background
        saveExecutor.submit(() -> {
            try {
                // salva gli utenti
                try (ObjectOutputStream outUser = new ObjectOutputStream(new FileOutputStream(FILE_USERS))) {
                    outUser.writeObject(users);
                }
                // salva i progetti
                try (ObjectOutputStream outProj = new ObjectOutputStream(new FileOutputStream(FILE_PROJECTS))) {
                    outProj.writeObject(projects);
                }
                System.out.println("Dati salvati in background in: " + FILE_PROJECTS); //messaggio di conferma stampa
            } catch (IOException e) {
                e.printStackTrace();// se c'è un errore, stampa i dettagli dell'errore
            }
        });
    }

    // --- CARICAMENTO ---
    @SuppressWarnings("unchecked")
    private void loadData() {
        //oggetto File contente  il percorso degli utenti (punta)
        File fUsers = new File(FILE_USERS);
        //controlla se esiste il file
        if (fUsers.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fUsers))) {
                users = (List<User>) ois.readObject(); //legge i dati sul file e li forza al tipo List<User>
            } catch (Exception e) {
                System.err.println("Errore lettura utenti, resetto lista.");
                users = new ArrayList<>(); //crea una nuova lista vuota
            }
        }

        // stessa cosa di prima
        File fProjects = new File(FILE_PROJECTS);
        if (fProjects.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fProjects))) {
                projects = (List<Project>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Errore lettura progetti, resetto lista.");
                projects = new ArrayList<>();
            }
        }
        
        // Ricollega i riferimenti (risolve i cloni della serializzazione)
        //se le liste non sono nulle, elimina i doppioni dovuti alla deserializzazione
        if (projects != null && users != null) {
        	//verifica tutti i progetti ocn il ciclo
            for (Project p : projects) {
            	// verifica ogni utente di ogni singolo progetto
                for (ProjectMembership pm : p.getMemberships()) {
                    // verifica che l'utente non sia nulla per evitare errori
                    if (pm.getUser() != null) {
                    	//cerca l'utente nella lista globale
                        for (User globalUser : users) {
                        	// Se l'ID dell'utente nel progetto coincide con quello globale
                            if (pm.getUser().getId() == globalUser.getId()) {
                                pm.setUser(globalUser); // Sostituisci il clone con l'utente in memoria
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}