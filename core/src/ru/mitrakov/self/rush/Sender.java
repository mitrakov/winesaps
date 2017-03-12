package ru.mitrakov.self.rush;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;

/**
 * Created by mitrakov on 27.02.2017
 */

class Sender implements Model.ISender {
    private final Network network;
    private final Thread.UncaughtExceptionHandler errorHandler;

    Sender(Network network, Thread.UncaughtExceptionHandler errorHandler) {
        assert network != null && errorHandler != null;
        this.network = network;
        this.errorHandler = errorHandler;
    }

    @Override
    public void send(Model.Cmd cmd) {
        try {
            byte msg[] = new byte[] {(byte)cmd.ordinal()};
            network.send(msg);
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void send(Model.Cmd cmd, int arg) {
        try {
            byte msg[] = new byte[] {(byte)cmd.ordinal(), (byte)arg};
            network.send(msg);
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void send(Model.Cmd cmd, byte[] data) {
        try {
            byte msg[] = new byte[data.length + 1];
            msg[0] = (byte)cmd.ordinal();
            System.arraycopy(data, 0, msg, 1, data.length);
            network.send(msg);
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }
}
