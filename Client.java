/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Entries.Server;
import Enctyption.DHClient;
import Entries.ClientAccount;
import Entries.ClientHistory;
import Entries.ClientInformation;
import Entries.ClientRequest;
import Entries.PersonalData;
import Entries.SetValueAt;
import Entries.Visitor;
import GUI.MainWindow;
import GUITools.TableManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Den
 */
public class Client extends CheckThread {
    /* personal information */
    private final Integer clientID;
    private final String name;
    private final PersonalData personalData;
    private DHClient dhClient;
    private final ClientInformation clientInformation;
    private ClientHistory clientHistory;
    /* client's activity states */
    private enum State { START, LOGGED_IN, LOGGED_OUT, ACCOUNT_CREATED, USED_LOGIN_CREATED}
    private State state = State.START;
    /* thread data */
    private long leaveTime;
    private final String enterTime;
    /* server connection data */
    private final Object clientServerMonitor;
    Boolean responsed, result;
    private final BlockingQueue<ClientRequest> createAccountBuffer, loginBuffer, logoutBuffer, deleteAccountBuffer, leaveQueue;
    /* GUI */
    private final javax.swing.JComboBox clientInformationComboBox;
        
    public Client (Server server, Object clientServerMonitor, Visitor visitor, Integer clientID) {
        // provide server with client's activity information
        clientInformation = new ClientInformation(server.getMainWindow());
        server.getMainWindow().getClientsInformation().add(this.clientInformation);

        // set client's personal data
        this.name = "Client  " + String.valueOf(clientID) + " ";
        this.clientID = clientID;
        this.personalData = visitor.getPersonalData();
        this.enterTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        
        /* GUI to fill with data */
        this.clientInformationComboBox = server.getMainWindow().getClientInformationComboBox();
        clientInformationComboBox.addItem(this.clientID);
        
        /* server connection data */
        this.clientServerMonitor = clientServerMonitor;
        this.createAccountBuffer = server.getCreateAccountBuffer();
        this.loginBuffer = server.getLoginBuffer();
        this.logoutBuffer = server.getLogoutBuffer();
        this.deleteAccountBuffer = server.getDeleteAccountBuffer();
        this.leaveQueue = server.getLeaveQueue();
               
        clientInformation.setClientID(String.valueOf(this.clientID));
        clientInformation.setPersonalData(this.personalData.toString());
        clientInformation.setEnterTime(this.enterTime);
        clientInformation.initializeHistory();
        clientInformation.setParameter("ClientID");
        clientInformation.setParameter("PersonalData");
        clientInformation.setParameter("EnterTime");
        clientInformation.setParameter("ClientMonitor");
    }
    
    public String getEnterTime() {
        return this.enterTime;
    }
    public Integer getClientID() {
        return this.clientID;
    }
    public ClientInformation getClientInformation() {
        return this.clientInformation;
    }
    public Boolean getResult() {
        return this.result;
    }
    public void setResult(Boolean result) {
        this.result = result;
    }
    public void setResponsed(Boolean responsed) {
        this.responsed = responsed;
    }
    public PersonalData getPersonalData() {
        return this.personalData;
    }
    public void setDHClient(DHClient dhClient) {
        this.dhClient = dhClient;
        clientInformation.setDHClient(this.dhClient.toString());
        clientInformation.setParameter("DHClient");
    }
    public void setClientHistory(ClientHistory clientHistory) {
        this.clientHistory = clientHistory;
    }
    private char[] generateSomeLogin() {
        byte loginLength = (byte) ((Math.random() * 100) % 15);
        char[] login = new char[loginLength];
        for (byte i = 0; i < loginLength; i++) {
            login[i] = (char) ((Math.random() * 100) % 59 + 65); // {'A'(65) - '}'(125)}
        }
        return login;
    }
    private char[] generateSomePassword() {
        byte passwordLength = (byte) ((Math.random() * 100) % 14);
        char[] password = new char[passwordLength];
        for (byte i = 0; i < passwordLength; i++) {
            password[i] = (char) ((Math.random() * 100) % 94 + 33); // {'!'(33) - '}'(125)}
        }
        return password;
    }
    private char[] generateSomeFirstName() {
        byte firstNameLength = (byte) ((Math.random() * 100) % 10);
        char[] firstName = new char[firstNameLength];
        for (byte i = 0; i < firstNameLength; i++) {
            firstName[i] = (char) ((Math.random() * 100) % 59 + 65); // {'A'(65) - '}'(125)}
        }
        return firstName;
    }
    private char[] generateSomeLastName() {
        byte lastNameLength = (byte) ((Math.random() * 100) % 10);
        char[] lastName = new char[lastNameLength];
        for (byte i = 0; i < lastNameLength; i++) {
            lastName[i] = (char) ((Math.random() * 100) % 59 + 65); // {'A'(65) - '}'(125)}
        }
        return lastName;
    }
    private char[] generateSomeEmail() {
        byte beforeA = (byte) ((Math.random() * 100) % 8); // number of symbols before '@'
        byte afterA = (byte) (Math.random() * 100 % 7); // number of symbols between '@' and '.'
        byte afterDot = (byte) (Math.random() * 100 % 5); // number if symbols after '.'

        char[] email = new char[beforeA + afterA + afterDot + 2]; // create email
        for (byte i = 0; i < beforeA; i++) {
            email[i] = (char) ((Math.random() * 100) % 26 + 'a'); // {'a'-'z'}
        }
        email[beforeA] = '@';
        for (byte i = 0; i < afterA; i++) {
            email[beforeA + i + 1] = (char) ((Math.random() * 100) % 26 + 'a'); // {'a'-'z'}
        }
        email[beforeA + afterA + 1] = '.';
        for (byte i = 0; i < afterDot; i++) {
            email[beforeA + afterA + i + 2] = (char) ((Math.random() * 100) % 26 + 'a'); // {'a'-'z'}
        }
        return email;
    }
    
    @Override
    public void run() {
        // define client's time after which he will leave the site
        leaveTime = multipliedPeriod/2 + new Random().nextInt(multipliedPeriod/2);
        clientInformation.setLifeTime(String.valueOf(leaveTime));
        leaveTime += System.currentTimeMillis();
        
        clientInformation.setParameter("LifeTime");
        clientInformation.endInitialization();
        
        while ( leaveTime > System.currentTimeMillis() ) {
            
            synchronized ( MainWindow.clientMonitor ) {
                while ( MainWindow.clientPaused ) {
                    clientInformation.pauseThread("clientMonitor");
                    try {
                        MainWindow.clientMonitor.wait();
                    } catch (InterruptedException e ) {}

                    if ( !MainWindow.clientPaused ) {
                        clientInformation.resumeThread();
                    }
                }
            }
            synchronized ( MainWindow.systemMonitor ) {
                while ( MainWindow.systemPaused ) {
                    clientInformation.pauseThread("systemMonitor");
                    try {
                        MainWindow.systemMonitor.wait();
                    } catch (InterruptedException e ) {}

                    if ( !MainWindow.systemPaused ) {
                        clientInformation.resumeThread();
                    }
                }
            }
            try {
                /* client activity period starts from "0" to the third part of life period */
                Thread.sleep(Long.valueOf(new Random().nextInt(multipliedPeriod/3)));
                // client is going to perform new action
                clientHistory.createNewAction();
                switch (state) {
                    case START :
                        tryToCreateAccount();
                        break;
                    case USED_LOGIN_CREATED :
                        tryToChangeLogin();
                        break;
                    case ACCOUNT_CREATED :
                        tryToLogin();
                        break;
                    case LOGGED_IN :
                        switch ( ( (Math.random()*2) > 1) ? 1 : 0 ) {
                            case 1: // client will log out
                                tryToLogout();
                                break;
                            default: // client will delete his account
                                tryToDeleteAccount();
                                break;
                        }
                        break;
                    case LOGGED_OUT :
                        switch ( ( (Math.random()*2) > 1) ? 1 : 0 ) {
                            case 1: // client will log out
                                tryToLogin();
                                break;
                            default: // client will delete his account
                                tryToCreateAccount();
                                break;
                        }
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException ex) { }
        }
        
        // tell server that client is leaving        
        do {
            tryToLeaveSite();
            if ( ! result ) {
                clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
            } else {
                clientInformation.addStep(Arrays.asList(new String[] {"Leaving site"}));
            }
            clientInformation.endAction();
        } while (!result);
        clientInformation.endHistory();
        clientInformation.setLeaveTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
    }
    private void tryToCreateAccount() {
        responsed = false;
        result = false;
        // infromtaion for server
        this.clientHistory.putClientAction("Tryed to create account");
        this.clientHistory.putClientActionTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        // information to system
        clientInformation.startAction("CreateAccount");
        // create new Client Account
        ClientAccount clientAccount = new ClientAccount(this, generateSomeLogin(), generateSomePassword(), generateSomeEmail(), generateSomeFirstName(), generateSomeLastName());
        // put client account to quarantine
        
        if ( passedQuarantine(clientAccount) ) {
            clientInformation.addStep(Arrays.asList(new String[] {"Save client"}));
            
            personalData.setAccountData(clientAccount);
            clientInformation.setPersonalData(personalData.toString());
                
            byte[] salt;
            try {
                // hashing password
                salt = getSalt();
                clientAccount.setPassword(getSecurePassword(clientAccount.getPassword(),salt));
                personalData.setSalt(salt);
                personalData.setHashedPassword(clientAccount.getPassword());

                clientInformation.addStep(Arrays.asList(new String[] {"Hash password", "Got Salt: " + Arrays.toString(salt),
                    "Got HashedPassword: " + String.valueOf(clientAccount.getPassword())}));
                clientInformation.setPersonalData(personalData.toString());
            } catch (NoSuchAlgorithmException ex) { }
            
            // send request to server
            byte[] encryptedRequest = dhClient.request(clientAccount.toString());
            
            try {
                Server.createAccountTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
            } catch (InterruptedException ex) {}
            
            try {
                createAccountBuffer.put(new ClientRequest(this, encryptedRequest));
            } catch (InterruptedException ex) {}

            
            
            clientInformation.addStep(Arrays.asList(new String[] {"Put request in CreateAccountBuffer", "Wait for response"}));
            // wait for response
            synchronized ( clientServerMonitor ) {
                while ( !responsed ) {
                    try {
                        clientServerMonitor.wait();
                    } catch (InterruptedException e ) {}
                }
            }

            clientInformation.addStep(Arrays.asList(new String[] {"Wake up", "Server responded with result: " + String.valueOf(result)}));
            if ( result == false ) {
                clientInformation.addStep(Arrays.asList(new String[] {"Go to penalty state"}));
                state = State.USED_LOGIN_CREATED;
            } else {
                clientInformation.addStep(Arrays.asList(new String[] {"Go to next state"}));
                state = State.ACCOUNT_CREATED;
            }
        } else {
            result = false;
            clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
        }
        
        this.clientHistory.putClientActionResult(result ? "Successful" : "Unsuccessful");
        
        clientInformation.endAction();
    }
    private void tryToLogin() {
        responsed = false;
        result = false;
        // server information
        clientHistory.putClientAction("Tryed to login");
        clientHistory.putClientActionTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        // system information
        clientInformation.startAction("Login");
        /* create option either to use correct authencination data or wrong */
        try {
            if ( Math.random()*2 > 1 ) { // correct
                clientInformation.addStep(Arrays.asList(new String[] {"Random option: use correct authencinatioin data"}));
                
                byte[] encryptedRequest = dhClient.request(personalData.getAuthencinationData());
                
                try {
                    Server.loginTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
                } catch (InterruptedException ex) {}
                
                loginBuffer.put(new ClientRequest(this, encryptedRequest));
            } else { // use wrong personal data
                clientInformation.addStep(Arrays.asList(new String[] {"Random option: use bad authencinatioin data"}));
                
                byte[] encryptedRequest = dhClient.request("some bad authenction data");
                        
                try {
                    Server.loginTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
                } catch (InterruptedException ex) {}
                
                loginBuffer.put(new ClientRequest(this, encryptedRequest));
            }
        } catch (InterruptedException e ) {}

        clientInformation.addStep(Arrays.asList(new String[] {"Put request in LoginBuffer", "Wait for response"}));
        // wait for response
        synchronized (clientServerMonitor ) {
            while ( !responsed ) {
                try {
                    clientServerMonitor.wait();
                } catch (InterruptedException e ) {}
            }
        }
        clientInformation.addStep(Arrays.asList(new String[] {"Wake up", "Server responded with result: " + String.valueOf(result)}));
        
        clientHistory.putClientActionResult(result ? "Successful" : "Unsuccessful");
        
        if ( result == true ) {
            clientInformation.addStep(Arrays.asList(new String[] {"Go to next state"}));

            state = State.LOGGED_IN;
        } else {
            clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
        }
        clientInformation.endAction();
    }
    private void tryToLogout() {
        responsed = false;
        result = false;
        // server information
        clientHistory.putClientAction("Tryed to logout");
        clientHistory.putClientActionTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        //system information
        clientInformation.startAction("Logout");
        
        byte[] encryptedRequest = dhClient.request("Please, logout me");
                
        try {
            Server.logoutTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
        } catch (InterruptedException ex) {}
        
        try {
            logoutBuffer.put(new ClientRequest(this, encryptedRequest));
        } catch (InterruptedException e ) {}

        clientInformation.addStep(Arrays.asList(new String[] {"Put request in LogoutBuffer", "Wait for response"}));
        // wait for response
        synchronized (clientServerMonitor ) {
            while ( !responsed ) {
                try {
                    clientServerMonitor.wait();
                } catch (InterruptedException e ) {}
            }
        }
        clientInformation.addStep(Arrays.asList(new String[] {"Wake up", "Server responded with result: " + String.valueOf(result)}));
        clientHistory.putClientActionResult(result ? "Successful" : "Unsuccessful");
        
        if ( result == true ) {
            clientInformation.addStep(Arrays.asList(new String[] {"Go to next state"}));

            state = State.LOGGED_OUT;
        } else {
            clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
        }
        clientInformation.endAction();
    }
    private void tryToDeleteAccount() {
        responsed = false;
        result = false;
        // server information
        clientHistory.putClientAction("Tryed to delete account");
        clientHistory.putClientActionTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        // system information
        clientInformation.startAction("DeleteAccount");
        
        byte[] encryptedRequest = dhClient.request("Please, delete my account");
                 
        try {
            Server.deleteAccountTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
        } catch (InterruptedException ex) {}
        
        try {
            deleteAccountBuffer.put(new ClientRequest(this, encryptedRequest));
        } catch (InterruptedException e ) {}

        clientInformation.addStep(Arrays.asList(new String[] {"Put request in DeleteAccountBuffer", "Wait for response"}));
        // wait for response
        synchronized (clientServerMonitor ) {
            while ( !responsed ) {
                try {
                    clientServerMonitor.wait();
                } catch (InterruptedException e ) {}
            }
        }
        clientInformation.addStep(Arrays.asList(new String[] {"Wake up", "Server responded with result: " + String.valueOf(result)}));
        clientHistory.putClientActionResult(result ? "Successful" : "Unsuccessful");
        
        if ( result == true ) {
            clientInformation.addStep(Arrays.asList(new String[] {"Go to next state"}));

            state = State.START;
        } else {
            clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
        }
        clientInformation.endAction();
    }
    private void tryToLeaveSite() {
        responsed = false;
        result = false;
        // server information
        clientHistory.putClientAction("Tryed to leave from site");
        clientHistory.putClientActionTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        // system information
        clientInformation.startAction("Leave");
        
        byte[] encryptedRequest = dhClient.request("I am leaving the site");
        
        try {
            Server.leaveTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
        } catch (InterruptedException ex) {}
        
        try {
           leaveQueue.offer(new ClientRequest(this, encryptedRequest), 5, TimeUnit.SECONDS);
        } catch (InterruptedException e ) {
        
        }

        clientInformation.addStep(Arrays.asList(new String[] {"Put request in LeaveBuffer", "Wait for response"}));
        // wait for response
        synchronized (clientServerMonitor ) {
            while ( !responsed ) {
                try {
                    clientServerMonitor.wait();
                } catch (InterruptedException e ) {}
            }
        }
        clientInformation.addStep(Arrays.asList(new String[] {"Wake up", "Server responded with result: " + String.valueOf(result)}));
        clientHistory.putClientActionResult(result ? "Successful" : "Unsuccessful");
    }
    private void tryToChangeLogin() {
        responsed = false;
        result = false;
        // infromtaion for server
        this.clientHistory.putClientAction("Tryed to change account login");
        this.clientHistory.putClientActionTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        // information to system
        clientInformation.startAction("CreateUnusedLogin");
        // get created Client Account
        ClientAccount clientAccount = personalData.getClientAccount();
        clientAccount.setLogin(generateSomeLogin());
        clientInformation.addStep(Arrays.asList(new String[] {"Created new login"}));
        // put client account to quarantine
        
        if ( passedQuarantine(clientAccount) ) {
            clientInformation.addStep(Arrays.asList(new String[] {"Save client"}));
            
            personalData.setAccountData(clientAccount);
            clientInformation.setPersonalData(personalData.toString());
            
            // send request to server
            byte[] encryptedRequest = dhClient.request(clientAccount.toString());
            
            try {
                Server.createAccountTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object[] {name + "\r\n" + Arrays.toString(encryptedRequest), " "}));
            } catch (InterruptedException ex) {}
            
            try {
                createAccountBuffer.put(new ClientRequest(this, encryptedRequest));
            } catch (InterruptedException ex) {}            
            
            clientInformation.addStep(Arrays.asList(new String[] {"Put request in CreateAccountBuffer", "Wait for response"}));
            // wait for response
            synchronized ( clientServerMonitor ) {
                while ( !responsed ) {
                    try {
                        clientServerMonitor.wait();
                    } catch (InterruptedException e ) {}
                }
            }

            clientInformation.addStep(Arrays.asList(new String[] {"Wake up", "Server responded with result: " + String.valueOf(result)}));
            if ( result == false ) {
                clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
            } else {
                clientInformation.addStep(Arrays.asList(new String[] {"Go to next state"}));
                state = State.ACCOUNT_CREATED;
            }
        } else {
            result = false;
            clientInformation.addStep(Arrays.asList(new String[] {"Keep current state"}));
        }
        this.clientHistory.putClientActionResult(result ? "Successful" : "Unsuccessful");
        
        clientInformation.endAction();
    }
    
    private Boolean passedQuarantine(ClientAccount clientAccount) {
        try {
            MainWindow.quarantineSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 0, 1));
        } catch (InterruptedException ex) {}
        ArrayList<String> stepParts = new ArrayList<>();
        if ( !passedLexerCheck(clientAccount) ) {
            stepParts.addAll(Arrays.asList(new String[] {"Put ClientAccount to Quarantine", "LexerChecker error:"}));
            stepParts.addAll(Arrays.asList(clientAccount.toString().split("\r\n")));
            clientInformation.addStep(stepParts);
            try {
                MainWindow.quarantineSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 1, 1));
            } catch (InterruptedException ex) {}
            return false;
        }
        if ( !passedGrammarCheck(clientAccount) ) {
            stepParts.addAll(Arrays.asList(new String[] {"Put ClientAccount to Quarantine", "LexerChecker passed", "GrammarChecher error:"}));
            stepParts.addAll(Arrays.asList(clientAccount.toString().split("\r\n")));
            clientInformation.addStep(stepParts);
            try {
                MainWindow.quarantineSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 2, 1));
            } catch (InterruptedException ex) {}
            return false;
        }
        if ( !passedSemanticCheck(clientAccount) ) {
            stepParts.addAll(Arrays.asList(new String[] {"Put ClientAccount to Quarantine", "LexerChecker passed", "GrammarChecher passed", "SemantickChecker error:"}));
            stepParts.addAll(Arrays.asList(clientAccount.toString().split("\r\n")));
            clientInformation.addStep(stepParts);
            try {
                MainWindow.quarantineSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 3, 1));
            } catch (InterruptedException ex) {}
            return false;
        }
        clientInformation.addStep(Arrays.asList(new String[] {"Put ClientAccount to Quarantine", "LexerChecker passed", "GrammarChecher passed", "SemantickChecker passed"}));
        try {
            MainWindow.quarantineSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 4, 1));
        } catch (InterruptedException ex) {}
        return true;
    }
    private Boolean passedLexerCheck(ClientAccount clientAccount) {
        // email and password letter check : all symbols
        // login, fist and last name letter check : only english letters
        char[] firstNameError = new char[clientAccount.getFirstName().length];
        int errorNumber = 0;
        for ( int i = 0, size = clientAccount.getFirstName().length; i < size ; i ++ ) {
            if (!( (64 < clientAccount.getFirstName()[i]) & ( clientAccount.getFirstName()[i] < 91) ||
                    (96 < clientAccount.getFirstName()[i]) & ( clientAccount.getFirstName()[i] < 123) )) {
                firstNameError[errorNumber] = clientAccount.getFirstName()[i];
                errorNumber ++;
            }
        }
        if ( errorNumber > 0 ) { // add information about mistakes to account
            clientAccount.addFirstNameError( ("only english letters " + String.valueOf(firstNameError)).toCharArray() );
        }

        char[] lastNameError = new char[clientAccount.getLastName().length];
        errorNumber = 0;
        for ( int i = 0, size = clientAccount.getLastName().length; i < size ; i ++ ) {
            if (   !( (64 < clientAccount.getLastName()[i]) & ( clientAccount.getLastName()[i] < 91) ||
                    (96 < clientAccount.getLastName()[i]) & ( clientAccount.getLastName()[i] < 123) )   ) {
                lastNameError[errorNumber] = clientAccount.getLastName()[i];
                errorNumber ++;
            }
        }
        if ( errorNumber > 0 ) {
            clientAccount.addLastNameError( ("only english letters " + String.valueOf(lastNameError)).toCharArray() );
        }
        
        char[] loginError = new char[clientAccount.getLogin().length];
        errorNumber = 0;
        for ( int i = 0, size = clientAccount.getLogin().length; i < size ; i ++ ) {
            if (!( (64 < clientAccount.getLogin()[i]) & ( clientAccount.getLogin()[i] < 91) ||
                    (96 < clientAccount.getLogin()[i]) & ( clientAccount.getLogin()[i] < 123) )) {
                loginError[errorNumber] = clientAccount.getLogin()[i];
                errorNumber ++;
            }
        }
        if ( errorNumber > 0 ) { // add information about mistakes to account
            clientAccount.addLoginError(("only english letters " + String.valueOf(loginError)).toCharArray() );
        }
        
        return !clientAccount.hasError();
    }
    private Boolean passedGrammarCheck(ClientAccount clientAccount) {
        // email grammar check : at least 1 symbol before '@',
        // must contain '@'
        // at least 1 symbol after '@',
        // must contain '.'
        // at least 2 symbols after '.'
        int beforeA = 0, afterA = 0, afterDot = 0;
        int size = clientAccount.getEmail().length;
        boolean findBeforeA = true, findAfterA = true;
        for ( int i = 0; i < size; i++) {
            if ( findBeforeA ) {
                if (clientAccount.getEmail()[i] == '@') {
                    findBeforeA = false;
                    continue;
                }
                beforeA++;
            } else if ( findAfterA ) {
                if (clientAccount.getEmail()[i] == '.') {
                    findAfterA = false;
                    continue;
                }
                afterA++;
            } else {
                afterDot++;
            }
        }
        if ( beforeA == 0 ) {
            clientAccount.addEmailError("at least 1 symbol before '@'".toCharArray());
        } else if ( beforeA == size ) {
            clientAccount.addEmailError("no '@'".toCharArray());
        }
        if ( afterA == 0 ) {
            clientAccount.addEmailError("at least 1 symbol after '@'".toCharArray());
        } else if ( afterA == (size - beforeA - 1) ) {
            clientAccount.addEmailError("no '.'".toCharArray());
        }
        if ( afterDot < 2 ) {
            clientAccount.addEmailError("at least 2 symbol after '.'".toCharArray());
        }

        // password grammar check : at least 6 symbols
        if ( clientAccount.getPassword().length < 6 ) {
            clientAccount.addPasswordError("at least 6 simbols".toCharArray());
        }

        // fist and last name grammar check : at least two letters
        if ( clientAccount.getFirstName().length < 2 ) {
            clientAccount.addFirstNameError("at least 2 letters".toCharArray());
        }
        if ( clientAccount.getLastName().length < 2 ) {
            clientAccount.addLastNameError("at least 2 letters".toCharArray());
        }
        
        // login grammar check: ar least 5 symbols
        if ( clientAccount.getLogin().length < 5 ) {
            clientAccount.addLoginError("at least 5 letters".toCharArray());
        }
        
        return !clientAccount.hasError();
    }
    private Boolean passedSemanticCheck(ClientAccount clientAccount) {
        // semantics check :
        // 1) password != email, password != first name, password != last name, password != login, password != (first name + last name)
        // 2) first name != last name, first name != login
        // 3) last name != login

        if ( java.util.Arrays.equals(           clientAccount.getPassword(),    clientAccount.getEmail() ) ) {
            clientAccount.addPasswordError("can not be as email".toCharArray());
        } else if ( java.util.Arrays.equals(    clientAccount.getPassword(),    clientAccount.getFirstName() ) ) {
            clientAccount.addPasswordError("can not be as first name".toCharArray());
        } else if ( java.util.Arrays.equals(    clientAccount.getPassword(),    clientAccount.getLastName() ) ) {
            clientAccount.addPasswordError("can not be as last name".toCharArray());
        } else if ( java.util.Arrays.equals(    clientAccount.getPassword(),    clientAccount.getLogin())) {
            clientAccount.addPasswordError("can not be as login".toCharArray());
        } else if ( java.util.Arrays.equals(    clientAccount.getPassword(),    (String.valueOf(clientAccount.getFirstName()) + String.valueOf(clientAccount.getLastName())).toCharArray() ) ) {
            clientAccount.addPasswordError("can not be as first name plus last name".toCharArray());
        } 

        if ( java.util.Arrays.equals(           clientAccount.getFirstName(),   clientAccount.getLastName() ) ) {
            clientAccount.addFirstNameError("can not be as last name".toCharArray());
        } else if ( java.util.Arrays.equals(    clientAccount.getFirstName(),   clientAccount.getLogin() ) ) {
            clientAccount.addFirstNameError("can not be as login".toCharArray());
        }
        
        if ( java.util.Arrays.equals(           clientAccount.getLastName(),    clientAccount.getLogin() ) ) {
            clientAccount.addLastNameError("can not be as login".toCharArray());
        }
        
        return !clientAccount.hasError();
    }
    
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        //return salt
        return salt;
    }
    public static char[] getSecurePassword(char[] passwordToHash, byte[] salt)
    {
        char[] generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt);
            //Get the hash's bytes 
            byte[] bytes = md.digest(String.valueOf(passwordToHash).getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString().toCharArray();
        } 
        catch (NoSuchAlgorithmException e) { }
        return generatedPassword;
    }
}
