package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.Arrays;
import java.io.IOException;

import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;
import static ru.mitrakov.self.rush.net.SwUDP.*;

/**
 * Created by mitrakov on 03.04.2017
 */
class Receiver {
    private final DatagramSocket socket;
    private final String host;
    private final int port;
    private final IHandler handler;
    private final IProtocol protocol;
    private final Item[] buffer = new Item[N];

    private int expected = 0;
    boolean connected = false;

    Receiver(DatagramSocket socket, String host, int port, IHandler handler, IProtocol protocol) {
        assert socket != null && host != null && 0 < port && port < 65536 && handler != null && protocol != null;
        this.socket = socket;
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.protocol = protocol;
    }

    void onMsg(int id, int crcid, int[] msg) throws IOException {
        int[] ack = new int[] {id, (crcid >> 24) & 0xFF, (crcid >> 16) & 0xFF, (crcid >> 8) & 0xFF, crcid & 0xFF};
        if (id == SYN) {
            log("Ack : " + Arrays.toString(ack));
            socket.send(new DatagramPacket(toByte(ack, ack.length), ack.length, InetAddress.getByName(host), port));
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = null;
            }
            expected = next(id);
            connected = true;
            protocol.onReceiverConnected();
        } else if (connected) {
            log("Ack : " + Arrays.toString(ack));
            socket.send(new DatagramPacket(toByte(ack, ack.length), ack.length, InetAddress.getByName(host), port));
            if (id == expected) {
                handler.onReceived(msg);
                expected = next(id);
                accept();
            } else if (after(id, expected))
                buffer[id] = new Item(msg);
        }
    }

    private void accept() {
        if (buffer[expected] != null) {
            handler.onReceived(buffer[expected].msg);
            buffer[expected] = null;
            expected = next(expected);
            accept();
        }
    }
}
