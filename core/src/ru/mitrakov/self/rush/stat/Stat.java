package ru.mitrakov.self.rush.stat;

import java.io.IOException;
import java.net.SocketException;

import com.badlogic.gdx.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.net.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.Winesaps.*;
import static ru.mitrakov.self.rush.utils.Utils.*;

/**
 * <b>Statistics Application. Entry Point</b>
 * <br>We need a tool to show current Server statistics and call Server Control functions (e.g. soft reboot and so on).
 * So we can take any tool that supports SwUDP and write simple client for those simple purposes.
 * One of the options - reuse the Winesaps applications itself! Maybe it's a not super-idiomatic way, but here we have
 * ready SwUDP protocol implementation, Network class and even Android support! The only thing we should do is to switch
 * between Stat class and Winesaps class (as well as some configs) to build Stat or Main application, respectively.
 *
 * <br><br>Probably, more "clean" way is to extract common classes to a library, create 2 independent projects and link
 * the extracted classes to them; but for now I decided that switching between Stat/Winesaps is the simpliest solution.
 *
 * <br><br>Algorithm:
 * <ul>
 *     <li>search "for Stat application" in the project and follow the further instructions
 *     <li>build the Stats application
 *     <li>revert changes back
 * </ul>
 * @author Mitrakov
 */
public class Stat extends Game {
    private final PsObject psObject;
    private final ParserStat parser = new ParserStat();
    private final IIntArray query = new GcResistantIntArray(32);
    private final IIntArray query2 = new GcResistantIntArray(32);
    private final Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    };
    private /*final*/ Network network;
    private /*final*/ SwUDP protocol;
    private /*final*/ ScreenStat screen;

    public Stat(PsObject psObject) {
        assert psObject != null;
        this.psObject = psObject;
        try {
            network = new Network(psObject, parser, errorHandler, HOST, PORT);
            protocol = new SwUDP(psObject, network.getSocket(), HOST, PORT, network);
            network.setProtocol(protocol);
            network.reset(0, 0x00000000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create() {
        setScreen(screen = new ScreenStat(this, psObject).init());
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
                    screen.setSrtt(protocol.getSrtt());
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

    void sendCmd(String arg) {
        try {
            query2.fromByteArray(getBytes(arg), arg.length()).prepend(0xF1);
            network.send(query2);
        } catch (IOException e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    public void mute(boolean value) {
    }

    public void setRatio(float value) {
    }
}
