package com.roman.test;

import com.roman.BitInputStream;
import com.roman.BitOuputStream;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.util.BitSet;

/**
 * Created by roman on 30.10.2016.
 */
public class BitStreamTest extends TestCase {

    @Test
    public void test() {
        //given
        BitInputStream bis = null;
        BitOuputStream bos = null;
        try {
            File file = new File("in.txt");
            bis = new BitInputStream(new FileInputStream(file));
            bos = new BitOuputStream(new FileOutputStream(new File("out.txt")));
            String byteBuffer = "";
            for (int i = 1; i <= file.length() * 8; i++) {
                if (i % 8 == 0) {

                }
                boolean bbit = bis.read();
                char bit = bbit ? '1' : '0';
                byteBuffer += bit;
                //System.out.print(bit);
                if (i % 8 == 0) {
                    //System.out.println("");
                    //byteBuffer = new StringBuilder(byteBuffer).reverse().toString();
                    bos.write(byteBuffer);
                    System.out.println(byteBuffer);
                    byteBuffer = "";
                }

                //bos.write(new String().valueOf(bit));
            }

        } catch (FileNotFoundException e) {
            Assert.assertTrue(false);
            e.printStackTrace();
        } catch (IOException ioe) {
            Assert.assertTrue(false);
            ioe.printStackTrace();
        } catch (Exception e) {
            Assert.assertTrue(false);
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                Assert.assertTrue(false);
                e.printStackTrace();
            }
        }
        //when

        //then
    }
}
