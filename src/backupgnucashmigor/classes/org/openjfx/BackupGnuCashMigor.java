/*
 * Copyright (C) 2019 Chris Good
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This project backs up GnuCash + Migor using 7-Zip.
 * Can be run for users: cgood or Gordon
 * Platform: Windows 7/10
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
