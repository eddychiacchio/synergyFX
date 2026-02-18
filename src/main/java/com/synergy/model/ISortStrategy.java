package com.synergy.model;

import java.util.List;

public interface ISortStrategy {
    // Il metodo che ogni strategia deve implementare
    void sort(List<Activity> activities);
}