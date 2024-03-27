package com.youtube.downloader;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Controller {
    private String resFileName = "res.txt";
    private String systemUsername = System.getProperty("user.name");
    private String systemDownloadPath = "C:\\Users\\"+systemUsername+"\\Documents";
    private String downloadPath = "%userprofile%\\Documents";
    private String setConsoleTitle = "title Youtube Video Downloader";
    private String setConsoleColor = "color 3F";
    private String setConsoleSize = "mode con:cols=120 lines=25";
    private String[] resolutions = {"144p","360p","480p","720p","1080p","1440p","2160p"};
    private String[] downloadTypes = {"Just One Video","Playlist","Playlist with specific numbers","Audio Only"};
    private String[] avaliableResolution = null;
    private String endLink = null;
    private String finalCommand = null;
    private DirectoryChooser downloadPathChooser = new DirectoryChooser();
    private Map<String,String> resFormat = new HashMap<>();
    private Map<String,String> resMemory = new HashMap<>();
    private String selectedFormat = null;
    private String downloadLink = "";
    private String thumbnailString = "";
    private String subtitleString = "";
    private String playlistString = "";
    private String serialString = "";
    private String serialNumberString = "";


    @FXML private JFXButton confirmButton;
    @FXML private JFXButton downloadButton;
    @FXML private JFXButton downloadPathButton;
    @FXML private JFXTextField startField;
    @FXML private JFXTextField endField;
    @FXML private JFXTextField urlLink;
    @FXML private JFXComboBox<String> resolutionMenu;
    @FXML private JFXComboBox<String> downloadTypeMenu;
    @FXML private JFXCheckBox thumbnail;
    @FXML private JFXCheckBox subtitle;
    @FXML private JFXCheckBox serial;
    @FXML private HBox startBox;
    @FXML private HBox endBox;
    @FXML private Label downloadFilePath;
    @FXML private Text note;



    @FXML
    public void initialize() {
        File tempResFile = new File(systemDownloadPath+"\\tempRes.txt");
        File resFile = new File(systemDownloadPath+"\\res.txt");
        if ( tempResFile.exists() ) tempResFile.delete();
        if ( resFile.exists() ) resFile.delete();
        setDownloadTypeMenuItems();
        downloadButton.setDisable(true);
        showHBox(false);
        downloadPathButton.setDisable(true);
        pressComboBox();
        downloadFilePath.setText(systemDownloadPath+"\\Download");
        disableItems(true,resolutionMenu,thumbnail,subtitle,serial);
        disableHBox(true);
//        youtube-dl -o "%%(playlist_index)3d - %%(title)s.%%(ext)s" %downloadLink%

    }

    @FXML
    public void pressConfirmButton() {
        downloadTypeMenu.setDisable(true);
        confirmButton.setDisable(true);
        downloadButton.setDisable(false);
        urlLink.setDisable(true);
        disableItems(false,resolutionMenu,thumbnail,subtitle,downloadPathButton);

        if ( downloadTypeMenu.getValue().equals(downloadTypes[0])) {
            runResolutionCommand(formatLink(urlLink.getText()));
        }else if ( downloadTypeMenu.getValue().equals(downloadTypes[1])) {
            runResolutionCommand(formatLink(urlLink.getText()));
            serial.setDisable(false);
        } else if ( downloadTypeMenu.getValue().equals(downloadTypes[2])) {
            runResolutionCommand(formatLink(urlLink.getText()));
            serial.setDisable(false);
        } else if ( downloadTypeMenu.getValue().equals(downloadTypes[3])) {
            disableItems(true,resolutionMenu,thumbnail,subtitle,serial);
        } else {
            System.out.println("Wrong options selected in download type...");
        }

        setResolutionMenuItems();
        disableHBox(false);


    }

    @FXML
    public void pressDownloadButton() {
        selectedFormat = resFormat.get(getFormatFromUser())+"+";
        downloadLink = formatLink(urlLink.getText());

        isPlaylist();
        isThumbnailChecked();
        isSubtitleChecked();
        isSerialChecked();
        isSerial();
        isAudio();

        // Final Command
        finalCommand = "cd \""+downloadFilePath.getText()+"\" & "+setConsoleTitle+"&"+setConsoleSize+"&"+setConsoleColor+" & youtube-dl "+thumbnailString+" -f "+selectedFormat+"m4a "
                +subtitleString+" "+playlistString+" "+serialNumberString+" -o \""+serialString+"%(title)s.%(ext)s\" "+downloadLink+" & echo. & echo   + Finished... & pause>nul & exit";
        // Final Command
        System.out.println(finalCommand);
        runCommand(finalCommand);

        setBackToOriginalStage();

    }

    @FXML
    public void pressDownloadPath(ActionEvent event) {
        Stage stage = (Stage) ( (Node) event.getSource() ).getScene().getWindow();
        File finalDownloadPath = downloadPathChooser.showDialog(stage);
        downloadFilePath.setText(finalDownloadPath.getAbsolutePath());
        downloadFilePath.setVisible(true);
        downloadPath = finalDownloadPath.getAbsolutePath();
    }

    private void runResolutionCommand(String link) {
        runInvisibleCommand("youtube-dl --no-playlist -F "+link+">>"+downloadPath+"\\tempRes.txt && rename "+downloadPath+"\\tempRes.txt res.txt");
        File resFile = new File(systemDownloadPath+"\\res.txt");
        ArrayList<String> tempResolution = new ArrayList<>();
        while ( true ) {
            if ( resFile.exists() ) break;
        }
        try ( Scanner readResFile = new Scanner( resFile )) {
            while ( readResFile.hasNext() ) {
                String readLine = readResFile.nextLine();
                String[] tempFormat = readLine.split(" ");
                for ( String res:resolutions ) {
                    if ( readLine.contains(res) && readLine.contains("video only") && readLine.contains("mp4") ) {
                        if ( !tempResolution.contains(res) ) {
                            tempResolution.add(res);
                            resFormat.put(res,tempFormat[0]);
                            resMemory.put(res,filterMemory(tempFormat[tempFormat.length-1]));
                        } else {
                            resFormat.put(res,tempFormat[0]);
                            resMemory.put(res,filterMemory(tempFormat[tempFormat.length-1]));
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        avaliableResolution = new String[tempResolution.size()];
        for ( int id=0 ; id < tempResolution.size() ; id++ ) {
            avaliableResolution[id] = tempResolution.get(id);
        }
    }

    @FXML
    public void pressComboBox() {

        if ( downloadTypeMenu.getValue().equals(downloadTypes[1]) || downloadTypeMenu.getValue().equals(downloadTypes[2])) {
            note.setVisible(true);
        } else {
            note.setVisible(false);
        }

        if ( downloadTypeMenu.getValue().equals(downloadTypes[2])) {
            showHBox(true);
        } else {
            showHBox(false);

        }

        if ( downloadTypeMenu.getValue().equals(downloadTypes[0]) || downloadTypeMenu.getValue().equals(downloadTypes[3]) ) {
            serial.setDisable(true);
        } else {
            if ( !downloadButton.isDisabled() ) {
                serial.setDisable(false);
            }
        }
    }


    private void runCommand ( String command ) {
        try {
            Runtime.getRuntime().exec("cmd.exe start cmd /c start cmd /k \""+command+"\"");
        } catch ( Exception e ) {
            System.out.println("Something wrong with runtime commands");
        }
    }

    private void runInvisibleCommand ( String command ) {
        try {
            Runtime.getRuntime().exec("cmd.exe start cmd /c \""+command+"\"");
        } catch ( Exception e ) {
            System.out.println("Something wrong with runtime commands");
        }
    }

    private void sleepByMilliSeconds(int value) {
        try {
            TimeUnit.MILLISECONDS.sleep(value);
        } catch(Exception e) {
            System.out.println("Something wrong with milliseconds sleep...");
        }
    }

    private void showHBox(boolean condition) {
        if ( condition ) {
            startBox.setVisible(true);
            endBox.setVisible(true);
        } else {
            startBox.setVisible(false);
            endBox.setVisible(false);
        }
    }

    private void disableHBox(boolean condition) {
        if ( condition ) {
            startBox.setDisable(true);
            endBox.setDisable(true);
        } else {
            startBox.setDisable(false);
            endBox.setDisable(false);
        }
    }

    private String formatLink(String link) {
        StringBuffer sb = new StringBuffer();
        for ( int id=0 ; id < link.length() ; id++ ) {
            if ( link.charAt(id) == '&' ) {
                sb.append('^');
                sb.append(link.charAt(id));
            } else {
                sb.append(link.charAt(id));
            }
        }
        return sb.toString();
    }

    private String filterMemory(String fullMemoryString) {
        int memLength = fullMemoryString.length();
        String mem = "";
        String size="";
        for ( int id=memLength-3 ; id < memLength-0 ; id++ ) {
            mem+=String.valueOf(fullMemoryString.charAt(id));
        }
        for ( int id=0 ; id < memLength-3 ; id++ ) {
            size += String.valueOf(fullMemoryString.charAt(id));
        }
        if ( mem.equals("MiB") ) {
            mem = "MB";
        } else if ( mem.equals("GiB") ) {
            mem= "GB";
        } else {
            mem = "Error";
        }
        return size+" "+mem;
    }

    private String getFormatFromUser() {
        String finalFormat = "";
        String tempFormat = resolutionMenu.getValue();
        if ( tempFormat == null ) return finalFormat;
        for ( int id=0 ; id < tempFormat.length() ; id++ ) {
            String tempString = String.valueOf(tempFormat.charAt(id));
            if ( tempString.equals("p") ) {
                finalFormat += tempString;
                break;
            }
            finalFormat += tempString;
        }
        return finalFormat;
    }

    private boolean isPlaylist() {
        boolean isPlaylist=false;
        if ( downloadTypeMenu.getValue().equals(downloadTypes[1]) || downloadTypeMenu.getValue().equals(downloadTypes[2])) {
            isPlaylist = true;
            playlistString = "--yes-playlist";
        } else {
            isPlaylist=false;
            playlistString = "--no-playlist";
        }
        return isPlaylist;
    }

    private boolean isAudio() {
        if ( downloadTypeMenu.getValue().equals(downloadTypes[3]) ) {
            selectedFormat = "";
            return true;
        }
        return false;
    }

    private boolean isSerial() {
        if ( downloadTypeMenu.getValue().equals(downloadTypes[2]) ) {
            serialNumberString = "--playlist-start "+startField.getText()+" --playlist-end "+endField.getText();
            return true;
        }
        return false;
    }

    private boolean isThumbnailChecked() {
        if ( thumbnail.isSelected() ) {
            thumbnailString = "--embed-thumbnail";
            return true;
        }
        return false;
    }

    private boolean isSubtitleChecked() {
        if ( subtitle.isSelected() ) {
            subtitleString = "--write-auto-sub";
            return true;
        }
        return false;
    }

    private boolean isSerialChecked() {
        if ( serial.isSelected() ) {
//            %%(playlist_index)3d -
              serialString = "%(playlist_index)3d - ";
              return true;
        }
        return false;
    }

    private void disableItems(boolean isDisable,Node ... node) {
        if ( isDisable ) {
            for ( Node n:node ) {
                n.setDisable(true);
            }
        } else {
            for ( Node n:node ) {
                n.setDisable(false);
            }
        }
    }

    private void setResolutionMenuItems() {
        for ( String res:avaliableResolution ) {
            resolutionMenu.getItems().add(res+" | "+resMemory.get(res));
        }
        resolutionMenu.getSelectionModel().selectFirst();
        File resFile = new File(systemDownloadPath+"\\"+resFileName);
        if ( resFile.exists() ) resFile.delete();
    }

    private void setDownloadTypeMenuItems() {
        for ( String value:downloadTypes ) {
            downloadTypeMenu.getItems().add(value);
        }
        downloadTypeMenu.getSelectionModel().select(0);
    }

    private void setBackToOriginalStage() {
        disableItems(false,urlLink,downloadTypeMenu,confirmButton);
        disableItems(true,resolutionMenu,thumbnail,subtitle,serial,downloadButton,downloadPathButton);
        systemDownloadPath = "C:\\Users\\"+systemUsername+"\\Documents";
        downloadPath = "%userprofile%\\Documents";
        downloadFilePath.setText("C:\\Users\\"+systemUsername+"\\Download");
        urlLink.clear();
        downloadTypeMenu.getSelectionModel().selectFirst();
        resolutionMenu.getItems().clear();
        thumbnail.setSelected(false);
        subtitle.setSelected(false);
        serial.setSelected(false);
    }

}
