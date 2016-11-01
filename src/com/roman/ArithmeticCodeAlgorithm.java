package com.roman;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by roman on 16.10.2016.
 */
public class ArithmeticCodeAlgorithm implements ZipAlgorithm {
    public static final long HDR_SIZE = 309;
    /*SIGN 3 bytes (ROM)
    * LENGTH OF NAME 4 bytes
    * NAME OF FILE $LENGTH OF NAME bytes
    * LENGTH OF FREQUENCY TABLE 4 bytes
    * FREQS OF BYTES 256 * 9 bytes
    * LENGTH OF ORIGINAL DATA 8 bytes
    * ZIPPED DATA
    * */

    @Override
    public void zip(String pFromFile, String pToFile) throws IOException {
        File fromFile = new File(pFromFile);
        File toFile = new File(pToFile);
        List<ByteFrequency> freqs = getFrequencyTable(fromFile);
        double codeNumber = 0;
        FileInputStream fis = new FileInputStream(fromFile);
        //BufferedWriter bw = new BufferedWriter(new FileWriter(toFile));
        FileOutputStream fos = new FileOutputStream(toFile);
        byte[] sign = {'R', 'O', 'M'};
        byte[] nameLengthBuf = ByteBuffer.allocate(Integer.BYTES).putInt(pFromFile.length()).array();
        byte[] nameBuf = pFromFile.getBytes();
        byte[] freqTableLengthBuf = ByteBuffer.allocate(Integer.BYTES).putInt(freqs.size()).array();
        byte[] freqBuf;
        byte[] fileLengthBuf = ByteBuffer.allocate(Long.BYTES).putLong(fromFile.length()).array();
        byte[] codeNumberBuf;
        fos.write(sign);
        fos.write(nameLengthBuf);
        fos.write(nameBuf);
        fos.write(freqTableLengthBuf);
        for (ByteFrequency bf : freqs) {
            fos.write(bf.b);
            freqBuf = ByteBuffer.allocate(Double.BYTES).putDouble(bf.freq).array();
            fos.write(freqBuf);
            //RATIONAL VERSION START
            /*fos.write(bf.b);
            Rational freq = ((ByteRationalFrequency)bf).rationalFreq;
            byte[] freqUp = ByteBuffer.allocate(Integer.BYTES).putInt(freq.up).array();
            byte[] freqDown = ByteBuffer.allocate(Integer.BYTES).putInt(freq.down).array();
            fos.write(freqUp);
            fos.write(freqDown);*/
            //RATIONAL VERSION END
        }
        fos.write(fileLengthBuf);

        byte[] buf = new byte[1];
        double lowOld = 0;
        double highOld = 1;
        double high = highOld;
        double low = lowOld;
        double rangeLow = 0;
        double rangeHigh = 0;
        //RATIONAL VERSION START
        /*Rational lowOld = new Rational();
        Rational highOld = new Rational(1, 1);
        Rational high = new Rational(highOld);
        Rational low = new Rational(lowOld);
        Rational rangeLow = new Rational();
        Rational rangeHigh = new Rational();*/
        //RATIONAL VERSION END
        int batchCntr = 0;
        int cntr = 0;
        while (fis.read(buf) > 0) {
            for (ByteFrequency bf : freqs) {
                if (bf.equalsToByte(buf[0])) {
                    if (freqs.indexOf(bf) == 0) {
                        rangeLow = 0;
                        //RATIONAL VERSION START
                        /*rangeLow = new Rational();*/
                        //RATIONAL VERSION END
                    } else {
                        rangeLow = freqs.get(freqs.indexOf(bf)-1).coord;
                        //RATIONAL VERSION START
                        /*rangeLow = new Rational(((ByteRationalFrequency)freqs.get(freqs.indexOf(bf)-1)).rationalCoord);*/
                        //RATIONAL VERSION END
                    }
                    rangeHigh = bf.coord;
                    low = lowOld + (highOld - lowOld) * rangeLow;
                    high = lowOld + (highOld - lowOld) * rangeHigh;
                    lowOld = low;
                    highOld = high;
                    //RATIONAL VERSION START
                    /*rangeHigh = new Rational(((ByteRationalFrequency)bf).rationalCoord);
                    low = new Rational(new Rational(lowOld).plus(new Rational(new Rational(rangeLow).multiply(new Rational(highOld).minus(lowOld)))));
                    high = new Rational(new Rational(lowOld).plus(new Rational(new Rational(rangeHigh).multiply(new Rational(highOld).minus(lowOld)))));
                    low = Rational.findEconomical(low, high, true);
                    high = Rational.findEconomical(low, high, false);
                    lowOld = new Rational(low);
                    highOld = new Rational(high);*/

                    //RATIONAL VERSION END
                    //System.out.println("lowold = " + lowOld + ", highold = " + highOld);
                }
            }
            batchCntr++;
            cntr++;
            if (batchCntr == 16 || cntr == fromFile.length()) {
                codeNumberBuf = ByteBuffer.allocate(Double.BYTES).putDouble(low).array();
                fos.write(codeNumberBuf);
                batchCntr = 0;
                lowOld = 0;
                highOld = 1;
                low = lowOld;
                high = highOld;
                rangeLow = 0;
                rangeHigh = 0;
                //RATIONAL VERSION START
                /*byte[] codeUp = ByteBuffer.allocate(Integer.BYTES).putInt(low.up).array();
                byte[] codeDown = ByteBuffer.allocate(Integer.BYTES).putInt(low.down).array();
                fos.write(codeUp);
                fos.write(codeDown);
                batchCntr = 0;
                lowOld = new Rational();
                highOld = new Rational(1, 1);
                low = new Rational(lowOld);
                high = new Rational(highOld);
                rangeLow = new Rational();
                rangeHigh = new Rational();*/
                //RATIONAL VERSION END
                if (cntr == fromFile.length()) {
                    break;
                }
            }
        }
        fis.close();
        fos.close();

    }



    @Override
    public void unzip(String pFromFile) throws IOException, ZipException {
        File fromFile = new File(pFromFile);
        FileInputStream fis = new FileInputStream(fromFile);
        byte[] sign = new byte[3];
        fis.read(sign);
        if (sign[0] != 'R' || sign[1] != 'O' || sign[2] != 'M') {
            throw new ZipException("Invalid signature");
        }
        byte[] fileNameLengthBuf = new byte[Integer.BYTES];
        fis.read(fileNameLengthBuf);
        int fileNameLength = ByteBuffer.wrap(fileNameLengthBuf).getInt();
        byte[] fileNameBuf = new byte[fileNameLength];
        fis.read(fileNameBuf);
        String fileName = new String(fileNameBuf, 0, fileNameLength);
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        byte[] freqTableLengthBuf = new byte[Integer.BYTES];
        fis.read(freqTableLengthBuf);
        int freqTableLength = ByteBuffer.wrap(freqTableLengthBuf).getInt();
        List<ByteFrequency> freqs = new ArrayList<>();
        double sum = 0;
        //RATIONAL VERSION START
        /*Rational sum = new Rational();*/
        //RATIONAL VERSION END
        for (int i = 0; i < freqTableLength; i++) {
            byte[] buf = new byte[1];
            byte[] freqBuf = new byte[Double.BYTES];
            fis.read(buf);
            fis.read(freqBuf);
            ByteFrequency bf = new ByteFrequency();
            bf.b = buf[0];
            bf.freq = ByteBuffer.wrap(freqBuf).getDouble();
            sum += bf.freq;
            bf.coord = sum;
            freqs.add(bf);
            //RATIONAL VERSION START
            /*byte[] buf = new byte[1];
            fis.read(buf);
            byte[] freqUp = new byte[Integer.BYTES];
            byte[] freqDown = new byte[Integer.BYTES];
            fis.read(freqUp);
            fis.read(freqDown);
            ByteRationalFrequency bf = new ByteRationalFrequency();
            bf.b = buf[0];
            bf.rationalFreq = new Rational(ByteBuffer.wrap(freqUp).getInt(), ByteBuffer.wrap(freqDown).getInt());
            sum.plus(bf.rationalFreq);
            bf.rationalCoord = new Rational(sum);
            freqs.add(bf);*/
            //RATIONAL VERSION END
            System.out.println("byte = " + (char)bf.b + ", coord = " + bf.coord);
            //System.out.println("byte = " + (char)bf.b + ", coord = " + ((ByteRationalFrequency)bf).rationalCoord.toString());
        }
        byte[] fileLengthBuf = new byte[Long.BYTES];
        fis.read(fileLengthBuf);
        long fileLength = ByteBuffer.wrap(fileLengthBuf).getLong();
        byte[] codeNumberBuf = new byte[Double.BYTES];
        //RATIONAL VERSION START
        /*byte[] codeUpBuf = new byte[Integer.BYTES];
        byte[] codeDownBuf = new byte[Integer.BYTES];*/
        //RATIONAL VERSION END
        int cntr = 0;
        int batchCntr = 0;
        boolean done = false;
        while (fis.read(codeNumberBuf) > 0) {
            double codeNumber = ByteBuffer.wrap(codeNumberBuf).getDouble();
            //System.out.println("first codeNumber = " + codeNumber);
            double rangeLow = 0;
            double rangeHigh = 1;
            //RATIONAL VERSION START
            /*Rational codeNumber = new Rational(ByteBuffer.wrap(codeUpBuf).getInt(), ByteBuffer.wrap(codeDownBuf).getInt());
            Rational rangeLow = new Rational();
            Rational rangeHigh = new Rational(1, 1);*/
            //RATIONAL VERSION END
            while (true) {

                byte[] toByte = new byte[1];
                for (ByteFrequency bf : freqs) {
                    if (freqs.indexOf(bf) == 0) {
                        if (codeNumber >= 0 && codeNumber < bf.coord) {
                            toByte[0] = bf.b;
                            fos.write(toByte);
                            rangeLow = 0;
                            rangeHigh = bf.coord;
                            break;
                        }
                        //RATIONAL VERSION START
                        /*if (codeNumber.compare(new Rational()) >= 0 && codeNumber.compare(((ByteRationalFrequency)bf).rationalCoord) < 0) {
                            toByte[0] = bf.b;
                            fos.write(toByte);
                            rangeLow = new Rational();
                            rangeHigh = new Rational(((ByteRationalFrequency)bf).rationalCoord);
                            break;
                        }*/
                        //RATIONAL VERSION END
                    } else {
                        if (codeNumber >= freqs.get(freqs.indexOf(bf)-1).coord && codeNumber < bf.coord) {
                            toByte[0] = bf.b;
                            fos.write(toByte);
                            rangeLow = freqs.get(freqs.indexOf(bf)-1).coord;
                            rangeHigh = bf.coord;
                            break;
                        }
                        //RATIONAL VERSION START
                        /*if (codeNumber.compare(((ByteRationalFrequency)freqs.get(freqs.indexOf(bf)-1)).rationalCoord) >= 0 && codeNumber.compare(((ByteRationalFrequency)bf).rationalCoord) < 0) {
                            toByte[0] = bf.b;
                            fos.write(toByte);
                            rangeLow = ((ByteRationalFrequency)freqs.get(freqs.indexOf(bf)-1)).rationalCoord;
                            rangeHigh = new Rational(((ByteRationalFrequency)bf).rationalCoord);
                            break;
                        }*/
                        //RATIONAL VERSION END
                    }
                }

                codeNumber = (codeNumber - rangeLow) / (rangeHigh - rangeLow);
                //RATIONAL VERSION START
                /*codeNumber.minus(rangeLow).divide(new Rational(rangeHigh).minus(rangeLow));*/
                //RATIONAL VERSION END

                cntr++;
                batchCntr++;
                if (batchCntr == 16) {
                    batchCntr = 0;
                    break;
                }
                if (cntr == fileLength) {
                    break;
                }

            }
        }
        fis.close();
        fos.close();
    }

    private List<ByteFrequency> getFrequencyTable(File fromFile) throws IOException {
        List<ByteFrequency> bfs = new ArrayList<>();
        long fileLength = fromFile.length();
        FileInputStream fis = new FileInputStream(fromFile);
        byte[] buf = new byte[1];
        while (fis.read(buf) > 0) {
            boolean found = false;
            for (ByteFrequency iter : bfs) {
                if (iter.equalsToByte(buf[0])) {
                    iter.freq++;
                    found = true;
                    //RATIONAL VERSION START
                    /*((ByteRationalFrequency)iter).rationalFreq.increment();
                    found = true;*/
                    //RATIONAL VERSION END
                    break;
                }
            }

            if (!found) {

                ByteFrequency bf = new ByteFrequency();
                bf.b = buf[0];
                bf.freq = 1;
                //RATIONAL VERSION START
                /*ByteRationalFrequency bf = new ByteRationalFrequency();
                bf.b = buf[0];
                bf.rationalFreq.increment();*/
                //RATIONAL VERSION END
                bfs.add(bf);
            }
        }
        fis.close();
        Collections.sort(bfs, new Comparator<ByteFrequency>() {
            @Override
            public int compare(ByteFrequency o1, ByteFrequency o2) {
                return (int)(o1.freq - o2.freq);
            }
        });
        //RATIONAL VERSION START
        /*Collections.sort(bfs, new Comparator<ByteFrequency>() {
            @Override
            public int compare(ByteFrequency o1, ByteFrequency o2) {
                Rational result = new Rational(((ByteRationalFrequency)o1).rationalFreq);
                result.minus(((ByteRationalFrequency)o2).rationalFreq);

                return result.up;
            }
        });*/
        //RATIONAL VERSION END
        double sum = 0.0;
        //RATIONAL VERSION START
        /*Rational sum = new Rational();*/
        //RATIONAL VERSION END
        for (ByteFrequency bf : bfs) {
            bf.freq /= fileLength;
            sum += bf.freq;
            bf.coord = sum;
            //RATIONAL VERSION START
            /*((ByteRationalFrequency)bf).rationalFreq.divide((int) fileLength);
            sum.plus(((ByteRationalFrequency)bf).rationalFreq);
            ((ByteRationalFrequency)bf).rationalCoord = new Rational(sum);*/
            //RATIONAL VERSION END
        }

        return bfs;
    }
}
