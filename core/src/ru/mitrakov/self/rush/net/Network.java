package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.*;
import java.io.IOException;

import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * Created by mitrakov on 23.02.2017
 */
public final class Network extends Thread implements IHandler {
    public static boolean TMP_NO_CONNECTION = false; // REMOVE ME IN A FUTURE

    private static final int BUF_SIZ = 1024;
    private static final int HEADER_SIZ = 7;
    private static final int RECONNECT_MSEC = 15000;

    // on Android don't forget to add "<uses-permission android:name="android.permission.INTERNET"/>" to manifest
    // otherwise new DatagramSocket() throws PermissionDeniedException
    private final DatagramSocket socket = new DatagramSocket();
    private final IHandler handler;
    private final UncaughtExceptionHandler errorHandler;
    private final String host;
    private final int port;
    private final byte[] recvBuf = new byte[BUF_SIZ];
    private final IIntArray recvData = new GcResistantIntArray(BUF_SIZ);

    private int sid = 0;
    private long token = 0;
    private IProtocol protocol;

    public Network(IHandler handler, UncaughtExceptionHandler eHandler, String host, int port)
            throws SocketException {
        assert handler != null && eHandler != null && host != null && 0 < port && port < 65536;
        this.handler = handler;
        this.errorHandler = eHandler;
        this.host = host;
        this.port = port;

        setDaemon(true);
        setName("Network thread");
        setUncaughtExceptionHandler(eHandler);
    }

    @Override
    public void run() {
        // connect to the server inside a timer
        if (protocol != null) {
            new Timer("Connect timer", true).schedule(new TimerTask() {
                @Override
                public void run() {
                    if (protocol != null && !protocol.isConnected()) try {
                        protocol.connect();
                    } catch (IOException e) {
                        errorHandler.uncaughtException(null, e);
                    }
                }
            }, 0, RECONNECT_MSEC);
        }

        // run infinite loop
        // noinspection InfiniteLoopStatement
        while (true) {
            try {
                DatagramPacket datagram = new DatagramPacket(recvBuf, BUF_SIZ);
                socket.receive(datagram);
                if (TMP_NO_CONNECTION) continue;
                if (protocol != null) {
                    recvData.fromByteArray(datagram.getData(), datagram.getLength());
                    log("Recv: ", recvData);
                    protocol.onReceived(recvData);
                } else onReceived(recvData.fromByteArray(datagram.getData(), datagram.getLength()));
            } catch (Exception e) {
                errorHandler.uncaughtException(this, e);
            }
        }
    }

    @Override
    public void onReceived(IIntArray data) {
        if (data.length() > HEADER_SIZ) try {
            int inSid = data.get(0) * 256 + data.get(1);
            long inToken = (data.get(2) << 24) | (data.get(3) << 16) | (data.get(4) << 8) | data.get(5);
            if (sid * token == 0) {
                sid = inSid;
                token = inToken;
            }
            if (sid == inSid && token == inToken)
                handler.onReceived(data.remove(0, HEADER_SIZ));
            else throw new IllegalAccessException("Incorrect sid/token pair");
        } catch (Exception e) {
            errorHandler.uncaughtException(this, e); // we MUST handle all exceptions to get SwUDP working
        }
    }

    @Override
    public void onChanged(boolean connected) {
        handler.onChanged(connected);
    }

    public void send(IIntArray data) throws IOException {
        if (TMP_NO_CONNECTION) return;

        // concatenate a header and data
        int h0 = sid / 256;
        int h1 = sid % 256;
        int h2 = (int) ((token >> 24) & 0xFF);
        int h3 = (int) ((token >> 16) & 0xFF);
        int h4 = (int) ((token >> 8) & 0xFF);
        int h5 = (int) (token & 0xFF);
        int h6 = 0; // flags
        data.prepend(h6).prepend(h5).prepend(h4).prepend(h3).prepend(h2).prepend(h1).prepend(h0);

        // sending
        if (protocol != null)
            protocol.send(data);
        else socket.send(new DatagramPacket(data.toByteArray(), data.length(), InetAddress.getByName(host), port));
    }

    public void reset() {
        sid = 0;
        token = 0;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setProtocol(IProtocol protocol) {
        this.protocol = protocol;
    }
}
