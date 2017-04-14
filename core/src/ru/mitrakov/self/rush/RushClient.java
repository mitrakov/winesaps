package ru.mitrakov.self.rush;

import java.util.Locale;
import java.net.InetAddress;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import ru.mitrakov.self.rush.net.*;
import ru.mitrakov.self.rush.screens.*;
import ru.mitrakov.self.rush.model.Model;

public class RushClient extends Game implements Localizable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    private final Model model = new Model();
    private final PsObject psObject; // may be NULL
    private /*final*/ Skin skin;
    private /*final*/ AudioManager audioManager;
    private /*final*/ LocalizableScreen screenLogin;
    private /*final*/ LocalizableScreen screenCharacter;
    private /*final*/ LocalizableScreen screenTraining;
    private /*final*/ LocalizableScreen screenMain;
    private /*final*/ LocalizableScreen screenBattle;
    private /*final*/ I18NBundle i18nEn;
    private /*final*/ I18NBundle i18nRu;

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

        i18nEn = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("en"));
        i18nRu = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("ru"));

        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        audioManager = new AudioManager("theme");
        screenLogin = new ScreenLogin(this, model, psObject, skin, audioManager);
        screenCharacter = new ScreenCharacter(this, model, psObject, skin, audioManager);
        screenTraining = new ScreenTraining(this, model, psObject, skin, audioManager);
        screenMain = new ScreenMain(this, model, psObject, skin, audioManager, i18nEn);
        screenBattle = new ScreenBattle(this, model, psObject, skin, audioManager, i18nEn);
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

        // set default locale
        updateLocale();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
        audioManager.dispose();
        screenLogin.dispose();
        screenCharacter.dispose();
        screenTraining.dispose();
        screenMain.dispose();
        screenBattle.dispose();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        screenLogin.onLocaleChanged(bundle);
        screenCharacter.onLocaleChanged(bundle);
        screenTraining.onLocaleChanged(bundle);
        screenMain.onLocaleChanged(bundle);
        screenBattle.onLocaleChanged(bundle);
    }

    public void setNextScreen() {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android
        if (screen == screenLogin)
            setScreen(screenCharacter);
        else if (screen == screenCharacter)
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

    public void updateLocale() {
        onLocaleChanged(model.languageEn ? i18nEn : i18nRu);
    }
}
