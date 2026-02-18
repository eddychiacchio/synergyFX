package com.synergy.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortByDeadline implements ISortStrategy {
    @Override
    public void sort(List<Activity> activities) {
        Collections.sort(activities, new Comparator<Activity>() {
            @Override
            public int compare(Activity a1, Activity a2) {
                // Gestione dei null (chi non ha scadenza va alla fine)
                if (a1.getDeadline() == null && a2.getDeadline() == null) return 0;
                if (a1.getDeadline() == null) return 1;  // a1 dopo
                if (a2.getDeadline() == null) return -1; // a2 dopo
                
                // Ordine crescente (prima le scadenze vicine)
                return a1.getDeadline().compareTo(a2.getDeadline());
            }
        });
    }
}