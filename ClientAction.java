/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

/**
 *
 * @author Den
 */
public class ClientAction {
    private String action;
    private String actionTime;
    private String actionResult;
    
    public ClientAction(String action, String actionTime, String actionResult) {
        this.action = action;
        this.actionTime = actionTime;
        this.actionResult = actionResult;
    }
    public ClientAction() {
        this.action = "";
        this.actionTime = "";
        this.actionResult = "";
    }
    public String getAction() {
        return action;
    }
    public String getActionTime() {
        return actionTime;
    }
    public String getActionResult() {
        return actionResult;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public void setTime(String actionTime) {
        this.actionTime = actionTime;
    }
    public void setResult(String actionResult) {
        this.actionResult = actionResult;
    }
    
}
