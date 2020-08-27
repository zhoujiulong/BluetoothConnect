package com.example.bluetoothtest;

import java.io.UnsupportedEncodingException;

public class ByteUtils {

    public static byte[] getBytes(String str) {
        try {
            return str.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

}
