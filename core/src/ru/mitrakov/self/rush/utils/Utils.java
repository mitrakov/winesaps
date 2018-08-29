package ru.mitrakov.self.rush.utils;

import java.io.UnsupportedEncodingException;

/**
 * Simple static utils class
 * @author mitrakov
 */
public class Utils {
    /**
     * Converts string "s" to a byte array
     * NOTE: don't use s.getBytes() without charsets: it's a bad practice (by FindBugs)
     * @param s string
     * @return byte array
     */
    public static byte[] getBytes(String s) {
        try {
            return s.getBytes("UTF-8");            // @mitrakov: don't use Charset class: it requires API Level 9
        } catch (UnsupportedEncodingException e) { // never thrown because all platforms support UTF-8
            return new byte[0];
        }
    }
}
