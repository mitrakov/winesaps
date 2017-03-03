package ru.mitrakov.self.rush;

import com.badlogic.gdx.*;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.Network;

public class RushClient extends Game {
    private final Model model = new Model();
    private final PsObject psObject; // may be NULL
    private Screen screenLogin;
    private Screen screenMain;
    private Screen screenBattle;

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
        // screens may be created only since here! Do not do it in constructor, because Gdx would not be ready
        screenLogin = new ScreenLogin(this, model, psObject);
        screenMain = new ScreenMain(this, model);
        screenBattle = new ScreenBattle(this, model);
        setScreen(screenLogin);
    }

    @Override
    public void dispose() {
        super.dispose();
        screenLogin.dispose();
        screenMain.dispose();
        screenBattle.dispose();
    }

    void setNextScreen() {
        if (screen == screenLogin)
            setScreen(screenMain);
        else if (screen == screenMain)
            setScreen(screenBattle);
        else if (screen == screenBattle)
            setScreen(screenMain);
    }
}
