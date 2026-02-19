package com.synergy.strategy;

import java.util.List;

import com.synergy.model.Activity;

// PATTERN STRATEGY: interfaccia che definisce una strategia generica per ordinare le attività.
public interface ISortStrategy {
    
    // metodo astratto che riceve in input una lista di attività e la ordina.
    // le classi che implementeranno questa interfaccia scriveranno 
    // qui dentro il loro algoritmo specifico di ordinamento.
    void sort(List<Activity> activities);
}