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
public class ClientAccount {
    private char[] login, email, firstName, lastName, password; // account fields
    private char[]
            loginError = new char[0],
            emailError = new char[0],
            firstNameError = new char[0],
            lastNameError = new char[0],
            passwordError = new char[0]; // account errors
    private boolean hasError = false;
    

    public ClientAccount(Client client, char[] login, char[] password, char[] email, char[] firstName, char[] lastName) {
        setLogin(login);
        setPassword(password);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        
        client.getClientInformation().addStep(Arrays.asList(new String[]{ "Create ClientAccount", "Login: " + String.valueOf(login),
                "Password: " + String.valueOf(password), "Email: " + String.valueOf(email),
                "First name" + String.valueOf(firstName), "Last name" + String.valueOf(lastName) }));
    }
    public ClientAccount(char[] login, char[] password, char[] email, char[] firstName, char[] lastName) {
        setLogin(login);
        setPassword(password);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
    }
    public ClientAccount(String decryptedClientAccount) {
        String[] clientAccountParts = decryptedClientAccount.split(" ");
        setLogin(clientAccountParts[1].toCharArray());
        setEmail(clientAccountParts[3].toCharArray());
        setFirstName(clientAccountParts[5].toCharArray());
        setLastName(clientAccountParts[7].toCharArray());
        setPassword(clientAccountParts[9].toCharArray());
    }
    
    public char[] getLogin() {
        return  login;
    }
    
    public char[] getEmail() {
        return  email;
    }

    public char[] getFirstName() {
        return firstName;
    }

    public char[] getLastName() {
        return lastName;
    }

    public char[] getPassword() {
        return password;
    }
    public void setLogin(char[] login) {
        this.login = new char[login.length];
        System.arraycopy(login,0,this.login,0,login.length);
    }
    private void setEmail(char[] email) {
        this.email = new char[email.length];
        System.arraycopy(email,0,this.email,0,email.length);
    }
    private void setFirstName(char[] firstName) {
        this.firstName = new char[firstName.length];
        System.arraycopy(firstName,0,this.firstName,0,firstName.length);
    }
    private void setLastName(char[] lastName) {
        this.lastName = new char[lastName.length];
        System.arraycopy(lastName,0,this.lastName,0,lastName.length);
    }
    public void setPassword(char[] password) {
        this.password = new char[password.length];
        System.arraycopy(password,0,this.password,0,password.length);
    }

    @Override
    public String toString() { // account string value to print
        String string = "";
        if (hasError) {
            string += "Account is bad" + "\r\n";
            
        }
        string +=
                "Login\t " + String.valueOf(login) + ((loginError.length > 0) ? (" " + String.valueOf(loginError)) : "") + " \r\n"
                + "Email\t " + String.valueOf(email) + ((emailError.length > 0) ? (" " + String.valueOf(emailError)) : "") + " \r\n"
                + "First_name\t " + String.valueOf(firstName) + ((firstNameError.length > 0) ? (" \t" + String.valueOf(firstNameError)) : "" ) + " \r\n"
                + "Last_name\t " + String.valueOf(lastName) + ((lastNameError.length > 0) ? (" \t" + String.valueOf(lastNameError)) : "" ) + " \r\n"
                + "Password\t " + String.valueOf(password) + ((passwordError.length > 0) ? (" \t" + String.valueOf(passwordError)) : "" );
        return string;
    }
    public String toHTML() {
        String html = "";
        html += "<div><font color=blue>Login:</font><font color=red> " + String.valueOf(login) + "</font></div>";
        html += "<div><font color=blue>Password:</font><font color=red> " + String.valueOf(password) + "</font></div>";
        html += "<div><font color=blue>Email:</font><font color=red> " + String.valueOf(email) + "</font></div>";
        html += "<div><font color=blue>First name:</font><font color=red> " + String.valueOf(firstName) + "</font></div>";
        html += "<div><font color=blue>Last name:</font><font color=red> " + String.valueOf(lastName) + "</font></div>";
        return html;
    }

    public boolean hasError() {
        return hasError;
    }
    
    public void addLoginError(char[] error) { // add first name field error
        if ( !hasError ) {
            hasError = true;
        }
        int lastLength = this.loginError.length;
        char[] lastError = this.loginError;
        this.loginError = new char[error.length + lastLength + 2];
        System.arraycopy(lastError,0,this.loginError, 0, lastError.length);
        this.loginError[lastLength] = ',';
        this.loginError[lastLength + 1] = ' ';
        System.arraycopy(error, 0, this.loginError, lastLength + 2, error.length);
    }
    public void addFirstNameError(char[] error) { // add first name field error
        if ( !hasError ) {
            hasError = true;
        }
        int lastLength = this.firstNameError.length;
        char[] lastError = this.firstNameError;
        this.firstNameError = new char[error.length + lastLength + 2];
        System.arraycopy(lastError,0,this.firstNameError, 0, lastError.length);
        this.firstNameError[lastLength] = ',';
        this.firstNameError[lastLength + 1] = ' ';
        System.arraycopy(error, 0, this.firstNameError, lastLength + 2, error.length);
    }
    public void addLastNameError(char[] error) { // add last name field error
        if ( !hasError ) {
            hasError = true;
        }
        int lastLength = this.lastNameError.length;
        char[] lastError = this.lastNameError;
        this.lastNameError = new char[error.length + lastLength + 2];
        System.arraycopy(lastError,0,this.lastNameError, 0, lastError.length);
        this.lastNameError[lastLength] = ',';
        this.lastNameError[lastLength + 1] = ' ';
        System.arraycopy(error, 0, this.lastNameError, lastLength + 2, error.length);
    }
    public void addEmailError(char[] error) { // add email name field error
        if ( !hasError ) {
            hasError = true;
        }
        int lastLength = this.emailError.length;
        char[] lastError = this.emailError;
        this.emailError = new char[error.length + lastLength + 2];
        System.arraycopy(lastError,0,this.emailError, 0, lastError.length);
        this.emailError[lastLength] = ',';
        this.emailError[lastLength + 1] = ' ';
        System.arraycopy(error, 0, this.emailError, lastLength + 2, error.length);
    }
    public void addPasswordError(char[] error) { // add password field error
        if ( !hasError ) {
            hasError = true;
        }
        int lastLength = this.passwordError.length;
        char[] lastError = this.passwordError;
        this.passwordError = new char[error.length + lastLength + 2];
        System.arraycopy(lastError,0,this.passwordError, 0, lastError.length);
        this.passwordError[lastLength] = ',';
        this.passwordError[lastLength + 1] = ' ';
        System.arraycopy(error, 0, this.passwordError, lastLength + 2, error.length);
    }
}
