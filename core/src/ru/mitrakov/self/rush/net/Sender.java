package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.*;
import java.io.IOException;

import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static java.lang.Math.*;
import static ru.mitrakov.self.rush.net.SwUDP.*;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

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
    private final IIntArray startMsg = new GcResistantIntArray(6);
    private /*final*/ DatagramPacket packet;

    private int id = 0, expectedAck = 0, srtt = 0, totalTicks = 0, crcid = 0;
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

    synchronized void connect(int crc_id) throws IOException {
        crcid = crc_id;
        id = expectedAck = SYN;
        srtt = DEFAULT_SRTT;
        totalTicks = 0;
        connected = false;
        for (int j = 0; j < buffer.length; j++) {
            buffer[j] = null;
        }
        startMsg.clear().add(id).add((crcid >> 24) & 0xFF).add((crcid >> 16) & 0xFF).add((crcid >> 8) & 0xFF)
                .add(crcid & 0xFF).add(0xFD);
        buffer[id] = new Item(startMsg);
        log("Send: ", startMsg);
        socket.send(getPacket(startMsg.toByteArray(), startMsg.length()));
    }

    synchronized void send(IIntArray msg) throws IOException {
        if (connected) {
            id = next(id);
            msg.prepend(crcid & 0xFF).prepend((crcid >> 8) & 0xFF).prepend((crcid >> 16) & 0xFF)
                    .prepend((crcid >> 24) & 0xFF).prepend(id);
            buffer[id] = new Item(msg, totalTicks);
            log("Send: ", msg);
            socket.send(getPacket(msg.toByteArray(), msg.length()));
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
                    IIntArray msg = buffer[i].msg; // already contains "crcid" and "id"
                    log("Sendd ", msg + ";ticks=" + buffer[i].ticks + ";attempt=" +
                            buffer[i].attempt + ";nextR=" + buffer[i].nextRepeat + ";rtt=" +
                            (totalTicks - buffer[i].startRtt + 1) + ";srtt=" + srtt);
                    buffer[i].startRtt = totalTicks;
                    socket.send(getPacket(msg.toByteArray(), msg.length()));
                }
            }
            buffer[i].ticks++;
        }
    }

    private DatagramPacket getPacket(byte[] data, int length) throws UnknownHostException {
        if (packet == null)
            packet = new DatagramPacket(data, length, InetAddress.getByName(host), port);
        else packet.setData(data, 0, length);
        return packet;
    }
}
