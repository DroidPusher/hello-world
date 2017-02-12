/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Entries.Server;
import Entries.PersonalData;
import Entries.Visitor;
import GUI.MainWindow;
import java.util.Random;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Den
 */
public class InternetThread extends CheckThread {    
    /* network information */
    private Integer ammountOfVisitors, visitorsEnteredSite, visitorsBlocked;
    private final javax.swing.JTable internetTable, internetAmmountTable;
    private Server server;

    public InternetThread(MainWindow mainWindow) {
        /* create output files */

        
        visitorsEnteredSite = 0;
        ammountOfVisitors = 0;
        visitorsBlocked = 0;
        
        /* get table to fill in */
        this.internetTable = mainWindow.getInternetTable();
        this.internetAmmountTable = mainWindow.getInternetAmmountTable();
        
    }
   

    public Integer getClientAmmount() {
        return this.ammountOfVisitors;
    }
    
    public void addServer(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        try {
            while ( true ) {
                synchronized ( MainWindow.internetMonitor ) {
                    while ( MainWindow.internetPaused ) {
                        try {
                            MainWindow.internetMonitor.wait();
                        } catch (InterruptedException e ) {}
                    }
                }
                synchronized ( MainWindow.systemMonitor ) {
                    while ( MainWindow.systemPaused ) {
                        try {
                            MainWindow.systemMonitor.wait();
                        } catch (InterruptedException e ) {}
                    }
                }
                long timeSleep = new Random().nextInt(multipliedPeriod);
                Thread.sleep(timeSleep);

                ammountOfVisitors++;
                Visitor visitor = new Visitor(ammountOfVisitors, new PersonalData("some personal info")); // create new account with some personal info

                internetAmmountTable.getModel().setValueAt( ammountOfVisitors, 0, 1);

                if ( server.canGetVisitor() ) { // if output buffer is not full write data to buffer and result file
                    ((DefaultTableModel)internetTable.getModel()).addRow(new Object [] {"Visitor " + String.valueOf(ammountOfVisitors), "Successful"});

                    visitorsEnteredSite++;
                    internetAmmountTable.getModel().setValueAt( visitorsEnteredSite, 1, 1);
                    server.getVisitor(visitor);

                } else {
                    ((DefaultTableModel)internetTable.getModel()).addRow(new Object [] {"Visitor " + String.valueOf(ammountOfVisitors), "Unsuccessful"});
                    visitorsBlocked++;
                    internetAmmountTable.getModel().setValueAt( visitorsBlocked, 2, 1);                        

                }
//                    internetTable.scrollRectToVisible(new Rectangle(0, internetTable.getRowHeight(0)*internetTable.getRowCount(),1,1));
                internetTable.repaint();
                internetAmmountTable.repaint();
            }
        } catch(InterruptedException e) { }

    }
}
