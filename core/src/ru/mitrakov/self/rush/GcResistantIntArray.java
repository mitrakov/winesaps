package ru.mitrakov.self.rush;

import com.badlogic.gdx.utils.IntArray;

import java.io.UnsupportedEncodingException;

import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Created by mitrakov on 19.05.2017
 */
public final class GcResistantIntArray implements IIntArray {
    private final IntArray array;
    private final byte[] bytes;

    public GcResistantIntArray(int bufSize) {
        array = new IntArray(bufSize);
        bytes = new byte[bufSize];
    }

    @Override
    public int get(int idx) {
        return array.get(idx);
    }

    @Override
    public IIntArray add(int item) {
        array.add(item);
        return this;
    }

    @Override
    public IIntArray prepend(int item) {
        array.insert(0, item);
        return this;
    }

    @Override
    public IIntArray remove(int startPos, int endPos) {
        array.removeRange(startPos, endPos-1);
        return this;
    }

    @Override
    public IIntArray clear() {
        array.clear();
        return this;
    }

    @Override
    public int length() {
        return array.size;
    }

    @Override
    public IIntArray copyFrom(IIntArray data, int length) {
        array.clear();
        for (int i = 0; i < Math.min(data.length(), length); i++) {
            array.add(data.get(i));
        }
        return this;
    }

    @Override
    public IIntArray fromByteArray(byte[] data, int length) {
        array.clear();
        for (int i = 0; i < Math.min(data.length, length); i++) {
            array.add(data[i] >= 0 ? data[i] : data[i] + 256);
        }
        return this;
    }

    @Override
    public byte[] toByteArray() {
        for (int i = 0; i < array.size; i++) {
            bytes[i] = (byte) array.get(i);
        }
        return bytes;
    }

    @Override
    public String toUTF8() {
        try {
            return new String(toByteArray(), 0, array.size, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    @Override
    public String toString() {
        return array.toString();
    }

    /*@Override
    public void print() {
        if (array.size == 0) System.out.println("[]");
        int j = 0;
        s.setCharAt(j++, '[');
        if (array.first() >= 100) s.setCharAt(j++, toChar(array.first()/100%10));
        if (array.first() >= 10)  s.setCharAt(j++, toChar(array.first()/10%10));
        s.setCharAt(j++, toChar(array.first()%10));
        for (int i = 1; i < array.size; i++) {
            s.setCharAt(j++, ',');
            s.setCharAt(j++, ' ');
            if (array.get(i) >= 100) s.setCharAt(j++, toChar(array.get(i)/100%10));
            if (array.get(i) >= 10) s.setCharAt(j++, toChar(array.get(i)/10%10));
            s.setCharAt(j++, toChar(array.get(i)%10));
        }
        s.setCharAt(j++, ']');
        return s.substring(0, j);
    }

    private char toChar(int x) {
        switch (x) {
            case 0: return '0';
            case 1: return '1';
            case 2: return '2';
            case 3: return '3';
            case 4: return '4';
            case 5: return '5';
            case 6: return '6';
            case 7: return '7';
            case 8: return '8';
            case 9: return '9';
        }
        return '?';
    }*/
}
