/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import GUI.MainWindow;
import Threads.Client;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Den
 */
public class ClientHistory {
    ArrayList<ClientAction> clientActions = new ArrayList<>(0);
    private final javax.swing.JTable clientTable;
    private final javax.swing.JComboBox clientComboBox;
    private final Integer clientID;
    private Integer actionIndex = 0;
    
    public ClientHistory(Client client, MainWindow mainWindow) {
        this.clientTable = mainWindow.getClientTable();
        this.clientComboBox = mainWindow.getClientComboBox();
        this.clientID = client.getClientID();
        clientActions.add(new ClientAction("Entered site", client.getEnterTime(), "Successful"));
    }
    public Integer ammountOfActions() {
        return clientActions.size();
    }
    public ArrayList<ClientAction> getClientActions() {
        return clientActions;
    }
    public void createNewAction() {
        clientActions.add(new ClientAction());
        if ( clientComboBox.getSelectedIndex() == (clientID - 1) ) {
            ((DefaultTableModel)clientTable.getModel()).addRow(new Object[] {" ", " ", " "});
        }
    }
    public void putClientAction(String action) {
        clientActions.get(actionIndex).setAction(action);
        if ( clientComboBox.getSelectedIndex() == (clientID - 1) ) {
            clientTable.setValueAt(action, actionIndex, 0);
        }
    }
    public void putClientActionTime(String time) {
        clientActions.get(actionIndex).setTime(time);
        if ( clientComboBox.getSelectedIndex() == (clientID - 1) ) {
            clientTable.setValueAt(time, actionIndex,1);
        }
    }
    public void putClientActionResult(String result) {
        clientActions.get(actionIndex).setResult(result);
        if ( clientComboBox.getSelectedIndex() == (clientID - 1) ) {
            clientTable.setValueAt(result, actionIndex,2);
        }
        actionIndex++;
    }
}
