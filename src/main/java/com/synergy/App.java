package com.synergy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException; //classe per gestire gli errori di I/O

//La classe app eredita "Application" da JavaFX 
public class App extends Application {

    private static Scene scene; //variabile della scena dell'app

    @Override
    //metoto di JavaFX chiamato ll'avvio della UI
    public void start(Stage stage) throws IOException {
    	
    	//crea una nuova scena caricndo come file il "login.fxml"
        scene = new Scene(loadFXML("login"), 800, 600);
        
        stage.setScene(scene);// assegna la scena creata alla finestra principale "stage"
        stage.setTitle("Synergy Desktop"); // imposta il titolo sulla barra della finestra
        stage.show();// rende visibile la scena
    }
    
    //metodo per cambiare la scena corrente con una nuova
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    
    //metodo per caricare fisicamente i file di layout FXML
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
    	//lancia l'applicaizone per poi chiamare il metodo start()
        launch();
    }
}