package ru.mitrakov.self.rush;

import java.net.InetAddress;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import ru.mitrakov.self.rush.net.*;
import ru.mitrakov.self.rush.screens.*;
import ru.mitrakov.self.rush.model.Model;

public class RushClient extends Game {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    private final Model model = new Model();
    private final PsObject psObject; // may be NULL
    private /*final*/ Skin skin;
    private /*final*/ AudioManager audioManager;
    private /*final*/ Screen screenLogin;
    private /*final*/ Screen screenTraining;
    private /*final*/ Screen screenMain;
    private /*final*/ Screen screenBattle;

    public RushClient(PsObject psObject) {
        this.psObject = psObject;
        try {
            ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true); // turn the asserts on

            // start Network in a separate thread (requirement of Android)
            Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            };
            InetAddress address = InetAddress.getByName("192.168.1.2");
            int port = 33996;
            Network network = new Network(new Parser(model, psObject), errorHandler, address, port);
            network.setProtocol(new Protocol(network.getSocket(), address, port, network));
            network.start();

            // set up model
            model.setSender(new MsgSender(network, errorHandler));
            model.setFileReader(new FileReader());
            model.connected = false; // it's true by default because no Protocols provided, but now we have SwUDP
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void create() {
        // the following actions MUST be done only since here! Don't do it in constructor because Gdx would not be ready
        model.loadSettings();
        model.signIn(); // try to sign in using stored credentials
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        audioManager = new AudioManager("theme");
        screenLogin = new ScreenLogin(this, model, psObject, skin);
        screenTraining = new ScreenTraining(this, model, psObject, skin);
        screenMain = new ScreenMain(this, model, psObject, skin);
        screenBattle = new ScreenBattle(this, model, psObject, audioManager, skin);
        setScreen(screenLogin);

        // catch Android buttons
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);

        // stop music on hide
        if (psObject != null) {
            psObject.setVisibleListener(new PsObject.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    audioManager.pauseMusic(!visible);
                }
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
        audioManager.dispose();
        screenLogin.dispose();
        screenTraining.dispose();
        screenMain.dispose();
        screenBattle.dispose();
    }

    public void setNextScreen() {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android
        if (screen == screenLogin)
            setScreen(screenTraining);
        else if (screen == screenTraining)
            setScreen(screenMain);
        else if (screen == screenMain)
            setScreen(screenBattle);
        else if (screen == screenBattle)
            setScreen(screenMain);
    }

    public void setLoginScreen() {
        setScreen(screenLogin);
    }
}
