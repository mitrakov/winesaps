package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.UUID;
import java.io.IOException;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.net.Network.BUF_SIZ_SEND;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * Created by mitrakov on 31.03.2017
 */
public class SwUDP implements IProtocol {
    final static int N = 256;
    final static int SYN = 0;
    final static int MAX_ATTEMPTS = 8;
    final static int PERIOD = 10;
    final static int MIN_SRTT = 2;
    final static int DEFAULT_SRTT = 6;
    final static int MAX_SRTT = 12;
    final static float RC = .8f;
    final static float AC = 2.5f;

    @SuppressWarnings("unused")
    static class Item {
        boolean exists = false;
        boolean ack = false;
        int startRtt = 0;
        int ticks = 0;
        int attempt = 0;
        int nextRepeat = 0;
        IIntArray msg = new GcResistantIntArray(BUF_SIZ_SEND);

        Item() {}

        Item(IIntArray msg) {
            this.msg.copyFrom(msg, msg.length());
        }

        Item(IIntArray msg, int startRtt) {
            this.msg.copyFrom(msg, msg.length());
            this.startRtt = startRtt;
        }

        void clear() {
            exists = ack = false;
            startRtt = ticks = attempt = nextRepeat = 0;
            msg.clear();
        }
    }

    static int next(int n) {
        int result = (n + 1) % N;
        return result != SYN ? result : next(result);
    }

    static boolean after(int x, int y) {
        return (y - x + N) % N > N / 2;
    }

    private final Sender sender;
    private final Receiver receiver;
    private final IHandler handler;

    public SwUDP(PsObject psObject, DatagramSocket socket, String host, int port, IHandler handler) {
        assert psObject != null && socket != null && host != null && handler != null && 0 < port && port < 65536;
        this.handler = handler;
        sender = new Sender(psObject, socket, host, port, this);
        receiver = new Receiver(socket, host, port, handler, this);
    }

    @Override
    public void connect() throws IOException {
        UUID uuid = UUID.randomUUID(); // don't use usual Random! Only SecureRandom
        int crcid = (int) (uuid.getLeastSignificantBits() + uuid.getMostSignificantBits());
        sender.connect(crcid);
    }

    @Override
    public void send(IIntArray data) throws IOException {
        sender.send(data);
    }

    @Override
    public void onReceived(IIntArray data) throws IOException {
        assert data != null;
        int id = data.get(0);
        int crcid = (data.get(1) << 24) | (data.get(2) << 16) | (data.get(3) << 8) | (data.get(4));
        if (data.length() == 5) // Ack (id + crcid)
            sender.onAck(id);
        else if (data.length() > 5)
            receiver.onMsg(id, crcid, data.remove(0, 5));
        else throw new IOException("Incorrect message length");
    }

    @Override
    public void onSenderConnected() throws IOException {
        log("", "Sender connected!");
    }

    @Override
    public void onReceiverConnected() throws IOException {
        log("", "Receiver connected!");
        handler.onChanged(true);
    }

    @Override
    public void connectionFailed() {
        log("", "Connection failed!");
        handler.onChanged(false);
    }

    @Override
    public boolean isConnected() {
        return sender.connected && receiver.connected;
    }
}
