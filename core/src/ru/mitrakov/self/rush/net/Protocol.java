package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import static ru.mitrakov.self.rush.net.Utils.*;

/**
 * Created by mitrakov on 31.03.2017
 */
public class Protocol implements IProtocol {
    final static int N = 256;
    final static int SYN = 0;
    final static int MAX_ATTEMPTS = 16;
    final static int REPEAT_MSEC = 120;

    static class Item {
        boolean ack = false;
        int attempts = 0;
        int[] msg;

        Item(int[] msg) {
            this.msg = msg;
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

    public Protocol(DatagramSocket socket, InetAddress addr, int port, IHandler handler) throws IOException {
        assert socket != null && addr != null && handler != null && 0 < port && port < 65536;
        sender = new Sender(socket, addr, port, handler);
        receiver = new Receiver(socket, addr, port, handler);
    }

    @Override
    public void connect() throws IOException {
        sender.connect();
    }

    @Override
    public void send(int[] data) throws IOException {
        sender.send(data);
    }

    @Override
    public void onReceived(int[] data) throws IOException {
        assert data != null && data.length > 0;
        if (data.length == 1) // Ack
            sender.onAck(data[0]);
        else receiver.onMsg(data[data.length - 1], copyOfRange(data, 0, data.length - 1));
    }

    @Override
    public void close() {
        sender.close();
    }
}
