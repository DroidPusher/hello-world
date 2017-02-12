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
public class ServerLeaveThread extends CheckThread {
    private final Server server;
    private Integer index = 0;
    
    public ServerLeaveThread(Server server) {
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
                
                ClientRequest clientRequest = server.getLeaveQueue().take();
                Client client = clientRequest.getClient();
                DHServer dhServer = server.getDHServers().get(client);

                Server.leaveTableBuffer.put(new SetValueAt(TableManager.ActionType.PUT, "Server:\r\n" + Arrays.toString(clientRequest.getMessage()), index, 1));
                index++;
                
                String leaveRequest = dhServer.decryptRequest(clientRequest.getMessage());

                Server.serverTableBuffer.put(new SetValueAt(TableManager.ActionType.CREATE, new Object [] {"Client " + String.valueOf(client.getClientID()) + " tryed to leave", "Successful"}));
                
                server.getLeaveThreadInformation().addNewClientRequest();
                server.getLeaveThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Took new ClientRequest from LeaveBuffer",
                    "ClientRequest came from Client" + String.valueOf(client.getClientID()),
                    "ClientRequest containes encrypted request message:",
                    "(byte array) " + Arrays.toString(clientRequest.getMessage()),
                    "(string) " + new String(clientRequest.getMessage())
                }));
                server.getLeaveThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Got DHServer for Client" + String.valueOf(client.getClientID())
                }));
                server.getLeaveThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Decrypted request message:",
                    leaveRequest
                }));
                server.getLeaveThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Called Server method \"removeClient(Client)\" to remove Client from site"
                }));
                
                
                
                String responseMessage = "Russia became stronger - come back ASAP, we will wait for you!!!";
                byte[] encryptedResponseMessage = dhServer.response(responseMessage);
                
                server.getLeaveThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Going to send response message to Client" + String.valueOf(client.getClientID()) + ":",
                        responseMessage
                }));
                server.getLeaveThreadInformation().addServerHelpThreadStep(Arrays.asList(new String[] {
                    "Encrypted response message:",
                    "(byte array) " + Arrays.toString(encryptedResponseMessage),
                    "(string) " + new String(encryptedResponseMessage)
                }));
                    
                synchronized (server.getClientMonitor(client)) {
                    server.getClientMonitor(client).notifyAll();
                }     
                server.removeClient(client);
                
                server.getLeaveThreadInformation().addServerHelpThreadSteps(Arrays.asList(new String[] {
                    "Sent encrypted response message to Client" + String.valueOf(client.getClientID()),
                    "Got Client's Monitor",
                    "Invoked \"notifyAll()\" on Client's Monitor"
                }));
                server.getLeaveThreadInformation().endNewClientRequest();
                
            }
        } catch (InterruptedException ex) {}
    }
}
