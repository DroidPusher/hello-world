/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import Enctyption.DHServer;
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
public class ServerLogoutThread extends CheckThread {
    private final Server server;
    private Integer index = 0;
    
    public ServerLogoutThread(Server server) {
        this.server =  server;
    }
    
    @Override
    public void run() {
        try{
            while ( true ) {
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
                
                ClientRequest clientRequest = server.getLogoutBuffer().take();
                Client client = clientRequest.getClient();
                DHServer dhServer = server.getDHServers().get(client);
                
                Server.logoutTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "Server:\r\n" + Arrays.toString(clientRequest.getMessage()), index, 1));
                index++;
                
                String logoutRequest = dhServer.decryptRequest(clientRequest.getMessage());
                
                server.getLogoutThreadInformation().addNewClientRequest();
                server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Took new ClientRequest from LogoutBuffer",
                    "ClientRequest came from Client" + String.valueOf(client.getClientID()),
                    "ClientRequest containes encrypted request message:",
                    "(byte array) " + Arrays.toString(clientRequest.getMessage()),
                    "(string) " + new String(clientRequest.getMessage())
                }));
                server.getLogoutThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Got DHServer for Client" + String.valueOf(client.getClientID())
                }));
                server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Decrypted request message:",
                    logoutRequest
                }));
                server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Called Server method \"logout(Client)\" to logout Client's Account"
                }));
                
                if ( server.logout(client) ) {
                    Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to logout", "Successful"}));
                    
                    String responseMessage = "Russia became stronger - you have logged out, good luck!!!";
                    byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                    
                    server.getLogoutThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                        "Got positive result from Server"
                    }));
                    server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Going to send positive response message to Client" + String.valueOf(client.getClientID()) + ":",
                            responseMessage
                    }));
                    server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Encrypted response message:",
                        "(byte array) " + Arrays.toString(encryptedResponseMessage),
                        "(string) " + new String(encryptedResponseMessage)
                    }));
                } else {
                    Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to logout", "Unsuccessful"}));
                    
                    String responseMessage = "Russia is waiting - you have not logged out, somehow you not logged in!!![bug]";
                    byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                    
                    server.getLogoutThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                        "Got negative result from Server"
                    }));
                    server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Going to send negative response message to Client" + String.valueOf(client.getClientID()) + ":",
                        responseMessage
                    }));
                    server.getLogoutThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Encrypted response message:",
                        "(byte array) " + Arrays.toString(encryptedResponseMessage),
                        "(string) " + new String(encryptedResponseMessage)
                    }));
                    
                }

                synchronized (server.getClientMonitor(client)) {
                    server.getClientMonitor(client).notifyAll();
                }
                
                server.getLogoutThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Sent encrypted response message to Client" + String.valueOf(client.getClientID()),
                    "Got Client's Monitor",
                    "Invoked \"notifyAll()\" on Client's Monitor"
                }));
                server.getLogoutThreadInformation().endNewClientRequest();
            }
        } catch (InterruptedException ex) {}
    }
}
