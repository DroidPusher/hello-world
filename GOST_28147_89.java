/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Enctyption;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

/**
 *
 * @author Den
 */
public class GOST_28147_89 {
    private int blockSize = 8; // bytes
    byte[] key;
    byte[][] keys;
    byte[][] table;

    public GOST_28147_89(BigInteger publicKey) {
        key  = new byte[32];
        System.arraycopy(publicKey.toByteArray(), 1, key, 0, key.length);

        // standard table for replacement
        table = new byte[][] {
                {0x4, 0xA, 0x9, 0x2, 0xD, 0x8, 0x0, 0xE, 0x6, 0xB, 0x1, 0xC, 0x7, 0xF, 0x5, 0x3 },
                {0xE, 0xB, 0x4, 0xC, 0x6, 0xD, 0xF, 0xA, 0x2, 0x3, 0x8, 0x1, 0x0, 0x7, 0x5, 0x9 },
                {0x5, 0x8, 0x1, 0xD, 0xA, 0x3, 0x4, 0x2, 0xE, 0xF, 0xC, 0x7, 0x6, 0x0, 0x9, 0xB },
                {0x7, 0xD, 0xA, 0x1, 0x0, 0x8, 0x9, 0xF, 0xE, 0x4, 0x6, 0xC, 0xB, 0x2, 0x5, 0x3 },
                {0x6, 0xC, 0x7, 0x1, 0x5, 0xF, 0xD, 0x8, 0x4, 0xA, 0x9, 0xE, 0x0, 0x3, 0xB, 0x2 },
                {0x4, 0xB, 0xA, 0x0, 0x7, 0x2, 0x1, 0xD, 0x3, 0x6, 0x8, 0x5, 0x9, 0xC, 0xF, 0xE },
                {0xD, 0xB, 0x4, 0x1, 0x3, 0xF, 0x5, 0x9, 0x0, 0xA, 0xE, 0x7, 0x6, 0x8, 0x2, 0xC },
                {0x1, 0xF, 0xD, 0x0, 0x5, 0x7, 0xA, 0x4, 0x9, 0x2, 0x3, 0xE, 0x6, 0xB, 0x8, 0xC }
        };
        // check table for repeat values
        checkTable();
    }
    private void checkTable() {
        for ( int i = 0; i < 8; i++ ) {
            for ( int j = 0; j < 16; j++ ) {
                for ( int k = j + 1; k < 16; k++ ) {
                    if ( table[i][j] == table[i][k]) {
                        System.out.println("(" + String.valueOf(i) + ", " + String.valueOf(j) + ") = ("
                                + String.valueOf(i) + ", " + String.valueOf(k) + ")" );
                    }
                }
            }
        }
    }

    public byte[] encrypt(String message) {
        setKeys(true);
        // divide message into block of 64 bit size
        byte[] byteMessage = message.getBytes();

        int numberOfBlocks;
        if ( (byteMessage.length % blockSize) > 0 ) {
            numberOfBlocks = 1 + byteMessage.length / blockSize;
        } else {
            numberOfBlocks = byteMessage.length / blockSize;
        }

        /* put message in 8 byte blocks*/
        byte[] block = new byte[blockSize];
        byte[] b_encryptedMessage = new byte[blockSize*numberOfBlocks];
        for ( int i = 0; i < numberOfBlocks; i++ ) {
            // fill block with '0' and copy part of message into it
            Arrays.fill(block, (byte) 0);
            if (i == (numberOfBlocks - 1)) {
                System.arraycopy(byteMessage, i*blockSize, block, 0, byteMessage.length - i*blockSize);
            } else {
                System.arraycopy(byteMessage, i*blockSize, block, 0, blockSize);
            }

            /* for each block perform algorithm*/

            // divide block into two parts
           byte[] A = new byte[blockSize / 2], B = new byte[blockSize / 2];
            System.arraycopy(block, 0, A, 0, A.length);
            System.arraycopy(block, A.length, B, 0, B.length);

            // make 32 iterations
            for ( int k = 0; k < 32; k++ ) {
                byte[] buf = A;
                A = xor(B, f(A, keys[k]));
                B = buf;
            }
            // include block algorithm result to commom result
            System.arraycopy(A, 0, b_encryptedMessage, 8*i+4, A.length);
            System.arraycopy(B, 0 ,b_encryptedMessage, 8*i, B.length);
        }
        return b_encryptedMessage;
    }
    public String decrypt(byte[] byteMessage) {
        setKeys(false);
        // divide message into block of 64 bit size

        int numberOfBlocks;
        if ( (byteMessage.length % blockSize) > 0 ) {
            numberOfBlocks = 1 + byteMessage.length / blockSize;
        } else {
            numberOfBlocks = byteMessage.length / blockSize;
        }

        /* put message in 8 byte blocks*/
        byte[] block = new byte[blockSize];
        byte[] b_encryptedMessage = new byte[blockSize*numberOfBlocks];
        for ( int i = 0; i < numberOfBlocks; i++ ) {
            Arrays.fill(block, (byte) 0);
            if (i == (numberOfBlocks - 1)) {
                System.arraycopy(byteMessage, i*blockSize, block, 0, byteMessage.length - i*blockSize);
            } else {
                System.arraycopy(byteMessage, i*blockSize, block, 0, blockSize);
            }

            /* for each block perform algorithm*/

            // divide block into two parts
            byte[] A = new byte[blockSize / 2], B = new byte[blockSize / 2];
            System.arraycopy(block, 0, A, 0, A.length);
            System.arraycopy(block, A.length, B, 0, B.length);

            // make 32 iterations
            for ( int k = 0; k < 32; k++ ) {
                byte[] buf = A;
                A = xor(B, f(A, keys[k]));
                B = buf;
            }
            System.arraycopy(A, 0, b_encryptedMessage, 8*i+4, A.length);
            System.arraycopy(B, 0 ,b_encryptedMessage, 8*i, B.length);
        }
        int erase = 0;
        for ( int i = b_encryptedMessage.length - 1; i > - 1; i--) {
            if ( b_encryptedMessage[i] == 0) {
                erase ++;
            }
        }
        byte[] realMessage = new byte[b_encryptedMessage.length - erase];
        System.arraycopy(b_encryptedMessage, 0, realMessage, 0, realMessage.length);
        return new String(realMessage);
    }

    public byte[] xor(byte[] A, byte[] B) {
        int size = 0;
        if (A.length > B.length ) {
            size = A.length;
        } else {
            size = B.length;
        }
        byte[] result = new byte[size];
        for ( int i = 0; i < size; i++ ) {
            result[i] = 0;
            for ( int j = 0; j < 8; j++ ) {
                if ( (A[i] & (1<<j)) != (B[i] & (1<<j)) ) {
                    result[i] += 1<<j;
                }
            }
        }

        return result;
    }

    public byte[] f(byte[] A, byte[] K) {

        // (A+K)mod(2^32)
        int result = ByteBuffer.wrap(A).getInt() + ByteBuffer.wrap(K).getInt();
        result = result % (int)Math.pow(2, 32);
        BitSet b_result = convert(result);
        long i_result = 0;

        BitSet block;
        for (int i = 0; i < 8; i++ ) {
            block = b_result.get(i*4, (i+1)*4);
            i_result = i_result + table[i][(int)convert(block)]* (long)Math.pow(2, i*4);
        }
        b_result = convert(i_result);
        return circleLeft(b_result.toByteArray(), 11);
    }

    public byte[] circleLeft(byte[] a, int circles) {
        int r = 0;
        for ( int i = 0; i < a.length; i++ ) {
            r += a[i]<<(8*i);
        }
        r = (r << circles) | (r >> (a.length*8 - circles));
        return ByteBuffer.allocate(4).putInt(r).array();
    }
    public void setKeys(boolean encrypt) {
        keys = new byte[32][4];
        if ( encrypt) {
            for ( int i = 0; i < 3; i++ ) {
                for (int j = 0; j < 8; j++) {
                    System.arraycopy(key, j*4, keys[i*8 + j], 0, keys[i*8 + j].length);
                }
            }
            for (int j = 0; j < 8; j++) {
                System.arraycopy(key, 28 - j*4, keys[24 + j], 0, keys[24 + j].length);
            }
        } else {
            for ( int i = 1; i < 4; i++ ) {
                for (int j = 0; j < 8; j++) {
                    System.arraycopy(key, 28 - j*4, keys[i*8 + j], 0, keys[i*8 + j].length);
                }
            }
            for (int j = 0; j < 8; j++) {
                System.arraycopy(key,  j*4, keys[j], 0, keys[j].length);
            }
        }

    }

    public static BitSet convert(long value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    public static long convert(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

}
