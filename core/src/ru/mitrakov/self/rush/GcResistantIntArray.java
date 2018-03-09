package ru.mitrakov.self.rush;

import com.badlogic.gdx.utils.IntArray;

import java.io.UnsupportedEncodingException;

import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * GcResistantIntArray - special implementation of IIntArray that DOES NOT creates new objects during its work to
 * decrease GC pressure. It can be safely used in render() method
 * @author mitrakov
 */
public final class GcResistantIntArray implements IIntArray {
    private final IntArray array;
    private final byte[] bytes;

    public GcResistantIntArray(int bufSize) {
        array = new IntArray(bufSize);
        bytes = new byte[bufSize];
    }

    @Override
    public synchronized int get(int idx) {
        return array.get(idx);
    }

    @Override
    public void set(int idx, int value) {
        array.set(idx, value);
    }

    @Override
    public synchronized IIntArray add(int item) {
        array.add(item);
        return this;
    }

    @Override
    public synchronized IIntArray prepend(int item) {
        array.insert(0, item);
        return this;
    }

    @Override
    public synchronized IIntArray remove(int startPos, int endPos) {
        array.removeRange(startPos, endPos-1);
        return this;
    }

    @Override
    public synchronized IIntArray clear() {
        array.clear();
        return this;
    }

    @Override
    public synchronized int length() {
        return array.size;
    }

    /**
     * Copies data from an existing array
     * if data.length() or length is larger than bufSize it's OK (internal buffer will be resized)
     * @param data - data
     * @param length - length
     * @return reference to "this"
     */
    @Override
    public synchronized IIntArray copyFrom(IIntArray data, int length) {
        array.clear();
        for (int i = 0; i < Math.min(data.length(), length); i++) {
            array.add(data.get(i));
        }
        return this;
    }

    @Override
    public synchronized IIntArray fromByteArray(byte[] data, int length) {
        array.clear();
        for (int i = 0; i < Math.min(data.length, length); i++) {
            array.add(data[i] >= 0 ? data[i] : data[i] + 256);
        }
        return this;
    }

    @Override
    public synchronized byte[] toByteArray() {
        for (int i = 0; i < Math.min(bytes.length, array.size) ; i++) {
            bytes[i] = (byte) array.get(i);
        }
        return bytes; // it's OK (please add an exception for FindBugs and DO NOT create a copy as it suggests)
    }

    @Override
    public synchronized String toUTF8() {
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
}
