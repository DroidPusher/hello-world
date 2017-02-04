/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import GUI.MainWindow;
import java.awt.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author Den
 */
public class ClientInformation {
    private String mainHistory = "";
    private String clientID = "";
    private String personalData = "";
    private String enterTime = "";
    private String lifeTime = "";
    private String dhClient = "";
    private String leaveTime = "";
    private Integer clientIndex;
    private Integer parameterIndex = 1, actionIndex = 1, stepIndex = 1;
    
    private final javax.swing.JTable clientInformationTable;
    private final javax.swing.JComboBox clientInformationComboBox;
    private final javax.swing.JTextPane clientInformationTextPane;
    private final HTMLEditorKit kit;
    private final HTMLDocument document;
    
    public ClientInformation(MainWindow mainWindow) {
        this.clientInformationTable = mainWindow.getClientInformationTable();
        this.clientInformationComboBox = mainWindow.getClientInformationComboBox();
        this.clientInformationTextPane = mainWindow.getClientInformationTextPane();
        this.kit = (HTMLEditorKit)this.clientInformationTextPane.getEditorKit();
        this.document = (HTMLDocument)this.clientInformationTextPane.getStyledDocument();
    }
    
    public String getMainHistory() {
        return this.mainHistory;
    }
    public String getClientID() {
        return this.clientID;
    }
    public String getPersonalData() {
        return this.personalData;
    }
    public String getEnterTime() {
        return this.enterTime;
    }
    public String getLeaveTime() {
        return this.leaveTime;
    }
    public String getDHClient() {
        return this.dhClient;
    }
    public String getLifeTime() {
        return this.lifeTime;
    }
    public void initializeHistory() {
        String head =   "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "   <head>\n" +
                        "	<meta http-equiv=\"Content-Type\" content=\"text/html\"; charset=\"utf-8\">\n" +
                        "   </head>";
        
        String append = head +
                "<body><div style=\"color:blue;\">Client class initialization </div>"
                + "<div style=\"text-indent: 20px; color:green;\">START (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div>";
        this.mainHistory = append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void endInitialization() {
        String append = "<div style=\"text-indent: 20px; color:green;\">END (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div><div></div>";
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void setParameter(String parameter) {
        String append = "<div style=\"text-indent: 40px; color:black;\">"
                + "Set Parameter (" + String.valueOf(parameterIndex) + "): " + parameter
                + "</div>";
        parameterIndex++;
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void startAction(String actionType) {
        String append = "<div style=\"color:blue;\">Next action ("
                + String.valueOf(actionIndex) + ") is " + "<font color=red> " + actionType + "</font></div>"
                + "<div style=\"text-indent: 20px; color:green;\">START (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div>";
        stepIndex = 1;
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void addStep(List<String> stepParts) {
        String append = "<div style=\"text-indent: 40px; color:black;\">" 
                + "Step " + String.valueOf(stepIndex) + ": " + stepParts.get(0) + "</div>";
        
        for (int i = 1, size = stepParts.size(); i < size; i++ ) {
            append += "<div style=\"text-indent: 73px; color:black;\">"
                    + stepParts.get(i) + "</div>";
        }
        stepIndex++;
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void endAction() {
        String append = "<div style=\"text-indent: 20px; color:green;\">END (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div><div></div>";
        actionIndex++;
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void endHistory() {
        String append = "<div style=\"color:blue;\">"
                + "Client class destruction " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + "):"
                + "</div></body></html>";
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void pauseThread(String pauseType) {
        String append = "<div style=\"color:gray;\">" 
                + "//Thread paused by " + pauseType + " (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div>";
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    public void resumeThread() {
        String append = "<div style=\"color:gray;\">" 
                + "//Thread resumed (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div>";
        this.mainHistory += append;
        // show in GUI
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
//        System.out.println(append);
    }
    
    public void setClientID(String clientID) {
        this.clientID = clientID;
        this.clientIndex = Integer.parseInt(clientID) - 1;
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            clientInformationTable.setValueAt(this.clientID, 0, 1);
        }
    }
    public void setPersonalData(String personalData) {
        this.personalData = personalData;
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            clientInformationTable.setValueAt(this.personalData, 1, 1);
            updateRowHeights();
        }
    }
    public void setEnterTime(String enterTime) {
        this.enterTime = enterTime;
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            clientInformationTable.setValueAt(this.enterTime, 2, 1);
        }
    }
    public void setLifeTime(String lifeTime) {
        this.lifeTime = lifeTime;
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            clientInformationTable.setValueAt(this.lifeTime, 3, 1);
        }
    }
    public void setDHClient(String dhClient) {
        this.dhClient = dhClient;
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            clientInformationTable.setValueAt(this.dhClient, 4, 1);
            updateRowHeights();
        }
    }
    public void setLeaveTime(String leaveTime) {
        this.leaveTime = leaveTime;
        if ( clientInformationComboBox.getSelectedIndex() == clientIndex ) {
            clientInformationTable.setValueAt(this.leaveTime, 5, 1);
        }
    }
    private void updateRowHeights()
    {
        for (int row = 0; row < clientInformationTable.getRowCount(); row++)
        {
            int rowHeight = 30;

            for (int column = 0; column < clientInformationTable.getColumnCount(); column++)
            {
                Component comp = clientInformationTable.prepareRenderer(clientInformationTable.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            if(rowHeight != clientInformationTable.getRowHeight(row)) {
                clientInformationTable.setRowHeight(row, rowHeight);
            }
        }
    }
}
