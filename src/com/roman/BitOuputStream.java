package com.roman;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Created by roman on 30.10.2016.
 */
public class BitOuputStream {
    private FileOutputStream fos;
    private String rest = "";


    public BitOuputStream(FileOutputStream pFos) {
        fos = pFos;
    }

    public void write(String bs) throws IOException {
        int n = bs.length();
        int restN = rest == null ? 0 : rest.length();

        if (n + restN < 8) {
            String newRest = new String();
            for (int i = 0; i < restN; i++) {
                newRest += rest.charAt(i);
            }
            for (int i = restN; i < n + restN; i++) {
                newRest += bs.charAt(i - restN);
            }
            rest = newRest;
        } else {
            String outByte = "";
            int i;
            for (i = 0; i < restN; i++) {
                outByte += rest.charAt(i);
            }
            for (; i < 8; i++) {
                outByte += bs.charAt(i - restN);
            }
            short a = Short.parseShort(outByte, 2);
            ByteBuffer bytes = ByteBuffer.allocate(2).putShort(a);
            byte[] buf = new byte[1];
            buf[0] = (byte)a;
            fos.write(buf);
            int newRestN = n - i;
            if (newRestN > 0) {
                rest = "";
                for (; i < n; i++) {
                    rest += bs.charAt(i);
                }
            }
        }
    }

    public void writeRest() throws IOException {
        short restSize = (short) rest.length();
        for (int i = 0; i < 8 - rest.length(); i++) {
            rest += '1';
        }
        short a = Short.parseShort(rest, 2);
        ByteBuffer bytes = ByteBuffer.allocate(2).putShort(a);
        byte[] buf = new byte[1];
        buf[0] = (byte)a;
        fos.write(buf);
        byte[] countOfBits = ByteBuffer.allocate(Short.BYTES).putShort(restSize).array();
        fos.write(countOfBits);
    }

    public void close() throws IOException {
        fos.close();
    }
}
