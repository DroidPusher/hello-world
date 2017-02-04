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
public class ServerLoginThread extends CheckThread {
    private final Server server;
    private Integer index = 0;
    
    public ServerLoginThread(Server server) {
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
                
                ClientRequest clientRequest = server.getLoginBuffer().take();
                Client client = clientRequest.getClient();
                DHServer dhServer = server.getDHServers().get(client);
                
                Server.loginTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "Server:\r\n" + Arrays.toString(clientRequest.getMessage()), index, 1));
                index++;

                String decryptedAuthData = dhServer.decryptRequest(clientRequest.getMessage());
                
                server.getLoginThreadInformation().addNewClientRequest();
                server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Took new ClientRequest from LoginBuffer",
                    "ClientRequest came from Client" + String.valueOf(client.getClientID()),
                    "ClientRequest containes encrypted request message:",
                    "(byte array) " + Arrays.toString(clientRequest.getMessage()),
                    "(string) " + new String(clientRequest.getMessage())
                }));
                server.getLoginThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Got DHServer for Client" + String.valueOf(client.getClientID())
                }));
                server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Decrypted request message:",
                    decryptedAuthData
                }));
                server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Called Server method \"login(Client, DecryptedAuthData)\" to login Client's Account"
                }));
                
                if ( server.login(client, decryptedAuthData) ) {
                    Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to login", "Successful"}));
                    
                    String responseMessage = "Russia became stronger - you have logged in successfully, glad to see you again, msr. "
                            + client.getPersonalData().getFirstName() + " " + client.getPersonalData().getLastName() +  "!!!";
                    byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                    
                    server.getLoginThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                        "Got positive result from Server"
                    }));
                    server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Going to send positive response message to Client" + String.valueOf(client.getClientID()) + ":",
                            responseMessage
                    }));
                    server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Encrypted response message:",
                        "(byte array) " + Arrays.toString(encryptedResponseMessage),
                        "(string) " + new String(encryptedResponseMessage)
                    }));
                    
                } else {
                    Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to login", "Wrong login or password"}));
                    
                    String responseMessage = "Russia is waiting - you have passed wrong authencination data, try again!!!";
                    byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                    
                    server.getLoginThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                        "Got negative result from Server"
                    }));
                    server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Going to send negative response message to Client" + String.valueOf(client.getClientID()) + ":",
                        responseMessage
                    }));
                    server.getLoginThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                        "Encrypted response message:",
                        "(byte array) " + Arrays.toString(encryptedResponseMessage),
                        "(string) " + new String(encryptedResponseMessage)
                    }));
                    
                }
                
                synchronized (server.getClientMonitor(client)) {
                    server.getClientMonitor(client).notifyAll();
                }
                
                server.getLoginThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Sent encrypted response message to Client" + String.valueOf(client.getClientID()),
                    "Got Client's Monitor",
                    "Invoked \"notifyAll()\" on Client's Monitor"
                }));
                server.getLoginThreadInformation().endNewClientRequest();
                
            }
        } catch (InterruptedException ex) {}
    }
}
