/*
 * Copyright (C) 2019 goodc
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

/* 31/08/2014 1.01 1. Fix bug in getUserDefaults() where it was trying to get
                      wrong property for Gordon.
                   2. Fix bug using cgood Migor front end for Gordon.
                   3. Show msg when settings are saved OK.
   30/11/2014 1.02 Add hhmm to archive filename.
   07/12/2014 1.03 1. MigorData.mdb -> MigorData.accdb.
                   2. Chk MigorData.accdb exists.
   25/12/2014 1.04 Use HH (00-23) instead of hh (01-12) for hour in archive
                   filename.
   10/01/2015 1.05 1. Make chbShowPswd wider so text is not truncated when 
                    Windows default text size is increased to Medium 
                    (fxml mod only).
                   2. Show visible password in same place as masked pswd.
                   3. Give error if Access or GnuCash lock files exist.
   11/01/2015 1.06 Run enable_or_disable_buttons when chbShowPswd changes.
   12/07/2015 1.07 1. Fix bug using lblGCModDate to show exception instead of 
                    lblMigorModDate.
                   2. Refresh modified dates after choosing a file.
                   3. handleBtnActionChooseMigor() : 
                        Make default Access extensions both *.accdb + *.mdb
                        and allow to select both or either.
                   4. Change error msg when Access file is not readable to
                        advise that it may be not readable or locked.
                   5. Add new field for the option GnuCash version file name 
                        suffix.
                   6. Force winUserName to lowercase as ausyncgood = CGOOD,
                                                        goodi7     = cgood
                   7. If FileChooser InitialDirectory directory does not exist,
                        use user.home instead to avoid error:
              IllegalArgumentException: Folder parameter must be a valid folder.
    19/07/2015 1.08 Set focus to password if all else OK.
                        Use runLater so initial requestFocus works.
    16/05/2016 1.09 Fix FileName.filename index out of bounds when passing a
                    file name without an extension.
    21/07/2016 1.10 Fix use of & instead of && (didn't actually matter).
    27/04/2018 1.11 User Gordon -> gordo for new PC GordonPc2.
    22/05/2018 1.2  Mods for GnuCash V3:
                      Backup %APPDATA%\gnucash if exists
                      Export Registry HKCU\Software\GSettings\org\gnucash
                        to %APPDATA%\GnuCashGSettings.reg and back it up.
    28/5/2018  1.21 Move %APPDATA%\GnuCashGSettings.reg to $HOME\.BupGcM\GnuCashGSettings.reg
    30/05/2018 1.22 1. Move V2 + V3 Cfg chkboxes
                    2. Do enable_or_disable_buttons on change of chbGcV[23]Cfg.isSelected
    10/01/2019 1.23 User cgood -> goodc for goodi7gl12cm
    10/02/2019 1.24 V3: look for saved-reports-2.8 before saved-reports-2.4
    16/02/2019 1.25 V3: look for saved-reports-2.4 even if V2 is selected as V2 & V3
                    put saved-reports-2.n in different places.
    13/08/2019 2.0  Port project from java 8 to a Modular Java 11 project.
    11/03/2020 2.01 Fix bug where a deleted Book was not also removed from defaultProps so
                    would reappear next time defaultProperties file was loaded.
*/

package org.openjfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
//import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
/* import java.time.LocalDate; */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 *
 * @author cgood
 */
public class BackupGnuCashMigorController implements Initializable {
    
    /* class variables */
    
    @FXML
    private GridPane grid;
    @FXML
    private Text sceneTitle;
    @FXML
    private Text versionNo;
    @FXML
    private Label lblUser;
    @FXML
    private RadioButton rb1;
    @FXML
    private RadioButton rb2;
    @FXML
    private Button btnSaveSettings;
    @FXML
    private Label lblGCDatFilStr;
    @FXML
    private TextField txtGcDatFilStr;
    @FXML
    private Button btnChooseGCDatFil;
    @FXML
    private Label lblGCModDate;
    @FXML
    private Label lblGCVer;
    @FXML
    private TextField txtGcVer;
    @FXML
    private Label lblGcCfg;
    @FXML
    private CheckBox chbGcV2Cfg;
    @FXML
    private CheckBox chbGcV3Cfg;
    @FXML
    private Label lblMigorFeFilStr;
    @FXML
    private TextField txtMigorFeFilStr;
    @FXML
    private Button btnChooseMigorFeFil;
    @FXML
    private Label lblMigorModDate;
    @FXML
    private Label lblDropBox;
    @FXML
    private TextField txtDropBox;
    @FXML
    private Button btnChooseDropBox;
    @FXML
    private Label lblPswd;
    @FXML
    private PasswordField txtPswd;
    @FXML
    private TextField txtVisPswd;
    @FXML
    private CheckBox chbShowPswd;
    @FXML
    private Separator sep1;
    @FXML
    private Separator sep2;
    @FXML
    private Separator sep3;
    @FXML
    private Separator sep4;
    @FXML
  //private Label lblShowPswd;
  //@FXML
    private Button btnBupGC;
    @FXML
    private Button btnBupMigor;
    @FXML
    private Label lblLog;
    @FXML
    private TextArea taLog;
        
    final private static String winUserName = System.getenv("USERNAME").toLowerCase();
    // character that separates folders from files in paths
    //  i.e. Windows = backslash, Linux/OSX = /
    private static final char FILE_SEPARATOR =
        System.getProperty("file.separator").charAt(0);

    // userDefaults

    /* Common to Chris Good + Gordon McFarlane */
    private static String gcV2Cfg = "true"; // Backup GnuCash V2 config files ?
    private static String gcV3Cfg = "true"; // Backup GnuCash V3 config files ?

    /* Chris Good */
    final private static String userCg = "goodc";

    private static String gcDatFilCg = 
        "E:\\Data\\GnuCash\\267\\MGCG2012\\MGCG2012.gnucash";
    private static String gcVerCg = ""; // optional version backup filename suffix
    private static String migorDatFilCg = 
        "E:\\Data\\Access\\Access2010\\MGCG\\Migor2014V05.accdb";
    private static String dropBoxCg = 
        "E:\\Data\\Dropbox";
    
    /* Gordon McFarlane */
  //final private static String userGm = "Gordon";  // OLD
  //final private static String userGm = "gordon";  // OLD
    final private static String userGm = "gordo";
    private static String gcDatFilGm = 
        "C:\\Users\\" + userGm + "\\My Documents\\GnuCash\\2.6.7\\BBGJ2012\\BBGJ2012.gnucash";
    private static String gcVerGm = "";
    private static String migorDatFilGm = 
        "C:\\Users\\" + userGm + "\\My Documents\\Migor\\Migor2014V05.accdb";
    private static String dropBoxGm = 
        "C:\\Users\\gordo\\Dropbox";
    
    private static Path pathGcDatFilStr;
    private static Path  pathMigorFeFilStr;
  //private static String strDropBoxDir;
  //private static final String strHome = System.getProperty("user.home");
    private static       String strHome = System.getProperty("user.home");
    private static final String PROPERTIES_DIR = ".BupGcM";
  //private static final String strErrFil = strHome + "\\" + PROPERTIES_DIR + "\\BackGnuCashMigor.err";
    private static       String strErrFil = strHome + "\\" + PROPERTIES_DIR + "\\BackGnuCashMigor.err";
  //private static final String strOutFil = strHome + "\\" + PROPERTIES_DIR + "\\BackGnuCashMigor.out";
    private static       String strOutFil = strHome + "\\" + PROPERTIES_DIR + "\\BackGnuCashMigor.out";
    private static final String OUT_REG_FILE = System.getenv("HOME")
            + FILE_SEPARATOR + PROPERTIES_DIR + FILE_SEPARATOR + "/gnucash.dconf";

    //  default properties
    private static final Properties defaultProps = new Properties();
        
    final private static ToggleGroup userGroup = new ToggleGroup();
    
    private static boolean firstTime = true;
    
    @FXML
    public void handleBtnActionSaveSettings(Event e) throws IOException {
        defaultProps.setProperty("dropBox",     txtDropBox.getText());
        defaultProps.setProperty("gcDatFil",    txtGcDatFilStr.getText());
        //if (! txtGcVer.getText().isEmpty()) {
            defaultProps.setProperty("gcVer",   txtGcVer.getText());
        //}
        defaultProps.setProperty("gcV2Cfg", String.valueOf(chbGcV2Cfg.isSelected()));
        defaultProps.setProperty("gcV3Cfg", String.valueOf(chbGcV3Cfg.isSelected()));
        defaultProps.setProperty("migorDatFil", txtMigorFeFilStr.getText());
        defaultProps.setProperty("migorDatFil", txtMigorFeFilStr.getText());
        
        try (FileOutputStream out = new FileOutputStream(Paths.get(strErrFil).getParent() +
                "\\defaultProperties")) {
            defaultProps.store(out, "---Backup GnuCash/Migor Settings---");
            taLog.setText("Settings successfully saved");
        } catch (IOException ex) {
            //System.out.println("My Exception Message " + ex.getMessage());
            //System.out.println("My Exception Class " + ex.getClass());            
            Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, ex);
            taLog.setText("Error: Cannot Save Settings to : " + Paths.get(strErrFil).getParent() +
                "\\defaultProperties");
        }
    }
    
    public static void getUserDefaults() {
        
        try (   // with resources
            FileInputStream in = new FileInputStream(Paths.get(strErrFil).getParent() +
                "\\defaultProperties");
        )
        {
            defaultProps.load(in);
            gcV2Cfg = defaultProps.getProperty("gcV2Cfg");
            gcV3Cfg = defaultProps.getProperty("gcV3Cfg");

//          if (getWinUserName().equals(getUserCg())) {
            if (winUserName.equals(getUserCg())) {
                gcDatFilCg = defaultProps.getProperty("gcDatFil");
                gcVerCg = defaultProps.getProperty("gcVer");
                migorDatFilCg = defaultProps.getProperty("migorDatFil");
                dropBoxCg = defaultProps.getProperty("dropBox");
            } else {
                gcDatFilGm = defaultProps.getProperty("gcDatFil");
                gcVerGm = defaultProps.getProperty("gcVer");
                migorDatFilGm = defaultProps.getProperty("migorDatFil");
                dropBoxGm = defaultProps.getProperty("dropBox");
            }
            //in.close();  // done automatically when 'try with resources' ends            
        } catch (IOException ex) {
            //System.out.println("My Exception Message " + ex.getMessage());
            //System.out.println("My Exception Class " + ex.getClass());
            
            if (ex.getClass().toString().equals("class java.io.FileNotFoundException")) {
                System.out.println("getUserDefaults: " + ex.getMessage());
            } else {
                Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, ex);
            }    
        }
    }

//    public static String getWinUserName() {
//        return winUserName;
//    }
    
    public static String getUserCg() {
        return userCg;
    }
    
    public static String getUserGm() {
        return userGm;
    }
    
    public static String getGcDatFil(String strInUser) {
        if (strInUser.equals(userCg)) {
            return gcDatFilCg;
        } else {
            return gcDatFilGm ;
        }
    }

    public static String getGcVer(String strInUser) {
        if (strInUser.equals(userCg)) {
            return gcVerCg;
        } else {
            return gcVerGm ;
        }
    }

    public static String getMigorDatFil(String strInUser) {
        if (strInUser.equals(userCg)) {
            return migorDatFilCg;
        } else {
            return migorDatFilGm;
        }
    }

/*    public static void setDropBoxDir(String strInUser) {
        if (strInUser.equals(userCg)) {
            strDropBoxDir = dropBoxCg;
        } else {
            strDropBoxDir = dropBoxGm;
        }
    }
*/
    public static String getDropBoxDir(String strInUser) {
        if (strInUser.equals(userCg)) {
            return dropBoxCg;
        } else {
            return dropBoxGm;
        }
    }

    /**
     * Check logged in user is either goodc or gordo
     * @return boolean
     */
    static boolean isValidUser() {
        switch (winUserName) {
            case userCg : ;
            case userGm :
                return true;
            default:
                return false;
        }
    }

    boolean isValidPswd() {
        return txtPswd.getText().length() > 7 ;
    }
    
    private static boolean isFirstTime() {
        return firstTime;
    }
    
/*    void logText(String strText) {
        taLog.appendText(strText);
    }
*/    

    boolean exportRegistry() {
        
        // Use reg.exe to export GnuCash registry entries to a text file which can be backed up

        int exitVal = 0;
        String[] cmdExport = new String[5];

        // chk C:\Windows\System32\reg.exe exists
        //  %SystemRoot% is usually C:\Windows
        Path pathRegExe = Paths.get(System.getenv("SystemRoot") + "\\System32\\reg.exe");
        if (! Files.isExecutable(pathRegExe)) {
            taLog.appendText("Error: Cannot find or execute " +
                pathRegExe.toString() + " on either C: or E:" );
            return false;
        }

        Runtime rt = Runtime.getRuntime();
        System.out.println("Execing reg.exe");
        taLog.appendText("Exporting GnuCash registry entries...\n");

        // Set up cmdExport to be like
        //  C:\Windows\System32\reg.exe
        //    EXPORT HKCU\Software\GSettings\org\gnucash %APPDATA%\GnuCashGSettings.reg /y
        cmdExport[0] = pathRegExe.toString();
        cmdExport[1] = "EXPORT";
        cmdExport[2] = "HKCU\\Software\\GSettings\\org\\gnucash";
        cmdExport[3] = strHome + FILE_SEPARATOR + PROPERTIES_DIR + FILE_SEPARATOR + "GnuCashGSettings.reg";
        cmdExport[4] = "/y";    // Force overwriting an existing output file without prompt

        try {
            Process proc = rt.exec(cmdExport);

            // make sure output is consumed so system buffers do not fill up
            // and cause the process to hang

            /*  Because any updates to the JavaFX gui must be done from the JavaFX
                application thread, it is not possible to update taLog from
                StreamGobbler, so I use StreamGobbler to put stdout &
                stderr to files, and just copy the contents of the files to taLog
                when the 7zip'ing finishes.
             */

            try (   // with resources
                FileOutputStream fosErr = new FileOutputStream(strErrFil);
                FileOutputStream fosOut = new FileOutputStream(strOutFil)
            )
            {
                // any error messages (stderr) ?
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", fosErr);
                // any output?
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fosOut);
                // kick them off - creates new threads
                errorGobbler.start();
                outputGobbler.start();
                // any error?
                exitVal = proc.waitFor();
                System.out.println("ExitValue: " + exitVal);
                taLog.appendText("reg.exe ExitValue: " + exitVal + "\n");
                fosErr.flush();
                fosOut.flush();
            }
            //fosErr.close();  // done automatically when try with resources ends
            //fosOut.close();  // done automatically when try with resources ends
        } catch (Throwable t)
        {
            taLog.appendText("reg.exe FAILED - StackTrace Logged");
            if (exitVal == 0) {
                exitVal = 99;
            }
            // NetBeans 11 suggests stack traces should be logged, not shown to users
            //t.printStackTrace();
            Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, t);
        }

        // add stderr of reg.exe process to taLog
        Path pthErrFil = Paths.get(strErrFil);
        try (InputStream in = Files.newInputStream(pthErrFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (!line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthErrFil);
        }
        // add stdout of 7-zip process to taLog
        Path pthOutFil = Paths.get(strOutFil);
        try (InputStream in = Files.newInputStream(pthOutFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))) {
//            String line = null;
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (! line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthOutFil);
        }

        if (exitVal != 0) {
            taLog.appendText("reg.exe proc.waitFor() returned: " + exitVal + "\n");
            return false;
        }

        return true;
    }

    void setTooltips() {

        // Create Tooltips (mouse-over text)
        btnSaveSettings.setTooltip(new Tooltip(
            "Save Settings:\nSave current settings in\n" +
            strHome + "\\" + PROPERTIES_DIR + "\\defaultProperties\n" +
            "The password is NOT saved."
        ));
        
        txtGcDatFilStr.setTooltip(new Tooltip(
            "GnuCash data file:\n" +
            "The full directory and file string of the GnuCash data file.\n"
        ));
        
        txtGcVer.setTooltip(new Tooltip(
            "GnuCash Version:\nOptional suffix added to GnuCash backup file name.\n"
        ));

        chbGcV2Cfg.setTooltip(new Tooltip(
            "Backup GnuCash V2 configuration files?\n" +
            "This includes saved report options\n" +
            " E.g. C:\\Users\\[USERNAME]\\.gnucash\\saved-reports-2.4\n" +
            "and metadata\n" +
            " E.g. C:\\Users\\[USERNAME]\\.gnucash\\books\\[BookName].gnucash.gcm\n"
        ));

        chbGcV3Cfg.setTooltip(new Tooltip(
            "Backup GnuCash V3 configuration files & Registry entries?\n" +
            "This backs up the user configuration directory and sub-folders\n" +
            " E.g. C:\\Users\\[USERNAME]\\AppData\\Gnucash\n" +
            "This includes amoung other things\n" +
            " the saved report options\n" +
            "  E.g. C:\\Users\\[USERNAME]\\AppData\\Gnucash\\saved-reports-2.8\n" +
            " and metadata\n" +
            "  E.g. C:\\Users\\[USERNAME]\\AppData\\GnuCash\\books\\[BookName].gnucash.gcm\n" +
            " and the contents of the registry key\n" +
            "  HKCU\\Software\\GSettings\\org\\gnucash"
        ));

        txtMigorFeFilStr.setTooltip(new Tooltip(
            "Migor Front End:\n" +
            "The full directory and file string of the Migor Front End database.\n"
        ));
        
        txtDropBox.setTooltip(new Tooltip(
            "DropBox Base Directory:\n" +
            "The encrypted compressed backup file will be saved in a sub-directory\n" +
            "of this directory.\n" +
            "The GnuCash backup file will be saved in a sub-directory called 'GnuCash'.\n" +
            "The Migor backup file will be saved in a sub-directory called 'Migor'."
        ));
        
        // Tooltips (same) for txtPswd & txtVisPswd
        final String strTooltipPswd =
            "Password:\n" +
            "The archived files will be compressed and encrypted with this password using 7-Zip.\n" +
            "Minimum length is 8 characters.";
        txtPswd.setTooltip(new Tooltip(strTooltipPswd));
        
        txtVisPswd.setTooltip(new Tooltip(strTooltipPswd));
        
        btnBupGC.setTooltip(new Tooltip(
            "Backup GnuCash:\n" +
            "The data file will be archived (compressed and encrypted) to the 'Backup' sub-directory\n" +
            "of the data file directory. The data file itself remains unaltered.\n" +
            "Then the archive will be copied to the 'GnuCash' sub-directory of the DropBox\n" +
            "directory.\n" +
            "Optionally, GnuCash V2 and/or V3 configuration will also be included.\n" +
            "See the Tooltips of those checkboxes for details.\n"
        ));

        btnBupMigor.setTooltip(new Tooltip(
            "Backup Migor:\n" +
            "The Migor Front End database, and also the MigorData database in the same directory,\n" +
            "will be archived (compressed and encrypted) to the 'Backup' sub-directory\n" +
            "of the database directory. The database files themself remain unaltered.\n" +
            "Then the archive will be copied to the 'Migor' sub-directory of the DropBox\n" +
            "directory.\n" +
            "The text files used when getting stock prices from the internet (GetQuote.*)\n" +
            "are also included in the archive.\n"
        ));

    }
    
    void enable_or_disable_buttons() {
        //System.out.println("enable_or_disable_buttons");
        boolean boolUserOk = false;
        boolean boolPswdOk = false;
        boolean boolGcOk = false;
        boolean boolMigorOk = false;
        boolean boolDropBoxOk = false;
        
        taLog.clear();
        
        // Note:    Disable  property : Defines the individual disabled state of this Node.
        //                              'Disable' may be false and 'Disabled true' if a parent node is Disabled.
        //          Disabled propery  : Indicates whether or not this Node is disabled. Could be 'Disabled' because parent node is disabled.
        
        // Test: isDisabled(), Set: setDisable() NOT setDisabled()
        
        // 18/07/2015 Do NOT enable or disable until it is determined this needs 
        //              happen to see if this is causing unwanted Focus change ???
        
/*      btnBupGC.setDisable(true);          //Disable
        btnBupMigor.setDisable(true);
        btnSaveSettings.setDisable(true);
*/
        //System.out.println("btnSaveSettings Disabled");
                
        if ((isValidUser() == true)) {
            boolUserOk = true;
        } else {
          //taLog.setText("Error: Invalid user: " + getWinUserName() + "\n");
            taLog.setText("Error: Invalid user: " + winUserName + "\n");
        }
        
        if (isValidPswd()) {
            boolPswdOk = true;
        } else {
            taLog.appendText("Please enter Password - minimum length is 8 characters\n");
        }
        
        pathGcDatFilStr = Paths.get(txtGcDatFilStr.getText());
        if (Files.isReadable(pathGcDatFilStr)) {
            // show the Last Modified date/time
            //  FileTime epoch 1970-01-01T00:00:00Z (FileTime no longer used)
            try {
               SimpleDateFormat sdfFormat = new SimpleDateFormat("EEE, dd/MM/yyyy hh:mm aa");
               //long lngGCMod = Files.getLastModifiedTime(pathGcDatFilStr).toMillis();
               //lblGCModDate.setText("Modified : " + sdfFormat.format(lngGCMod));     //OK

               // following line does same as both commented out lines above
               lblGCModDate.setText("Modified : " + sdfFormat.format(Files.getLastModifiedTime(pathGcDatFilStr).toMillis()));
            } catch (IOException x) {
                System.err.println(x);
                lblGCModDate.setText("IOException getLastModifiedTime" + pathGcDatFilStr.toString());
            }
            // chk GnuCash file is not open (ensure lockfile does NOT exist)
            // i.e If data file is MGCG2012.gnucash
            //      ensure MGCG2012.gnucash.LCK does NOT exist in same folder

            Path pathGcLckFilStr = Paths.get(txtGcDatFilStr.getText() + ".LCK");
            if (Files.isReadable(pathGcLckFilStr)) {
                taLog.appendText("Error: GnuCash lock file " +
                    Paths.get(txtGcDatFilStr.getText() + ".LCK") +
                    " exists - GnuCash may be open or may have crashed leaving the lockfile\n");
            } else {
                boolGcOk = true;
                Path pathGcGcm;
                Path pathGcSavRpt;
                FileName fileName = new FileName(txtGcDatFilStr.getText(),
                            System.getProperty("file.separator").charAt(0), '.');
                if (chbGcV2Cfg.isSelected()) {
                    // chk C:\Users\goodc\.gnucash\saved-reports-2.4 exists - GnuCash V2.4 to 2.6
                    pathGcSavRpt = Paths.get(strHome + "\\.gnucash\\saved-reports-2.4");
                    if (Files.isReadable(pathGcSavRpt)) {
                        taLog.appendText("Info: Found GnuCash 2 " + pathGcSavRpt.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 2 " + pathGcSavRpt.toString() +
                            " is not readable or does not exist\n");
                    }
                    // chk C:\Users\goodc\.gnucash\books\MGCG2012.gnucash.gcm exists
                    //  GnuCash V#2.4 to 2.6
                    pathGcGcm = Paths.get(strHome + "\\.gnucash\\books\\" +
                        fileName.filename() + "." + fileName.extension()+ ".gcm");
                    if (Files.isReadable(pathGcGcm)) {
                        taLog.appendText("Info: Found GnuCash 2 Configuration file " +
                            pathGcGcm.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 2 Configuration file " +
                            pathGcGcm.toString() + " is not readable or does not exist\n");
                    }
                }

                if (chbGcV3Cfg.isSelected()) {
                    // GnuCash V3
                    // uses saved-reports-2.4 if no saved-reports-2.8 exists
                    //  but only writes to saved-reports-2.8
                    
                    // chk %APPDATA%\GnuCash\saved-reports-2.8 exists
                    pathGcSavRpt = Paths.get(System.getenv("APPDATA") +
                        "\\GnuCash\\saved-reports-2.8");
                    if (Files.isReadable(pathGcSavRpt)) {
                        taLog.appendText("Info: Found GnuCash 3 " + pathGcSavRpt.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 3 " + pathGcSavRpt.toString() +
                            " is not readable or does not exist\n");
                        // chk %APPDATA%\GnuCash\saved-reports-2.4 exists
                        pathGcSavRpt = Paths.get(System.getenv("APPDATA") +
                            "\\GnuCash\\saved-reports-2.4");
                        if (Files.isReadable(pathGcSavRpt)) {
                            taLog.appendText("Info: Found GnuCash 3 " + pathGcSavRpt.toString() + "\n");
                        } else {
                            taLog.appendText("Info: GnuCash 3 " + pathGcSavRpt.toString() +
                                " is not readable or does not exist\n");
                        }
                    }
                    
                    // chk %APPDATA%\GnuCash\books\[BOOK].gnucash.gcm exists
                    //   Note %APPDATA% is usually C:\Users\%USERNAME%\AppData\Roaming
                    //   GnuCash V#2.7+
                    pathGcGcm = Paths.get(System.getenv("APPDATA") + "\\GnuCash\\books\\" +
                        fileName.filename() + "." + fileName.extension()+ ".gcm");
                    if (Files.isReadable(pathGcGcm)) {
                        taLog.appendText("Info: Found GnuCash 3 Configuration file " +
                            pathGcGcm.toString() + "\n");
                    } else {
                        taLog.appendText("Info: GnuCash 3 Configuration file " +
                            pathGcGcm.toString() + " is not readable or does not exist\n");
                    }
                }
            }
        } else {                
            taLog.appendText("Error: GnuCash data is not readable or does not exist\n");
        }

        // Check Migor front end file
        //  Note: If MS Access 2013 has the database file open, it seems to have it locked
        //      which causes Files.isReadable to fail
        
        pathMigorFeFilStr = Paths.get(txtMigorFeFilStr.getText());
        if (Files.isReadable(pathMigorFeFilStr)) {
            String strMigorData;
            if (txtMigorFeFilStr.getText().endsWith(".accdb")) {
                strMigorData = "MigorData.accdb";
            } else {
                strMigorData = "MigorData.mdb";
            }
            Path pathMigorDatFilStr = Paths.get(pathMigorFeFilStr.getParent().toString(), "\\" + strMigorData);
            if (Files.isReadable(pathMigorDatFilStr)) {
                // show the front end Last Modified date/time
                //  FileTime epoch 1970-01-01T00:00:00Z (FileTime no longer used)
                try {
                   SimpleDateFormat sdfFormat = new SimpleDateFormat("EEE, dd/MM/yyyy hh:mm aa");
                   lblMigorModDate.setText("Modified : " + sdfFormat.format(Files.getLastModifiedTime(pathMigorFeFilStr).toMillis()));
                } catch (IOException x) {
                    System.err.println(x);
                    lblMigorModDate.setText("IOException getLastModifiedTime" + pathMigorFeFilStr.toString());
                }

                // chk Migor Access front end file is not open (ensure lockfile does NOT exist)
                // i.e If Access front end file is Migor2014.accdb
                //          ensure Migor2014.laccdb does NOT exist in same folder
                //     If Access front end file is Migor2014.mdb
                //          ensure Migor2014.ldb does NOT exist in same folder
                Path pathMigorLckFilStr;
                Path pathMigorDatLckFilStr;
                if (txtMigorFeFilStr.getText().endsWith(".accdb")) {
                    pathMigorLckFilStr = Paths.get(txtMigorFeFilStr.getText().replace(".accdb", ".laccdb"));
                    pathMigorDatLckFilStr = Paths.get(pathMigorFeFilStr.getParent().toString(), "\\" + strMigorData.replace(".accdb", ".laccdb"));
                } else {
                    pathMigorLckFilStr = Paths.get(txtMigorFeFilStr.getText().replace(".mdb", ".ldb"));
                    pathMigorDatLckFilStr = Paths.get(pathMigorFeFilStr.getParent().toString(), "\\" + strMigorData.replace(".mdb", ".ldb"));
                }
                if (Files.isReadable(pathMigorLckFilStr)) {
                    taLog.appendText("Error: Migor lock file " + pathMigorLckFilStr.toString() +
                        " exists - Migor may be open or may have crashed leaving the lockfile\n");
                } else {
                    if (Files.isReadable(pathMigorDatLckFilStr)) {
                        taLog.appendText("Error: MigorData lock file " + pathMigorDatLckFilStr.toString() +
                            " exists - Migor may be open or may have crashed leaving the lockfile\n");                        
                    } else {
                            boolMigorOk = true;
                    }
                }
            } else {                    
                taLog.appendText("Error: " + strMigorData + " does not exist in same folder as front end\n");
            }                        
        } else {                    
            taLog.appendText("Error: Migor Front End is not readable or does not exist or is locked\n");
        }

        // Validate DropBox directory
        
        if (Files.isWritable(Paths.get(txtDropBox.getText()))) {
            if (Files.isWritable(Paths.get(txtDropBox.getText() + "\\GnuCash"))) {
                if (Files.isWritable(Paths.get(txtDropBox.getText() + "\\Migor"))) {
                    boolDropBoxOk = true;
                } else {
                    taLog.appendText("Error: DropBox directory " + txtDropBox.getText() + "\\Migor is not writable or does not exist\n");
                }
            } else {
                taLog.appendText("Error: DropBox directory " + txtDropBox.getText() + "\\GnuCash is not writable or does not exist\n");
            }
        } else {
            taLog.appendText("Error: DropBox directory " + txtDropBox.getText() + " is not writable or does not exist\n");
        }

        // Note: Test: isDisabled(), but to actually enable or disable: setDisable()
        
        // enable or disable btnBupGC
        if (boolUserOk && boolPswdOk && boolGcOk && boolDropBoxOk) {
            if (btnBupGC.isDisabled()) {        // if Disabled
                btnBupGC.setDisable(false);     //     Enable
            }
        } else {
            if (! btnBupGC.isDisabled()) {      // if Enabled
                btnBupGC.setDisable(true);     //      Disable
            }
        }
        
        // enable or disable btnBupMigor
        if (boolUserOk && boolPswdOk && boolMigorOk && boolDropBoxOk) {
            if (btnBupMigor.isDisabled()) {        // if Disabled
                btnBupMigor.setDisable(false);     //     Enable
            }
        } else {
            if (! btnBupMigor.isDisabled()) {      // if Enabled
                btnBupMigor.setDisable(true);     //      Disable
            }
        }
        
        // enable or disable btnSaveSettings
        if (boolUserOk && boolGcOk && boolMigorOk && boolDropBoxOk) {
            if (btnSaveSettings.isDisabled()) {        // if Disabled
                btnSaveSettings.setDisable(false);     //     Enable
                //System.out.println("btnSaveSettings Enabled");
            }
        } else {
            if (! btnSaveSettings.isDisabled()) {      // if Enabled
                btnSaveSettings.setDisable(true);     //      Disable
            }
        }

        // Change Focus to password if all except password OK
        if ((boolUserOk && boolGcOk && boolMigorOk && boolDropBoxOk) && (! isValidPswd()) ) {
            //System.out.println("txtPswd.isVisible=" + txtPswd.isVisible() + 
            //    " txtVisPswd.isVisible=" + txtVisPswd.isVisible());
            if (txtPswd.isVisible()) {
                if (! txtPswd.isFocused()) {
                    if (isFirstTime()) {
                        firstTime = false;
                        // When run from initialize(), controls are not yet ready to handle focus
                        //  so delay first execution of requestFocus until later
                        //  Refer http://stackoverflow.com/questions/12744542/requestfocus-in-textfield-doesnt-work-javafx-2-1
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                txtPswd.requestFocus();
                            }
                        });
                    } else {
                        txtPswd.requestFocus();
                        //System.out.println("enable_or_disable_buttons: txtPswd.requestFocus");
                    }
                }
            } else {
                if (! txtVisPswd.isFocused()) {
                    txtVisPswd.requestFocus();
                    //System.out.println("enable_or_disable_buttons: txtVisPswd.requestFocus");
                }
            }
        }

/*        if (boolUserOk) {
            if (boolPswdOk) {
                if (boolGcOk && boolDropBoxOk) {
                    btnBupGC.setDisable(false);     // Enable
                }
                if (boolMigorOk && boolDropBoxOk) {
                    btnBupMigor.setDisable(false);
                }
            } else {
                if (boolGcOk && boolMigorOk && boolDropBoxOk ) {
                    System.out.println("txtPswd.isVisible=" + txtPswd.isVisible() + 
                        " txtVisPswd.isVisible=" + txtVisPswd.isVisible());
                    if (txtPswd.isVisible()) {
                        txtPswd.requestFocus();     // NOT working when called from initialize()
                    } else {
                        txtVisPswd.requestFocus();
                    }
                }
            }
 */              
    }
    
    public void handleUserGroupToggled(String strInUser) {
        //System.out.println("handleUserGroupToggled");
        
        if (strInUser.equals(winUserName)) {
            strHome = System.getProperty("user.home");
        } else {
            String strOtherUserName = strInUser.equals(getUserCg()) ? getUserCg() : getUserGm();
            strHome = System.getProperty("user.home").replace(winUserName, strOtherUserName);
        }
        strErrFil = strHome + "\\" + PROPERTIES_DIR + "\\BackGnuCashMigor.err";
        strOutFil = strHome + "\\" + PROPERTIES_DIR + "\\BackGnuCashMigor.out";
        
        txtGcDatFilStr.setText  (getGcDatFil   (strInUser));
        txtGcVer.setText        (getGcVer      (strInUser));
        txtMigorFeFilStr.setText(getMigorDatFil(strInUser));
        txtDropBox.setText      (getDropBoxDir (strInUser));
        
        setTooltips();  // contain user dependent filestrings
        enable_or_disable_buttons();
    }
    
    /**
     * Backup GnuCash by using commands like
     * cmd.exe          NOT Used
     * /C               NOT Used
     * "E:\Program Files\7-Zip\7z.exe"
     *   a
     *   E:\Data\GnuCash\267\MGCG2012\Backup\GnuCashMGCG2012_%yyyymmdd%_267.7z
     *   -p%pswd%
     *   E:\Data\GnuCash\267\MGCG2012\MGCG2012.gnucash
     *   C:\Users\goodc\.gnucash\saved-reports-2.4
     *   C:\Users\goodc\.gnucash\books\MGCG2012.gnucash.gcm
     *   C:\Users\goodc\AppData\Roaming\GnuCash\
     *   C:\Users\goodc\Appdata\Roaming\GnuCashGSettings.reg
     *
     * Note that 7z will backup all files and directories, including sub-folders,
     *  when the arg is a directory with a "\" suffix
     *  Therefore C:\Users\goodc\AppData\Roaming\GnuCash\
     *   will also backup V3 files
     *    C:\Users\goodc\AppData\Roaming\GnuCash\saved-reports-2.4
     *    C:\Users\goodc\AppData\Roaming\GnuCash\books\MGCG2012.gnucash.gcm
     *
     * copy /y ...Backup\GnuCashMGCG2012_%yyyymmdd%_267.7z  E:\Data\Dropbox\GnuCash
     * 
     * @author goodc
     * @param e
     * @throws java.io.IOException
     */
    @FXML
    public void handleBtnActionBupGC(Event e) throws IOException 
    {    
        /* NOTE: it does NOT seem possible to include redirection args like
            > or 2>&1 even if using cmd.exe /c
        */
        final int cmdElements = 10;
        String strArchive = "";
        int exitVal = 0;
        
        // create archive using 7z.exe
        taLog.clear();
        taLog.appendText("Backing up GnuCash...\n");

        String str7z = "\\Program Files\\7-Zip\\7z.exe";
        Path path7z = Paths.get("C:" + str7z);
        if (! Files.isExecutable(path7z)) {
            path7z = Paths.get("E:" + str7z);
            if (! Files.isExecutable(path7z)) {
                taLog.appendText("Error: Cannot execute " + str7z
                    + " on either C: or E:" );
                return;
            }
        }

        try {            
            int i = 0;
            String[] cmd = new String[cmdElements];
            
/*          As I'm not using internal shell commands, cmd.exe is not neeeded
            
            String osName = System.getProperty("os.name" );
            switch (osName) {
                case "Windows 95":
                    cmd[i++] = "command.com" ;
                    cmd[i++] = "/C" ;
                    break;
                case "Windows NT":
                case "Windows 7":
                    cmd[i++] = "cmd.exe" ;
                    cmd[i++] = "/C" ;
                    break;
                default:
                    if (osName.startsWith("Windows") == true) {
                        cmd[i++] = "cmd.exe" ;
                        cmd[i++] = "/C" ;
                        break;
                    }
            }
*/
            // 7-zip executable eg
            // "E:\Program Files\7-Zip\7z.exe"
            // not sure if need to quote, but doesn't hurt
            cmd[i++] = "\"" + path7z + "\"";
            cmd[i++] = "a";     // add to archive
            
            // archive file string eg
            // E:\Data\GnuCash\267\MGCG2012\Backup\GnuCashMGCG2012_yyyymmddhhmm_267.7z
            FileName fileName = new FileName(txtGcDatFilStr.getText(),
                System.getProperty("file.separator").charAt(0), '.');
            LocalDateTime today = LocalDateTime.now();
//          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); */

            String strVerSuffix = "";
            if (! txtGcVer.getText().isEmpty()) {
                strVerSuffix = "_" + txtGcVer.getText();
            }
            strArchive = fileName.path() + "\\Backup\\GnuCash" +
                fileName.filename() + "_" +
             /* today.format(DateTimeFormatter.BASIC_ISO_DATE) + */
                today.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) +
                strVerSuffix + ".7z";
            cmd[i++] = "\"" + strArchive + "\"";
         
            // password
            cmd[i++] = "-p" + txtPswd.getText();
            
            // GC data file eg E:\Data\GnuCash\267\MGCG2012\MGCG2012.gnucash
            cmd[i++] = "\"" + txtGcDatFilStr.getText() + "\"";

            if (chbGcV2Cfg.isSelected()) {
                // saved reports file
                // GnuCash V2.4+ to 2.6 eg C:\\Users\goodc\\.gnucash\saved-reports-2.4
                Path pathGcSavRpt = Paths.get(strHome + "\\.gnucash\\saved-reports-2.4");
                if (Files.isReadable(pathGcSavRpt)) {
                    cmd[i++] = "\"" + strHome + "\\.gnucash\\saved-reports-2.4\"";
                }

                // GC options V2.4 to 2.6
                //  eg C:\\users\goodc\.gnucash\books\MGCG2012.gnucash.gcm
                cmd[i++] = "\"" + strHome + "\\.gnucash\\books\\" +
                    fileName.filename() + "." + fileName.extension()+ ".gcm\"";
            }

            if (chbGcV3Cfg.isSelected()) {
                // GnuCash V3 user configuration files
                //  backup whole directory %APPDATA%\GnuCash so does
                //  local config changes like
                //      %APPDATA%\GnuCash\saved-reports-2.4 and saved-reports-2.8
                //      %APPDATA%\GnuCash\gtk-3.0.css
                Path pathGcUsrCfgDir = Paths.get(System.getenv("APPDATA") + "\\GnuCash\\");
                if (Files.exists(pathGcUsrCfgDir)) {
                    cmd[i++] = pathGcUsrCfgDir.toString() + "\\";
                }

                // Exported registry file
                if (exportRegistry()) {
                    cmd[i++] = strHome + FILE_SEPARATOR + PROPERTIES_DIR + FILE_SEPARATOR + "GnuCashGSettings.reg";
                }
            }

            while (i < cmdElements) {
                // stop rt.exec getting NullPointerException
                cmd[i++] = "";
            }
            
            Runtime rt = Runtime.getRuntime();
            
            System.out.println("Execing ");
//            for (int j = 0; j < i; j++) {
//                System.out.println(cmd[j]);
//            }
            Process proc = rt.exec(cmd);
            
            // make sure output is consumed so system buffers do not fill up
            // and cause the process to hang
            
            /*  Because any updates to the JavaFX gui must be done from the JavaFX
                application thread, it is not possible to update taLog from
                StreamGobbler, so I use StreamGobbler to put stdout &
                stderr to files, and just copy the contents of the files to taLog
                when the 7zip'ing finishes.
             */
            
            try (   // with resources
                FileOutputStream fosErr = new FileOutputStream(strErrFil);
                FileOutputStream fosOut = new FileOutputStream(strOutFil)
            )
            {
                // any error messages (stderr) ?
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", fosErr);
                // any output?
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", fosOut);
                // kick them off - creates new threads
                errorGobbler.start();
                outputGobbler.start();
                // any error?
                exitVal = proc.waitFor();
                System.out.println("ExitValue: " + exitVal);
                taLog.appendText("7-zip ExitValue: " + exitVal + "\n");
                fosErr.flush();
                fosOut.flush();
            }        
            //fosErr.close();  // done automatically when try with resources ends      
            //fosOut.close();  // done automatically when try with resources ends      
        } catch (Throwable t)
        {
            taLog.appendText("7-Zip FAILED - StackTrace logged");
            if (exitVal == 0) {
                exitVal = 99;
            }
            //t.printStackTrace();
            Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, t);
        }
        
        // add stderr of 7-zip process to taLog
        Path pthErrFil = Paths.get(strErrFil);
        try (InputStream in = Files.newInputStream(pthErrFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (!line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthErrFil);
        }
        // add stdout of 7-zip process to taLog
        Path pthOutFil = Paths.get(strOutFil);
        try (InputStream in = Files.newInputStream(pthOutFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))) {
//            String line = null;
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (! line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthOutFil);
        }
        // copy archive to dropbox
        if (exitVal != 0) {
            taLog.appendText("Error creating archive - skipping copy to DropBox");
            return;
        }
        // copy /y ...Backup\GnuCashMGCG2012_%yyyymmdd%_267.7z  E:\Data\Dropbox\GnuCash
        try {
            Path pthFromFile = Paths.get(strArchive);
            Path pthToFile = Paths.get(txtDropBox.getText() + "\\GnuCash\\"
                + pthFromFile.getFileName());
            taLog.appendText("Copying " + strArchive + " to " + pthToFile.toString());
            Files.copy(pthFromFile, pthToFile, REPLACE_EXISTING);
            taLog.appendText(" ... OK");
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException " + x + " copying " + strArchive + " to " +
                    txtDropBox.getText());
        }
    }

    /**
     * Backup Migor by using commands like
     * cmd.exe          NOT Used
     * /C               NOT Used
     * "E:\Program Files\7-Zip\7z.exe"
     *  a
     *  E:\Data\Access\Access2010\MGCG\Backup\MigorMGCG%yyyymmdd%.7z
     *  -p%pswd% 
     *  E:\Data\Access\Access2010\MGCG\Migor2014V05.accdb 
     *  E:\Data\Access\Access2010\MGCG\MigorData.accdb 
     *  E:\Data\Access\Access2010\MGCG\GetQuote.bat 
     *  E:\Data\Access\Access2010\MGCG\GetQuote.err 
     *  E:\Data\Access\Access2010\MGCG\GetQuote.out 
     *  E:\Data\Access\Access2010\MGCG\GetQuote.pl 
     *  E:\Data\Access\Access2010\MGCG\GetQuoteList.txt 
     *  E:\Data\Access\Access2010\MGCG\WorkingSharesCalc.xlsx
     * 
     * copy /y MigorMGCG%yyyymmdd%.7z  E:\Data\Dropbox\Migor
     * @author goodc
     * @param e
     * @throws java.io.IOException
     */
    @FXML
    public void handleBtnActionBupMigor(Event e) throws IOException {
        final int cmdElements = 12;
        String strArchive = "";
        int exitVal = 0;
        
        // create archive using 7z.exe
        taLog.clear();
        String str7z = "\\Program Files\\7-Zip\\7z.exe";
        Path path7z = Paths.get("C:" + str7z);
        if (! Files.isExecutable(path7z)) {
            path7z = Paths.get("E:" + str7z);
            if (! Files.isExecutable(path7z)) {
                taLog.setText("Error: Cannot execute " + str7z 
                    + " on either C: or E:" );
                return;
            }
        }
        try {            
            int i = 0;
            String[] cmd = new String[cmdElements];
            
            // 7-zip executable eg
            // "E:\Program Files\7-Zip\7z.exe"
            // not sure if need to quote, but doesn't hurt
            cmd[i++] = "\"" + path7z + "\"";
            cmd[i++] = "a";     // add to archive
            
            // archive file string eg
            // E:\Data\Access\Access2010\MGCG\Backup\MigorMGCGyyyymmddhhmm.7z
            FileName fileName = new FileName(txtMigorFeFilStr.getText(),
                    System.getProperty("file.separator").charAt(0), '.');
            LocalDateTime today = LocalDateTime.now();
            strArchive = fileName.path() + "\\Backup\\Migor";
            if (userGroup.getSelectedToggle().getUserData().toString().equals(userCg)) {
                strArchive = strArchive + "MGCG";
            } else {
                strArchive = strArchive + "BBGJ";
            }
            strArchive = strArchive + 
//              today.format(DateTimeFormatter.BASIC_ISO_DATE) +
                today.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) +
                ".7z";
            cmd[i++] = "\"" + strArchive + "\"";
         
            // password
            cmd[i++] = "-p" + txtPswd.getText();
            
            // Migor FrontEnd file eg 
            //  E:\Data\Access\Access2010\MGCG\Migor2014V02.accdb
            cmd[i++] = "\"" + txtMigorFeFilStr.getText() + "\"";
            
            // Migor data db file string
            //  eg E:\Data\Access\Access2010\MGCG\MigorData.accdb 
            cmd[i++] = "\"" + fileName.path() + "\\MigorData." +
                (txtMigorFeFilStr.getText().endsWith(".mdb") ? "mdb" : "accdb") + "\"" ;
                
            // E:\Data\Access\Access2010\MGCG\GetQuote.bat
            cmd[i++] = "\"" + fileName.path() + "\\GetQuote.bat\"";
        
            // E:\Data\Access\Access2010\MGCG\GetQuote.err 
            cmd[i++] = "\"" + fileName.path() + "\\GetQuote.err\"";
            
            // E:\Data\Access\Access2010\MGCG\GetQuote.out 
            cmd[i++] = "\"" + fileName.path() + "\\GetQuote.out\"";
            
            // E:\Data\Access\Access2010\MGCG\GetQuote.pl 
            cmd[i++] = "\"" + fileName.path() + "\\GetQuote.pl\"";
            
            // E:\Data\Access\Access2010\MGCG\GetQuoteList.txt
            cmd[i++] = "\"" + fileName.path() + "\\GetQuoteList.txt\"";
            
            // E:\Data\Access\Access2010\MGCG\WorkingSharesCalc.xlsx
            if (userGroup.getSelectedToggle().getUserData().toString().equals(userCg)) {
                cmd[i++] = "\"" + fileName.path() + "\\WorkingSharesCalc.xlsx\"";
            }
            
            while (i < cmdElements) {
                // stop rt.exec getting NullPointerException
                cmd[i++] = "";
            }
            
            Runtime rt = Runtime.getRuntime();
            
            System.out.println("Execing ");
//            for (int j = 0; j < i; j++) {
//                System.out.println(cmd[j]);
//            }
            taLog.appendText("Backing up Migor...\n");
            Process proc = rt.exec(cmd);
            
/*          Because any updates to the JavaFX gui must be done from the JavaFX
            application thread, it is not possible to update taLog from
            StreamGobbler, so I use StreamGobbler to put stdout &
            stderr files, and copy the contents of the files to taLog
            when the 7zip'ing finishes.
*/            
            // any error message?
            FileOutputStream fosErr = new FileOutputStream(strErrFil);
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "ERROR", fosErr);            

            // any output?
            FileOutputStream fosOut = new FileOutputStream(strOutFil);
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT", fosOut);

            // kick them off - creates new threads
            errorGobbler.start();
            outputGobbler.start();

            // any error?
            exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
            taLog.appendText("7-zip ExitValue: " + exitVal + "\n");
            fosErr.flush();
            fosErr.close();        
            fosOut.flush();
            fosOut.close();        
        } catch (Throwable t)
        {
            taLog.appendText("7-Zip FAILED - StackTrace logged");
            if (exitVal == 0) {
                exitVal = 99;
            }
            //t.printStackTrace();
            Logger.getLogger(BackupGnuCashMigorController.class.getName())
                .log(Level.SEVERE, null, t);
        }
        
        // add stderr of 7-zip process to taLog
        Path pthErrFil = Paths.get(strErrFil);
        try (InputStream in = Files.newInputStream(pthErrFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (!line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthErrFil);
        }
        // add stdout of 7-zip process to taLog
        Path pthOutFil = Paths.get(strOutFil);
        try (InputStream in = Files.newInputStream(pthOutFil);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (! line.equals("")) {
                    taLog.appendText(line + "\n");
                }
            }
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException reading " + pthOutFil);
        }        
        // copy archive to dropbox
        if (exitVal != 0) {
            taLog.appendText("Error creating archive - skipping copy to DropBox");
            return;
        }
        // copy  E:\Data\Access\Access2010\MGCG\Backup\MigorMGCGyyyymmdd.7z to E:\Data\Dropbox\Migor
        try {
            Path pthFromFile = Paths.get(strArchive);
            Path pthToFile = Paths.get(txtDropBox.getText() + "\\Migor\\"
                + pthFromFile.getFileName());
            taLog.appendText("Copying " + strArchive + " to " + pthToFile.toString());
            Files.copy(pthFromFile, pthToFile, REPLACE_EXISTING);
            taLog.appendText(" ... OK");
        } catch (IOException x) {
            System.err.println(x);
            taLog.appendText("IOException " + x + " copying " + strArchive + " to " +
                    txtDropBox.getText());
        }
    }

    @FXML
    public void handleBtnActionChooseGCDat(Event e) throws IOException {
              
        final FileChooser fileChooser = new FileChooser();         
        fileChooser.setTitle("Choose GnuCash Data file");   
        final File file = new File(txtGcDatFilStr.getText());
        final String strDir = file.getParent();
        final Path pathGcDatDir = Paths.get(strDir);
        if (Files.isReadable(pathGcDatDir)) {
            fileChooser.setInitialDirectory(new File(strDir));
        } else {
            fileChooser.setInitialDirectory(new File(strHome));
        }
        fileChooser.setInitialFileName(file.getName());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("GnuCash files (*.gnucash)", "*.gnucash");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // get a reference to the current stage for use with showOpenDialog
        //  so it is modal
                
        Scene scene = btnChooseGCDatFil.getScene(); // any control would do
        if (scene != null) {
            //System.out.println("scene!=null");
            Window window = scene.getWindow();
            File fileSel = fileChooser.showOpenDialog(window);
            if (fileSel != null) {
                try {
                    txtGcDatFilStr.setText(fileSel.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, ex);
                }
                enable_or_disable_buttons();
            }
        } else {
            //System.out.println("scene=null");
            taLog.appendText("Error: Cannot open modal fileChooser - scene is null\n");
        }
    } 

    @FXML
    public void handleBtnActionChooseMigor() {
              
        final FileChooser fileChooser = new FileChooser();         
        fileChooser.setTitle("Choose Migor Front End file");   
        final File file = new File(txtMigorFeFilStr.getText());
        final String strDir = file.getParent();
        final Path pathMigorDir = Paths.get(strDir);
        
        if (Files.isReadable(pathMigorDir)) {
            fileChooser.setInitialDirectory(new File(strDir));
        } else {
            fileChooser.setInitialDirectory(new File(strHome));
        }        
        fileChooser.setInitialFileName(file.getName());
//      FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("Microsoft Access (*.mdb)", "*.mdb");
//      FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("Microsoft Access (*.accdb)", "*.accdb");
//      fileChooser.getExtensionFilters().addAll(extFilter1, extFilter2 );

        FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("Microsoft Access (*.mdb, *.accdb )", "*.mdb", "*.accdb");
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("Microsoft Access (*.accdb)", "*.accdb");
        FileChooser.ExtensionFilter extFilter3 = new FileChooser.ExtensionFilter("Microsoft Access (*.mdb)", "*.mdb");
        fileChooser.getExtensionFilters().addAll(extFilter1, extFilter2, extFilter3 );

        Scene scene = btnChooseGCDatFil.getScene(); // any control would do
        if (scene != null) {
            //System.out.println("scene!=null");
            Window window = scene.getWindow();
            File fileSel = fileChooser.showOpenDialog(window);
            if (fileSel != null) {
                try {
                    txtMigorFeFilStr.setText(fileSel.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, ex);
                }
                enable_or_disable_buttons();
            }
        } else {
            //System.out.println("scene=null");
            taLog.appendText("Error: Cannot open modal fileChooser - scene is null\n");
        }
    }

    @FXML
    public void handleBtnActionChooseDropBox() {

        // Chose the DropBox base directory
        
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose DropBox Base Directory");
        final File file = new File(txtDropBox.getText());
        final String strDir = file.getPath();
        final Path pathDropDir = Paths.get(strDir);
        if (Files.isReadable(pathDropDir)) {
            directoryChooser.setInitialDirectory(file);
        } else {
            directoryChooser.setInitialDirectory(new File(strHome));
        }
        
        Scene scene = btnChooseGCDatFil.getScene(); // any control would do
        if (scene != null) {
            //System.out.println("scene!=null");
            Window window = scene.getWindow();
            final File selectedDirectory = directoryChooser.showDialog(window);
            if (selectedDirectory != null) {
                selectedDirectory.getAbsolutePath();
                try {
                    txtDropBox.setText(selectedDirectory.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, ex);
                }
                enable_or_disable_buttons();
            }
        } else {
            //System.out.println("scene=null");
            taLog.appendText("Error: Cannot open modal directoryChooser - scene is null\n");
        }
    }    
   
    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        // ToggleGroup userGroup is not a control so has to be created here instead of in Scene Builder
        userGroup.selectedToggleProperty().addListener(
            (ObservableValue<? extends Toggle> ov, Toggle old_toggle, 
            Toggle new_toggle) -> {
                if (userGroup.getSelectedToggle() != null) {
                    //System.out.println(userGroup.getSelectedToggle().getUserData().toString());
                    handleUserGroupToggled(userGroup.getSelectedToggle().getUserData().toString());
                }
        });

        rb1.setUserData(getUserCg());
        rb1.setText(getUserCg());
        rb1.setToggleGroup(userGroup);

        rb2.setUserData(getUserGm());
        rb2.setText(getUserGm());
        rb2.setToggleGroup(userGroup);

        setTooltips();
               
        if (isValidUser() == false) {
            //System.out.println("Unknown user: " + BackupGnuCashMigor.winUserName);
                   
          //taLog.setText("Error: Unknown user: " + BackupGnuCashMigorController.getWinUserName());
            taLog.setText("Error: Unknown user: " + winUserName);
            //taLog.setFill(Color.FIREBRICK);
        } else {
            // create dir $HOME/.BupGcM if doesn't already exist
            Boolean boolDirOK = true;
            Path pthBupGcM = Paths.get(strErrFil).getParent();
            if (Files.exists(pthBupGcM)) {
                getUserDefaults();
                chbGcV2Cfg.setSelected(Boolean.valueOf(gcV2Cfg));
                chbGcV3Cfg.setSelected(Boolean.valueOf(gcV3Cfg));
            } else {
                try {
                    Files.createDirectory(pthBupGcM);
                } catch (IOException ex) {
                    //Logger.getLogger(BackupGnuCashMigorController.class.getName()).log(Level.SEVERE, null, ex);
                    boolDirOK = false;
                    taLog.setText("Error: Cannot create folder: " + pthBupGcM.toString());
                }
            }
            
            if (boolDirOK == true) {
              //if (getWinUserName().equals(getUserCg())) {
                if (winUserName.equals(getUserCg())) {
                    rb1.setSelected(true);
                    txtGcDatFilStr.setText(getGcDatFil(getUserCg()));
                    txtGcVer.setText(getGcVer(getUserCg()));
                    txtMigorFeFilStr.setText(getMigorDatFil(getUserCg()));
                    txtDropBox.setText(getDropBoxDir(getUserCg()));
                    //setDropBoxDir(getUserCg());
                } else {
                    rb2.setSelected(true);
                    txtGcDatFilStr.setText(getGcDatFil(getUserGm()));
                    txtGcVer.setText(getGcVer(getUserGm()));
                    txtMigorFeFilStr.setText(getMigorDatFil(getUserGm()));
                    txtDropBox.setText(getDropBoxDir(getUserGm()));
                    //setDropBoxDir(getUserGm());
                }

                pathGcDatFilStr = Paths.get(txtGcDatFilStr.getText());
                pathMigorFeFilStr= Paths.get(txtMigorFeFilStr.getText());

                // handle changes to txtGcDatFilStr
                txtGcDatFilStr.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });

                // handle changes to txtMigorFeDatFilStr
                txtMigorFeFilStr.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });

                // handle changes to txtDropBox
                txtDropBox.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        if (oldVal == true) {
                            // has just lost focus
                            enable_or_disable_buttons();
                        }
                    }
                });
                                
                // handle changes to txtPswd
/*              txtPswd.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        //System.out.println("txtPswd.focusedProperty has changed!" + 
                        //    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                        if (oldVal == true) {
                            // txtPswd has just lost focus
                            enable_or_disable_buttons();
                            //lblShowPswd.setText(txtPswd.getText());
                            if ((! btnBupGC.isDisabled()) && isValidPswd()) {
                                btnBupGC.requestFocus();
                            }
                        }
                    }
                });
*/
  
                // 18/7/2015 Try listening for changes to textProperty instead of focusedProperty
                //      Works! ChangeListener is executed for every character added or deleted,
                //          NOT just when Focus is lost.
                
                txtPswd.textProperty().addListener(new ChangeListener<String>(){
                    @Override
                    public void changed(ObservableValue<? extends String> o, String oldVal, String newVal){
                        //System.out.println("txtPswd.textProperty has changed" + 
                        //    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                        
                            enable_or_disable_buttons();
                    }
                });

                // handle changes to txtVisPswd
                
                // 18/7/2015 I don't think we need following listener 
                //      as txtPswd.textProperty and txtVisPswd.textProperty are 
                //      bound bidirectionally
                
/*              txtVisPswd.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal){
                        //System.out.println("txtVisPswd.focusedProperty has changed!" + 
                        //    " oldVal=" + oldVal + " newVal=" + newVal + " o=" + o);
                        if (oldVal == true) {
                            // txtVisPswd has just lost focus
                            enable_or_disable_buttons();
                            if ((! btnBupGC.isDisabled()) && isValidPswd()) {
                                btnBupGC.requestFocus();
                            }
                        }
                    }
                });
*/
                // handle changes to chbShowPswd
                chbShowPswd.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) -> {
                //            lblShowPswd.setVisible(new_val);
                //            //icon.setImage(new_val ? image : null);
                              enable_or_disable_buttons();
                });

                // handle changes to chbGcV2Cfg
                chbGcV2Cfg.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) -> {
                              enable_or_disable_buttons();
                });

                // handle changes to chbGcV3Cfg
                chbGcV3Cfg.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) -> {
                              enable_or_disable_buttons();
                });

                // txtPswd    is a PasswordField    (masked)
                // txtVisPswd is a TextField        (not masked)
                // Only 1 is visible based on if chbShowPswd is ticked
                
                // Bind properties. Toggle txtVisPswd and txtPswd
                // visibility and managability properties mutually when chbShowPswd's state is changed.
                // Because we want to display only one component (txtVisPswd or txtPswd)
                // on the scene at a time.
                // Ref http://stackoverflow.com/questions/17014012/how-to-unmask-a-javafx-passwordfield-or-properly-mask-a-textfield
                //
                // managedProperty : Defines whether or not this node's layout will be managed by it's parent.
                
              //txtVisPswd.managedProperty().bind(chbShowPswd.selectedProperty());
                txtVisPswd.visibleProperty().bind(chbShowPswd.selectedProperty());

              //txtPswd.managedProperty().bind(chbShowPswd.selectedProperty().not());
                txtPswd.visibleProperty().bind(chbShowPswd.selectedProperty().not());

                // Bind the textField and passwordField text values bidirectionally.
                //      ie If 1 changes, the other also changes
                txtVisPswd.textProperty().bindBidirectional(txtPswd.textProperty());
                
                enable_or_disable_buttons();
            }
        }
    }
}
