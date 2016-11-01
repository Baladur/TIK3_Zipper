package com.roman;

/**
 * Created by roman on 18.10.2016.
 */
public class ByteRationalFrequency extends ByteFrequency {
    public Rational rationalFreq;
    public Rational rationalCoord;

    public ByteRationalFrequency() {
        rationalFreq = new Rational();
        rationalCoord = new Rational();
    }
}
