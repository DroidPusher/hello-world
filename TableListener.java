/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUITools;

import java.awt.Component;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author Den
 */
public class TableListener  implements TableModelListener {
    private final javax.swing.JTable table;

    public TableListener(javax.swing.JTable table) {
        this.table = table;
        table.getModel().addTableModelListener(this);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        updateLastRowHeight();
    }
    private void updateLastRowHeight()
    {
        int lastRow = table.getRowCount() - 1;
        int rowHeight = 30;

        table.prepareRenderer(table.getCellRenderer(lastRow, 0), lastRow, 0);
        Component comp = table.prepareRenderer(table.getCellRenderer(lastRow, 1), lastRow, 1);

        rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
        if(rowHeight != table.getRowHeight(lastRow)) {
            table.setRowHeight(lastRow, rowHeight);
        }
    }
};