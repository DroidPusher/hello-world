/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import Enctyption.DHClient;
import Enctyption.DHGenerator;
import Enctyption.DHServer;
import GUI.MainWindow;
import GUITools.TableManager;
import Threads.CheckThread;
import Threads.Client;
import Threads.ServerCreateAccountThread;
import Threads.ServerDeleteAccountThread;
import Threads.ServerLeaveThread;
import Threads.ServerLoginThread;
import Threads.ServerLogoutThread;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Den
 */
public final class Server extends CheckThread {
    /* staff to communicate with clients */
    private final BlockingQueue<ClientRequest> createAccountBuffer, loginBuffer, logoutBuffer, deleteAccountBuffer, leaveQueue;
    private final HashMap<Client, DHServer> dhServers = new HashMap<>(); // DH servers for current clients
    private final HashMap<Client, Object> clientMonitors = new HashMap<>(); // monitor for current clients
    private final HashMap<Integer, ClientAccount> clientAccounts = new HashMap<>();
    private final BigInteger dhKey = BigInteger.probablePrime(1024, new Random()); // hacked DH key for faster proccessing
    
    private final Integer clientLeavePeriod; // client leave site period
   
    /* histories of all clients like in CIA */
    private final ArrayList<ClientHistory> clientHistories = new ArrayList<>();
    // system information
    private Integer clientsOnSite = 0, clientsLeftSite = 0, ammountOfClients = 0;
    private Integer accountsCreated = 0, accountsDeleted = 0, accountsAvailable = 0;
    private Integer accountsLoggedIn = 0, accountsOnline = 0, accountsLoggedOut = 0;
    
    private final ServerInformation
            clientsOnSiteInformation,
            accountsAvailableInformation,
            accountsOnlineInformation,
            dataTransferInformation,
            clientServerActivitiesInformation,
            createAccountThreadInformation,
            loginThreadInformation,
            logoutThreadInformation,
            deleteAccountThreadInformation,
            leaveThreadInformation;
    /* GUI to fill with data */
    private final javax.swing.JTable serverTable;
    private final javax.swing.JTable serverAmmountTable;
    private final javax.swing.JSpinner speedGainSpinner;
    private final javax.swing.JComboBox clientComboBox;
    private final javax.swing.JTextPane serverInformationTextPane;
    private final MainWindow mainWindow;
    
    // GUI buffers
    public static BlockingQueue<SetValueAt> 
            serverTableBuffer =         new ArrayBlockingQueue<>(1000),
            
            serverSummaryTableBuffer =  new ArrayBlockingQueue<>(1000),
            
            leaveTableBuffer =          new ArrayBlockingQueue<>(1000),
            createAccountTableBuffer =  new ArrayBlockingQueue<>(1000),
            deleteAccountTableBuffer =  new ArrayBlockingQueue<>(1000),
            loginTableBuffer =          new ArrayBlockingQueue<>(1000),
            logoutTableBuffer =         new ArrayBlockingQueue<>(1000);
    
    private final BlockingQueue<SetValueAt>
            serverErrorsTableBuffer =   new ArrayBlockingQueue<>(1000);
    
    public Server(MainWindow mainWindow) {
        /* GUI to fill in */
        this.mainWindow = mainWindow;
        this.clientLeavePeriod = mainWindow.getClientLeavePeriod();
        this.serverTable = mainWindow.getServerTable();
        this.serverAmmountTable = mainWindow.getServerAmmountTable();
        this.speedGainSpinner = mainWindow.getSpeedGainSpinner();
        this.clientComboBox = mainWindow.getClientComboBox();
        this.serverInformationTextPane = mainWindow.getServerInformationTextPane();
        
        /* system information */
        clientsOnSiteInformation = new ServerInformation(mainWindow.getClientsOnSiteTextPane());
        accountsAvailableInformation = new ServerInformation(mainWindow.getAccountsAvailableTextPane());
        accountsOnlineInformation = new ServerInformation(mainWindow.getAccountsOnlineTextPane());
        dataTransferInformation = new ServerInformation(mainWindow.getDataTransferTextPane());
        dataTransferInformation.notHTML();
        clientServerActivitiesInformation = new ServerInformation(mainWindow.getClientServerActivitiesComboBox(), mainWindow.getClientServerActivitiesTextPane());
        createAccountThreadInformation = new ServerInformation(mainWindow.getCreateAccountThreadTextPane());
        loginThreadInformation = new ServerInformation(mainWindow.getLoginThreadTextPane());
        logoutThreadInformation = new ServerInformation(mainWindow.getLogoutThreadTextPane());
        deleteAccountThreadInformation = new ServerInformation(mainWindow.getDeleteAccountThreadTextPane());
        leaveThreadInformation = new ServerInformation(mainWindow.getLeaveThreadTextPane());
        /*initialize help thread buffers*/
        createAccountBuffer = new ArrayBlockingQueue<>(1000);
        loginBuffer = new ArrayBlockingQueue<>(1000);
        logoutBuffer = new ArrayBlockingQueue<>(1000);
        deleteAccountBuffer = new ArrayBlockingQueue<>(1000);
        leaveQueue =  new ArrayBlockingQueue<>(1000);
    }
    
    public void initThreads() {
        // server client requests' threads
        ServerCreateAccountThread serverCreateAccountThread = new ServerCreateAccountThread(this);
        serverCreateAccountThread.setBasePeriod(this.getBasePeriod());
        ServerLoginThread serverLoginThread = new ServerLoginThread(this);
        serverLoginThread.setBasePeriod(this.getBasePeriod());
        ServerLogoutThread serverLogoutThread = new ServerLogoutThread(this);
        serverLogoutThread.setBasePeriod(this.getBasePeriod());
        ServerDeleteAccountThread serverDeleteAccountThread = new ServerDeleteAccountThread(this);
        serverDeleteAccountThread.setBasePeriod(this.getBasePeriod());
        ServerLeaveThread serverLeaveThread = new ServerLeaveThread(this);
        serverLeaveThread.setBasePeriod(this.getBasePeriod());
        // GUI manager threads
        TableManager serverTableManager = new TableManager(getMainWindow().getServerTable(), serverTableBuffer);
        TableManager serverErrorsTableManager = new TableManager(getMainWindow().getServerErrorsTable(), serverErrorsTableBuffer);
        TableManager serverSummaryTableManager = new TableManager(getMainWindow().getServerSummaryTable(), serverSummaryTableBuffer);
        TableManager leaveTableManager = new TableManager(getMainWindow().getLeaveTable(), leaveTableBuffer);
        TableManager loginTableManager = new TableManager(getMainWindow().getLoginTable(), loginTableBuffer);
        TableManager logoutTableManager = new TableManager(getMainWindow().getLogoutTable(), logoutTableBuffer);
        TableManager createAccountTableManager = new TableManager(getMainWindow().getCreateAccountTable(), createAccountTableBuffer);
        TableManager deleteAccountTableManager = new TableManager(getMainWindow().getDeleteAccountTable(), deleteAccountTableBuffer);
        // add threads to container
        childThreads.add(serverCreateAccountThread);
        childThreads.add(serverErrorsTableManager);
        childThreads.add(serverLoginThread);
        childThreads.add(serverLogoutThread);
        childThreads.add(serverDeleteAccountThread);
        childThreads.add(serverLeaveThread);
        childThreads.add(serverSummaryTableManager);
        childThreads.add(leaveTableManager);
        childThreads.add(loginTableManager);
        childThreads.add(logoutTableManager);
        childThreads.add(createAccountTableManager);
        childThreads.add(deleteAccountTableManager);
        childThreads.add(serverTableManager);
    }
    
    public Boolean canGetVisitor() {
        return (clientsOnSite < 1000);
    }
    public ClientHistory getClientHistory(Integer index) {
        return clientHistories.get(index);
    }
    public BlockingQueue<ClientRequest> getCreateAccountBuffer() {
        return this.createAccountBuffer;
    }
    public BlockingQueue<ClientRequest> getLoginBuffer() {
        return this.loginBuffer;
    }
    public BlockingQueue<ClientRequest> getLogoutBuffer() {
        return this.logoutBuffer;
    }
    public BlockingQueue<ClientRequest> getDeleteAccountBuffer() {
        return this.deleteAccountBuffer;
    }
    public BlockingQueue<ClientRequest> getLeaveQueue() {
        return this.leaveQueue;
    }
    public Object getClientMonitor(Client client) {
        return clientMonitors.get(client);
    }
    public javax.swing.JTextPane getServerInformationTextPane() {
        return serverInformationTextPane;
    }
    public HashMap<Client, DHServer> getDHServers() {
        return dhServers;
    }
    public javax.swing.JTable getServerTable() {
        return serverTable;
    }
    public ArrayList<ClientHistory> getClientHistories() {
        return clientHistories;
    }
    public ServerInformation getClientsOnSiteInformation() {
        return this.clientsOnSiteInformation;
    }
    public ServerInformation getAccountsAvailableInformation() {
        return this.accountsAvailableInformation;
    }
    public ServerInformation getAccountsOnlineInformation() {
        return this.accountsOnlineInformation;
    }
    public ServerInformation getDataTransferInformation() {
        return this.dataTransferInformation;
    }
    public ServerInformation getClientServerActivitiesInformation() {
        return this.clientServerActivitiesInformation;
    }
    public ServerInformation getCreateAccountThreadInformation() {
        return this.createAccountThreadInformation;
    }
    public ServerInformation getLoginThreadInformation() {
        return this.loginThreadInformation;
    }
    public ServerInformation getLogoutThreadInformation() {
        return this.logoutThreadInformation;
    }
    public ServerInformation getDeleteAccountThreadInformation() {
        return this.deleteAccountThreadInformation;
    }
    public ServerInformation getLeaveThreadInformation() {
        return this.leaveThreadInformation;
    }
    public MainWindow getMainWindow() {
        return this.mainWindow;
    }
    public HashMap<Integer, ClientAccount> getClientAccounts() {
        return this.clientAccounts;
    }
    public void getVisitor(Visitor visitor) {
        // create new client
        clientsOnSite ++;
        ammountOfClients ++;
        Object clientMonitor = new Object();
        Client client = new Client(this, clientMonitor, visitor, ammountOfClients);
        
        // GUI fill
        mainWindow.getClientServerActivitiesComboBox().addItem(client.getClientID());
        clientServerActivitiesInformation.initialize(client.getClientID());
        clientServerActivitiesInformation.startAction(client.getClientID(), "Create Client from visitor" + String.valueOf(visitor.getVisitorID()));
        clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
            "Increased ammount of clients on site",
            "Increased ammount of clients created",
            "Created Client's Monitor",
            "Created new Client" + String.valueOf(client.getClientID())
        
        }));
        serverAmmountTable.getModel().setValueAt( clientsOnSite, 0, 1);
        serverAmmountTable.repaint();
        // set DH connection with client
        setEasyDHConnectionWithClient(client); // really hard proccessings
        
        client.setBasePeriod(clientLeavePeriod);
        client.setMultiplyIndex((int)speedGainSpinner.getValue());
        ClientHistory clientHistory = new ClientHistory(client, mainWindow);
        clientHistories.add(clientHistory);
        client.setClientHistory(clientHistory);
        
        childThreads.add(client);
        clientMonitors.put(client, clientMonitor);
        
        clientComboBox.addItem(ammountOfClients);
        client.start();
        // GUI fill
        clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
            "Set base thread period for Client" + String.valueOf(client.getClientID()) + " from system data",
            "Set multiply thread period index for Client" + String.valueOf(client.getClientID()) + " from system data",
            "Created and saved instance of ClientHistory for Client" + String.valueOf(client.getClientID()),
            "Provided Client" + String.valueOf(client.getClientID()) + " with instance of his ClientHistory to spy on him",
            "Put Client" + String.valueOf(client.getClientID()) + " to child thread container",
            "Saved Monitor for Client" + String.valueOf(client.getClientID()),
            "Gave permission to Client" + String.valueOf(client.getClientID()) + "  to act"
        }));
        
        clientServerActivitiesInformation.endAction(client.getClientID());
        //GUI information
        try {
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(ammountOfClients), 1, 1));
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(clientsOnSite), 2, 1));
        } catch (InterruptedException e) {}
        
        /* System information */
        clientsOnSiteInformation.addClientOnSite(client.getClientID());
    }
    
    public void removeClient(Client client) {
        clientsOnSite--;
        clientsLeftSite++;
        clientMonitors.remove(client);
        dhServers.remove(client);
        childThreads.remove(client);
        // GUI to fill
        clientServerActivitiesInformation.startAction(client.getClientID(), "Remove from site Client" + String.valueOf(client.getClientID()));
        clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
            "Decreased ammount of clients on site",
            "Increased ammount of clients left",
            "Removed Client's Monitor",
            "Removed DHServer for Client's ID",
            "Removed Client" + String.valueOf(client.getClientID()) + " from child thread container"
        }));        
        
        serverAmmountTable.getModel().setValueAt( clientsOnSite, 0, 1);
        serverAmmountTable.getModel().setValueAt( clientsLeftSite, 1, 1);
        serverAmmountTable.repaint();
        
        try {
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(clientsLeftSite), 3, 1));
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(clientsOnSite), 2, 1));
        } catch (InterruptedException e) {}

        // log out client account if he is logged ib
        clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
            "Check if Client" + String.valueOf(client.getClientID()) + " has ClientAccount logged in"
        }));
        if ( client.getPersonalData().getLoggedIn() ) {
            client.getPersonalData().setLoggedIn(Boolean.FALSE);
            accountsOnline--;
            accountsLoggedOut++;
            //GUI information
            try {
                serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsLoggedOut), 9, 1));
                serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsOnline), 8, 1));
            } catch (InterruptedException e) {}
            
            /* System information */
            accountsOnlineInformation.removeAccountOnline(client.getPersonalData().getLogin());
            clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
                "Client" + String.valueOf(client.getClientID()) + " has ClientAccount which is logged in",
                "Decreased ammount of accounts online",
                "Increased ammount of accounts logged out"
            }));
        } else {
            clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
                "Client" + String.valueOf(client.getClientID()) + " has not ClientAccount logged in"
            }));
        }
        clientServerActivitiesInformation.endAction(client.getClientID());
        
        /* System information */
        clientsOnSiteInformation.removeClientOnSite(client.getClientID());
        clientServerActivitiesInformation.endHistory(client.getClientID());
    }
    
    private void setHardDHConnectionWithClient(Client client) {
        BigInteger[] parameters = new DHGenerator().generateParameters(); // get common parameters g and p
        
        DHServer dhServer = new DHServer(this);
        DHClient dhClient = new DHClient(client);
        
        clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
            "Going to set hard DH connection between Server and Client" + String.valueOf(client.getClientID()),
            "Generated DH parameters",
            "g: " + parameters[0].toString(),
            "p: " + parameters[1].toString(),
            "New DHClient intialized",
            "New DHServer intialized",
        }));
        
        dhServer.setParameters(client.getClientID(), parameters);
        dhServer.setConnection(dhClient);
        
        client.setDHClient(dhClient);
        dhServers.put(client, dhServer);
    }
    private void setEasyDHConnectionWithClient(Client client) {
        
        DHServer dhServer = new DHServer(this);        
        DHClient dhClient = new DHClient(client);

        clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
            "Going to set easy DH connection between Server and Client" + String.valueOf(client.getClientID()),
            "New DHClient intialized",
            "New DHServer intialized",
        }));
        dhClient.setHackerkey(dhKey);
        dhServer.setHackerkey(dhClient, dhKey);
        
        client.setDHClient(dhClient);
        dhServers.put(client, dhServer);
        clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
            "Set private common hacked key K for both DHClient and DHServer ",
            "Provided Client" + String.valueOf(client.getClientID()) + " with DHClient",
            "Saved DHServer for Client's ID"
        }));
    }
    public Boolean registerClientAccount(Integer clientID, ClientAccount clientAccount) {
        // GUI to fill
        clientServerActivitiesInformation.startAction(clientID, "Register new ClientAccount of Client" + String.valueOf(clientID));
        clientServerActivitiesInformation.addStep(clientID, Arrays.asList(new String[] {
            "Check if ClientAccount login is used",
        }));
        for ( Integer registeredClientID : clientAccounts.keySet() ) {
            if (Arrays.equals(clientAccounts.get(registeredClientID).getLogin(), clientAccount.getLogin())) {
                try {
                    serverErrorsTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 0, 1));
                } catch (InterruptedException ex) {}
                // GUI to fill
                clientServerActivitiesInformation.addSteps(clientID, Arrays.asList(new String[] {
                    "ClientAccount login is used",
                    "Sent negative response to Client" + String.valueOf(clientID)
                }));
                clientServerActivitiesInformation.endAction(clientID);
                return false;
            }
        }
        clientAccounts.put(clientID, clientAccount);
        accountsCreated++;
        accountsAvailable++;
        //GUI information
        clientServerActivitiesInformation.addSteps(clientID, Arrays.asList(new String[] {
            "ClientAccount login is not used",
            "Increased ammount of accountes created",
            "Increased ammount of accounts available",
            "Sent positive response to Client"
        }));
        clientServerActivitiesInformation.endAction(clientID);
        
        try {
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsCreated), 4, 1));
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsAvailable), 5, 1));
        } catch (InterruptedException e) {}
        
        /* System information */
        accountsAvailableInformation.addAccountAvailable(clientID, clientAccount);
        
        return true;
    }
    public Boolean deleteClientAccount(Integer clientID) {
        // GUI to fill
        clientServerActivitiesInformation.startAction(clientID, "Delete ClientAccount of Client" + String.valueOf(clientID));
        clientServerActivitiesInformation.addStep(clientID, Arrays.asList(new String[] {
            "Check if server database contains Client's Account",
        })); 
        
        if ( !clientAccounts.containsKey(clientID)) {
            clientServerActivitiesInformation.addSteps(clientID, Arrays.asList(new String[] {
                "Server database does not contain Client's Account",
                "Sent negative response to Client"
            }));
            clientServerActivitiesInformation.endAction(clientID);
            return false;
        }
        clientAccounts.remove(clientID);
        accountsDeleted++;
        accountsAvailable--;
        //GUI information
        clientServerActivitiesInformation.addSteps(clientID, Arrays.asList(new String[] {
            "Server database does contain Client's Account",
            "Removed Client's Account",
            "Increased ammount of accounts deleted",
            "Decreased ammount of accounts available",
            "Sent positive response to Client"
        }));
        clientServerActivitiesInformation.endAction(clientID);
        try {
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsDeleted), 6, 1));
            serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsAvailable), 5, 1));
        } catch (InterruptedException e) {}
        /* System information */
        accountsAvailableInformation.removeAccountAvailable(clientID);
        return true;
    }
    public Boolean login(Client client, String decryptedAuthData) {
        // GUI to fill
        clientServerActivitiesInformation.startAction(client.getClientID(), "Log in ClientAccount of Client" + String.valueOf(client.getClientID()));
        clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
            "Check if server database contains Client's Account",
        }));
        String[] authParts = decryptedAuthData.split(" ");
        if ( clientAccounts.containsKey(client.getClientID())) {
            clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
                "Server database does contains Client's Account",
                "Check if authencination data is correct"
            }));
            if ( java.util.Arrays.equals(clientAccounts.get(client.getClientID()).getLogin(),authParts[1].toCharArray()) && java.util.Arrays.equals(clientAccounts.get(client.getClientID()).getPassword(),authParts[3].toCharArray())) {
                accountsLoggedIn++;
                accountsOnline++;
                client.getPersonalData().setLoggedIn(Boolean.TRUE);
                //GUI information
                clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
                    "Authencination data is correct",
                    "Increased ammount of accounts logged in",
                    "Increased ammount of accounts online",
                    "Set Client's parameter \"LoggedIn\" to \"true\"",
                    "Sent positive response to Client"
                }));
                clientServerActivitiesInformation.endAction(client.getClientID());
                
                try {
                    serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsLoggedIn), 7, 1));
                    serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsOnline), 8, 1));
                } catch (InterruptedException e) {}
                
                /* System information */
                accountsOnlineInformation.addAccountOnline(authParts[1]);
                return true;
            } else {

                clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
                    "Authencination data is not correct"
                }));
                try {
                    serverErrorsTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 2, 1));
                } catch (InterruptedException ex) {}
            }
        } else {
            clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
                "Server database does not contains Client's Account",
            }));
            try {
                serverErrorsTableBuffer.put(new SetValueAt(TableManager.ActionType.ADD, 1, 1));
            } catch (InterruptedException ex) {}
        }
        clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
                "Sent negative response to Client"
        }));
        clientServerActivitiesInformation.endAction(client.getClientID());
        return false;
    }
    public Boolean logout(Client client) {
        // GUI to fill
        clientServerActivitiesInformation.startAction(client.getClientID(), "Log out ClientAccount of Client" + String.valueOf(client.getClientID()));
        clientServerActivitiesInformation.addStep(client.getClientID(), Arrays.asList(new String[] {
            "Check if Client's parameter \"LoggedIn\" is set to \"true\"",
        }));
        if ( client.getPersonalData().getLoggedIn() ) {
            client.getPersonalData().setLoggedIn(Boolean.FALSE);
            accountsOnline--;
            accountsLoggedOut++;
            //GUI information
            clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
                "Client's parameter \"LoggedIn\" is set to \"true\"",
                "Increased ammount of accounts logged out",
                "Decreased ammount of accounts online",
                "Sent positive response to Client"
            }));
            clientServerActivitiesInformation.endAction(client.getClientID());
            try {
                serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsLoggedOut), 9, 1));
                serverSummaryTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, String.valueOf(accountsOnline), 8, 1));
            } catch (InterruptedException e) {}
            /* System information */
            accountsOnlineInformation.removeAccountOnline(client.getPersonalData().getLogin());
                
            return true;
        }
        clientServerActivitiesInformation.addSteps(client.getClientID(), Arrays.asList(new String[] {
            "Client's parameter \"LoggedIn\" is not set to \"true\"",
            "Sent negative response to Client"
        }));
        
        clientServerActivitiesInformation.endAction(client.getClientID());
        return false;
    }
    
    @Override
    public void run() {
        childThreads.forEach((thread) -> {
            thread.start();
        });
        // server has help threades, nothing to do
    }
}
