package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.Gui;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 01.03.2017
 */

public class ScreenTraining extends ScreenAdapter {
    private final Model model;
    private final RushClient game;
    private final PsObject psObject;
    private final Stage stage = new Stage(new FitViewport(RushClient.WIDTH, RushClient.HEIGHT));
    private final TextureAtlas atlasTraining = new TextureAtlas(Gdx.files.internal("pack/training.pack"));
    private final TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));

    private final Table table = new Table();
    private final Gui gui;
    private final ImageButton btnThing;
    private final Button btnSkip;
    private final DialogFinished infoDialog;
    private final DialogTraining trainingDialog;

    private final ObjectMap<Class, Drawable> things = new ObjectMap<Class, Drawable>(2);
    private final Queue<Window> curtains = new Queue<Window>(3);

    private int score = 0;
    private CellObject thing = null;
    private boolean started = false;
    private boolean finished = false;

    public ScreenTraining(final RushClient game, final Model model, PsObject psObject, Skin skin) {
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
        btnSkip = new TextButton("Skip Training", skin, "default") {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.newbie = false;
                    model.stopBattle();
                    game.setNextScreen();
                }
            });
        }};

        initComponents(skin);
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
        checkFinished();

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
        stage.dispose();
        atlasTraining.dispose(); // disposing an atlas also disposes all its internal textures
        atlasThing.dispose();
        gui.dispose();
        super.dispose();
    }

    private void loadTextures() {
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

    private void initComponents(Skin skin) {
        // building table
        table.add(gui).colspan(2);
        table.row();
        table.add(btnThing).align(Align.left);
        table.add(btnSkip).align(Align.right).width(200).height(btnThing.getHeight());

        // initialize curtains windows
        Window window;
        window = new Window("", skin, "default");
        window.setBounds(235, 380, 125, 268);
        curtains.addLast(window);
        window = new Window("", skin, "default");
        window.setBounds(135, 212, 105, 180);
        curtains.addLast(window);
        window = new Window("", skin, "default");
        window.setBounds(35, 212, 105, 180);
        curtains.addLast(window);
        for (Window w : curtains) {
            w.setTouchable(Touchable.disabled); // skip touch events through the window
            stage.addActor(w);
        }
    }

    private void addContent() {
        // note: if atlas.findRegion() returns null, the image would be empty (no Exceptions expected)
        trainingDialog
                .addMessage(atlasTraining.findRegion("msg1"), "Tap on the left or right hand of\na character to move.\n" +
                        "On keyboard you can also use arrows or AD keys", "Move right and take an apple")
                .addMessage(atlasTraining.findRegion("msg2"), "You can use doors to move up and down.\nJust tap on the " +
                                "top or bottom of a character.\nOn keyboard you can also use arrows or WS keys",
                        "Go to the door, move down and take a pear")
                .addMessage(atlasTraining.findRegion("msg3"), "You can use ropes to move up",
                        "Go to the rope, crawl up and take an apple")
                .addMessage(atlasTraining.findRegion("msg4"), "You can take some useful stuff\nE.g. an umbrella assists " +
                        "to keep you from\ngetting wet", "Go left and take an umbrella")
                .addMessage(atlasTraining.findRegion("msg5"), "Now push the button on the bottom-left corner\nto use " +
                        "the umbrella\nOn keyboard you can also push a space button", "")
                .addMessage(atlasTraining.findRegion("msg6"), "Good! Take the last pear to finish training", "");
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
            if (curtains.size > 0)
                curtains.removeFirst().remove();
        }
        if (thing != model.curThing) {
            thing = model.curThing;
            trainingDialog.next();
            gui.setMovesAllowed(thing == null); // forbid moving to make a user use the umbrella (see note#1)
        }
    }

    private void checkFinished() {
        if (!finished && model.roundFinishedTime > 0) {
            finished = true;
            model.newbie = false;
            model.stopBattle();
            trainingDialog.remove();
            String msg = "Now invite your friends and tear them to shreds in a real battle!";
            infoDialog.setText("Nice going!", msg).setScore(1, 0).setQuitOnResult(true).show(stage);
        }
    }
}

// note#1 (@mitrakov): even though bool condition "thing==null" is unreliable, actor won't die because waterfall is fake
