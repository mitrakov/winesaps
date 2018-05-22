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
@SuppressWarnings("unused")
public class Stat extends Game {
    /** Platform Specific Object */
    private final PsObject psObject;
    /** Incoming message handler */
    private final ParserStat parser = new ParserStat();
    /** Helper array to avoid "new" operations and decrease GC pressure */
    private final IIntArray array = new GcResistantIntArray(32);
    /** Simple error handler */
    private final Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    };

    /** Network */
    private /*final*/ Network network;
    /** Reference to the protocol (only SwUDP supported for now) */
    private /*final*/ SwUDP protocol;
    /** Main Statistics Screen */
    private /*final*/ ScreenStat screen;

    /**
     * Creates new instance of Statistics Application.
     * <br>Please DO NOT put here any things related with LibGDX objects. Do it in {@link #create()} method
     * @param psObject Platform Specific Object (NON-NULL)
     */
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
        final IIntArray query = new GcResistantIntArray(1).add(0xF0);
        psObject.runDaemon(2000, 2000, new Runnable() {
            @Override
            public void run() {
                try {
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
            array.fromByteArray(getBytes(arg), arg.length()).prepend(0xF1);
            network.send(array);
        } catch (IOException e) {
            errorHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    /**
     * Turns the music/SFX on/off
     * @param value true to mute music/SFX
     */
    public void mute(boolean value) {
    }

    /**
     * Informs the game about changing the screen ratio
     * @param ratio float value of width/height
     */
    public void setRatio(float ratio) {
    }
}
