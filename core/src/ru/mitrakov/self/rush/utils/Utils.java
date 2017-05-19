package ru.mitrakov.self.rush.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by mitrakov on 29.03.2017
 */
public class Utils {

    public static byte[] getBytes(String s) {
        // @mitrakov: don't use s.getBytes() without charsets: it's a bad practice (by FindBugs)
        // @mitrakov: don't use Charset class: it requires API Level 9
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

}
