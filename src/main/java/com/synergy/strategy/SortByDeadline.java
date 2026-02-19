package com.synergy.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.synergy.model.Activity;

//PATTERN STRATEGY 
//questa ordina le attività in base alla data di scadenza (Deadline)
public class SortByDeadline implements ISortStrategy {
	
    @Override
    public void sort(List<Activity> activities) {
    	//oridina la lista utilizzando un ordinatore personalizzato
        Collections.sort(activities, new Comparator<Activity>() {
            @Override
            public int compare(Activity a1, Activity a2) {
                // gestione dei null per chi non ha scadenza va alla fine
            	//se entrambe non hanno dds sono considerate uguali
                if (a1.getDeadline() == null && a2.getDeadline() == null) return 0;
                if (a1.getDeadline() == null) return 1;  // a1 dopo
                if (a2.getDeadline() == null) return -1; // a2 dopo
                
                // ordine crescente, la data più vicina compare prima di quella più lontana
                return a1.getDeadline().compareTo(a2.getDeadline());
            }
        });
    }
}