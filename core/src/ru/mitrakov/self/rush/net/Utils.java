package ru.mitrakov.self.rush.net;

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
}
