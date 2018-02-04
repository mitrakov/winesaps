package ru.mitrakov.self.rush;

import java.util.Arrays;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.emulator.ServerEmulator;
import static ru.mitrakov.self.rush.utils.Utils.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.net.Network.BUF_SIZ_SEND;

/**
 * Created by mitrakov on 04.02.2018
 */
class MsgSenderEmulator implements Model.ISender {
    private final ServerEmulator emulator;
    private final IIntArray sendBuf = new GcResistantIntArray(BUF_SIZ_SEND);

    MsgSenderEmulator(ServerEmulator emulator) {
        assert emulator != null;
        this.emulator = emulator;
    }

    @Override
    public void send(Model.Cmd cmd) {
        send(Arrays.binarySearch(Model.cmdValues, cmd)); // don't use "cmd.ordinal()" (GC pressure)
    }

    @Override
    public void send(int cmd) {
        emulator.send(sendBuf.clear().add(cmd));
    }

    @Override
    public void send(Model.Cmd cmd, int... arg) {
        send(Arrays.binarySearch(Model.cmdValues, cmd), arg);
    }

    @Override
    public void send(int cmd, int... arg) {
        sendBuf.clear().add(cmd);
        for (int i: arg) {
            sendBuf.add(i);
        }
        emulator.send(sendBuf);
    }

    @Override
    public void send(Model.Cmd cmd, String arg) {
        send(Arrays.binarySearch(Model.cmdValues, cmd), arg);
    }

    @Override
    public void send(int cmd, String arg) {
        emulator.send(sendBuf.clear().fromByteArray(getBytes(arg), arg.length()).prepend(cmd));
    }

    @Override
    public void reset() {}
}
