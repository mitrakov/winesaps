package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.UUID;
import java.io.IOException;

import static ru.mitrakov.self.rush.utils.SimpleLogger.*;
import static ru.mitrakov.self.rush.utils.Utils.*;

/**
 * Created by mitrakov on 31.03.2017
 */
public class SwUDP implements IProtocol {
    final static int N = 256;
    final static int SYN = 0;
    final static int MAX_ATTEMPTS = 12;
    final static int PERIOD = 10;
    final static int MIN_SRTT = 2;
    final static int DEFAULT_SRTT = 6;
    final static int MAX_SRTT = 12;
    final static float RC = .8f;
    final static float AC = 2.5f;

    static class Item {
        boolean ack = false;
        int startRtt = 0;
        int ticks = 0;
        int attempt = 0;
        int nextRepeat = 0;
        int[] msg;

        Item(int[] msg) {
            this.msg = msg;
        }

        Item(int[] msg, int startRtt) {
            this.msg = msg;
            this.startRtt = startRtt;
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

    public SwUDP(DatagramSocket socket, String host, int port, IHandler handler) {
        assert socket != null && host != null && handler != null && 0 < port && port < 65536;
        this.handler = handler;
        sender = new Sender(socket, host, port, this);
        receiver = new Receiver(socket, host, port, handler, this);
    }

    @Override
    public void connect() throws IOException {
        UUID uuid = UUID.randomUUID(); // don't use usual Random! Only SecureRandom
        int crcid = (int) (uuid.getLeastSignificantBits() + uuid.getMostSignificantBits());
        sender.connect(crcid);
    }

    @Override
    public void send(int[] data) throws IOException {
        sender.send(data);
    }

    @Override
    public void onReceived(int[] data) throws IOException {
        assert data != null;
        int id = data[0];
        int crcid = (data[1] << 24) | (data[2] << 16) | (data[3] << 8) | (data[4]);
        if (data.length == 5) // Ack (id + crcid)
            sender.onAck(id);
        else if (data.length > 5)
            receiver.onMsg(id, crcid, copyOfRange(data, 5, data.length));
        else throw new IOException("Incorrect message length");
    }

    @Override
    public void onSenderConnected() throws IOException {
        log("Sender connected!");
    }

    @Override
    public void onReceiverConnected() throws IOException {
        log("Receiver connected!");
        handler.onChanged(true);
    }

    @Override
    public void connectionFailed() throws IOException {
        log("Connection failed!");
        handler.onChanged(false);
    }

    @Override
    public void close() {
        sender.close();
    }

    @Override
    public boolean isConnected() {
        return sender.connected && receiver.connected;
    }
}
