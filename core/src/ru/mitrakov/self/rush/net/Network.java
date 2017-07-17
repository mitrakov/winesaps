package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * Created by mitrakov on 23.02.2017
 */
public final class Network extends Thread implements IHandler {
    public static boolean TMP_NO_CONNECTION = false; // REMOVE ME IN A FUTURE

    public static final int BUF_SIZ_SEND = 768;
    private static final int BUF_SIZ_RECV = 1024;
    private static final int HEADER_SIZ = 7;
    private static final int FLAGS = 0;

    // on Android don't forget to add "<uses-permission android:name="android.permission.INTERNET"/>" to manifest
    // otherwise new DatagramSocket() throws PermissionDeniedException
    private final DatagramSocket socket = new DatagramSocket();
    private final PsObject psObject;
    private final IHandler handler;
    private final UncaughtExceptionHandler errorHandler;
    private final String host;
    private final int port;
    private final byte[] recvBuf = new byte[BUF_SIZ_RECV];
    private final IIntArray recvData = new GcResistantIntArray(BUF_SIZ_RECV);
    private /*final*/ DatagramPacket packet;

    private int sid = 0;
    private long token = 0;
    private IProtocol protocol;

    public Network(PsObject psObject, IHandler handler, UncaughtExceptionHandler eHandler, String host, int port)
            throws SocketException {
        assert psObject != null && handler != null && eHandler != null && host != null && 0 < port && port < 65536;
        this.psObject = psObject;
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
        // connect to the server
        if (protocol != null) try {
            protocol.connect();
        } catch (IOException e) {
            errorHandler.uncaughtException(null, e);
        }

        // create DatagramPacket OUTSIDE the loop to minimize memory allocations
        DatagramPacket datagram = new DatagramPacket(recvBuf, recvBuf.length);

        // run infinite loop
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
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
        if (!connected) {
            psObject.runTask(2000, new Runnable() {
                @Override
                public void run() {
                    if (protocol != null && !protocol.isConnected()) try {
                        protocol.connect();
                    } catch (IOException e) {
                        errorHandler.uncaughtException(null, e);
                    }
                }
            });
        }
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
        int h6 = FLAGS;
        int h7 = data.length() / 256;
        int h8 = data.length() % 256;
        data.prepend(h8).prepend(h7).prepend(h6).prepend(h5).prepend(h4).prepend(h3).prepend(h2).prepend(h1)
                .prepend(h0);

        // sending
        if (protocol != null)
            protocol.send(data);
        else socket.send(getPacket(data.toByteArray(), data.length()));
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

    private DatagramPacket getPacket(byte[] data, int length) throws UnknownHostException {
        if (packet == null)
            packet = new DatagramPacket(data, length, InetAddress.getByName(host), port);
        else packet.setData(data, 0, length);
        return packet;
    }
}
