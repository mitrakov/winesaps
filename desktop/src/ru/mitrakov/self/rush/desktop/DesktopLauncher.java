package ru.mitrakov.self.rush.desktop;

import java.awt.*;
import java.net.*;
import java.io.IOException;

import javax.swing.*;

import com.badlogic.gdx.backends.lwjgl.*;

import ru.mitrakov.self.rush.*;

public class DesktopLauncher extends JFrame {
    public static void main(String[] arg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = 800;
                config.height = 480;
                new DesktopLauncher(config);
            }
        });
    }

    private DesktopLauncher(LwjglApplicationConfiguration config) throws HeadlessException {
        super();
        registerInstance();
        PsObject obj = new PsObject() {
            @Override
            public void activate() {
                setVisible(true);
            }
        };
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Container container = getContentPane();
        container.add(new LwjglAWTCanvas(new RushClient(obj), config).getCanvas(), BorderLayout.CENTER);
        container.setPreferredSize(new Dimension(config.width, config.height));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void registerInstance() {
        final int PORT = 31681;
        // creating new thread because accept() is a blocking operation
        Thread th = new Thread(new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            @Override
            public void run() {
                try {
                    // start listening on a loopback socket (if no exceptions thrown, then we run a first instance
                    ServerSocket serverSocket =
                            new ServerSocket(PORT, 10, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
                    while (true) {
                        Socket sock = serverSocket.accept();
                        // this code would run only if the user is attempting to start another instance of the app;
                        // it usually means that the user wants to switch the app from background to normal mode;
                        // so we just restart itself (whilst the other instance will be closed automatically)
                        if (sock.getInetAddress().isLoopbackAddress())
                            setVisible(true);
                    }
                } catch (IOException ignored) {
                    // failed to seize socket! It means that the user is attempting to start another instance;
                    // so we notify the first instance about this (causing it to restart) and shutdown itself
                    try {
                        new Socket("127.0.0.1", PORT).close();
                        System.exit(0);
                    } catch (IOException ignore) {
                    }
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }
}
