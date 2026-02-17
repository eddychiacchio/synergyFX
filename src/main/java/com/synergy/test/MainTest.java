package com.synergy.test;

import com.synergy.model.User;
import com.synergy.util.DataManager;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("--- INIZIO TEST SYNERGY ---");
        
        DataManager dm = DataManager.getInstance();
        
        // Creo un nuovo utente
        User u = new User(1, "Pasquale", "test@synergy.com", "12345");
        dm.getUsers().add(u);
        
        // Salvo
        dm.saveData();
        
        System.out.println("--- FINE TEST ---");
        System.out.println("Fai Refresh sul progetto (F5) e cerca il file .ser");
    }
}