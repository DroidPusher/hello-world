/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import GUITools.TableManager;

/**
 *
 * @author Den
 */
public class SetValueAt {
    private TableManager.ActionType type;
    private Object object;
    private Object[] objects;
    private int row, column;
    
    public SetValueAt(TableManager.ActionType type, Object object, int row, int column) {
        this.object = object;
        this.row = row;
        this.column = column;
        this.type = type;
    }
    public SetValueAt(TableManager.ActionType type, int row, int column) {
        this.row = row;
        this.column = column;
        this.type = type;
    }
    public SetValueAt(TableManager.ActionType type, Object[] objects) {
        this.objects = objects;
        this.type = type;
    }
    public TableManager.ActionType getActionType() {
        return type;
    }
    public Object[] getObjects() {
        return objects;
    }
    public Object getObject() {
        return this.object;
    }
    public int getRow() {
        return this.row;
    }
    public int getColumn() {
        return this.column;
    }
}
