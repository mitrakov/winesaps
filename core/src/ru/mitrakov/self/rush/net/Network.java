package ru.mitrakov.self.rush.net;

import java.net.*;
import java.util.Locale;
import java.io.IOException;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * Main networking class
 * Class is intended to have a single instance
 * @author mitrakov
 */
public final class Network extends Thread implements IHandler {
    // @note uncomment only for debug! public static boolean TMP_NO_CONNECTION = false;

    /** Buffer size for SEND operations (in bytes) */
    public static final int BUF_SIZ_SEND = 768;
    /** Buffer size for RECV operations (in bytes) */
    private static final int BUF_SIZ_RECV = 1024;
    /** SwUDP header size (see SwUDP protocol for more details) */
    private static final int HEADER_SIZ = 7;
    /** Standard flags for SEND operations */
    private static final int FLAGS = 0;
    /** SwUDP protocol version, supported by this client (note that the versions <b>MUST</b> be equal!) */
    private static final int PROTOCOL_VERSION = 0;

    /**
     * Datagram socket
     * <br><b>Note:</b> on Android don't forget to add "<uses-permission android:name="android.permission.INTERNET"/>"
     * to manifest otherwise new DatagramSocket() throws PermissionDeniedException
     */
    private final DatagramSocket socket = new DatagramSocket();
    /** Platform Specific object */
    private final PsObject psObject;
    /** Handler for received messages */
    private final IHandler handler;
    /** Error handler */
    private final UncaughtExceptionHandler errorHandler;
    /** Host (IP address or DNS name) */
    private final String host;
    /** Application UDP port */
    private final int port;
    /** Main buffer for incoming messages */
    private final byte[] recvBuf = new byte[BUF_SIZ_RECV];
    /** Internal storage for the last received message (needed to empty the main buffer) */
    private final IIntArray recvData = new GcResistantIntArray(BUF_SIZ_RECV);
    /** Datagram packet for outgoing messages */
    private /*final*/ DatagramPacket packet;

    /** Session ID for this client (see SwUDP protocol for more details) */
    private int sid = 0;
    /** Session Token for this client (see SwUDP protocol for more details) */
    private long token = 0;
    /** Reference to the protocol (only SwUDP supported for now) */
    private IProtocol protocol;

    /**
     * Creates a new instance of Network
     * @param psObject Platform Specific Object (NON-NULL)
     * @param handler handler to process incoming messages
     * @param eHandler error handler
     * @param host host (IP-address or host name)
     * @param port port (0 < port < 65536)
     * @throws SocketException if DatagramSocket cannot be created (e.g. if there are no permissions on Android)
     */
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
                // @note uncomment only for debug! if (TMP_NO_CONNECTION) continue;
                if (protocol != null) {
                    recvData.fromByteArray(datagram.getData(), datagram.getLength());
                    if (recvData.length() > 5)
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
            int flags = data.get(6);
            int protocolVersion = (flags & 0x70) >> 4;

            if (protocolVersion != PROTOCOL_VERSION) {
                String msg = String.format(Locale.getDefault(), "Unsupported protocol version (cur: %d, required: %d)",
                        PROTOCOL_VERSION, protocolVersion);
                throw new UnsupportedOperationException(msg);
            }
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

    /**
     * Sends message to the server, prepending it with sid, token, flags and msgSize fields
     * @param data data
     * @throws IOException if the host cannot be resolved
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public void send(IIntArray data) throws IOException {
        // @note uncomment only for debug! if (TMP_NO_CONNECTION) return;

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

    /**
     * Resets the network state with a given sid and token
     * @param sid sid (default is 0)
     * @param token token (default is 0)
     */
    public void reset(int sid, long token) {
        this.sid = sid;
        this.token = token;
    }

    /** @return socket */
    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Sets a new transport protocol for the network
     * @param protocol protocol (may be NULL)
     */
    public void setProtocol(IProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Packs given data to a datagram packet and returns this packet.
     * Method is designed to reduce GC pressure by avoiding "new DatagramPacket" operations
     * @param data data
     * @param length data length
     * @return ready to send datagram packet
     * @throws UnknownHostException if the host cannot be resolved
     */
    private DatagramPacket getPacket(byte[] data, int length) throws UnknownHostException {
        if (packet == null)
            packet = new DatagramPacket(data, length, InetAddress.getByName(host), port);
        else packet.setData(data, 0, length);
        return packet;
    }
}
