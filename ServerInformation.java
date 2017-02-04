/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author Den
 */
public class ServerInformation {
    private String information = "<html><body>";
    
    private final javax.swing.JTextPane textPane;
    private javax.swing.JComboBox comboBox;
    private final HTMLEditorKit kit;
    private final HTMLDocument document;
    
    private Style black;
    
    private final ArrayList<Integer>                clientsOnSite                       = new ArrayList<>();
    private final HashMap<Integer, ClientAccount>   accountsAvailable                   = new HashMap<>();
    private final ArrayList<String>                 accountsOnline                      = new ArrayList<>();
    private final HashMap<Integer, String>          clientServerActivitiesHistories     = new HashMap<>();
    private final HashMap<Integer, Integer>         clientServerActivitiesStepIndex     = new HashMap<>();
    private final HashMap<Integer, Integer>         clientServerActivitiesActionIndex   = new HashMap<>();
    private Integer                                 serverHelpThreadProcessing          = 1,
                                                    serverHelpThreadProcessingStep      = 1;
    
    public ServerInformation(javax.swing.JTextPane textPane) {
        this.textPane = textPane;
        this.kit = (HTMLEditorKit) this.textPane.getEditorKit();
        this.document = (HTMLDocument)this.textPane.getStyledDocument();
        
        black = document.addStyle("StyleName", null);
        StyleConstants.setBackground(black, Color.black);
        StyleConstants.setForeground(black, new Color(17,107,24));
    }
    public ServerInformation(javax.swing.JComboBox comboBox, javax.swing.JTextPane textPane) {
        this.comboBox = comboBox;
        this.textPane = textPane;
        this.kit = (HTMLEditorKit) this.textPane.getEditorKit();
        this.document = (HTMLDocument)this.textPane.getStyledDocument();
    }
    
    public void notHTML() {
        information = "";
    }  
    
    /* Clients on site information */
    public void addClientOnSite(Integer clientID) {
        clientsOnSite.add(clientID);
    }
    public void removeClientOnSite(Integer clientID) {
        clientsOnSite.remove(clientID);
    }
    public String getClientsOnSite() {
        information = "";
        for (int i = 0, size = clientsOnSite.size(); i < size; i++ ) {
            double colorR = Math.random(), colorG = Math.random(), colorB = Math.random();
            information += "<div style=\"color: rgb(" + String.valueOf((int)(colorR*255)) + ", " + String.valueOf((int)(colorG*255)) + ", " + String.valueOf((int)(colorB*255)) + ");\">" 
                + String.valueOf(i + 1) + ": Client " + String.valueOf(clientsOnSite.get(i)) + "</div>";
        }

        return information + "</body></html>";
    }
    
    /* Accounts available information */
    public void addAccountAvailable(Integer clientID, ClientAccount clientAccount) {
        accountsAvailable.put(clientID, clientAccount);
    }
    public void removeAccountAvailable(Integer clientID) {
        accountsAvailable.remove(clientID);
    }
    public String getAccountsAvailable() {
        information = "";
        for ( Integer clientID : accountsAvailable.keySet() ) {
            information += "<div ><font color=blue>" + "Client:</font><font color=red> " + String.valueOf(clientID) + "</font></div>";
            information += accountsAvailable.get(clientID).toHTML();
            information += "<hr>";
        }

        return information + "</body></html>";
    }
    
    /* Accounts online information */
    public void addAccountOnline(String clientAccount) {
        accountsOnline.add(clientAccount);
    }
    public void removeAccountOnline(String clientAccount) {
        accountsOnline.remove(clientAccount);
    }
    public String getAccountsOnline() {
        information = "";
        for (int i = 0, size = accountsOnline.size(); i < size; i++ ) {
            double colorR = Math.random(), colorG = Math.random(), colorB = Math.random();
            information += "<div style=\"color: rgb(" + String.valueOf((int)(colorR*255)) + ", " + String.valueOf((int)(colorG*255)) + ", " + String.valueOf((int)(colorB*255)) + ");\">" 
                + String.valueOf(i + 1) + ": " + String.valueOf(accountsOnline.get(i)) + "</div>";
        }

        return information + "</body></html>";
    }
    
    /* Data transfer information */
    public void addDataTransfer(byte[] encryptedMessage) {
        String append = "";
        for ( byte b: encryptedMessage ) {
            append += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        }
        information += append;
        if (information.length() > 100000 ) {
            information = information.substring(80000);
            if ( textPane.isVisible() ) {
                try {
                    document.remove(0, 80000);
                } catch (BadLocationException ex) {}
            }
        }
        if ( textPane.isVisible() ) {
            try {
                document.insertString(document.getLength(), append, black);
            } catch( BadLocationException ex) {}
        }
    }
    public String getDataTransfer() {
        return this.information;
    }
    
    /* client server activities */
    public void initialize(Integer clientID) {
        clientServerActivitiesStepIndex.put(clientID, 1);
        clientServerActivitiesActionIndex.put(clientID, 1);
        clientServerActivitiesHistories.put(clientID, "<html><body>");
    }
    public void startAction(Integer clientID, String actionType) {
        String append = "<div style=\"color:blue;\">Next action ("
                + String.valueOf(clientServerActivitiesActionIndex.get(clientID)) + ") is " + "<font color=red> " + actionType + "</font></div>"
                + "<div style=\"text-indent: 20px; color:green;\">START (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div>";
        clientServerActivitiesStepIndex.put(clientID, 1);
        clientServerActivitiesHistories.put(clientID, clientServerActivitiesHistories.get(clientID) + append);
        // show in GUI
        if ( comboBox.isVisible() ) {
            if ( comboBox.getSelectedIndex() == (clientID - 1) ) {
                try {
                    try{
                        kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                    } catch( BadLocationException ex) {}
                } catch(IOException ex) {}
            }
        }
//        System.out.println(append);
    }
    public void addStep(Integer clientID, List<String> stepParts) {
        String append = "<div style=\"text-indent: 40px; color:black;\">" 
                + "Step " + String.valueOf(clientServerActivitiesStepIndex.get(clientID)) + ": " + stepParts.get(0) + "</div>";
        
        for (int i = 1, size = stepParts.size(); i < size; i++ ) {
            append += "<div style=\"text-indent: 73px; color:black;\">"
                    + stepParts.get(i) + "</div>";
        }
        clientServerActivitiesStepIndex.put(clientID, clientServerActivitiesStepIndex.get(clientID) + 1);
        clientServerActivitiesHistories.put(clientID, clientServerActivitiesHistories.get(clientID) + append);
        // show in GUI
        if ( comboBox.isVisible() ) {
            if ( comboBox.getSelectedIndex() == (clientID - 1) ) {
                try {
                    try{
                        kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                    } catch( BadLocationException ex) {}
                } catch(IOException ex) {}
            }
        }
//        System.out.println(append);
    }
    public void addSteps(Integer clientID, List<String> stepParts) {
        String append = "";
        
        for (int i = 0, size = stepParts.size(); i < size; i++ ) {
            append +=  "<div style=\"text-indent: 40px; color:black;\">" 
                    + "Step " + String.valueOf(clientServerActivitiesStepIndex.get(clientID) + i) + ": " + stepParts.get(i) + "</div>";
        }
        clientServerActivitiesStepIndex.put(clientID, clientServerActivitiesStepIndex.get(clientID) + stepParts.size());
        clientServerActivitiesHistories.put(clientID, clientServerActivitiesHistories.get(clientID) + append);
        // show in GUI
        if ( comboBox.isVisible() ) {
            if ( comboBox.getSelectedIndex() == (clientID - 1) ) {
                try {
                    try{
                        kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                    } catch( BadLocationException ex) {}
                } catch(IOException ex) {}
            }
        }
//        System.out.println(append);
    }
    public void endAction(Integer clientID) {
        String append = "<div style=\"text-indent: 20px; color:green;\">END (" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div><div></div>";
        clientServerActivitiesActionIndex.put(clientID, clientServerActivitiesActionIndex.get(clientID) + 1);;
        clientServerActivitiesHistories.put(clientID, clientServerActivitiesHistories.get(clientID) + append);
        // show in GUI
        if ( comboBox.isVisible() ) {
            if ( comboBox.getSelectedIndex() == (clientID - 1) ) {
                try {
                    try{
                        kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                    } catch( BadLocationException ex) {}
                } catch(IOException ex) {}
            }
        }
//        System.out.println(append);
    }
    public void endHistory(Integer clientID) {
        String append = "<div style=\"color:blue;\">"
                + "Client class destruction " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + "):"
                + "</div></body></html>";
        clientServerActivitiesHistories.put(clientID, clientServerActivitiesHistories.get(clientID) + append);
        // show in GUI
        if ( comboBox.isVisible() ) {
            if ( comboBox.getSelectedIndex() == (clientID - 1) ) {
                try {
                    try{
                        kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                    } catch( BadLocationException ex) {}
                } catch(IOException ex) {}
            }
        }
//        System.out.println(append);
    }
    public String getClientServerActivities(Integer clientID) {
        return clientServerActivitiesHistories.get(clientID);
    }
    
    // server help threads
    public void addNewClientRequest() {
        String append = "<div style=\"color: green;\">"
                + "Processing " + String.valueOf(serverHelpThreadProcessing) + " start ("
                + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div>";
        serverHelpThreadProcessingStep = 1;
        information += append;
        if ( textPane.isVisible() ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
    }
    public void endNewClientRequest() {
        String append = "<div style=\"color: green;\">"
                + "Processing " + String.valueOf(serverHelpThreadProcessing) + " end ("
                + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8) + ")</div><div></div>";
        serverHelpThreadProcessing++;
        information += append;
        if ( textPane.isVisible() ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
    }
    public void addServerHelpThreadStep(List<String> stepParts) {
        String append = "<div style=\"text-indent: 20px; color:black;\">" 
                + "Step " + String.valueOf(serverHelpThreadProcessingStep) + ": " + stepParts.get(0) + "</div>";
        
        for (int i = 1, size = stepParts.size(); i < size; i++ ) {
            append += "<div style=\"text-indent: 53px; color:black;\">"
                    + stepParts.get(i) + "</div>";
        }
        serverHelpThreadProcessingStep++;
        information += append;
        if ( textPane.isVisible() ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
    }
    public void addServerHelpThreadSteps(List<String> stepParts) {
        String append = "";
        
        for (int i = 0, size = stepParts.size(); i < size; i++ ) {
            append += "<div style=\"text-indent: 20px; color:black;\">" 
                    + "Step " + String.valueOf(serverHelpThreadProcessingStep + i) + ": " + stepParts.get(i) + "</div>";
        }
        serverHelpThreadProcessingStep += stepParts.size();
        information += append;
        if ( textPane.isVisible() ) {
            try {
                try{
                    kit.insertHTML(document, document.getLength(), append, 0, 0, null);
                } catch( BadLocationException ex) {}
            } catch(IOException ex) {}
        }
    }
    public String getInformation() {
        return information + "</body></html>";
    }
}
