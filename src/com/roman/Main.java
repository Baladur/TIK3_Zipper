package com.roman;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	// write your code here
        //test
        Zipper zipper = new Zipper();
        try {
            zipper.zip("in.txt", "out.rom");
            zipper.unzip("out.rom");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
