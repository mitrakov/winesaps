package ru.mitrakov.self.rush.screens;

import java.util.Locale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.ui.Gui;
import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.RushClient;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 01.03.2017
 */

public class ScreenBattle extends ScreenAdapter {
    private final Model model;
    private final PsObject psObject;
    private final Stage stage = new Stage(new FitViewport(RushClient.WIDTH, RushClient.HEIGHT));
    private final TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));
    private final TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));

    private final Table table = new Table();
    private final Gui gui;
    private final ImageButton btnThing;
    private final Table abilityButtons = new Table();
    private final Label lblScore;
    private final Label lblTime;
    private final ScrollPane abilityButtonsScroll;
    private final DialogFinished infoDialog;
    private final DialogConnect connectingDialog;

    private final ObjectMap<Class, Drawable> things = new ObjectMap<Class, Drawable>(3);
    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);

    private long roundFinishedTime = 0;
    private long gameFinishedTime = 0;

    public ScreenBattle(RushClient game, Model model, PsObject psObject, Skin skin) {
        assert game != null && model != null && skin != null;
        this.model = model;
        this.psObject = psObject; // may be NULL

        table.setFillParent(true);
        stage.addActor(table);

        loadTextures();
        gui = new Gui(model);
        infoDialog = new DialogFinished(game, skin, "default");
        connectingDialog = new DialogConnect(skin, "default", stage);
        btnThing = createButtonThing();
        lblScore = new Label("", skin, "default");
        lblTime = new Label("", skin, "default");
        abilityButtonsScroll = new ScrollPane(abilityButtons);

        // @mitrakov: BUG in LibGDX! If a skin is not assigned to a ScrollPane then ScrollPane supposes any upper actor
        // as its scrollbar and makes it invisible after fadeOut; all that remains is to forbid fading
        abilityButtonsScroll.setFadeScrollBars(false);

        buildTable();
    }

    @Override
    public void render(float delta) {
        // redraw all
        Gdx.gl.glClearColor(.35f, .87f, .91f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        connectingDialog.setVisible(!model.connected);

        // updating the thing
        Class clazz = model.curThing != null ? model.curThing.getClass() : CellObject.class;
        btnThing.getStyle().imageUp = things.get(clazz); // here getStyle() != NULL

        // updating labels
        long t = model.roundLengthSec - (TimeUtils.millis() - model.roundStartTime) / 1000;
        lblScore.setText(String.format(Locale.getDefault(), "Score: %d-%d", model.score1, model.score2));
        lblTime.setText(String.valueOf(t >= 0 ? t : 0));

        // checking
        checkAbilities();
        checkRoundFinished();

        // checking BACK and MENU buttons on Android
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
        roundFinishedTime = model.roundFinishedTime;
        gameFinishedTime = model.gameFinishedTime;
    }

    @Override
    public void dispose() {
        stage.dispose();
        atlasThing.dispose();   // disposing an atlas also disposes all its internal textures
        atlasAbility.dispose();
        gui.dispose();
        super.dispose();
    }

    private void loadTextures() {
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
        table.add(gui).colspan(5);
        table.row(); // fake row to make the table think there are 5 columns instead of 4;
        table.add(); // without the fake row "abilityButtonsScroll.colspan(2)" would not work properly
        table.add();
        table.add();
        table.add();
        table.add();
        table.row();
        table.add(btnThing).align(Align.left);
        table.add(abilityButtonsScroll).colspan(2);
        table.add(lblScore);
        table.add(lblTime);
    }

    private void checkAbilities() {
        if (abilityButtons.getColumns() != model.abilities.size()) {
            abilityButtons.clear();
            for (Model.Ability ability : model.abilities) {
                abilityButtons.add(abilities.get(ability)).spaceLeft(10); // @mitrakov: adding NULL is safe
            }
        }
    }

    private void checkRoundFinished() {
        if (roundFinishedTime != model.roundFinishedTime) {
            roundFinishedTime = model.roundFinishedTime;
            String msg = String.format("You %s the round", model.roundWinner ? "win" : "lose");
            infoDialog.setText("", msg).setScore(model.totalScore1, model.totalScore2).show(stage);
        }
        if (gameFinishedTime != model.gameFinishedTime) { // don't use 'elseif' here because both events are possible
            gameFinishedTime = model.gameFinishedTime;
            String s = "GAME OVER!";
            String msg = model.roundWinner ? "You win the battle! You get a reward of 1 crystal"
                    : "You lose the battle...";
            infoDialog.setText(s, msg).setScore(model.totalScore1, model.totalScore2).setQuitOnResult(true).show(stage);
        }
    }
}
