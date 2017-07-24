package ru.mitrakov.self.rush;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.net.Network.BUF_SIZ_SEND;

/**
 * Created by mitrakov on 27.02.2017
 */
class MsgSender implements Model.ISender {
    private final Network network;
    private final Thread.UncaughtExceptionHandler errorHandler;
    private final IIntArray sendBuf = new GcResistantIntArray(BUF_SIZ_SEND);

    MsgSender(Network network, Thread.UncaughtExceptionHandler errorHandler) {
        assert network != null && errorHandler != null;
        this.network = network;
        this.errorHandler = errorHandler;
    }

    @Override
    public void send(Model.Cmd cmd) {
        try {
            network.send(sendBuf.clear().add(cmd.ordinal()));
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void send(Model.Cmd cmd, int... arg) {
        try {
            sendBuf.clear().add(cmd.ordinal());
            for (int i : arg) {
                sendBuf.add(i);
            }
            network.send(sendBuf);
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void send(Model.Cmd cmd, String arg) {
        try {
            network.send(sendBuf.fromByteArray(arg.getBytes(), arg.length()).prepend(cmd.ordinal()));
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void reset() {
        network.reset();
    }
}
