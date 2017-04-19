package ru.mitrakov.self.rush;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;
import static ru.mitrakov.self.rush.utils.Utils.*;

/**
 * Created by mitrakov on 27.02.2017
 */

class MsgSender implements Model.ISender {
    private final Network network;
    private final Thread.UncaughtExceptionHandler errorHandler;

    MsgSender(Network network, Thread.UncaughtExceptionHandler errorHandler) {
        assert network != null && errorHandler != null;
        this.network = network;
        this.errorHandler = errorHandler;
    }

    @Override
    public void send(Model.Cmd cmd) {
        try {
            network.send(new int[] {cmd.ordinal()});
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void send(Model.Cmd cmd, int arg) {
        try {
            network.send(new int[] {cmd.ordinal(), arg});
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void send(Model.Cmd cmd, byte[] data) {
        try {
            int msg[] = new int[data.length + 1];
            msg[0] = cmd.ordinal();
            System.arraycopy(toInt(data, data.length), 0, msg, 1, data.length);
            network.send(msg);
        } catch (Exception e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void reset() {
        network.reset();
    }
}
