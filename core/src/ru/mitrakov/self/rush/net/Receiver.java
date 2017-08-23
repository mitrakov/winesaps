package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.net.SwUDP.*;

/**
 * Receiver is the part of SwUDP class (it was extracted to reduce the source file size)
 * This class should have a single instance for each SwUDP instance
 * @author mitrakov
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
class Receiver {
    private final DatagramSocket socket;
    private final String host;
    private final int port;
    private final IHandler handler;
    private final IProtocol protocol;
    private final Item[] buffer = new Item[N];
    private final IIntArray ack = new GcResistantIntArray(5);
    private /*final*/ DatagramPacket packet;

    private int expected = 0;
    boolean connected = false;
    private int pending = 0;

    /**
     * Creates a new instance of Receiver
     * @param socket - UDP-socket
     * @param host - host (IP-address or host name)
     * @param port - port
     * @param handler - handler to process incoming messages
     * @param protocol - transport protocol (in our case, SwUDP)
     */
    Receiver(DatagramSocket socket, String host, int port, IHandler handler, IProtocol protocol) {
        assert socket != null && host != null && 0 < port && port < 65536 && handler != null && protocol != null;
        this.socket = socket;
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.protocol = protocol;

        // create all 256 items RIGHT AWAY (to avoid dynamic memory allocations)
        for (int i = 0; i < N; i++) {
            buffer[i] = new Item();
        }
    }

    /**
     * Callback on a new message received
     * @param id - SwUDP packet ID
     * @param crcid - SwUDP crcID
     * @param msg - message
     * @throws IOException
     */
    void onMsg(int id, int crcid, IIntArray msg) throws IOException {
        ack.clear();
        ack.add(id).add((crcid >> 24) & 0xFF).add((crcid >> 16) & 0xFF).add((crcid >> 8) & 0xFF).add(crcid & 0xFF);
        if (id == SYN) {
            socket.send(getPacket(ack.toByteArray(), ack.length()));
            for (int j = 0; j < buffer.length; j++) {
                buffer[j].clear();
            }
            expected = next(id);
            connected = true;
            pending = 0;
            protocol.onReceiverConnected();
        } else if (connected) {
            socket.send(getPacket(ack.toByteArray(), ack.length()));
            if (id == expected) {
                handler.onReceived(msg);
                expected = next(id);
                pending = 0;
                accept();
            } else if (after(id, expected)) {
                if (++pending < MAX_PENDING) {
                    buffer[id].exists = true;
                    buffer[id].msg.copyFrom(msg, msg.length());
                } else {
                    connected = false;
                    for (int j = 0; j < buffer.length; j++) {
                        buffer[j].clear();
                    }
                    protocol.connectionFailed();
                }
            }
        } else {
            ack.clear().add(ERRACK);
            ack.add((crcid >> 24) & 0xFF).add((crcid >> 16) & 0xFF).add((crcid >> 8) & 0xFF).add(crcid & 0xFF);
            socket.send(getPacket(ack.toByteArray(), ack.length()));
        }
    }

    /**
     * Transmits the successful packet to the handler and removes it from the buffer
     */
    private void accept() {
        if (buffer[expected].exists) {
            handler.onReceived(buffer[expected].msg);
            buffer[expected].clear();
            expected = next(expected);
            accept();
        }
    }

    /**
     * Packs given data to a datagram packet and returns this packet.
     * Method is designed to reduce GC pressure by avoiding "new DatagramPacket" operations
     * @param data - data
     * @param length - data length
     * @return ready to send datagram packet
     * @throws UnknownHostException - if the host cannot be resolved
     */
    private DatagramPacket getPacket(byte[] data, int length) throws UnknownHostException {
        if (packet == null)
            packet = new DatagramPacket(data, length, InetAddress.getByName(host), port);
        else packet.setData(data, 0, length);
        return packet;
    }
}
