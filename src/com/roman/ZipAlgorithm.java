package com.roman;

import java.io.IOException;

/**
 * Created by roman on 16.10.2016.
 */
public interface ZipAlgorithm {
    public void zip(String pFromFile, String pToFile) throws IOException;
    public void unzip(String pFromFile) throws IOException, ZipException;
}
