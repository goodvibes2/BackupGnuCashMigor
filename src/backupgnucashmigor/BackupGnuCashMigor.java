/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backupgnucashmigor;

//import java.io.File;
import javafx.application.Application;
//import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
//import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * This project backs up GnuCash + Migor using 7-Zip.
 * Can be run for users: cgood or Gordon
 * Platform: Windows 7
 * 
 * 18/05/2014 CRG Created
 * 25/05/2016 CRG Remove unused imports.
 * 
 * @author cgood
 */
public class BackupGnuCashMigor extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("BackupGnuCashMigor.fxml"));
        Scene scene = new Scene(root);
        
        stage.setTitle("Backup GnuCash/Migor");        
        stage.setScene(scene);
        
        scene.getStylesheets().add(BackupGnuCashMigor.class.getResource("BackupGnuCashMigor.css").toExternalForm());

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
