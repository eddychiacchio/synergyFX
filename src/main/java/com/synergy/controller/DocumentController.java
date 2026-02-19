package com.synergy.controller;

import com.synergy.model.*;
import com.synergy.util.DataManager;
//librerie per gestire file, input/output e percorsi sul computer
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

//classe responsabile della gestione del caricamento e cancellazione dei documenti
public class DocumentController {
    
    // cartella dove salvare i file (crea una cartella "synergy_uploads" nella directory)
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "synergy_uploads";

    //carica un file in un progetto specifico
    public void uploadFile(int projectId, File sourceFile) throws IOException {
        
    	DataManager dm = DataManager.getInstance();
        
        Project p = null;
        //cerca il progetto corrispondente all'id passato
        for(Project proj : dm.getProjects()) {
            if(proj.getId() == projectId) { p = proj; break; }
        }
        
        //se il progetto esiste
        if (p != null) {
            // 2. crea la cartella se non esiste
            File uploadDir = new File(UPLOAD_DIR);
            //crea la directory se non è ancora stata creata
            if (!uploadDir.exists()) uploadDir.mkdir();

            // 3. ottiene il nome del file caricato
            String originalName = sourceFile.getName();
            String extension = "";
            int i = originalName.lastIndexOf('.');//trova l'ultimo punto per estrapolare l'estensione del file
            if (i > 0) extension = originalName.substring(i+1).toUpperCase();// estrae l'estensione e la converte in maiuscolo
            
            // genera un nome univoco per evitare sovrascritture di file con lo stesso nome usando un UUID
            String savedFilename = java.util.UUID.randomUUID().toString() + "_" + originalName;
            File destFile = new File(UPLOAD_DIR + File.separator + savedFilename);//percorso finale dove il file viene salvato
            
            // 4. copia il file dalla posizione originale alla cartella dell'app
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 5. crea un ID pseudo-casuale e salva nel DB
            int newId = (int)(System.currentTimeMillis() & 0xfffffff);
            // crea un nuovo oggetto ProjectDocument che rappresenta il file appena salvato
            ProjectDocument doc = new ProjectDocument(newId, originalName, savedFilename, extension);
            
            p.getDocuments().add(doc);//aggiunge il documento alla lista dei documenti
            
            //notifica gli utenti
            p.notifyObservers("È stato caricato un nuovo documento: '" + originalName + "' nel progetto '" + p.getName() + "'.");
            
            dm.saveData();
        }
    }
    
    //metodo per cancella un documento da un progetto
    public void deleteDocument(int projectId, int docId) {
        DataManager dm = DataManager.getInstance();
        Project p = null;
        
        //cerco il progetto con l'id
        for(Project proj : dm.getProjects()) 
        	if(proj.getId() == projectId) p = proj;

        if (p != null) {
            ProjectDocument toRemove = null;
            //cerca il documento tramite id
            for(ProjectDocument d : p.getDocuments()) {
                if(d.getId() == docId) {
                    toRemove = d;
                    break;
                }
            }
            
            //se il docuemnto è stato trovato
            if(toRemove != null) {
            	//crea un riferimento al file fisico salvato sul disco
                File f = new File(UPLOAD_DIR + File.separator + toRemove.getFilename());
                if(f.exists()) f.delete();
                
                p.getDocuments().remove(toRemove);
                dm.saveData();
            }
        }
    }
    
 // Metodo aggiunto per recuperare il file fisico dal disco
    public File getDocumentFile(ProjectDocument doc) {
    	//restituisce l'oggetto File puntnato al percorso esatto in cui è salvato
        return new File(UPLOAD_DIR + File.separator + doc.getFilename());
    }
}