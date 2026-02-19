package com.synergy;

//classe Launcher creata per forzare l'avvio del programma che non partiva dall'App.java
public class Launcher {
	//questo è il metodo effettivo che fa partire l'app
    public static void main(String[] args) {
        App.main(args); //richiama il main di App.java
    }
}