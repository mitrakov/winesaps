package ru.mitrakov.self.rush.net;

import java.io.IOException;

/**
 * Created by mitrakov on 03.04.2017
 */

interface IProtocol {
    void connect() throws IOException;
    void send(int[] data) throws IOException;
    void onReceived(int[] data) throws IOException;
    void onSenderConnected() throws IOException;
    void onReceiverConnected() throws IOException;
    void connectionFailed() throws IOException;
    void close();
    boolean isConnected();
}
