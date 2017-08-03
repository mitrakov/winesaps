package ru.mitrakov.self.rush.desktop;

import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import com.badlogic.gdx.backends.lwjgl.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.stat.Stat;

public class DesktopLauncher extends JFrame {
    public static void main(String[] arg) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true); // turn the asserts on (Desktop only)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = Winesaps.WIDTH;
                config.height = Winesaps.HEIGHT;
                new DesktopLauncher(config);
            }
        });
    }

    private DesktopLauncher(LwjglApplicationConfiguration config) {
        super();

        // register single instance application
        registerInstance();
        try {
            Thread.sleep(200); // see note#3 below
        } catch (InterruptedException ignored) {
        }

        // create platform specific object
        URL url = getClass().getResource("/icon.png"); // to get resource inside a FAT jar
        ImageIcon icon = url != null ? new ImageIcon(url) : new ImageIcon("icon.png");
        final PsObject obj = new DesktopPsObject(null, this, icon);

        final Winesaps game = new Winesaps(obj);
        // for Stat application: uncomment line below and (optionally) remove call to registerInstance() above, remove
        // call setDefaultCloseOperation(...) below, change title below
        // final Stat game = new Stat(obj);

        // set up JFrame
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Container container = getContentPane();
        container.add(new LwjglAWTCanvas(game, config).getCanvas(), BorderLayout.CENTER);
        container.setPreferredSize(new Dimension(config.width, config.height));
        setResizable(false); // this must be BEFORE pack()!
        pack();
        setTitle("Winesaps");
        setIconImage(icon.getImage());
        setVisible(true);
        setLocationRelativeTo(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                game.mute(false);
                obj.setActive(true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                game.mute(true);
                obj.setActive(false);
            }
        });
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
                    ServerSocket srv = new ServerSocket(PORT, 10, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
                    while (true) {
                        Socket sock = srv.accept();
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
                        System.exit(0); // it's OK! Please add an exception for FindBugs
                    } catch (IOException ignore) {
                    }
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }
}

// note#3 (@mitrakov, 2017-04-07): without delay, a new app instance is late to detect the first instance (because a new
// thread is being created) and SwUDP protocol sends needless 'SYN' message to the server; it's not dangerous, but it'd
// better terminate the new instance before that
