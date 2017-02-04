/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Enctyption;

import Entries.Server;
import Entries.ServerInformation;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Den
 */
public class DHServer {
    private final ServerInformation clientServerActivitiesInformation, dataTransferInformation;
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

    private DHClient dhClient;

    public DHServer(Server server) {
        clientServerActivitiesInformation = server.getClientServerActivitiesInformation();
        dataTransferInformation = server.getDataTransferInformation();
    }
    
    public BigInteger getOpenKey() {
        return A;
    }

    public void setConnection(DHClient dhClient) {
        this.dhClient = dhClient;
        // provide agent with common public primary key
        dhClient.setParameters(new BigInteger[] {p, g});
        // give common public primary key to each other
        dhClient.setEncryptionKey(A);
        this.setEncryptionKey(dhClient.getOpenKey());
        clientServerActivitiesInformation.addSteps(dhClient.getClient().getClientID(), Arrays.asList(new String[] {
            "Saved DHClient for this DHServer",
            "Provided DHClient with DH parameters", 
            "Sent public key A to DHClient",
        }));
    }

    public void setParameters(Integer clientID, BigInteger[] parameters) {
        // create private primary key
        a = BigInteger.probablePrime(1024, new Random());
        p = parameters[0];
        g = parameters[1];
        A = g.modPow(a, p);
        clientServerActivitiesInformation.addStep(clientID, Arrays.asList(new String[] {
            "Got DH parameters",
            "g: " + g.toString(), 
            "p: " + p.toString(),
            "Got public key A: " + A.toString()
        }));
        clientServerActivitiesInformation.addStep(clientID, Arrays.asList(new String[] {
            "Got public key A: " + A.toString()
        }));
    }

    public void setEncryptionKey(BigInteger openKey) {
        this.B = openKey;
        K = B.modPow(a, p);
        // initialize encrypted protocol
        protocol = new GOST_28147_89(K);
        clientServerActivitiesInformation.addSteps(dhClient.getClient().getClientID(), Arrays.asList(new String[] {
            "Got foreign public key B: " + B.toString(),
            "Got private key K: " + K.toString(),
            "Initialized GOST 28147-89 protocol"
        }));

    }
    public void setHackerkey(DHClient dhClient, BigInteger K) {
        this.dhClient = dhClient;
        this.K = K;
        // initialize encrypted protocol
        protocol = new GOST_28147_89(K);
        clientServerActivitiesInformation.addSteps(dhClient.getClient().getClientID(), Arrays.asList(new String[] {
            "Saved DHClient for this DHServer",
            "Got private hacked key K: " + K.toString(),
            "Initialized GOST 28147-89 protocol"
        }));
    }
    public byte[] response(String  message) {
        // encrypt responce
        byte[] encryptedMessage = protocol.encrypt(message);

        dhClient.decryptResponse(encryptedMessage);
//        System.out.println("Server is responsing to " + dhClient.getName() + ":\r\n" + message + "\r\n" + "Server has encrypted response:\r\n" + Arrays.toString(encryptedMessage) + "\r\n\r\n");
//        dataTransferInformation.addStep(Arrays.asList(new String[] {"Going to encrypt response to " + dhClient.getName() + ":", message,
//        "Enctypted message:", "(byte array) " + Arrays.toString(encryptedMessage), "(string) " + new String(encryptedMessage)} ) );
        dataTransferInformation.addDataTransfer(encryptedMessage);
        return encryptedMessage;
    }

    public String decryptRequest(byte[] encryptedMessage) {
        // decrypt encrypted request
        String decryptedRequest = protocol.decrypt(encryptedMessage);
//        System.out.println("Server has received encrypted request:\r\n" + Arrays.toString(encryptedMessage) + "\r\n" + "Server has decrypted request:\r\n" + decryptedRequest + "\r\n\r\n");
//        dataTransferInformation.addStep(Arrays.asList(new String[] {"Going to decrypt encrypted request from " + dhClient.getName() +":", Arrays.toString(encryptedMessage), "(string) " + new String(encryptedMessage),
//        "Ecrypted request:", decryptedRequest} ) );
        dataTransferInformation.addDataTransfer(encryptedMessage);        
        return decryptedRequest;
    }
}
