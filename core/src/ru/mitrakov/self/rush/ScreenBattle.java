package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 01.03.2017
 */

class ScreenBattle extends ScreenAdapter {
    private final Stage stage = new Stage(new FitViewport(800, 480), new SpriteBatch());
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    private final Table table = new Table();
    private final Actor gui;
    private final TextButton btnThing = new TextButton("Thing", skin, "default"); // new ImageButton(...);
    private final Group abilityButtons = new TextButton("Abilities", skin, "default"); //new Group();
    private final Label lblScore = new Label("Score: 0-0", skin);

    ScreenBattle(Model model) {
        gui = new Gui(model);
        Gdx.input.setInputProcessor(stage);
        table.setFillParent(true);
        stage.addActor(table);

        initializeComponents();
        model.invite("Bobby");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.35f, .87f, .91f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose(); // batch will also be disposed
        skin.dispose();
    }

    private void initializeComponents() {
        table.add(gui).colspan(3);
        table.row();
        table.add(btnThing);
        table.add(abilityButtons);
        table.add(lblScore);
    }
}
