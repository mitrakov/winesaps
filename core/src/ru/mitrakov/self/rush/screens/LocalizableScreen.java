package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.model.Model;

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

    LocalizableScreen(RushClient game, Model model, PsObject psObject, Skin skin, AudioManager audioManager) {
        assert game != null && model != null && skin != null && audioManager != null; // psObject may be NULL
        this.game = game;
        this.model = model;
        this.psObject = psObject;
        this.skin = skin;
        this.audioManager = audioManager;

        table.setFillParent(true);
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // redraw all
        Gdx.gl.glClearColor(.25f, .77f, .81f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

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
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
