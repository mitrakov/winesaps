package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import static ru.mitrakov.self.rush.net.Protocol.*;

/**
 * Created by mitrakov on 03.04.2017
 */
class Receiver {
    private final DatagramSocket socket;
    private final InetAddress addr;
    private final int port;
    private final IHandler handler;
    private final IProtocol protocol;
    private final Item[] buffer = new Item[N];

    private int expected = 0;
    boolean connected = false;

    Receiver(DatagramSocket socket, InetAddress addr, int port, IHandler handler, IProtocol protocol) {
        assert socket != null && addr != null && 0 < port && port < 65536 && handler != null && protocol != null;
        this.socket = socket;
        this.addr = addr;
        this.port = port;
        this.handler = handler;
        this.protocol = protocol;
    }

    void onMsg(int id, int[] msg) throws IOException {
        if (id == SYN) {
            System.out.println("Ack : [" + id + "]");
            socket.send(new DatagramPacket(new byte[]{(byte) id}, 1, addr, port));
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = null;
            }
            expected = next(id);
            connected = true;
            protocol.onReceiverConnected();
        } else if (connected) {
            System.out.println("Ack : [" + id + "]");
            socket.send(new DatagramPacket(new byte[]{(byte) id}, 1, addr, port));
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