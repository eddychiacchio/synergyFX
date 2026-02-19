package com.synergy.factory;

import com.synergy.model.Activity;
import com.synergy.model.PriorityLevel;
import java.time.LocalDate;// importa la classe per gestire le date

// PATTERN FACTORY: interfaccia che definisce il "Creatore" astratto.
// specifica il "contratto" che qualsiasi fabbrica di attività dovrà rispettare,
// ovvero avere un metodo per creare un'attività fornendo i parametri base.
public interface IActivityFactory {

    // Metodo astratto che riceve titolo, priorità, data di scadenza e un array di sotto-task,
    // e restituisce un oggetto generico di tipo Activity (che potrà essere un task singolo o un gruppo)
    Activity createActivity(String title, PriorityLevel priority, LocalDate deadline, String[] subTasks);
}
