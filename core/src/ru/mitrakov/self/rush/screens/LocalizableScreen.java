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
 * Created by mitrakov on 11.04.2017
 */
@SuppressWarnings("WeakerAccess")
public abstract class LocalizableScreen extends ScreenAdapter implements Localizable {
    protected final Winesaps game;
    protected final Model model;
    protected final PsObject psObject;
    protected final AssetManager assetManager;
    protected final AudioManager audioManager;
    protected final Stage stage = new Stage(new FitViewport(Winesaps.WIDTH, Winesaps.HEIGHT));
    protected final Table table = new Table();
    protected final DialogLock connectingDialog;
    protected final DialogInfo infoDialog;

    protected float glClearR;
    protected float glClearG;
    protected float glClearB;

    private boolean connected;

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
                Gdx.app.postRunnable(new Runnable() {
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

    private void handleImportantEvents(EventBus.Event event) {
        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
        if (event instanceof EventBus.VersionNotAllowedEvent) {
            EventBus.VersionNotAllowedEvent ev = (EventBus.VersionNotAllowedEvent) event;
            String msg = i18n.format("dialog.info.unsupported.version", Winesaps.VERSION_STR, ev.minVersion);
            infoDialog.setText(i18n.format("error"), msg).show(stage);
        }
        if (event instanceof EventBus.UnsupportedProtocolEvent) {
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.unsupported.protocol")).show(stage);
        }
    }

    public abstract void handleEvent(EventBus.Event event);
    public abstract void handleEventBackground(EventBus.Event event);
}
