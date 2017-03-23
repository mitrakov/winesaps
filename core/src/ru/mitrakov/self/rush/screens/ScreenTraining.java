package ru.mitrakov.self.rush.screens;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.RushClient;
import ru.mitrakov.self.rush.dialogs.DialogTraining;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.object.*;
import ru.mitrakov.self.rush.dialogs.DialogFinished;

/**
 * Created by mitrakov on 01.03.2017
 */

public class ScreenTraining extends ScreenAdapter {
    private final Model model;
    private final RushClient game;
    private final PsObject psObject;
    private final Stage stage = new Stage(new FitViewport(RushClient.WIDTH, RushClient.HEIGHT));
    private final Table table = new Table();
    private final Actor gui;
    private final ImageButton btnThing;
    private final DialogFinished infoDialog;
    private final DialogTraining trainingDialog;

    private final Map<Class, Drawable> things = new HashMap<Class, Drawable>(3);

    private int score = 0;
    private CellObject thing = null;
    private boolean started = false;
    private long roundFinishedTime = 0;
    private long gameFinishedTime = 0;

    public ScreenTraining(RushClient game, Model model, PsObject psObject, Skin skin) {
        assert game != null && model != null && skin != null;
        this.model = model;
        this.game = game;
        this.psObject = psObject; // may be NULL

        table.setFillParent(true);
        stage.addActor(table);

        loadTextures();
        gui = new Gui(model);
        infoDialog = new DialogFinished(game, skin, "default");
        trainingDialog = new DialogTraining(skin, "default");
        btnThing = createButtonThing();

        buildTable();
        addContent();
    }

    @Override
    public void render(float delta) {
        // redraw all
        Gdx.gl.glClearColor(.35f, .87f, .91f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        // updating the thing
        Class clazz = model.curThing != null ? model.curThing.getClass() : CellObject.class;
        btnThing.getStyle().imageUp = things.get(clazz); // here getStyle() != NULL

        // checking
        checkStarted();
        checkNextMessage();
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
        if (model.newbie) {
            model.receiveTraining();
            Gdx.input.setInputProcessor(stage);
        } else game.setNextScreen();
    }

    @Override
    public void dispose() {
        stage.dispose(); // what about the internal actors?
        for (Drawable drawable : things.values()) {
            assert drawable != null;
            if (drawable instanceof TextureRegionDrawable)
                ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose(); // no NULL references here
        }
    }

    private void loadTextures() {
        TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));

        for (Class clazz : new Class[]{CellObject.class, Umbrella.class}) {
            TextureRegion region = atlasThing.findRegion(clazz.getSimpleName());
            if (region != null)
                things.put(clazz, new TextureRegionDrawable(region));
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
        table.add(gui);
        table.row();
        table.add(btnThing).align(Align.left);
    }

    private void addContent() {
        trainingDialog
                .addMessage(null, "Tap on the left or right hand of\n a character to move\nOn keyboard you can also " +
                        "use arrows or AD keys", "Move right and take an apple")
                .addMessage(null, "You can use doors to move up and down\nJust tap on the top or bottom of a " +
                        "character\nOn keyboard you can also use arrows or WS " +
                        "keys", "Go to the door, move down and take a pear")
                .addMessage(null, "You can use ropes to move up", "Go to the rope, crawl up and take an apple")
                .addMessage(null, "You can take some useful stuff\nE.g. an umbrella assists to keep you from\n " +
                        "getting wet", "Go left and take an umbrella")
                .addMessage(null, "Now push the button on the bottom-left corner\n to use the umbrella\nOn keyboard " +
                        "you can also push a space button", "")
                .addMessage(null, "Good! Take the last pear to finish training", "");
    }

    private void checkStarted() {
        if (!started && model.field != null) {
            started = true;
            trainingDialog.show(stage).next();
        }
    }

    private void checkNextMessage() {
        if (score != model.score1) {
            score = model.score1;
            trainingDialog.next();
        }
        if (thing != model.curThing) {
            thing = model.curThing;
            trainingDialog.next();
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
