package com.synergy.strategy;

//importazione per gestire l'ordinamento delle liste
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.synergy.model.Activity;

// PATTERN STRATEGY: Questa è una delle strategie concrete di ordinamento.
// implementa l'interfaccia ISortStrategy per ordinare le attività in base alla PRIORITÀ.
public class SortByPriority implements ISortStrategy {

    // sovrascrive il metodo definito nell'interfaccia
    @Override
    public void sort(List<Activity> activities) {

        // usa il metodo di utilità di Java per ordinare la lista, passandogli un "Comparatore" personalizzato
        Collections.sort(activities, new Comparator<Activity>() {

            // definisce la logica di confronto tra due attività (a1 e a2)
            @Override
            public int compare(Activity a1, Activity a2) {
                
                // confrontiamo le Enum (livelli di priorità: BASSA, MEDIA, ALTA).
                // supponendo che l'Enum sia definito in ordine {BASSA, MEDIA, ALTA}, 
                // il loro ordine numerico "naturale" in Java è 0, 1, 2.
                // siccome vogliamo un ordine DECRESCENTE (ALTA per prima, in cima alla lista), 
                // facciamo il confronto al contrario: usiamo a2.compareTo(a1) invece di a1.compareTo(a2).
                return a2.getPriority().compareTo(a1.getPriority());

            }
        });
    }
}