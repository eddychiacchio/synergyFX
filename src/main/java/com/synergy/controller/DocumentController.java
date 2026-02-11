package com.synergy.controller;

import com.synergy.model.*;
import com.synergy.util.DataManager;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class DocumentController {
    
    // Cartella dove salvare i file (Nella tua cartella Utente/synergy_uploads)
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "synergy_uploads";

    public void uploadFile(int projectId, Part filePart) throws IOException {
        DataManager dm = DataManager.getInstance();
        
        // 1. Trova il progetto (Logica manuale come in ProjectController)
        Project p = null;
        for(Project proj : dm.getProjects()) {
            if(proj.getId() == projectId) { p = proj; break; }
        }

        if (p != null) {
            // 2. Crea la cartella se non esiste
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdir();

            // 3. Prepara il nome file univoco
            String originalName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) extension = originalName.substring(i+1).toUpperCase();
            
            // Nome salvato su disco: TIMESTAMP + NomeOriginale (per evitare sovrascritture)
            String savedFilename = System.currentTimeMillis() + "_" + originalName;
            
            // 4. Salva il file fisicamente
            filePart.write(UPLOAD_DIR + File.separator + savedFilename);

            // 5. Crea l'oggetto nel DB
            int newId = (int)(System.currentTimeMillis() & 0xfffffff);
            ProjectDocument doc = new ProjectDocument(newId, originalName, savedFilename, extension);
            
            p.getDocuments().add(doc);
            dm.saveData();
        }
    }
    
    public void deleteDocument(int projectId, int docId) {
        DataManager dm = DataManager.getInstance();
        Project p = null;
        for(Project proj : dm.getProjects()) if(proj.getId() == projectId) p = proj;

        if (p != null) {
            // Trova e rimuovi
            ProjectDocument toRemove = null;
            for(ProjectDocument d : p.getDocuments()) {
                if(d.getId() == docId) {
                    toRemove = d;
                    break;
                }
            }
            
            if(toRemove != null) {
                // Cancella file fisico
                File f = new File(UPLOAD_DIR + File.separator + toRemove.getFilename());
                if(f.exists()) f.delete();
                
                // Rimuovi da lista e salva
                p.getDocuments().remove(toRemove);
                dm.saveData();
            }
        }
    }
    
    // Helper per il download
    public String getUploadPath() {
        return UPLOAD_DIR;
    }
}