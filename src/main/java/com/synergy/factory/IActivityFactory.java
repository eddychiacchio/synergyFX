package com.synergy.factory;

import com.synergy.model.Activity;
import com.synergy.model.PriorityLevel;
import java.time.LocalDate;

public interface IActivityFactory {
    // Definisce il contratto per la creazione, ma non l'implementazione
    Activity createActivity(String title, PriorityLevel priority, LocalDate deadline, String[] subTasks);
}