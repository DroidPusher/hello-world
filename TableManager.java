/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUITools;

import Entries.SetValueAt;
import GUI.MainWindow;
import Threads.CheckThread;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Den
 */
public class TableManager extends CheckThread {
    public static enum ActionType { PUT, CREATE, ADD };
    private final String name;
    private final javax.swing.JTable table;
    private final BlockingQueue<SetValueAt> buffer;
    
    public TableManager(javax.swing.JTable table, BlockingQueue<SetValueAt> buffer) {
        this.table = table;
        this.buffer = buffer;
        this.name = "help thread";
    }
    public TableManager(String name, javax.swing.JTable table, BlockingQueue<SetValueAt> buffer) {
        this.table = table;
        this.buffer = buffer;
        this.name = name;
    }
    
    @Override
    public void run() {
        int i = 0;
        while ( true ) {
            synchronized ( MainWindow.systemMonitor ) {
                while ( MainWindow.systemPaused ) {
                    try {
                        MainWindow.systemMonitor.wait();
                    } catch (InterruptedException e ) {}
                }
            }
            try {
                SetValueAt setValueAt = buffer.take();
                switch (setValueAt.getActionType()) {
                    case PUT:
                        if ( setValueAt.getColumn() ==  0 ) {
//                        System.out.println(setValueAt.getRow() + " " + setValueAt.getColumn());
                        }   while ( (setValueAt.getRow() > (table.getRowCount() - 1)) ) {
                            Object[] objects = new Object[table.getColumnCount()];
                            Arrays.fill(objects, " ");
                            ((DefaultTableModel)table.getModel()).addRow(objects);
                        }   table.setValueAt(setValueAt.getObject(), setValueAt.getRow(), setValueAt.getColumn());
                        break;
                    case ADD:
                        table.setValueAt((Integer)table.getValueAt(setValueAt.getRow(), setValueAt.getColumn()) + 1, setValueAt.getRow(), setValueAt.getColumn());
                        break;
                    case CREATE:
                        ((DefaultTableModel)table.getModel()).addRow(setValueAt.getObjects());
                        break;
                    default:
                        break;
                }
                table.repaint();
            } catch ( InterruptedException e) {}
        }
    }
}
