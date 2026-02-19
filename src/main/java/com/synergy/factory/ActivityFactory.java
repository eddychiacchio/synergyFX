package com.synergy.factory;

import com.synergy.model.*;
import java.time.LocalDate;

// PATTERN FACTORY: Questa è la "Fabbrica Concreta" (Concrete Factory).
//interfaccia per creare attività.
//chi la usa deve implementare un metodo per creare nuove attività.
public class ActivityFactory implements IActivityFactory{

    // indica che stiamo fornendo l'implementazione del metodo definito nell'interfaccia
    @Override
    public Activity createActivity(String title, PriorityLevel priority, LocalDate deadline, String[] subTasks) {
        
        
        // 1. Generazione ID univoco 
        // usa il tempo di sistema attuale convertito in intero tramite una maschera bit a bit
        int id = (int) (System.currentTimeMillis() & 0xfffffff);

        
        // 2. Controllo: Ci sono sotto-attività valide?
        // variabile booleana per capire se l'utente ha inserito dei sotto-task (quindi se servirà un Gruppo)
        boolean isGroup = false;

        // controlla che l'array dei sotto-task esista e non sia vuoto
        if (subTasks != null && subTasks.length > 0) {

             // scorre tutti gli elementi (le stringhe) dell'array
            for (String s : subTasks) {

                // verifica che la singola stringa non sia vuota (ignorando eventuali spazi iniziali e finali con trim())
                if (s != null && !s.trim().isEmpty()) {

                     // è stato trovato almeno un sotto-task valido: l'attività principale deve essere un Gruppo
                    isGroup = true;

                    // interrompe il ciclo "for" in anticipo, poiché basta un solo sotto-task per confermare che è un gruppo
                    break;
                }
            }
        }

        // 3. Creazione dell'oggetto corretto in base all'esito del controllo precedente
        if (isGroup) {

            // È UN GRUPPO: l'utente ha inserito dei sotto-task
            // crea un nuovo contenitore TaskGroup (che fa parte del PATTERN COMPOSITE)
            TaskGroup group = new TaskGroup(id, title, priority);

            // imposta la scadenza del gruppo
            group.setDeadline(deadline);
            
            // creazione e aggiunta dei figli
            int i = 1; // contatore utilizzato per differenziare gli ID dei task figli
            
            for (String subTitle : subTasks) {

                // ricontrolla che il testo del sotto-task sia valido prima di crearlo
                if (subTitle != null && !subTitle.trim().isEmpty()) {

                    // ID leggermente diverso per i figli per evitare collisioni (ID doppi) nel sistema.
                    // somma l'ID del padre, il contatore e un numero casuale da 0 a 1000.
                    int subId = id + i + (int)(Math.random() * 1000); 
                    
                    // crea il task figlio come attività singola (SingleTask)
                    SingleTask subTask = new SingleTask(subId, subTitle, priority);
                    
                    // i figli ereditano in automatico la scadenza del padre
                    subTask.setDeadline(deadline); 
                    
                    // aggiunge il figlio appena creato alla lista interna del gruppo padre
                    group.addActivity(subTask);

                    i++; // incrementa il contatore per il prossimo figlio
                }
            }

            // restituisce l'oggetto TaskGroup finito e già popolato.
            // essendo TaskGroup una sottoclasse di Activity, il tipo di ritorno è valido grazie al Polimorfismo.

            return group;
            
        } else {

            // È UNA TASK SINGOLA: l'utente NON ha inserito sotto-task
            // crea una semplice attività singola (SingleTask)
            SingleTask task = new SingleTask(id, title, priority);

            // ne imposta la data di scadenza
            task.setDeadline(deadline);

            // restituisce l'attività pronta per essere usata
            return task;
        }
    }
}
