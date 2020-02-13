package Indy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This is the  main class to get things started. 
 * The main method of this application calls the App constructor.
 * Unlike most of the previous projects, the PaneOragnizer
 * class associates the stage object.
 * This became useful when creating popup windows for later work.
 */

public class App extends Application {

    public App() {
        // Constructor code goes here.
    }

    @Override
    public void start(Stage stage) throws Exception {
        /* TODO: EVERYTHING!!! */
    	PaneOrganizer organizer = new PaneOrganizer(stage);
    	Scene scene = new Scene(organizer.getRoot());
    	stage.setScene(scene);
    	stage.setTitle("Indy");
    	stage.setMaximized(true);
    	stage.show();
    }

    public static void main(String[] args) {
        launch(args); // launch is a method inherited from Application
    }
}
