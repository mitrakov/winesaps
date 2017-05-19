package ru.mitrakov.self.rush.net;

import java.io.IOException;

import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Created by mitrakov on 03.04.2017
 */

interface IProtocol {
    void connect() throws IOException;
    void send(IIntArray data) throws IOException;
    void onReceived(IIntArray data) throws IOException;
    void onSenderConnected() throws IOException;
    void onReceiverConnected() throws IOException;
    void connectionFailed() throws IOException;
    void close();
    boolean isConnected();
}
