package com.synergy.pattern.observer;

//PATTERN OBSERVER: Questa è l'interfaccia "Soggetto" (Subject).
//Definisce i metodi base che una classe (Project) deve avere per poter 
//essere "osservata" da altri oggetti (User).
public interface ISubject {
    void attach(IObserver o);
    void detach(IObserver o);
    void notifyObservers(String message);
}