package com.synergy.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortByPriority implements ISortStrategy {
    @Override
    public void sort(List<Activity> activities) {
        Collections.sort(activities, new Comparator<Activity>() {
            @Override
            public int compare(Activity a1, Activity a2) {
                // Confrontiamo le Enum.
                // Supponendo che l'Enum sia {BASSA, MEDIA, ALTA}, 
                // l'ordine naturale è 0, 1, 2.
                // Noi vogliamo decrescente (ALTA prima), quindi usiamo a2.compareTo(a1)
                return a2.getPriority().compareTo(a1.getPriority());
            }
        });
    }
}