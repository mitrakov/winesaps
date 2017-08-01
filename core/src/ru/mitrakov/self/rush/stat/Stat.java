package ru.mitrakov.self.rush.stat;

import java.io.IOException;
import java.net.SocketException;

import com.badlogic.gdx.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.net.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Entry point
 */
public class Stat extends Game {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    private static final String HOST = "winesaps.com";
    private static final int PORT = 33996;

    private final PsObject psObject;
    private final ParserStat parser = new ParserStat();
    private final IIntArray query = new GcResistantIntArray(32);
    private final Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    };
    private /*final*/ Network network;
    private /*final*/ ScreenStat screen;

    public Stat(PsObject psObject) {
        assert psObject != null;
        this.psObject = psObject;
        try {
            network = new Network(psObject, parser, errorHandler, HOST, PORT);
            network.setProtocol(new SwUDP(psObject, network.getSocket(), HOST, PORT, network));

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create() {
        setScreen(screen = new ScreenStat(psObject).init());
        parser.setScreen(screen);
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);

        network.start();
        psObject.runDaemon(2000, 2000, new Runnable() {
            @Override
            public void run() {
                try {
                    query.clear().add(0xF0);
                    network.send(query);
                } catch (IOException e) {
                    errorHandler.uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        screen.dispose();
    }

    public void mute(boolean value) {
    }

    public void setRatio(float value) {
    }
}
