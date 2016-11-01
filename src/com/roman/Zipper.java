package com.roman;

import java.io.File;

/**
 * Created by roman on 16.10.2016.
 */
public class Zipper {
    public static final String EXT = ".rom";
    private ZipAlgorithm algo;

    public Zipper() {
        algo = new HaffmanAlgorithm();
    }

    public void zip(String pFromFile, String pToFile) throws Exception {
        validateFromFile(pFromFile);
        String toFile = validateToFile(pToFile);
        algo.zip(pFromFile, toFile);
    }

    public void unzip(String pFromFile) throws Exception {
        validateFromFile(pFromFile);
        algo.unzip(pFromFile);
    }

    private void validateFromFile(String pFile) throws Exception {
        File file = new File(pFile);
        if (!file.exists()) {
            throw new Exception("File '" + pFile + "' does not exist!");
        }
    }

    private static String validateToFile(String pFile) {
        if (!pFile.endsWith(EXT)) {
            return pFile + EXT;
        }

        return pFile;
    }
}
