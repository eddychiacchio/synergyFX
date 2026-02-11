package com.synergy.model;

public interface ISubject {
    void attach(IObserver o);
    void detach(IObserver o);
    void notifyObservers(String message);
}