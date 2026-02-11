package com.synergy.model;

import java.io.Serializable;
import java.time.LocalDate;

public class ProjectDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;      // Nome visualizzato (es. "Specifica.pdf")
    private String filename;  // Nome reale sul disco (es. "17100023_Specifica.pdf")
    private String type;      // Estensione (PDF, JPG...)
    private LocalDate uploadDate;

    public ProjectDocument(int id, String name, String filename, String type) {
        this.id = id;
        this.name = name;
        this.filename = filename;
        this.type = type;
        this.uploadDate = LocalDate.now();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFilename() { return filename; }
    public String getType() { return type; }
    public LocalDate getUploadDate() { return uploadDate; }
}