package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import static ru.mitrakov.self.rush.utils.SimpleLogger.*;
import static ru.mitrakov.self.rush.net.Protocol.*;

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

    void onMsg(int id, int[] msg) throws IOException {
        if (id == SYN) {
            log("Ack : [" + id + "]");
            socket.send(new DatagramPacket(new byte[]{(byte) id}, 1, InetAddress.getByName(host), port));
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = null;
            }
            expected = next(id);
            connected = true;
            protocol.onReceiverConnected();
        } else if (connected) {
            log("Ack : [" + id + "]");
            socket.send(new DatagramPacket(new byte[]{(byte) id}, 1, InetAddress.getByName(host), port));
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
