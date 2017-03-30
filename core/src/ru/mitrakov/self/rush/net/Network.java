package ru.mitrakov.self.rush.net;

import java.net.*;
import java.io.IOException;

import static ru.mitrakov.self.rush.net.Utils.copyOfRange;


/**
 * Created by mitrakov on 23.02.2017
 */

public class Network extends Thread {
    public interface IHandler {
        void handle(int[] data);
    }

    private static final int BUF_SIZ = 1024;
    private static final int HEADER_SIZ = 7;

    // on Android don't forget to add "<uses-permission android:name="android.permission.INTERNET"/>" to manifest
    // otherwise new DatagramSocket() throws PermissionDeniedException
    private final DatagramSocket socket = new DatagramSocket();
    private final IHandler handler;
    private final UncaughtExceptionHandler errorHandler;

    private int sid = 0;
    private long token = 0;

    public Network(IHandler handler, UncaughtExceptionHandler errorHandler) throws IOException {
        assert handler != null;
        this.handler = handler;
        this.errorHandler = errorHandler;
        setDaemon(true);
        setName("Network thread");
        setUncaughtExceptionHandler(errorHandler);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                DatagramPacket datagram = new DatagramPacket(new byte[BUF_SIZ], BUF_SIZ);
                socket.receive(datagram);
                socket.send(new DatagramPacket(new byte[] {datagram.getData()[0]}, 1, datagram.getAddress(),
                        datagram.getPort())); // TODO
                int[] data = new int[datagram.getLength()-1]; // TODO -1
                for (int i = 1; i < datagram.getLength(); i++) { // TODO i=1
                    data[i-1] = datagram.getData()[i] >= 0 ? datagram.getData()[i] : datagram.getData()[i] + 256; // TODO -1
                }
                if (data.length > HEADER_SIZ) {
                    if (sid * token == 0) {
                        sid = data[0] * 256 + data[1];
                        token = (data[2] << 24) | (data[3] << 16) | (data[4] << 8) | data[5];
                    }
                    handler.handle(copyOfRange(data, HEADER_SIZ, data.length));
                }
            } catch (Exception e) {
                errorHandler.uncaughtException(this, e);
            }
        }
    }

    public void send(byte[] data) throws IOException {
        // concatenate a header and data
        byte[] msg = new byte[data.length + HEADER_SIZ + 1]; // TODO +1
        msg[0] = (byte) 0xCC; // TODO
        msg[1] = (byte) (sid / 256);
        msg[2] = (byte) (sid % 256);
        msg[3] = (byte) ((token >> 24) & 0xFF);
        msg[4] = (byte) ((token >> 16) & 0xFF);
        msg[5] = (byte) ((token >> 8) & 0xFF);
        msg[6] = (byte) (token & 0xFF);
        msg[7] = 0; // flags
        System.arraycopy(data, 0, msg, HEADER_SIZ+1, data.length);// TODO +1

        // sending
        socket.send(new DatagramPacket(msg, msg.length, InetAddress.getByName("192.168.1.2"), 33996));
    }

    public void reset() {
        sid = 0;
        token = 0;
    }
}
