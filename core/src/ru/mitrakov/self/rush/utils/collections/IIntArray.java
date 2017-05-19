package ru.mitrakov.self.rush.utils.collections;

/**
 * Created by mitrakov on 19.05.2017
 */

public interface IIntArray {
    int get(int idx);
    IIntArray add(int item);
    IIntArray prepend(int item);
    IIntArray remove(int startPos, int endPos);
    IIntArray clear();
    int length();
    IIntArray copyFrom(IIntArray data, int length);
    IIntArray fromByteArray(byte[] data, int length);
    byte[] toByteArray();
    String toUTF8();
}
