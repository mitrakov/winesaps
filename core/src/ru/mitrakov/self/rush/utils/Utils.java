package ru.mitrakov.self.rush.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by mitrakov on 29.03.2017
 */
public class Utils {
    public static int[] copyOfRange(int[] original, int from, int to) {
        // copied from java.util.Arrays
        int newLength = to - from;
        if (newLength < 0) throw new IllegalArgumentException(from + " > " + to);
        int[] copy = new int[newLength];
        System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
        return copy;
    }

    public static byte[] toByte(int[] original, int len) {
        assert original != null;
        byte[] copy = new byte[Math.min(original.length, len)];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = (byte) original[i];
        }
        return copy;
    }

    public static int[] toInt(byte[] original, int len) {
        assert original != null;
        int[] copy = new int[Math.min(original.length, len)];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = original[i] >= 0 ? original[i] : original[i] + 256;
        }
        return copy;
    }

    public static int[] append(int[] original, int... elements) {
        assert original != null;
        int[] copy = new int[original.length + elements.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        System.arraycopy(elements, 0, copy, original.length, elements.length);
        return copy;
    }

    public static byte[] getBytes(String s) {
        // @mitrakov: don't use s.getBytes() without charsets: it's a bad practice (by FindBugs)
        // @mitrakov: don't use Charset class: it requires API Level 9
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    public static String newString(byte[] bytes) {
        // @mitrakov: don't use new String(bytes) without charsets: it's a bad practice (by FindBugs)
        // @mitrakov: don't use Charset class: it requires API Level 9
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
