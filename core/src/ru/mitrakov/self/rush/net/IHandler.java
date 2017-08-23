package ru.mitrakov.self.rush.net;

import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Handler interface to process incoming messages
 * @author mitrakov
 */
public interface IHandler {
    /**
     * Callback on a new incoming message received
     * @param data - data
     */
    void onReceived(IIntArray data);

    /**
     * Callback on connection status changed
     * @param connected - true, if connected
     */
    void onChanged(boolean connected);
}
