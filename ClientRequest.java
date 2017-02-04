/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entries;

import Threads.Client;

/**
 *
 * @author Den
 */
public class ClientRequest {
    private final Client client;
    private final byte[] message;
    public ClientRequest(Client client, byte[] message) {
        this.client = client;
        this.message = message;
    }
    public byte[] getMessage() {
        return message;
    }
    public Client getClient() {
        return client;
    }
}
