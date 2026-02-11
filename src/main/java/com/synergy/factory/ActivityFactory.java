package com.synergy.factory;

import com.synergy.model.*;
import java.time.LocalDate;

public class ActivityFactory {

    /**
     * Factory Method Statico.
     * Analizza i parametri e decide se creare un SingleTask o un TaskGroup.
     */
    public static Activity createActivity(String title, PriorityLevel priority, LocalDate deadline, String[] subTasks) {
        
        // 1. Generazione ID univoco (incapsuliamo qui la logica)
        int id = (int) (System.currentTimeMillis() & 0xfffffff);

        // 2. Controllo: Ci sono sotto-attività valide?
        boolean isGroup = false;
        if (subTasks != null && subTasks.length > 0) {
            for (String s : subTasks) {
                if (s != null && !s.trim().isEmpty()) {
                    isGroup = true;
                    break;
                }
            }
        }

        // 3. Creazione dell'oggetto corretto
        if (isGroup) {
            // È un GRUPPO
            TaskGroup group = new TaskGroup(id, title, priority);
            group.setDeadline(deadline);
            
            // Creazione e aggiunta dei figli (logica spostata dal Controller)
            int i = 1;
            for (String subTitle : subTasks) {
                if (subTitle != null && !subTitle.trim().isEmpty()) {
                    // ID leggermente diverso per i figli per evitare collisioni
                    int subId = id + i + (int)(Math.random() * 1000); 
                    
                    SingleTask subTask = new SingleTask(subId, subTitle, priority);
                    subTask.setDeadline(deadline); // I figli ereditano la scadenza del padre
                    
                    group.addActivity(subTask);
                    i++;
                }
            }
            return group;
            
        } else {
            // È una TASK SINGOLA
            SingleTask task = new SingleTask(id, title, priority);
            task.setDeadline(deadline);
            return task;
        }
    }
}