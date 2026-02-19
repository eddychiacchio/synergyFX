package com.synergy.controller;

// importazione necessarie (modelli,utilità di dati, pattern factory, liste, date)
import com.synergy.model.*;
import com.synergy.strategy.*;
import com.synergy.util.DataManager;
import com.synergy.factory.*;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;


// controller principale che gestisce la logica dei progetti e delle loro attività
public class ProjectController {

    // PATTERN FACTORY: Riferimento alla fabbrica di attività per delegarne la creazione
	private IActivityFactory activityFactory;

	    // costruttore che inizializza la factory
	public ProjectController() {
        this.activityFactory = new ActivityFactory();
    }
	
    // crea un nuovo PROGETTO
    public void createProject(String name, String description, User creator) {
        DataManager dm = DataManager.getInstance();
        
        // assegna un ID sequenziale basato sul numero attuale di progetti
        int newId = dm.getProjects().size() + 1;

        // crea il PROGETTO
        Project newProject = new Project(newId, name, description);
        
        // crea l'associazione tra l'utente che crea il progetto e il progetto stesso
        ProjectMembership membership = new ProjectMembership();
        membership.setProject(newProject);
        membership.setUser(creator);

        // chi crea il progetto è automaticamente Amministratore (Admin)
        membership.setIsAdmin(true);
        
        // aggiunge il membro al PROGETTO
        newProject.getMemberships().add(membership);
        
        // aggiunge il nuovo progetto alla lista globale
        dm.getProjects().add(newProject);

        // salva Modifiche su disco
        dm.saveData();
    }
    

    // metodo di utilità per trovare un progetto passando il suo ID
    public Project getProjectById(int id) {
        for (Project p : DataManager.getInstance().getProjects()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null; // restituisce null se non lo trova
    }
    
    // aggiunge una nuova attività (task) a un progetto
    public void addActivityToProject(int projectId, String title, String priorityStr, String dateStr, String[] subTasks) {
        DataManager dm = DataManager.getInstance();

        // recupera il progetto a cui aggiungere l'attività
        Project p = getProjectById(projectId);
        
        if (p != null) {

            // converte la stringa della priorità (es. "ALTA") nel tipo Enumeratore corrispondente
            PriorityLevel priority = PriorityLevel.valueOf(priorityStr);
            
            // gestione della data di scadenza
            LocalDate deadline = null;

            // se l'utente ha fornito una data, la formatta
            if (dateStr != null && !dateStr.isEmpty()) {
                deadline = LocalDate.parse(dateStr);
            } else {
                // altrimenti imposta una scadenza di default a 7 giorni da oggi
                deadline = LocalDate.now().plusDays(7);
            }

            // PATTERN FACTORY: Delega alla factory il compito di creare l'attività.
            // la factory deciderà se creare una SingleTask o una TaskGroup a seconda se ci sono sotto-task.
            Activity newActivity = activityFactory.createActivity(title, priority, deadline, subTasks);
            

            // imposta il titolo 
            newActivity.setTitle(title);
            
            // aggiunge la nuova attività alla lista del progetto
            p.getActivities().add(newActivity);
            
            // PATTERN OBSERVER: Notifica gli utenti del progetto della nuova attività
            p.notifyObservers("È stata assegnata una nuova attività: '" + title + "' nel progetto '" + p.getName() + "'.");
            
            // salva su file
            dm.saveData();
        }
    }


    // elimina un'attività o una sotto-attività
    public boolean deleteActivity(int projectId, int activityId) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {

            // rimuove l'attività dalla lista principale se l'ID corrisponde
            boolean removed = p.getActivities().removeIf(a -> a.getId() == activityId);
            
            if (removed) {
                dm.saveData();
                return true;
            }
            
            // se non era nell'elenco principale, cerca se era un sotto-task dentro un "TaskGroup" (PATTERN COMPOSITE)
            for (Activity a : p.getActivities()) {
                if (a instanceof TaskGroup) {  // Se l'attività è un gruppo
                    TaskGroup group = (TaskGroup) a;
                    
                    // prova a rimuoverlo dai suoi figli (sotto-task)
                    boolean removedFromChild = group.getChildren().removeIf(child -> child.getId() == activityId);
                    if (removedFromChild) {
                        dm.saveData();
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // aggiorna lo stato di un'attività (es. da DA_FARE a COMPLETATO)
    public boolean updateActivityStatus(int projectId, int activityId, String newStatusStr) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {
            // cerca tra le attività principali
            for (Activity a : p.getActivities()) {
                if (a.getId() == activityId) {
                    a.setStatus(ActivityStatus.valueOf(newStatusStr));
                    dm.saveData(); 
                    return true;
                }

                // se è un gruppo, cerca tra i figli (PATTERN COMPOSITE)
                if (a instanceof TaskGroup) {
                    for (Activity child : ((TaskGroup) a).getChildren()) {
                        if (child.getId() == activityId) {
                            child.setStatus(ActivityStatus.valueOf(newStatusStr));
                            dm.saveData();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    // aggiorna i dettagli (titolo, priorità, data, sotto-task) di un'attività esistente
    public void updateActivityContent(int projectId, int activityId, String title, String priorityStr, String dateStr, String[] subTasks) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {
            List<Activity> list = p.getActivities();
            
            // scorre la lista usando l'indice classico per poter eventualmente sostituire l'elemento
            for (int i = 0; i < list.size(); i++) {
                Activity a = list.get(i);
                
                if (a.getId() == activityId) {
                	
                    // aggiorna i campi base
                    a.setTitle(title);
                    a.setPriority(PriorityLevel.valueOf(priorityStr));
                    
                    if (dateStr != null && !dateStr.isEmpty()) {
                        a.setDeadline(LocalDate.parse(dateStr));
                    }

                    // verifica se l'utente ha inserito dei sotto-task
                    boolean newSubtasksExist = (subTasks != null && subTasks.length > 0 && subTasks[0].trim().length() > 0);
                    

                    // se l'attività era GIA' un gruppo di task
                    if (a instanceof TaskGroup) {
                        TaskGroup group = (TaskGroup) a;
                        group.getChildren().clear();  // Svuota i vecchi sotto-task
                        
                        // riempie il gruppo con i nuovi sotto-task forniti
                        if (newSubtasksExist) {
                            int count = 1;
                            for (String s : subTasks) {
                                if (s != null && !s.trim().isEmpty()) {

                                    // genera un nuovo ID progressivo per il sotto-task
                                    int subId = (int)(System.currentTimeMillis() + count * 100);
                                    SingleTask sub = new SingleTask(subId, s, PriorityLevel.valueOf(priorityStr));
                                    sub.setDeadline(a.getDeadline()); // Eredita la scadenza del genitore
                                    group.addActivity(sub);
                                    count++;
                                }
                            }
                        }
                    
                    // se non era un gruppo ma ORA l'utente ha aggiunto dei sotto-task, lo trasforma in gruppo
                    } else if (newSubtasksExist) {
                    	
                        // crea un nuovo TaskGroup con gli stessi dati del vecchio SingleTask
                        TaskGroup newGroup = new TaskGroup(a.getId(), title, PriorityLevel.valueOf(priorityStr));
                        newGroup.setStatus(a.getStatus());
                        newGroup.setDeadline(a.getDeadline()); 
                        
                        int count = 1;

                        // aggiunge i sotto-task
                        for (String s : subTasks) {
                             if (s != null && !s.trim().isEmpty()) {
                                int subId = (int)(System.currentTimeMillis() + count * 100);
                                SingleTask sub = new SingleTask(subId, s, PriorityLevel.MEDIA);
                                sub.setDeadline(a.getDeadline());
                                newGroup.addActivity(sub);
                                count++;
                             }
                        }

                        // sostituisce la vecchia attività singola con il nuovo gruppo all'interno della lista
                        list.set(i, newGroup);
                    }
                    
                    dm.saveData(); // salva
                    return; // esce dalla funzione una volta trovato e aggiornato
                }
            }
        }
    }


    // invita un altro utente a partecipare al progetto tramite 
    public boolean inviteUserToProject(int projectId, String userEmail) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p == null) return false;

        // cerca se esiste un utente nel sistema con quella email
        User userfound = null;
        for (User u : dm.getUsers()) {
            if (u.getEmail().equalsIgnoreCase(userEmail)) {
                userfound = u;
                break;
            }
        }

        if (userfound == null) {
            System.out.println("Utente non trovato: " + userEmail);
            return false;
        }

        // DELEGHIAMO AL PATTERN OBSERVER L'ISCRIZIONE!
        int membriIniziali = p.getMemberships().size();
        
        // chiama il metodo "attach" del Pattern Observer definito nella classe Project
        // che oltre ad iscriverlo alle notifiche, lo aggiunge ai membri del progetto.
        p.attach(userfound); 
        
        // se la dimensione della lista è aumentata, vuol dire che l'attach ha funzionato 
        // (l'utente non era già presente)
        if (p.getMemberships().size() > membriIniziali) {
            dm.saveData();
            return true;
        } else {
            System.out.println("Utente già presente nel progetto.");
            return false;
        }
    }
    
    // PATTERN STRATEGY
    // ordina le attività del progetto in base al criterio scelto
    public List<Activity> getSortedActivities(int projectId, String sortType) {
        Project p = getProjectById(projectId);
        if (p == null) return new ArrayList<>();

        // 1. creo una COPIA della lista per poterla ordinare liberamente per la visualizzazione, 
        // senza intaccare l'ordine cronologico salvato nel Database/File.
        List<Activity> sortedList = new ArrayList<>(p.getActivities());

        // 2. scelgo la strategia (Context)
        ISortStrategy strategy;

        // seleziono l'oggetto Strategia corretto in base alla richiesta
        if ("priority".equals(sortType)) {
            strategy = new SortByPriority();
        } else if ("deadline".equals(sortType)) {
            strategy = new SortByDeadline();
        } else {
            // default: restituisce la lista normale non riordinata
            return sortedList;
        }

        // 3. eseguo la strategia chiamando il metodo sort specifico (Polimorfismo)
        strategy.sort(sortedList);

        return sortedList;
    }
    
    // metodo FILTRATO: Restituisce esclusivamente la lista dei progetti a cui l'utente loggato partecipa
    public List<Project> getProjectsByUser(User user) {
        List<Project> result = new ArrayList<>();
        DataManager dm = DataManager.getInstance();
        
        // scorro tutti i progetti del sistema
        for (Project p : dm.getProjects()) {

            // per ogni progetto, controllo la lista dei membri
            for (ProjectMembership pm : p.getMemberships()) {

                // se trovo l'ID dell'utente tra i membri
                if (pm.getUser().getId() == user.getId()) {
                    
                    result.add(p); //aggiungo il progetto alla lista

                    break; // passo al prossimo progetto
                }
            }
        }
        return result;
    }
    
    // metodo per eliminare un intero progetto
    public boolean deleteProject(int projectId) {
        DataManager dm = DataManager.getInstance();
        
        // cerca il progetto e rimuovilo dalla lista globale di DataManager se l'ID coincide
        boolean removed = dm.getProjects().removeIf(p -> p.getId() == projectId);
        
        if (removed) {
            dm.saveData(); // salva istantaneamente su file la rimozione
        }
        
        return removed;
    }
    
    // metodo per MODIFICARE i dettagli principali (nome, descrizione) di un progetto esistente
    public void updateProjectDetails(int projectId, String newName, String newDesc) {
        
        Project project = getProjectById(projectId);

        //se il progetto viene trovato, ne aggiorna i parametri
        if (project != null) {
            project.setName(newName);
            project.setDescription(newDesc);
            DataManager.getInstance().saveData(); // Salva immediatamente le modifiche nel file.ser
        }
    }
}
