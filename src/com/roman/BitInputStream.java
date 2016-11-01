package com.roman;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;

/**
 * Created by roman on 30.10.2016.
 */
public class BitInputStream {
    private FileInputStream fis;
    private BitSet bitSetBuffer;
    private int currentIndex = 0;
    private byte[] buffer = new byte[1];
    public BitInputStream(FileInputStream pFis) {
        fis = pFis;
    }

    public boolean read() throws IOException {
        if (currentIndex == 0) {
            currentIndex = 8;
            int read = fis.read(buffer);
            bitSetBuffer = BitSet.valueOf(buffer);
        }
        currentIndex--;

        return bitSetBuffer.get(currentIndex);
    }

    public void close() throws IOException {
        fis.close();
    }
}
