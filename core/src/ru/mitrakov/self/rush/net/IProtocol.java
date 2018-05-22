package ru.mitrakov.self.rush.net;

import java.io.IOException;

import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Interface for a transport protocol
 * @author mitrakov
 */
public interface IProtocol {
    /**
     * Tries to establish a connection
     * @throws IOException if IOException occurred
     */
    void connect() throws IOException;

    /**
     * Sends a message
     * @param data data
     * @throws IOException if IOException occurred
     */
    void send(IIntArray data) throws IOException;

    /**
     * Callback on a new message received
     * @param data data
     * @throws IOException if IOException occurred
     */
    void onReceived(IIntArray data) throws IOException;

    /**
     * Callback on a new sender connected event
     * @throws IOException if IOException occurred
     */
    void onSenderConnected() throws IOException;

    /**
     * Callback on a new receiver connected event
     * @throws IOException if IOException occurred
     */
    void onReceiverConnected() throws IOException;

    /** Callback on a connection failed event */
    void connectionFailed();

    /** @return true, if a connection established */
    boolean isConnected();
}
