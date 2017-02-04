/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUITools;

import java.awt.Component;

/**
 *
 * @author Den
 */
public class LineWrapCellRenderer  extends javax.swing.JTextArea implements javax.swing.table.TableCellRenderer {
    
    @Override
    public Component getTableCellRendererComponent(
            javax.swing.JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        this.setText((String)value);
        return this;
    }
};
