package ru.mitrakov.self.rush;

import java.net.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.net.*;
import ru.mitrakov.self.rush.screens.*;
import ru.mitrakov.self.rush.model.Model;

/**
 * Entry point
 */
public class Winesaps extends Game {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    private final PsObject psObject;
    private final Model model;
    private /*final*/ Network network;
    private /*final*/ AssetManager assetManager;
    private /*final*/ AudioManager audioManager;
    private /*final*/ LocalizableScreen screenLogin;
    private /*final*/ LocalizableScreen screenCharacter;
    private /*final*/ LocalizableScreen screenTraining;
    private /*final*/ LocalizableScreen screenMain;
    private /*final*/ LocalizableScreen screenBattle;
    private /*final*/ Stage stage; // to draw splash screen only!

    public Winesaps(PsObject psObject) {
        assert psObject != null;
        this.psObject = psObject;
        model = new Model(psObject);
        try {
            // start Network in a separate thread (requirement of Android)
            Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            };
            String host = "winesaps.ru"; // TODO move to config
            int port = 33996;
            network = new Network(psObject, new Parser(model, psObject), errorHandler, host, port);
            network.setProtocol(new SwUDP(psObject, network.getSocket(), host, port, network));

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
        assetManager = new AssetManager();
        assetManager.load("back/login.jpg", Texture.class);
        assetManager.finishLoading(); // synchronous loading of the splash screen

        stage = new Stage(new FitViewport(WIDTH, HEIGHT));
        stage.addActor(new Image(assetManager.<Texture>get("back/login.jpg")));

        enqueueAssets();              // other assets will be loaded asynchronously
    }

    @Override
    public void render() {
        if (screen != null)                             // screen exists
            screen.render(Gdx.graphics.getDeltaTime());
        else if (assetManager.update()) {               // loading assets (returns true when finished)
            init();
        } else {                                        // draw splash screen
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            stage.act();
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
        screenLogin.dispose();
        screenCharacter.dispose();
        screenTraining.dispose();
        screenMain.dispose();
        screenBattle.dispose();
        assetManager.dispose();
        stage.dispose();
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
        I18NBundle bundle = assetManager.get(String.format("i18n/bundle_%s", model.language));
        screenLogin.onLocaleChanged(bundle);
        screenCharacter.onLocaleChanged(bundle);
        screenTraining.onLocaleChanged(bundle);
        screenMain.onLocaleChanged(bundle);
        screenBattle.onLocaleChanged(bundle);
    }

    public String getDebugInfo(String key) {
        // Java-6 doesn't support switch on strings (https://stackoverflow.com/questions/338206)
        if (key.equals("#!name")) return model.name;
        if (key.equals("#!hash")) return model.hash;
        if (key.equals("#!connected")) return model.connected + "";
        if (key.equals("#!settings")) return model.fileReader.read(Model.SETTINGS_FILE).replaceAll(" ", "\n");
        if (key.equals("#!products")) {
            if (psObject.getBillingProvider() != null)
                return psObject.getBillingProvider().getProducts().toString().replaceAll(",",",\n");
        }
        return "";
    }

    private void init() {
        // the following actions MUST be done only since here or create() method!
        // Do NOT do it in constructor because Gdx would not be ready
        model.loadSettings();

        audioManager = new AudioManager(assetManager);
        screenLogin = new ScreenLogin(this, model, psObject, assetManager, audioManager);
        screenCharacter = new ScreenCharacter(this, model, psObject, assetManager, audioManager);
        screenTraining = new ScreenTraining(this, model, psObject, assetManager, audioManager);
        screenMain = new ScreenMain(this, model, psObject, assetManager, audioManager);
        screenBattle = new ScreenBattle(this, model, psObject, assetManager, audioManager);
        setScreen(screenLogin);
        audioManager.music("theme", false);

        // starting network Thread (recommended to start after building all screens to avoid skipping events)
        network.start();

        // catch Android buttons
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);

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

        // set default locale
        updateLocale();
    }

    private void enqueueAssets() {
        // List all your assets here (except "back/login.jpg" - it is our splash screen, and must be loaded before)
        assetManager.load("back/main.jpg", Texture.class);
        assetManager.load("back/battle0.jpg", Texture.class);
        assetManager.load("back/battle1.jpg", Texture.class);
        assetManager.load("back/battle2.jpg", Texture.class);
        assetManager.load("back/battle3.jpg", Texture.class);
        assetManager.load("skin/uiskin.json", Skin.class);
        assetManager.load("i18n/bundle_en", I18NBundle.class);
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
    }
}
