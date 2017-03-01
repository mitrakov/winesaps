package ru.mitrakov.self.rush;

import com.badlogic.gdx.Game;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;

public class RushClient extends Game {
    private final Model model = new Model();
    private final PsObject psObject; // may be NULL

    public RushClient(PsObject psObject) {
        this.psObject = psObject;
        try {
            ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true); // turn the asserts on
            Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            };
            Network network = new Network(new Parser(model), errorHandler);
            network.start();
            model.setSender(new Sender(network, errorHandler));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void create() {
        setScreen(new ScreenLogin(this, model, psObject));
    }
}
