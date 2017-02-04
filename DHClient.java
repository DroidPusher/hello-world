/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Enctyption;

import Entries.ClientInformation;
import Threads.Client;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Den
 */
public class DHClient {
    private final Client client;
    private final String name;
    private final ClientInformation clientInformation;
    // common public primary key
    private BigInteger p, g;
    // private primary key
    private BigInteger a;
    // calculated public key to send to another agent
    private BigInteger A;
    // calculated public key sent by another agent
    private BigInteger B;
    // common private calculated key
    private BigInteger K;
    // encrypted protocol
    GOST_28147_89 protocol;

    public DHClient(Client client) {
        // initial parameters
        this.client = client;
        this.clientInformation = this.client.getClientInformation();
        this.name = "Client " + String.valueOf(client.getClientID());
        
        clientInformation.addStep(Arrays.asList(new String[] {"New DHClient intialized"}));
    }
    
    public BigInteger getOpenKey() {
        clientInformation.addStep(Arrays.asList(new String[] {"Send public key A: " + A.toString()}));
        
        return A;
    }
    public String getName() {
        return name;
    }
    public Client getClient() {
        return client;
    }

    public void setParameters(BigInteger[] parameters) {
        // create private primary key
        a = BigInteger.probablePrime(1024, new Random());
        p = parameters[0];
        g = parameters[1];
        A = g.modPow(a, p);
        
        clientInformation.addStep(Arrays.asList(new String[] {"Got DH parameters", "g: " + g.toString(), "p: " + p.toString(),
            "Got public key A: " + A.toString()}));
    }
    public void setEncryptionKey(BigInteger openKey) {
        this.B = openKey;
        K = B.modPow(a, p);
        // initialize encrypted protocol
        protocol = new GOST_28147_89(K);

        clientInformation.addStep(Arrays.asList(new String[] {"Got foreign public key B: " + B.toString(), "Got private key K: " + K.toString(), "Initialized GOST 28147-89 protocol"}));
    }
    public void setHackerkey(BigInteger K) {
        this.K = K;
        // initialize encrypted protocol
        protocol = new GOST_28147_89(K);

        clientInformation.addStep(Arrays.asList(new String[] {"Got private hacked key K: " + K.toString(), "Initialized GOST 28147-89 protocol"}));
    }

    public byte[] request(String  message) {
        // encrypt each message
        byte[] encryptedMessage = protocol.encrypt(message);
        
        clientInformation.addStep(Arrays.asList(new String[] {"(DH Client)", "Encrypt request message to Server", "Message: " + message, "Encrypting request done", "EncryptedMessage:", "(byte array) " + Arrays.toString(encryptedMessage), "(string) " + new String(encryptedMessage)}));
        return encryptedMessage;
    }

    public void decryptResponse(byte[] encryptedMessage) {
        // decrypt encrypted response
        String serverResponse = protocol.decrypt(encryptedMessage);
        String[] serverResponseParts = serverResponse.split(" ");
        client.setResponsed(true);
        client.setResult((serverResponseParts[0] + " " + serverResponseParts[1] + " " + serverResponseParts[2]).equals("Russia became stronger"));
        
        clientInformation.addStep(Arrays.asList(new String[] {"(DH Client)", "Decrypt Server response", "EncryptedMessage:", "(byte array) " + Arrays.toString(encryptedMessage), "(sting) " + new String(encryptedMessage),
            "Decrypting response message done", "DecryptedMessage: " + serverResponse, "Response result: " + String.valueOf(client.getResult())}));
    }
    @Override
    public String toString() {
        
        return  ((p != null) ? ("Common keys:\r\np: " + p.toString() + "\r\n") : "") + ((g != null) ?  ("g: " + g.toString() + "\r\n") : "")
                + ((a != null) ? ("Private own key:\r\na: " + a.toString() + "\r\n") : "")
                + ((A != null) ? ("Public own key:\r\nA: " + A.toString() + "\r\n") : "")
                + ((B != null) ? ("Public foreign key:\r\nB: " + B.toString() + "\r\n") : "")
                + ((K != null) ? ("Private common key:\r\nK: " + K.toString()) : "");
    }
}
