package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import static ru.mitrakov.self.rush.net.Utils.*;

/**
 * Created by mitrakov on 31.03.2017
 */
class Protocol {

    private class Item {
        int[] msg;

        Item(int[] msg) {
            this.msg = msg;
        }
    }

    private final static int N = 256;

    private final DatagramSocket socket;
    private final InetAddress addr;
    private final int port;
    private final IHandler handler;
    private final Item[] buffer = new Item[N];
    private int expected = 0;

    Protocol(DatagramSocket socket, InetAddress addr, int port, IHandler handler) {
        assert socket != null && addr != null && 0 < port && port < 65536 && handler != null;
        this.socket = socket;
        this.addr = addr;
        this.port = port;
        this.handler = handler;
        init();
    }

    void received(int[] data) throws IOException {
        if (data.length > 0) {
            int N = data.length - 1;
            int id = data[N];
            onMsg(id, copyOfRange(data, 0, N));
        }
    }

    private void init() {
        expected = next(expected);
    }

    private void onMsg(int id, int[] msg) throws IOException {
        System.out.println("Send: [" + id + "]");
        socket.send(new DatagramPacket(new byte[]{(byte) id}, 1, addr, port));
        if (id == expected) {
            handler.handle(msg);
            expected = next(expected);
            accept();
        } else if (after(id, expected))
            buffer[id] = new Item(msg);
    }

    private void accept() {
        if (buffer[expected] != null) {
            handler.handle(buffer[expected].msg);
            buffer[expected] = null;
            expected = next(expected);
            accept();
        }
    }

    private int next(int n) {
        return (n + 1) % N;
    }

    private boolean after(int x, int y) {
        return (y - x + N) % N > N / 2;
    }
}
