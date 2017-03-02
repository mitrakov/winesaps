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
    private final Table abilityButtons = new Table();
    private final Label lblScore = new Label("", skin);
    private final Map<Class, Drawable> things = new HashMap<Class, Drawable>(3);
    private final Map<Model.Ability, ImageButton> abilities = new HashMap<Model.Ability, ImageButton>(10);

    ScreenBattle(final Model model) {
        assert model != null;
        this.model = model;
        gui = new Gui(model);

        Gdx.input.setInputProcessor(stage);
        table.setFillParent(true);
        stage.addActor(table);

        loadTextures();
        btnThing = createButtonThing();
        buildTable();

        model.invite("Bobby");
    }

    @Override
    public void render(float delta) {
        // updating the thing
        Class clazz = model.curThing != null ? model.curThing.getClass() : CellObject.class;
        btnThing.getStyle().imageUp = things.get(clazz); // here getStyle() != NULL

        // updating the abilities
        if (abilityButtons.getColumns() != model.abilities.size())
            rebuildAbilities();

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
        stage.dispose(); // batch will also be disposed (but what about the internal actors?)
        skin.dispose();
        for (Drawable drawable : things.values()) {
            if (drawable instanceof TextureRegionDrawable)
                ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose();
        }
        for (ImageButton button : abilities.values()) {
            Drawable drawable = button.getStyle().imageUp;
            if (drawable != null && drawable instanceof TextureRegionDrawable)
                ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose();
        }
    }

    private void loadTextures() {
        TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));
        TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));

        for (Class clazz : new Class[]{CellObject.class, Mine.class, Umbrella.class}) { // all subclasses of CellObject
            TextureRegion region = atlasThing.findRegion(clazz.getSimpleName());
            if (region != null)
                things.put(clazz, new TextureRegionDrawable(region));
        }

        for (final Model.Ability ability : Model.Ability.values()) {
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                ImageButton imageButton = new ImageButton(new TextureRegionDrawable(region));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        model.useAbility(ability);
                    }
                });
                abilities.put(ability, imageButton);
            }
        }
    }

    private ImageButton createButtonThing() {
        return new ImageButton(things.get(CellObject.class)) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.useThing();
                }
            });
        }};
    }

    private void buildTable() {
        table.add(gui).colspan(3);
        table.row();
        table.add(btnThing).align(Align.left);
        table.add(abilityButtons);
        table.add(lblScore);
    }

    private void rebuildAbilities() {
        abilityButtons.clear();
        for (Model.Ability ability : model.abilities) {
            abilityButtons.add(abilities.get(ability)).spaceLeft(10); // @mitrakov: adding null is OK
        }
    }
}
