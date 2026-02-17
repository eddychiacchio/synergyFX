package com.synergy.controller;

import com.synergy.model.*;
import com.synergy.util.DataManager;
import com.synergy.factory.*;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;

public class ProjectController {

	private IActivityFactory activityFactory;
	
	public ProjectController() {
        this.activityFactory = new ActivityFactory();
    }
	
    public void createProject(String name, String description, User creator) {
        DataManager dm = DataManager.getInstance();
        
        int newId = dm.getProjects().size() + 1;
        Project newProject = new Project(newId, name, description);
        
        ProjectMembership membership = new ProjectMembership();
        membership.setProject(newProject);
        membership.setUser(creator);
        membership.setIsAdmin(true);
        
        newProject.getMemberships().add(membership);
        
        dm.getProjects().add(newProject);
        dm.saveData();
    }
    
    public Project getProjectById(int id) {
        for (Project p : DataManager.getInstance().getProjects()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
    
    public void addActivityToProject(int projectId, String title, String priorityStr, String dateStr, String[] subTasks) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {

            PriorityLevel priority = PriorityLevel.valueOf(priorityStr);
            
            LocalDate deadline = null;
            if (dateStr != null && !dateStr.isEmpty()) {
                deadline = LocalDate.parse(dateStr);
            } else {
                deadline = LocalDate.now().plusDays(7);
            }

            Activity newActivity = activityFactory.createActivity(title, priority, deadline, subTasks);
            
            newActivity.setTitle(title);
            
            p.getActivities().add(newActivity);
            
            p.notifyObservers("È stata assegnata una nuova attività: '" + title + "' nel progetto '" + p.getName() + "'.");
            
            dm.saveData();
        }
    }
    
    public boolean deleteActivity(int projectId, int activityId) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {
            boolean removed = p.getActivities().removeIf(a -> a.getId() == activityId);
            
            if (removed) {
                dm.saveData();
                return true;
            }
            
            for (Activity a : p.getActivities()) {
                if (a instanceof TaskGroup) {
                    TaskGroup group = (TaskGroup) a;
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
    
    public boolean updateActivityStatus(int projectId, int activityId, String newStatusStr) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {
            for (Activity a : p.getActivities()) {
                if (a.getId() == activityId) {
                    a.setStatus(ActivityStatus.valueOf(newStatusStr));
                    dm.saveData(); 
                    return true;
                }
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

    public void updateActivityContent(int projectId, int activityId, String title, String priorityStr, String dateStr, String[] subTasks) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p != null) {
            List<Activity> list = p.getActivities();
            for (int i = 0; i < list.size(); i++) {
                Activity a = list.get(i);
                
                if (a.getId() == activityId) {
                	
                    a.setTitle(title);
                    a.setPriority(PriorityLevel.valueOf(priorityStr));
                    
                    if (dateStr != null && !dateStr.isEmpty()) {
                        a.setDeadline(LocalDate.parse(dateStr));
                    }

                    boolean newSubtasksExist = (subTasks != null && subTasks.length > 0 && subTasks[0].trim().length() > 0);
                    
                    if (a instanceof TaskGroup) {
                        TaskGroup group = (TaskGroup) a;
                        group.getChildren().clear();
                        
                        if (newSubtasksExist) {
                            int count = 1;
                            for (String s : subTasks) {
                                if (s != null && !s.trim().isEmpty()) {
                                    int subId = (int)(System.currentTimeMillis() + count * 100);
                                    SingleTask sub = new SingleTask(subId, s, PriorityLevel.valueOf(priorityStr));
                                    sub.setDeadline(a.getDeadline()); 
                                    group.addActivity(sub);
                                    count++;
                                }
                            }
                        }
                    } else if (newSubtasksExist) {
                    	
                        TaskGroup newGroup = new TaskGroup(a.getId(), title, PriorityLevel.valueOf(priorityStr));
                        newGroup.setStatus(a.getStatus());
                        newGroup.setDeadline(a.getDeadline()); 
                        
                        int count = 1;
                        for (String s : subTasks) {
                             if (s != null && !s.trim().isEmpty()) {
                                int subId = (int)(System.currentTimeMillis() + count * 100);
                                SingleTask sub = new SingleTask(subId, s, PriorityLevel.MEDIA);
                                sub.setDeadline(a.getDeadline());
                                newGroup.addActivity(sub);
                                count++;
                             }
                        }
                        list.set(i, newGroup);
                    }
                    
                    dm.saveData();
                    return;
                }
            }
        }
    }

    public boolean inviteUserToProject(int projectId, String userEmail) {
        DataManager dm = DataManager.getInstance();
        Project p = getProjectById(projectId);
        
        if (p == null) return false;

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

        for (ProjectMembership pm : p.getMemberships()) {
            if (pm.getUser().getId() == userfound.getId()) {
                System.out.println("Utente già presente nel progetto.");
                return false; 
            }
        }

        ProjectMembership membership = new ProjectMembership();
        membership.setProject(p);
        membership.setUser(userfound);
        membership.setIsAdmin(false);

        p.getMemberships().add(membership);
        dm.saveData();
        
        return true;
    }
    
 // --- PATTERN STRATEGY ---
    public List<Activity> getSortedActivities(int projectId, String sortType) {
        Project p = getProjectById(projectId);
        if (p == null) return new ArrayList<>();

        // 1. Creo una COPIA della lista (per non modificare l'ordine salvato nel DB/File)
        List<Activity> sortedList = new ArrayList<>(p.getActivities());

        // 2. Scelgo la strategia (Context)
        SortStrategy strategy;

        if ("priority".equals(sortType)) {
            strategy = new SortByPriority();
        } else if ("deadline".equals(sortType)) {
            strategy = new SortByDeadline();
        } else {
            // Default: nessun ordinamento particolare (ordine di creazione)
            return sortedList;
        }

        // 3. Eseguo la strategia
        strategy.sort(sortedList);

        return sortedList;
    }
    
 // Metodo FILTRATO: Restituisce solo i progetti dell'utente
    public List<Project> getProjectsByUser(User user) {
        List<Project> result = new ArrayList<>();
        DataManager dm = DataManager.getInstance();
        
        // Scorro tutti i progetti del sistema
        for (Project p : dm.getProjects()) {
            // Per ogni progetto, controllo la lista dei membri
            for (ProjectMembership pm : p.getMemberships()) {
                // Se trovo l'ID dell'utente tra i membri...
                if (pm.getUser().getId() == user.getId()) {
                    result.add(p); // ...aggiungo il progetto alla lista
                    break; // Passo al prossimo progetto
                }
            }
        }
        return result;
    }
    
 // --- Metodo per eliminare un intero progetto ---
    public boolean deleteProject(int projectId) {
        DataManager dm = DataManager.getInstance();
        
        // Cerca il progetto e rimuovilo dalla lista globale
        boolean removed = dm.getProjects().removeIf(p -> p.getId() == projectId);
        
        if (removed) {
            dm.saveData(); // Salva istantaneamente su file
        }
        
        return removed;
    }
    
 // --- Metodo per MODIFICARE un progetto esistente ---
    public void updateProjectDetails(int projectId, String newName, String newDesc) {
        Project project = getProjectById(projectId);
        if (project != null) {
            project.setName(newName);
            project.setDescription(newDesc);
            DataManager.getInstance().saveData(); // Salva immediatamente le modifiche nel file .ser
        }
    }
}