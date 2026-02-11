package com.synergy.model;

import java.io.Serializable;

public interface IObserver extends Serializable {
    void update(String message);
}