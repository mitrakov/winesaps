package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.*;
import java.io.IOException;

import static java.lang.Math.max;
import static ru.mitrakov.self.rush.utils.Utils.*;
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

    private int id = 0, expectedAck = 0, srtt = 0, totalTicks = 0;
    volatile boolean connected = false; // volatile needed (by FindBugs)

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
        }, PERIOD, PERIOD);
    }

    void close() {
        timer.cancel();
    }

    synchronized void connect() throws IOException {
        id = expectedAck = SYN;
        srtt = DEFAULT_SRTT;
        totalTicks = 0;
        connected = false;
        for (int j = 0; j < buffer.length; j++) {
            buffer[j] = null;
        }
        int[] data = new int[]{0xFD, id}; // FD = fake data
        buffer[id] = new Item(data);
        System.out.println("Send: " + Arrays.toString(data));
        socket.send(new DatagramPacket(toByte(data, data.length), data.length, addr, port));
    }

    synchronized void send(int[] msg) throws IOException {
        if (connected) {
            id = next(id);
            int[] data = append(msg, id);
            buffer[id] = new Item(data, totalTicks);
            System.out.println("Send: " + Arrays.toString(data));
            socket.send(new DatagramPacket(toByte(data, data.length), data.length, addr, port));
        } else throw new ConnectException("Not connected");
    }

    synchronized void onAck(int ack) throws IOException {
        if (buffer[ack] != null) {
            buffer[ack].ack = true;
            if (ack == expectedAck) {
                int rtt = totalTicks - buffer[ack].startRtt + 1;
                srtt = max((int) (RC * srtt + (1 - RC) * rtt), DEFAULT_SRTT);
                accept();
            }
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
        totalTicks++;
        int i = expectedAck;
        if (buffer[i] != null && !buffer[i].ack) {
            if (buffer[i].attempt > MAX_ATTEMPTS) {
                connected = false;
                for (int j = 0; j < buffer.length; j++) {
                    buffer[j] = null;
                }
                protocol.connectionFailed();
                return;
            } else if (buffer[i].ticks == buffer[i].nextRepeat) {
                buffer[i].attempt++;
                buffer[i].nextRepeat += AC * srtt * buffer[i].attempt;
                if (buffer[i].attempt > 1) {
                    int[] msg = buffer[i].msg;
                    System.out.println("Send^ " + Arrays.toString(msg) + ";ticks=" + buffer[i].ticks + ";attempt=" +
                            buffer[i].attempt + ";nextR=" + buffer[i].nextRepeat + ";rtt=" +
                            (totalTicks - buffer[i].startRtt + 1) + ";srtt=" + srtt);
                    buffer[i].startRtt = totalTicks;
                    socket.send(new DatagramPacket(toByte(msg, msg.length), msg.length, addr, port));
                }
            }
            buffer[i].ticks++;
        }
    }
}
