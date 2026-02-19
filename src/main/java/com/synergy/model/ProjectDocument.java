package com.synergy.model;

import java.io.Serializable;
import java.time.LocalDate;

//classe che rappresenta il singolo documento all'intenro di un progetto
public class ProjectDocument implements Serializable {
	
	// identificatore di versione per la serializzazione (come in activity)
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;      // nome visualizzato (es. "Specifica.pdf")
    private String filename;  // nome reale sul disco (es. "17100023_Specifica.pdf")
    private String type;      // estensione (PDF, JPG...)
    private LocalDate uploadDate;

    //costruttore per ininzializzare
    public ProjectDocument(int id, String name, String filename, String type) {
        this.id = id;
        this.name = name;
        this.filename = filename;
        this.type = type;
        this.uploadDate = LocalDate.now();//imposta la data del caricamento
    }
    
    //getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getFilename() { return filename; }
    public String getType() { return type; }
    public LocalDate getUploadDate() { return uploadDate; }
}