/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Enctyption.DHServer;
import Entries.ClientAccount;
import Entries.ClientRequest;
import Entries.Server;
import Entries.SetValueAt;
import GUI.MainWindow;
import GUITools.TableManager;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Den
 */
public class ServerCreateAccountThread extends CheckThread {
    private final Server server;
    private Integer index = 0;
    
    public ServerCreateAccountThread(Server server) {
        this.server =  server;
    }
    
    @Override
    public void run() {
        try{
            while ( true ) {
//                System.out.println("ServerCreateAccountThread KNOCK paused: " + pause);
                synchronized ( MainWindow.serverMonitor ) {
                    while ( MainWindow.serverPaused ) {
                        try {
                            MainWindow.serverMonitor.wait();
                        } catch (InterruptedException e ) {}
                    }
                }
                synchronized ( MainWindow.systemMonitor ) {
                    while ( MainWindow.systemPaused ) {
                        try {
                            MainWindow.systemMonitor.wait();
                        } catch (InterruptedException e ) {}
                    }
                }
                if ( multipliedPeriod > 0 ) {
                    long timeSleep = new Random().nextInt(multipliedPeriod);
                    Thread.sleep(timeSleep);
                }
                
                // get client request
                ClientRequest clientRequest = server.getCreateAccountBuffer().take();
                // get DHServer for client
                Client client = clientRequest.getClient();
                DHServer dhServer = server.getDHServers().get(client);

                // GUI to fill
                Server.createAccountTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "Server:\r\n" + Arrays.toString(clientRequest.getMessage()), index, 1));
                index++;
                
                // decrypt request message
                String decryptedClientAccount = dhServer.decryptRequest(clientRequest.getMessage());
                
                server.getCreateAccountThreadInformation().addNewClientRequest();
                server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Took new ClientRequest from CreateAccountBuffer",
                    "ClientRequest came from Client" + String.valueOf(client.getClientID()),
                    "ClientRequest containes encrypted request message:",
                    "(byte array) " + Arrays.toString(clientRequest.getMessage()),
                    "(string) " + new String(clientRequest.getMessage())
                }));
                server.getCreateAccountThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Got DHServer for Client" + String.valueOf(client.getClientID())
                }));
                server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Decrypted request message:",
                    decryptedClientAccount
                }));
                server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Called Server method \"registerClientAccount(ClientID, ClientAccount)\" to register new ClientAccount"
                }));
                
                if ( server.registerClientAccount(client.getClientID(), new ClientAccount(decryptedClientAccount)) ) {
                    Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to create account", "Successful"}));
                    
                    String responseMessage = "Russia became stronger - you have created account successfully, welcome, friend!!!";
                    byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                    
                    server.getCreateAccountThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                        "Got positive result from Server"
                    }));
                    server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Going to send positive response message to Client" + String.valueOf(client.getClientID()) + ":",
                            responseMessage
                    }));
                    server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Encrypted response message:",
                        "(byte array) " + Arrays.toString(encryptedResponseMessage),
                        "(string) " + new String(encryptedResponseMessage)
                    }));
                } else {
                    Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to create account", "Unsuccessful"}));
                    
                    String responseMessage = "Russia is waiting - this login is used already, enter another, please!!!";
                    byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                    
                    server.getCreateAccountThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                        "Got negative result from Server"
                    }));
                    server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Going to send negative response message to Client" + String.valueOf(client.getClientID()) + ":",
                        responseMessage
                    }));
                    server.getCreateAccountThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Encrypted response message:",
                        "(byte array) " + Arrays.toString(encryptedResponseMessage),
                        "(string) " + new String(encryptedResponseMessage)
                    }));
                    
                }
                synchronized (server.getClientMonitor(client)) {
                    server.getClientMonitor(client).notifyAll();
                }
                server.getCreateAccountThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Sent encrypted response message to Client" + String.valueOf(client.getClientID()),
                    "Got Client's Monitor",
                    "Invoked \"notifyAll()\" on Client's Monitor"
                }));
                server.getCreateAccountThreadInformation().endNewClientRequest();
            }
        } catch (InterruptedException ex) {}
    }
}