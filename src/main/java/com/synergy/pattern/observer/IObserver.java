package com.synergy.pattern.observer;
import java.io.Serializable;

// PATTERN OBSERVER: Definisce l'interfaccia "IObserver" (Osservatore).
// Estende Serializable per garantire
public interface IObserver extends Serializable {
    
    // metodo che tutti gli "osservatori" (es. User) devono obbligatoriamente implementare.
    // viene chiamato dal "Soggetto" (es. il Progetto) quando c'è una novità da notificare.
    void update(String message);
}