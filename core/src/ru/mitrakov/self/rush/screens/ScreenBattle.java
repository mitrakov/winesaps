package ru.mitrakov.self.rush.screens;

import java.util.Locale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 01.03.2017
 */

public class ScreenBattle extends ScreenAdapter {
    private final Model model;
    private final PsObject psObject;
    private final AudioManager audioManager;
    private final I18NBundle i18n;
    private final Stage stage = new Stage(new FitViewport(RushClient.WIDTH, RushClient.HEIGHT));
    private final TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));
    private final TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));

    private final Table table = new Table();
    private final Gui gui;
    private final Table abilityButtons = new Table();
    private final Label lblScore;
    private final Label lblTime;
    private final ScrollPane abilityButtonsScroll;
    private final ImageButton btnThing;
    private final DialogFinished finishedDialog;
    private final DialogConnect connectingDialog;

    private final ObjectMap<Class, Drawable> things = new ObjectMap<Class, Drawable>(3);
    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);

    private long roundFinishedTime = 0;
    private long gameFinishedTime = 0;
    private int scores = 0;
    private int lives = 0;
    private CellObject curThing, enemyThing;

    public ScreenBattle(RushClient game, final Model model, PsObject psObject, Skin skin, AudioManager audioManager,
                        I18NBundle i18n) {
        assert game != null && model != null && skin != null && i18n != null; // psObject may be NULL
        this.model = model;
        this.psObject = psObject; // may be NULL
        this.audioManager = audioManager; // may be NULL
        this.i18n = i18n;

        table.setFillParent(true);
        stage.addActor(table);

        loadTextures();
        gui = new Gui(model);
        finishedDialog = new DialogFinished(game, skin, "default", i18n);
        connectingDialog = new DialogConnect(skin, "default", stage, i18n);
        lblScore = new Label("", skin, "default");
        lblTime = new Label("", skin, "default");
        abilityButtonsScroll = new ScrollPane(abilityButtons);

        btnThing = new ImageButtonFeat(things.get(CellObject.class), audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.useThing();
                }
            });
        }};

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
        lblScore.setText(i18n.format("battle.score", model.score1, model.score2));
        lblTime.setText(String.valueOf(t >= 0 ? t : 0));

        // checking
        checkAbilities();
        checkScore();
        checkRoundFinished(); // this must be before 'checkLives()' but after 'checkScore()' (to play sounds correctly)
        checkLives();
        checkThing();
        checkEnemyThing();

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
        audioManager.music(String.format(Locale.getDefault(), "battle%d", MathUtils.random(1, 4)));

        // we should update our timestamps because some model's timestamps may be changed off-stage (e.g. in Training)
        roundFinishedTime = model.roundFinishedTime;
        gameFinishedTime = model.gameFinishedTime;
        reset();
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
                ImageButton imageButton = new ImageButtonFeat(new TextureRegionDrawable(region), audioManager) {{
                    addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            model.useAbility(ability);
                        }
                    });
                }};
                abilities.put(ability, imageButton);
            }
        }
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

    private void reset() {
        scores = model.score1 + model.score2;
        lives = model.myLives + model.enemyLives;
        curThing = model.curThing;
        enemyThing = model.enemyThing;
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
            audioManager.sound("round");
            reset();
            String msg = model.roundWinner ? i18n.format("battle.win.header") : i18n.format("battle.lose.header");
            finishedDialog.setText("", msg).setScore(model.totalScore1, model.totalScore2).show(stage);
            audioManager.music(String.format(Locale.getDefault(), "battle%d", MathUtils.random(1, 4)));
        }
        if (gameFinishedTime != model.gameFinishedTime) { // don't use 'elseif' here because both events are possible
            gameFinishedTime = model.gameFinishedTime;
            audioManager.sound("game");
            reset();
            String header = i18n.format("battle.finish");
            String msg = model.roundWinner ? i18n.format("battle.win.text") : i18n.format("battle.lose.text");
            finishedDialog.setText(header, msg).setScore(model.totalScore1, model.totalScore2).setQuitOnResult(true)
                    .show(stage);
            audioManager.music("theme");
        }
    }

    private void checkScore() {
        if (scores != model.score1 + model.score2) {
            scores = model.score1 + model.score2;
            audioManager.sound("food");
        }
    }

    private void checkLives() {
        if (lives != model.myLives + model.enemyLives) {
            lives = model.myLives + model.enemyLives;
            audioManager.sound("die");
        }
    }

    private void checkThing() {
        if (curThing != model.curThing) {
            if (curThing != null && model.curThing == null)
                audioManager.sound(curThing.getClass().getSimpleName());
            else audioManager.sound("thing");
            curThing = model.curThing;
        }
    }

    private void checkEnemyThing() {
        if (enemyThing != model.enemyThing) {
            if (enemyThing != null && model.enemyThing == null)
                audioManager.sound(enemyThing.getClass().getSimpleName());
            else audioManager.sound("thing");
            enemyThing = model.enemyThing;
        }
    }
}
