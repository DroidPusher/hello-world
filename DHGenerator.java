/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Enctyption;

import Entries.Server;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;

/**
 *
 * @author Den
 */
public class DHGenerator {
    public DHGenerator() { }
    public BigInteger[] generateParameters() {
        try {
            try {
                AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
                paramGen.init(1024);
                AlgorithmParameters params = paramGen.generateParameters();
                DHParameterSpec dhSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
                return new BigInteger[] { dhSpec.getP(), dhSpec.getG() };
            } catch (InvalidParameterSpecException e) {}
        } catch (NoSuchAlgorithmException e) { }
        return null;
    }
}
