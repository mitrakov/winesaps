package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;

/**
 * Superclass for all screens. Also supports localization for other languages
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public abstract class LocalizableScreen extends ScreenAdapter implements Localizable {
    /** Winesaps instance */
    protected final Winesaps game;
    /** Reference to the model */
    protected final Model model;
    /** Platform Specific Object */
    protected final PsObject psObject;
    /** LibGdx Assets Manager */
    protected final AssetManager assetManager;
    /** Audio Manager */
    protected final AudioManager audioManager;
    /** LibGdx Scene2D Stage */
    protected final Stage stage = new Stage(new FitViewport(Winesaps.WIDTH, Winesaps.HEIGHT));
    /** Main Scene2D Table */
    protected final Table table = new Table();
    /** Reference to Connecting Pop Up Window (useful for all derived screens) */
    protected final DialogLock connectingDialog;
    /** Reference to Info Message Box (useful for all derived screens) */
    protected final DialogInfo infoDialog;

    /** OpenGL Clear color (Red component of RGB), default is Black (0-0-0) */
    protected float glClearR;
    /** OpenGL Clear color (Green component of RGB), default is Black (0-0-0) */
    protected float glClearG;
    /** OpenGL Clear color (Blue component of RGB), default is Black (0-0-0) */
    protected float glClearB;

    /** Connection flag (reflected to Model's connection flag) */
    private boolean connected;

    /**
     * Constructor
     * @param game instance of Winesaps (NON-NULL)
     * @param model model (NON-NULL)
     * @param psObject Platform Specific Object (NON-NULL)
     * @param assetManager asset manager (NON-NULL)
     * @param audioManager audio manager (NON-NULL)
     */
    LocalizableScreen(final Winesaps game, final Model model, PsObject psObject, final AssetManager assetManager,
                      AudioManager audioManager) {
        assert game != null && model != null && psObject != null && assetManager != null && audioManager != null;
        this.game = game;
        this.model = model;
        this.psObject = psObject;
        this.assetManager = assetManager;
        this.audioManager = audioManager;

        Skin skin = assetManager.get("skin/uiskin.json");
        connectingDialog = new DialogLock(skin, "panel-lock");
        infoDialog = new DialogInfo("", skin, "default");

        table.setFillParent(true);
        stage.addActor(table);
        infoDialog.setOnResultAction(new Runnable() {
            @Override
            public void run() {
                Gdx.net.openURI(Winesaps.URL);
                Gdx.app.exit(); // NOTE! DO NOT use it on iOS (see javaDocs)
            }
        });

        // adding event bus listener (subclasses must implement handleEvent() method)
        final Screen self = this;
        model.bus.addListener(new EventBus.Listener() {
            @Override
            public void OnEvent(final EventBus.Event event) {
                Gdx.app.postRunnable(new Runnable() { // see note#8 below
                    @Override
                    public void run() {
                        if (game.getScreen() == self)
                            handleEvent(event);
                        handleEventBackground(event);
                        handleImportantEvents(event); // handle the most important events right here (for ALL screens)
                    }
                });
            }
        });
    }

    @Override
    public void render(float delta) {
        // redraw all
        Gdx.gl.glClearColor(glClearR, glClearG, glClearB, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        // check connecting
        if (connected != model.connected) {
            if (connected && !model.connected)
                connectingDialog.show(stage);
            else connectingDialog.remove();
            connected = model.connected;
        }

        // checking BACK button on Android
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            psObject.hide();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        connectingDialog.remove(); // hide dialog if it was already opened before
        connected = model.connected;
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        connectingDialog.setText(bundle.format("dialog.connecting"));
    }

    /**
     * Handler for the most important events (for ALL screens). Please do not trespass on this method!
     * @param event model's event from Event Bus
     */
    private void handleImportantEvents(EventBus.Event event) {
        // @mitrakov (2017-08-05): do NOT put here local vars like "String.format()" or "i18n.format()". It causes
        // excessive work for GC on each event during a battle (because all screens are subscribed to events)

        if (event instanceof EventBus.VersionNotAllowedEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            EventBus.VersionNotAllowedEvent ev = (EventBus.VersionNotAllowedEvent) event;
            String msg = i18n.format("dialog.info.unsupported.version", Winesaps.VERSION_STR, ev.minVersion);
            infoDialog.setText(i18n.format("error"), msg).show(stage);
        }
        if (event instanceof EventBus.UnsupportedProtocolEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.unsupported.protocol")).show(stage);
        }
    }

    /**
     * Handles new events if and only if the current screen is active
     * @param event model's event from Event Bus
     */
    public abstract void handleEvent(EventBus.Event event);

    /**
     * Handles new events in any way (no matter whether the screen is active or not)
     * @param event model's event from Event Bus
     */
    public abstract void handleEventBackground(EventBus.Event event);
}

// note#8 (@mitrakov, 2017-08-05): unfortunately it's not possible to extract "new Runnable{...}" to a single field and
// substitute a new value of EventBus.Event each time an event appears (that would be very good for GC!).
// Gdx.app.postRunnable() stores different (!) Runnable instances inside itself, and calls all of them one-by-one
// when OpenGL cycle is ready to render
