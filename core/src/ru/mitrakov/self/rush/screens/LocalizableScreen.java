package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.DialogLock;

/**
 * Created by mitrakov on 11.04.2017
 */
@SuppressWarnings("WeakerAccess")
public abstract class LocalizableScreen extends ScreenAdapter implements Localizable {
    protected final RushClient game;
    protected final Model model;
    protected final PsObject psObject;
    protected final Skin skin;
    protected final AudioManager audioManager;
    protected final Stage stage = new Stage(new FitViewport(RushClient.WIDTH, RushClient.HEIGHT));
    protected final Table table = new Table();
    protected final DialogLock connectingDialog;

    protected float glClearR = .96f;
    protected float glClearG = .92f;
    protected float glClearB = .92f;

    private boolean connected;

    LocalizableScreen(final RushClient game, Model model, PsObject psObject, Skin skin, AudioManager audioManager) {
        assert game != null && model != null && skin != null && audioManager != null; // psObject may be NULL
        this.game = game;
        this.model = model;
        this.psObject = psObject;
        this.skin = skin;
        this.audioManager = audioManager;
        connectingDialog = new DialogLock(skin, "panel-lock");

        table.setFillParent(true);
        stage.addActor(table);

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
            if (psObject != null)
                psObject.hide();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
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

    public abstract void handleEvent(EventBus.Event event);
    public abstract void handleEventBackground(EventBus.Event event);
}
