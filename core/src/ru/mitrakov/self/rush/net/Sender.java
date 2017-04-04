package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.*;
import java.io.IOException;

import static ru.mitrakov.self.rush.net.Utils.*;
import static ru.mitrakov.self.rush.net.Protocol.*;

/**
 * Created by mitrakov on 03.04.2017
 */

class Sender {

    private final DatagramSocket socket;
    private final InetAddress addr;
    private final int port;
    private final IProtocol protocol;
    private final Item[] buffer = new Item[N];
    private final Timer timer;

    private int id = 0, expectedAck = 0;
    boolean connected = false;

    Sender(DatagramSocket socket, InetAddress addr, int port, IProtocol protocol) {
        assert socket != null && addr != null && 0 < port && port < 65536 && protocol != null;
        this.socket = socket;
        this.addr = addr;
        this.port = port;
        this.protocol = protocol;

        timer = new Timer("protocol timer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    trigger();
                } catch (IOException ignored) {
                }
            }
        }, REPEAT_MSEC, REPEAT_MSEC);
    }

    void close() {
        timer.cancel();
    }

    void connect() throws IOException {
        id = expectedAck = SYN;
        connected = false;
        for (int j = 0; j < buffer.length; j++) {
            buffer[j] = null;
        }
        int[] data = new int[]{0, id}; // 0 = fake data
        buffer[id] = new Item(data);
        System.out.println("Send: " + Arrays.toString(data));
        socket.send(new DatagramPacket(toByte(data, data.length), data.length, addr, port));
    }

    synchronized void send(int[] msg) throws IOException {
        if (connected) {
            id = next(id);
            int[] data = append(msg, id);
            buffer[id] = new Item(data);
            System.out.println("Send: " + Arrays.toString(data));
            socket.send(new DatagramPacket(toByte(data, data.length), data.length, addr, port));
        } else throw new ConnectException("Not connected");
    }

    synchronized void onAck(int ack) throws IOException {
        if (buffer[ack] != null) {
            buffer[ack].ack = true;
            if (ack == expectedAck)
                accept();
            else trigger();
        }
        if (ack == SYN) {
            connected = true;
            protocol.onSenderConnected();
        }
    }

    private void accept() {
        if (buffer[expectedAck] != null) {
            if (buffer[expectedAck].ack) {
                buffer[expectedAck] = null;
                expectedAck = next(expectedAck);
                accept();
            }
        }
    }

    private synchronized void trigger() throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] != null && !buffer[i].ack) {
                buffer[i].attempts++;
                if (buffer[i].attempts > MAX_ATTEMPTS) {
                    connected = false;
                    for (int j = 0; j < buffer.length; j++) {
                        buffer[j] = null;
                    }
                    protocol.connectionFailed();
                    return;
                } else if (buffer[i].attempts > 1) {
                    int[] msg = buffer[i].msg;
                    System.out.println("Send^ " + Arrays.toString(msg) + "; cnt = " + buffer[i].attempts);
                    socket.send(new DatagramPacket(toByte(msg, msg.length), msg.length, addr, port));
                }
            }
        }
    }
}
