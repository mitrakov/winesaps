package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.UUID;
import java.io.IOException;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.net.Network.BUF_SIZ_SEND;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * SwUDP protocol. Consists of 2 parts: {@link Sender} and {@link Receiver}
 * Please, see digit-by-digit algorithm in corresponding SwUDP documentation
 * @author mitrakov
 */
public class SwUDP implements IProtocol {
    final static int N = 256;
    final static int SYN = 0;
    final static int ERRACK = 1;
    final static int MAX_ATTEMPTS = 9;
    final static int PERIOD = 10;
    final static int MAX_PENDING = 5;
    final static float MIN_SRTT = 2f;
    final static float DEFAULT_SRTT = 5f;
    final static float MAX_SRTT = 12.5f;
    final static float RC = .8f;
    final static float AC = 2.2f;

    /**
     * Single message item.
     * Please DO NOT call "new Item" each time the next packet is sent/received. Use clear() method to reuse the old one
     * @author mitrakov
     */
    static class Item {
        boolean exists = false;
        boolean ack = false;
        int startRtt = 0;
        int ticks = 0;
        int attempt = 0;
        int nextRepeat = 0;
        IIntArray msg = new GcResistantIntArray(BUF_SIZ_SEND);

        /**
         * Resets the internal state (designed specially to get it reusable and reduce GC pressure)
         */
        void clear() {
            exists = ack = false;
            startRtt = ticks = attempt = nextRepeat = 0;
            msg.clear();
        }
    }

    /**
     * @param n - current SwUDP ID
     * @return next SwUDP ID (number in range 2-255)
     */
    static int next(int n) {
        int result = (n + 1) % N;
        boolean ok = result != SYN && result != ERRACK;
        return ok ? result : next(result);
    }

    /**
     * @param x - SwUDP ID1
     * @param y - SwUDP ID2
     * @return whether ID1 is after or before ID2
     */
    static boolean after(int x, int y) {
        return (y - x + N) % N > N / 2;
    }

    private final Sender sender;
    private final Receiver receiver;
    private final IHandler handler;

    /**
     * Creates a new SwUDP protocol implementation
     * @param psObject - Platform Specific Object
     * @param socket - UDP-socket
     * @param host - host (IP-address or host name)
     * @param port - port
     * @param handler - handler to process incoming messages
     */
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

    /**
     * @return current Smoothed Round-Trip-Time in ticks (1 tick is 10 msec)
     */
    public float getSrtt() {
        return sender.srtt;
    }
}
