package com.synergy.controller;

import com.synergy.model.*;
import com.synergy.util.DataManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DocumentController {
    
    // Cartella dove salvare i file
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "synergy_uploads";

    // --- MODIFICA: Accetta 'File' invece di 'Part' ---
    public void uploadFile(int projectId, File sourceFile) throws IOException {
        DataManager dm = DataManager.getInstance();
        
        // 1. Trova il progetto
        Project p = null;
        for(Project proj : dm.getProjects()) {
            if(proj.getId() == projectId) { p = proj; break; }
        }

        if (p != null) {
            // 2. Crea la cartella se non esiste
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdir();

            // 3. Prepara i nomi
            String originalName = sourceFile.getName();
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) extension = originalName.substring(i+1).toUpperCase();
            
            String savedFilename = System.currentTimeMillis() + "_" + originalName;
            File destFile = new File(UPLOAD_DIR + File.separator + savedFilename);
            
            // 4. Copia il file (Metodo Desktop)
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 5. Salva nel DB
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
            ProjectDocument toRemove = null;
            for(ProjectDocument d : p.getDocuments()) {
                if(d.getId() == docId) {
                    toRemove = d;
                    break;
                }
            }
            
            if(toRemove != null) {
                File f = new File(UPLOAD_DIR + File.separator + toRemove.getFilename());
                if(f.exists()) f.delete();
                
                p.getDocuments().remove(toRemove);
                dm.saveData();
            }
        }
    }
    
 // Metodo aggiunto per recuperare il file fisico dal disco
    public File getDocumentFile(ProjectDocument doc) {
        return new File(UPLOAD_DIR + File.separator + doc.getFilename());
    }
}