package ru.mitrakov.self.rush;

import java.util.Arrays;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.emulator.ServerEmulator;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.net.Network.BUF_SIZ_SEND;

/**
 * Same as {@link MsgSender} but intended of transferring messages to the server transfers to ServerEmulator (that, in
 * turn, is useful for SinglePlayer mode
 * @author mitrakov
 * @since 2.0.0
 */
class MsgSenderEmulator implements Model.ISender {
    /**
     * Reference to ServerEmulator (note that {@link MsgSender} contains a reference to
     * {@link ru.mitrakov.self.rush.net.Network Network})
     */
    private final ServerEmulator emulator;
    /** Internal buffer to send messages (to avoid creating new arrays and decrease Garbage Collector pressure) */
    private final IIntArray sendBuf = new GcResistantIntArray(BUF_SIZ_SEND);

    /**
     * Creates new MsgSenderEmulator (please note that it's intended to be used in SinglePlayer only, because it does
     * not sends actual messages to network; so for any other purposes use {@link MsgSender} instead)
     * @param emulator Server Emulator
     */
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
