package ru.mitrakov.self.rush;

import java.net.*;
import java.util.Locale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import ru.mitrakov.self.rush.net.*;
import ru.mitrakov.self.rush.screens.*;
import ru.mitrakov.self.rush.model.Model;

/**
 * Entry point
 */
public class Winesaps extends Game implements Localizable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    private final Model model = new Model();
    private final PsObject psObject; // may be NULL
    private /*final*/ Network network;
    private /*final*/ Skin skin;
    private /*final*/ AudioManager audioManager;
    private /*final*/ LocalizableScreen screenLogin;
    private /*final*/ LocalizableScreen screenCharacter;
    private /*final*/ LocalizableScreen screenTraining;
    private /*final*/ LocalizableScreen screenMain;
    private /*final*/ LocalizableScreen screenBattle;
    private /*final*/ I18NBundle i18nEn;
    private /*final*/ I18NBundle i18nRu;
    private /*final*/ I18NBundle i18nEs;
    private /*final*/ I18NBundle i18nPt;
    private /*final*/ I18NBundle i18nFr;

    public Winesaps(PsObject psObject) {
        this.psObject = psObject;
        try {
            // start Network in a separate thread (requirement of Android)
            Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            };
            String host = "winesaps.ru";
            int port = 33996;
            network = new Network(new Parser(model, psObject), errorHandler, host, port);
            network.setProtocol(new SwUDP(network.getSocket(), host, port, network));

            // set up model
            model.setSender(new MsgSender(network, errorHandler));
            model.setFileReader(new FileReader());
            model.connected = false; // it's true by default because no Protocols provided, but now we have SwUDP
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create() {
        // the following actions MUST be done only since here! Don't do it in constructor because Gdx would not be ready
        model.loadSettings();

        i18nEn = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("en"));
        i18nRu = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("ru"));
        i18nEs = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("es"));
        i18nPt = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("pt"));
        i18nFr = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"), new Locale("fr"));

        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        audioManager = new AudioManager("theme");
        screenLogin = new ScreenLogin(this, model, psObject, skin, audioManager, i18nEn);
        screenCharacter = new ScreenCharacter(this, model, psObject, skin, audioManager);
        screenTraining = new ScreenTraining(this, model, psObject, skin, audioManager);
        screenMain = new ScreenMain(this, model, psObject, skin, audioManager, i18nEn);
        screenBattle = new ScreenBattle(this, model, psObject, skin, audioManager, i18nEn);
        setScreen(screenLogin);

        // starting network Thread (recommended to start after building all screens to avoid skipping events)
        network.start();

        // catch Android buttons
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);

        // stop music on hide
        if (psObject != null) {
            psObject.setVisibleListener(new PsObject.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    audioManager.mute(!visible);
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
        switch (model.language) {
            case English:
                onLocaleChanged(i18nEn);
                break;
            case Russian:
                onLocaleChanged(i18nRu);
                break;
            case Spanish:
                onLocaleChanged(i18nEs);
                break;
            case Portuguese:
                onLocaleChanged(i18nPt);
                break;
            case French:
                onLocaleChanged(i18nFr);
                break;
            default:
                onLocaleChanged(i18nEn);
        }
    }
}
