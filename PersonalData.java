/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import Threads.Client;
import java.util.Arrays;

/**
 *
 * @author Den
 */
public class PersonalData {
    private final String data;
    private char[]
            login = new char[0],
            password = new char[0],
            hashedPassword = new char[0],
            firstName = new char[0],
            lastName = new char[0],
            email  = new char[0];
    private byte[] salt = new byte[0];
    private Boolean loggedIn = false;
    
    public PersonalData(String data) {
        this.data = data;
    }
    public void setAccountData(ClientAccount clientAccount) {
        this.login = clientAccount.getLogin();
        this.password = clientAccount.getPassword();
        this.email = clientAccount.getEmail();
        this.firstName = clientAccount.getFirstName();
        this.lastName = clientAccount.getLastName();
    }
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
    public void setHashedPassword(char[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    public Boolean getLoggedIn() {
        return this.loggedIn;
    }
    public ClientAccount getClientAccount() {
        return new ClientAccount(login, hashedPassword, email, firstName, lastName);
    }
    public String getAuthencinationData() {
        return  "Login\t " + String.valueOf(login) + " \r\n" +
                "Password\t " + String.valueOf(Client.getSecurePassword(password, salt));
    }
    public String getLogin() {
        return String.valueOf(login);
    }
    public String getFirstName() {
        return String.valueOf(firstName);
    }
    public String getLastName() {
        return String.valueOf(lastName);
    }
    @Override
    public String toString() {
        return data + ((login.length > 0) ? ("\r\nLogin:\t\t" + String.valueOf(login) + "\r\n") : "")
                + ((password.length > 0) ? ("Password:\t\t" + String.valueOf(password) + "\r\n") : "" )
                + ((hashedPassword.length > 0) ? ("Hashed password:\t" + String.valueOf(hashedPassword) + "\r\n") : "" )
                + ((salt.length > 0) ? ("Salt:\t\t" + Arrays.toString(salt) + "\r\n") : "" )
                + ((email.length > 0) ? ("Email:\t\t" + String.valueOf(email) + "\r\n") : "")
                + ((firstName.length > 0) ? ("First name:\t\t" + String.valueOf(firstName) + "\r\n") : "" )
                + ((lastName.length > 0) ? ("Last name:\t\t" + String.valueOf(lastName)) : "" );
    }
}
