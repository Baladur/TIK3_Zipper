package com.roman;



import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by u131261 on 19.10.2016.
 */
public class HaffmanAlgorithm implements ZipAlgorithm {
    /*SIGN 3 bytes
    * Name length 4 bytes
    * Name $name length bytes
    * File length 8 bytes
    * */
    public static final byte DELIM = '~';
     @Override
    public void zip(String pFromFile, String pToFile) throws IOException {
        File fromFile = new File(pFromFile);
        File toFile = new File(pToFile);
        double codeNumber = 0;
        FileInputStream fis = new FileInputStream(fromFile);
        //BufferedWriter bw = new BufferedWriter(new FileWriter(toFile));
        FileOutputStream fos = new FileOutputStream(toFile);
        byte[] sign = {'R', 'O', 'M'};
        byte[] nameLengthBuf = ByteBuffer.allocate(Integer.BYTES).putInt(pFromFile.length()).array();
        byte[] nameBuf = pFromFile.getBytes();
        byte[] fileLengthBuf = ByteBuffer.allocate(Long.BYTES).putLong(fromFile.length()).array();
        int hdrSize = 0;
        fos.write(sign);
        fos.write(nameLengthBuf);
        fos.write(nameBuf);
        fos.write(fileLengthBuf);
        hdrSize += Integer.BYTES + 3 + Integer.BYTES + pFromFile.length() + Long.BYTES;
        HashMap<Byte, String> codeMap = prepareCodeMap(pFromFile);
        byte[] codeMapLengthBuf = ByteBuffer.allocate(Integer.BYTES).putInt(codeMap.size()).array();
         fos.write(codeMapLengthBuf);
         hdrSize += Integer.BYTES;
        for (Map.Entry<Byte, String> pair : codeMap.entrySet()) {
            fos.write(pair.getKey());
            hdrSize += 1;
            System.out.println(pair.getKey() + " = " + pair.getValue());
            for (Character ch : pair.getValue().toCharArray()) {
                fos.write(ch);
                hdrSize += 1;
            }
            fos.write(DELIM);
            hdrSize += 1;
        }
        byte[] hdrBuf = ByteBuffer.allocate(Integer.BYTES).putInt(hdrSize).array();
        fos.write(hdrBuf);
        byte[] buf = new byte[1];
        BitOuputStream bos = new BitOuputStream(fos);
         while (fis.read(buf) > 0) {
             for (Map.Entry<Byte, String> pair : codeMap.entrySet()) {
                 if (pair.getKey().equals(buf[0])) {
                     bos.write(pair.getValue());
                    break;
                 }
             }
         }
         bos.writeRest();
         fis.close();
         bos.close();
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
        byte[] fileLengthBuf = new byte[Long.BYTES];
        fis.read(fileLengthBuf);
        long fileLength = ByteBuffer.wrap(fileLengthBuf).getLong();
        byte[] codeMapLengthBuf = new byte[Integer.BYTES];
        fis.read(codeMapLengthBuf);
        int codeMapLength = ByteBuffer.wrap(codeMapLengthBuf).getInt();

        byte[] byteBuf = new byte[1];
        byte[] binBuf = new byte[1];
        HashMap<Byte, String> codeMap = new HashMap<>(codeMapLength);
        for (int i = 0; i < codeMapLength; i++) {
            fis.read(byteBuf);
            char ch;
            String code = "";
            do {
                fis.read(binBuf);
                ch = (char)binBuf[0];
                code += ch;
            } while (ch != DELIM);
            code = code.substring(0, code.length()-1);
            codeMap.put(byteBuf[0], code);
        }
        byte[] hdrBuf = new byte[Integer.BYTES];
        fis.read(hdrBuf);
        int hdrSize = ByteBuffer.wrap(hdrBuf).getInt();
        int codedSize = (int)(fromFile.length() - hdrSize);
        BitInputStream bis = new BitInputStream(fis);
        String code = "";
        for (int i = 0; i < (codedSize-Short.BYTES - 1) * 8; i++) {
            boolean bbit = bis.read();
            char bit = bbit ? '1' : '0';
            code += bit;
            for (Map.Entry<Byte, String> pair : codeMap.entrySet()) {
                if (pair.getValue().equals(code)) {
                    fos.write(pair.getKey());
                    code = "";
                    break;
                }
            }
        }
        byte[] bitCountBuf = new byte[Short.BYTES];
        fis.read(bitCountBuf);
        short bitCount = ByteBuffer.wrap(bitCountBuf).getShort();
        for (short i = 0; i < bitCount; i++) {
            boolean bbit = bis.read();
            char bit = bbit ? '1' : '0';
            code += bit;
            for (Map.Entry<Byte, String> pair : codeMap.entrySet()) {
                if (pair.getValue().equals(code)) {
                    fos.write(pair.getKey());
                    code = "";
                    break;
                }
            }
        }

        bis.close();
        fos.close();
    }

    private HashMap<Byte,String> getCodeMap(Node pTree, List<Byte> pBytes) {
        HashMap<Byte, String> result = new HashMap<>();
        for (Byte b : pBytes) {
            result.put(b, pTree.getCode(b, new String("")));
        }
        return  result;

    }

    private HashMap<Byte,String> prepareCodeMap(String pFileName) throws IOException{
        Queue<Node> sortedQueue = new PriorityQueue<Node>(new Comparator<Node>() {
            public int compare(Node node1, Node node2) {
                return node1.priority - node2.priority;
            }
        });
        File file = new File(pFileName);
        List<Byte> bytes = new ArrayList<>();
        List<ByteFrequency> bfs = getFrequencyTable(file);
        String line = "";
        double previousValue = 0;
        int previousPriority = 0;
            /*while ((line = br.readLine()) != null) {
                char ch = line.charAt(line.length()-1);
                chars.add(ch);
                String num_str = line.substring(0, line.length()-2).trim();
                if (num_str.equals("")) continue;
                int val = Integer.parseInt(num_str);
                if (val > previousValue) {
                    previousValue = val;
                    previousPriority++;
                }
                sortedQueue.add(new Node(true, ch, previousPriority));
            }*/
        for (ByteFrequency bf : bfs) {
            byte b = bf.b;
            bytes.add(b);
            if (bf.freq > previousValue) {
                previousValue = bf.freq;
                previousPriority++;
            }
            sortedQueue.add(new Node(true, b, previousPriority));
        }

        int ctr = 0;
        Node leftNode = null;
        Node rightNode = null;
        Node beg = null;
        for (;;) {
            if (ctr == 2) ctr = 0;
            if (ctr == 0) {
                leftNode = sortedQueue.poll();
            }
            if (ctr == 1) {
                rightNode = sortedQueue.poll();
                Node parent = new Node(leftNode, rightNode);
                sortedQueue.add(parent);
                if (sortedQueue.size() == 1) {
                    beg = parent;
                    break;
                }
            }
            ctr++;
        }
        HashMap<Byte, String> codeMap = getCodeMap(beg, bytes);

        return codeMap;
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

    private BitSet fromString(String s) {
        return BitSet.valueOf(new long[] {Long.parseLong(s, 2)});
    }

    static class Node {
        public Node parent;
        public Node[] children = new Node[2];
        public byte value;
        public int priority;
        public boolean isLeaf;

        public Node(boolean pLeaf, byte pValue, int pPriority) {
            isLeaf = pLeaf;
            value = pValue;
            priority = pPriority;
        }

        public Node(Node pNode1, Node pNode2) {
            pNode1.parent = this;
            pNode2.parent = this;
            children[0] = pNode1;
            children[1] = pNode2;
            priority = pNode1.priority + pNode2.priority;
            isLeaf = false;
        }

        public String getCode(byte pValue, String pCode) {
            if (isLeaf) {
                if (pValue == value) {
                    return pCode;
                } else {
                    return null;
                }
            } else {
                String left = children[0].getCode(pValue, pCode + "0");
                if (left == null) {
                    String right = children[1].getCode(pValue, pCode + "1");
                    return right;
                } else {
                    return left;
                }
            }
        }
    }

}
