package com.roman;

/**
 * Created by roman on 16.10.2016.
 */
public class ByteFrequency {
    public byte b;
    public double freq;
    public double coord;

    public boolean equalsToByte(Object o) {
        Byte pByte = (Byte) o;
        return pByte.equals(b);
    }
}
