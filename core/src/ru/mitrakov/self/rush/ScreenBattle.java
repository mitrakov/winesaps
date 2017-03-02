package ru.mitrakov.self.rush;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 01.03.2017
 */

class ScreenBattle extends ScreenAdapter {
    private final Model model;
    private final Stage stage = new Stage(new FitViewport(800, 480), new SpriteBatch());
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    private final Table table = new Table();
    private final Actor gui;
    private final ImageButton btnThing;
    private final Group abilityButtons = new TextButton("Abilities", skin, "default"); //new Group();
    private final Label lblScore = new Label("", skin);

    private final Map<Class, Drawable> things = new HashMap<Class, Drawable>(3);

    ScreenBattle(final Model model) {
        assert model != null;
        this.model = model;
        gui = new Gui(model);
        Gdx.input.setInputProcessor(stage);
        table.setFillParent(true);
        stage.addActor(table);

        loadTextures();
        btnThing = new ImageButton(things.get(CellObject.class)); // CellObject.class means an 'empty' thing
        btnThing.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.useThing();
            }
        });

        buildTable();
        model.invite("Bobby");
    }

    @Override
    public void render(float delta) {
        // updating the thing
        Class clazz = model.curThing != null ? model.curThing.getClass() : CellObject.class;
        btnThing.getStyle().imageUp = things.get(clazz); // here getStyle() != NULL

        // updating the score
        lblScore.setText(String.format(Locale.getDefault(), "Score: %d-%d", model.score1, model.score2));

        // redraw all
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
        stage.dispose(); // batch and internal actors will also be disposed
        skin.dispose();
        for (Drawable drawable : things.values()) {
            if (drawable instanceof TextureRegionDrawable)
                ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose();
        }
    }

    private void loadTextures() {
        TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));
        for (Class clazz : new Class[]{CellObject.class, Mine.class, Umbrella.class}) {
            TextureRegion region = atlasThing.findRegion(clazz.getSimpleName());
            if (region != null)
                things.put(clazz, new TextureRegionDrawable(region));
        }
    }

    private void buildTable() {
        table.add(gui).colspan(3);
        table.row();
        table.add(btnThing).align(Align.left);
        table.add(abilityButtons);
        table.add(lblScore);
    }
}
