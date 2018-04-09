package ru.mitrakov.self.rush.utils.collections;

/**
 * Byte array interface.
 * Implementations may use own format of storing internal data
 * It is designed as an abstraction for {@link ru.mitrakov.self.rush.GcResistantIntArray GcResistantIntArray}.
 * Please see javadoc for further details.
 * @author mitrakov
 */
public interface IIntArray {
    /**
     * @param idx index
     * @return element by its index
     */
    int get(int idx);

    /**
     * Sets the element to a given value
     * @param idx array index
     * @param value valu
     */
    void set(int idx, int value);

    /**
     * Appends element to the end of the collection
     * @param item element
     * @return "this"
     */
    IIntArray add(int item);

    /**
     * Prepends element at the start of the collection
     * @param item element
     * @return "this"
     */
    IIntArray prepend(int item);

    /**
     * Removes elements from the collection
     * @param startPos start index (inclusive)
     * @param endPos end index (exclusive)
     * @return "this"
     */
    IIntArray remove(int startPos, int endPos);

    /**
     * Removes all the elements from the collection
     * @return "this"
     */
    IIntArray clear();

    /**
     * @return the collection size
     */
    int length();

    /**
     * Clears current collection and copies N elements from the given collection
     * @param data source collection
     * @param length count of elements
     * @return "this"
     */
    IIntArray copyFrom(IIntArray data, int length);

    /**
     * Clears current collection and copies N elements from the given byte array
     * @param data source collection
     * @param length count of elements
     * @return "this"
     */
    IIntArray fromByteArray(byte[] data, int length);

    /**
     * @return byte array representing the current collection
     */
    byte[] toByteArray();

    /**
     * @return current byte array converted to String according to "UTF-8" encoding
     */
    String toUTF8();
}
