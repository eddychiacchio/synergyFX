package com.synergy.model;

//PATTERN COMPOSITE: Questa classe rappresenta la "Foglia" (Leaf) del pattern.
//eredita dalla classe astratta base "Activity" e rappresenta un'attività semplice (singola),
//ovvero che non contiene altre sotto-attività.
public class SingleTask extends Activity {
	
	//id di serializzazione
    private static final long serialVersionUID = 1L;

    //metoto per ricevere i parametri da passare poi alla classe genitore
    public SingleTask(int id, String title, PriorityLevel priority) {
    	//super richiama il cotruttore Activity.java
        super(id, title, priority);
    }
    
}
