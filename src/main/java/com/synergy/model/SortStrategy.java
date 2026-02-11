package com.synergy.model;

import com.synergy.model.Activity;
import java.util.List;

public interface SortStrategy {
    // Il metodo che ogni strategia deve implementare
    void sort(List<Activity> activities);
}