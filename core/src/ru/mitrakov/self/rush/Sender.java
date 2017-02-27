package ru.mitrakov.self.rush;

import ru.mitrakov.self.rush.net.Network;

/**
 * Created by mitrakov on 27.02.2017
 */

public class Sender {
    private final Network network;
    private final Thread.UncaughtExceptionHandler errorHandler;

    public Sender(Network network, Thread.UncaughtExceptionHandler errorHandler) {
        assert network != null && errorHandler != null;
        this.network = network;
        this.errorHandler = errorHandler;
    }

    public void send(int cmd, byte[] data) {
        try {
            byte msg[] = new byte[data.length + 1];
            msg[0] = (byte)cmd;
            System.arraycopy(data, 0, msg, 1, data.length);
            network.send(msg);
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }
}
