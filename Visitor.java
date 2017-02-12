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
public class Visitor {
    private final PersonalData personalData;
    private final Integer visitorID;
    
    public Visitor (Integer visitorID,PersonalData personalData ) {
        this.visitorID = visitorID;
        this.personalData = personalData;
    }
    
    public PersonalData getPersonalData() {
        return this.personalData;
    }
    public Integer getVisitorID() {
        return this.visitorID;
    }
}
