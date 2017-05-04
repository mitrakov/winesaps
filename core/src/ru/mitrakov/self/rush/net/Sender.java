package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.*;
import java.io.IOException;

import static java.lang.Math.*;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;
import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.net.Protocol.*;

/**
 * Created by mitrakov on 03.04.2017
 */
class Sender {

    private final DatagramSocket socket;
    private final String host;
    private final int port;
    private final IProtocol protocol;
    private final Item[] buffer = new Item[N];
    private final Timer timer;

    private int id = 0, expectedAck = 0, srtt = 0, totalTicks = 0;
    volatile boolean connected = false; // volatile needed (by FindBugs)

    Sender(DatagramSocket socket, String host, int port, IProtocol protocol) {
        assert socket != null && host != null && 0 < port && port < 65536 && protocol != null;
        this.socket = socket;
        this.host = host;
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
        log("Send: " + Arrays.toString(data));
        socket.send(new DatagramPacket(toByte(data, data.length), data.length, InetAddress.getByName(host), port));
    }

    synchronized void send(int[] msg) throws IOException {
        if (connected) {
            id = next(id);
            int[] data = append(msg, id);
            buffer[id] = new Item(data, totalTicks);
            log("Send: " + Arrays.toString(data));
            socket.send(new DatagramPacket(toByte(data, data.length), data.length, InetAddress.getByName(host), port));
        } else throw new ConnectException("Not connected");
    }

    synchronized void onAck(int ack) throws IOException {
        System.out.println("SRTT = " + srtt);
        if (buffer[ack] != null) {
            buffer[ack].ack = true;
            if (ack == expectedAck) {
                int rtt = totalTicks - buffer[ack].startRtt + 1;
                int newSrtt = (int) (RC * srtt + (1 - RC) * rtt);
                srtt = min(max(newSrtt, MIN_SRTT), MAX_SRTT);
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
                    log("Sendd " + Arrays.toString(msg) + ";ticks=" + buffer[i].ticks + ";attempt=" +
                            buffer[i].attempt + ";nextR=" + buffer[i].nextRepeat + ";rtt=" +
                            (totalTicks - buffer[i].startRtt + 1) + ";srtt=" + srtt);
                    buffer[i].startRtt = totalTicks;
                    InetAddress addr = InetAddress.getByName(host);
                    socket.send(new DatagramPacket(toByte(msg, msg.length), msg.length, addr, port));
                }
            }
            buffer[i].ticks++;
        }
    }
}
