package ru.mitrakov.self.rush;

import java.net.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
            String host = "winesaps.ru"; // TODO move to config (@mitrakov: right, I'll do it in the future)
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
        AssetManager assetManager = new AssetManager(); // TODO WTF?
        assetManager.load("back/login.jpg", Texture.class);
        assetManager.load("back/main.jpg", Texture.class);
        assetManager.load("back/battle0.jpg", Texture.class);
        assetManager.load("back/battle1.jpg", Texture.class);
        assetManager.load("back/battle2.jpg", Texture.class);
        assetManager.load("back/battle3.jpg", Texture.class);
        assetManager.load("skin/uiskin.json", Skin.class);
        assetManager.load("i18n/bundle", I18NBundle.class);
        assetManager.load("i18n/bundle_ru", I18NBundle.class);
        assetManager.load("i18n/bundle_es", I18NBundle.class);
        assetManager.load("i18n/bundle_pt", I18NBundle.class);
        assetManager.load("i18n/bundle_fr", I18NBundle.class);
        assetManager.load("pack/ability.pack", TextureAtlas.class);
        assetManager.load("pack/animated.pack", TextureAtlas.class);
        assetManager.load("pack/cat.pack", TextureAtlas.class);
        assetManager.load("pack/char.pack", TextureAtlas.class);
        assetManager.load("pack/down.pack", TextureAtlas.class);
        assetManager.load("pack/effects.pack", TextureAtlas.class);
        assetManager.load("pack/flare.pack", TextureAtlas.class);
        assetManager.load("pack/goods.pack", TextureAtlas.class);
        assetManager.load("pack/hedgehog.pack", TextureAtlas.class);
        assetManager.load("pack/icons.pack", TextureAtlas.class);
        assetManager.load("pack/ladder.pack", TextureAtlas.class);
        assetManager.load("pack/menu.pack", TextureAtlas.class);
        assetManager.load("pack/rabbit.pack", TextureAtlas.class);
        assetManager.load("pack/squirrel.pack", TextureAtlas.class);
        assetManager.load("pack/thing.pack", TextureAtlas.class);
        assetManager.load("pack/training.pack", TextureAtlas.class);
        assetManager.load("pack/up.pack", TextureAtlas.class);
        assetManager.load("pack/wolf.pack", TextureAtlas.class);
        assetManager.load("music/battle0.mp3", Music.class);
        assetManager.load("music/battle1.mp3", Music.class);
        assetManager.load("music/battle2.mp3", Music.class);
        assetManager.load("music/battle3.mp3", Music.class);
        assetManager.load("music/theme.mp3", Music.class);
        assetManager.load("sfx/AntidoteThing.wav", Sound.class);
        assetManager.load("sfx/BeamThing.wav", Sound.class);
        assetManager.load("sfx/BoxThing.wav", Sound.class);
        assetManager.load("sfx/call.wav", Sound.class);
        assetManager.load("sfx/click.wav", Sound.class);
        assetManager.load("sfx/DetectorThing.wav", Sound.class);
        assetManager.load("sfx/Devoured.wav", Sound.class);
        assetManager.load("sfx/die.wav", Sound.class);
        assetManager.load("sfx/Exploded.wav", Sound.class);
        assetManager.load("sfx/FlashbangThing.wav", Sound.class);
        assetManager.load("sfx/food.wav", Sound.class);
        assetManager.load("sfx/game.wav", Sound.class);
        assetManager.load("sfx/ladder.wav", Sound.class);
        assetManager.load("sfx/MineThing.wav", Sound.class);
        assetManager.load("sfx/Poisoned.wav", Sound.class);
        assetManager.load("sfx/round.wav", Sound.class);
        assetManager.load("sfx/Soaked.wav", Sound.class);
        assetManager.load("sfx/Sunk.wav", Sound.class);
        assetManager.load("sfx/TeleportThing.wav", Sound.class);
        assetManager.load("sfx/thing.wav", Sound.class);
        assetManager.load("sfx/UmbrellaThing.wav", Sound.class);

        assetManager.finishLoading(); // TODO !!!!!!

        i18nEn = assetManager.get("i18n/bundle");
        i18nRu = assetManager.get("i18n/bundle_ru");
        i18nEs = assetManager.get("i18n/bundle_es");
        i18nPt = assetManager.get("i18n/bundle_pt");
        i18nFr = assetManager.get("i18n/bundle_fr");

        Skin skin = assetManager.get("skin/uiskin.json");
        audioManager = new AudioManager(assetManager);
        screenLogin = new ScreenLogin(this, model, psObject, assetManager, skin, audioManager, i18nEn);
        screenCharacter = new ScreenCharacter(this, model, psObject, assetManager, skin, audioManager);
        screenTraining = new ScreenTraining(this, model, psObject, assetManager, skin, audioManager);
        screenMain = new ScreenMain(this, model, psObject, assetManager, skin, audioManager, i18nEn);
        screenBattle = new ScreenBattle(this, model, psObject, assetManager, skin, audioManager, i18nEn);
        setScreen(screenLogin);
        audioManager.music("theme", false);

        // starting network Thread (recommended to start after building all screens to avoid skipping events)
        network.start();

        // catch Android buttons
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);

        if (psObject != null) {
            // stop music on hide
            psObject.setVisibleListener(new PsObject.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    audioManager.mute(!visible);
                }
            });
            // start Google Play Billing service
            IBillingProvider provider = psObject.getBillingProvider();
            if (provider != null) {
                provider.startService(new IBillingProvider.BillingListener() {
                    @Override
                    public void onResponse(String data, String signature) {
                        model.checkPurchase(data, signature);
                    }
                });
            }
        }

        // set default locale
        updateLocale();
    }

    @Override
    public void dispose() {
        super.dispose();
        screenLogin.dispose();
        screenCharacter.dispose();
        screenTraining.dispose();
        screenMain.dispose();
        screenBattle.dispose();
        // assetManager.dispose(); TODO
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

    public String getDebugInfo(String key) {
        // Java-6 doesn't support switch on strings (https://stackoverflow.com/questions/338206)
        if (key.equals("#!name")) return model.name;
        if (key.equals("#!hash")) return model.hash;
        if (key.equals("#!connected")) return model.connected + "";
        if (key.equals("#!settings")) return model.fileReader.read(Model.SETTINGS_FILE).replaceAll(" ", "\n");
        if (key.equals("#!products")) {
            if (psObject != null && psObject.getBillingProvider() != null)
                return psObject.getBillingProvider().getProducts().toString().replaceAll(",",",\n");
        }
        return "";
    }
}
