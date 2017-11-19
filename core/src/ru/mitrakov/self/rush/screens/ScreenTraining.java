package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.model.Cells.*;

/**
 * ScreenTraining shows the Training battle screen with UseThing and Skip buttons
 * @author mitrakov
 */
public class ScreenTraining extends LocalizableScreen {

    private final Gui gui;
    private final ImageButton btnThing;
    // private final TextButton btnSkip; @mitrakov (2017-08-16) removed Skip button to make new users pass the tutorial
    private final DialogFinished finishedDialog;
    private final DialogTutorial trainingDialog;
    private final DialogInfo infoDialog;

    private final ObjectMap<Class, Drawable> things = new ObjectMap<Class, Drawable>(2);
    private final Queue<Window> curtains = new Queue<Window>(3);

    public ScreenTraining(final Winesaps game, final Model model, PsObject psObject, AssetManager assetManager,
                          AudioManager manager) {
        super(game, model, psObject, assetManager, manager);

        loadTextures();
        gui = new Gui(model, assetManager); // do NOT share this GUI with ScreenBattle (because it's an Actor)

        Skin skin = assetManager.get("skin/uiskin.json");
        finishedDialog = new DialogFinished(skin, "default", assetManager.<TextureAtlas>get("pack/menu.pack"));
        trainingDialog = new DialogTutorial(skin, "panel-maroon");
        infoDialog = new DialogInfo("", skin, "panel-maroon");
        btnThing = new ImageButtonFeat(things.get(CellObject.class), audioManager, new Runnable() {
            @Override
            public void run() {
                model.useThing();
            }
        });

        initComponents();
    }

    @Override
    public void show() {
        super.show();
        if (model.newbie)
            model.receiveLevel("tutorial.level");
        else game.setNextScreen();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;

        finishedDialog.setText(bundle.format("tutorial.msgX.text"), bundle.format("tutorial.msgX.action"));
        infoDialog.setText(bundle.format("dialog.warning"), bundle.format("dialog.info.server.stop"));

        finishedDialog.onLocaleChanged(bundle);
        infoDialog.onLocaleChanged(bundle);
        addContent(bundle);
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        // @mitrakov (2017-08-05): do NOT put here local vars like "String.format()" or "i18n.format()". It causes
        // excessive work for GC on each event during a battle (because all screens are subscribed to events)

        if (event instanceof EventBus.MoveResponseEvent || event instanceof EventBus.ConnectedChangeEvent) {
            gui.handleEvent(event);
        }
        if (event instanceof EventBus.RoundStartedEvent) {
            EventBus.RoundStartedEvent ev = (EventBus.RoundStartedEvent) event;
            if (ev.number == 0)
                trainingDialog.show(stage);
        }
        if (event instanceof EventBus.RoundFinishedEvent || event instanceof EventBus.BattleNotFoundEvent) {
            if (model.newbie) { // check is necessary because RoundFinishedEvent is raised once again after giveUp()
                trainingDialog.remove();
                finishedDialog.setScore("", "", 1, 0).setOnResultAction(new Runnable() {
                    @Override
                    public void run() {
                        game.setNextScreen();
                    }
                }).show(stage);
            }
        }
        if (event instanceof EventBus.GameFinishedEvent) {
            gui.setMovesAllowed(false); // forbid moving to restrict sending useless messages to the server
        }
        if (event instanceof EventBus.ScoreChangedEvent) {
            EventBus.ScoreChangedEvent ev = (EventBus.ScoreChangedEvent) event;
            if (ev.score1 > 0 && curtains.size > 0)
                curtains.removeFirst().remove();
            trainingDialog.next();
        }
        if (event instanceof EventBus.ThingChangedEvent) {
            EventBus.ThingChangedEvent ev = (EventBus.ThingChangedEvent) event;
            if (ev.mine) {
                // 1) change button image
                Class clazz = ev.newThing != null ? ev.newThing.getClass() : CellObject.class;
                ImageButton.ImageButtonStyle style = btnThing.getStyle();
                if (style != null)
                    style.imageUp = things.get(clazz);
                // 2) show the next dialog tip
                if (ev.oldThing != null || ev.newThing != null)
                    trainingDialog.next();
                // 3) forbid moving to make a user use the umbrella (note#1)
                gui.setMovesAllowed(ev.newThing == null);
            }
        }
        if (event instanceof EventBus.ServerGonnaStopEvent) {
            infoDialog.setOnResultAction(new Runnable() {
                @Override
                public void run() {
                    game.setNextScreen();
                }
            }).show(stage);
        }
    }

    @Override
    public void handleEventBackground(EventBus.Event event) {
    }

    /**
     * Prepares textures
     */
    private void loadTextures() {
        TextureAtlas atlasThing = assetManager.get("pack/thing.pack");
        for (Class clazz : new Class[]{CellObject.class, UmbrellaThing.class}) {
            TextureRegion region = atlasThing.findRegion(clazz.getSimpleName());
            if (region != null)
                things.put(clazz, new TextureRegionDrawable(region));
        }
    }

    /**
     * Initializes components
     */
    private void initComponents() {
        // building table
        table.add(gui);
        table.row();
        table.add(btnThing).align(Align.left).padLeft(2);

        // initialize curtains windows
        Skin skin = assetManager.get("skin/uiskin.json");
        Window window;
        window = new Window("", skin, "panel-black");
        window.setBounds(235, 380, 125, 268);
        curtains.addLast(window);
        window = new Window("", skin, "panel-black");
        window.setBounds(135, 212, 105, 180);
        curtains.addLast(window);
        window = new Window("", skin, "panel-black");
        window.setBounds(35, 212, 105, 180);
        curtains.addLast(window);
        for (Window w : curtains) {
            w.setTouchable(Touchable.disabled); // skip touch events through the window
            stage.addActor(w);
        }
    }

    /**
     * Adds content (messages and pictures) to the tutorial dialog
     * @param b - i18n bundle
     */
    private void addContent(I18NBundle b) {
        // note: if atlas.findRegion() returns null, the image would be empty (no Exceptions expected)
        TextureAtlas atlas = assetManager.get("pack/tutorial.pack");
        trainingDialog.clearMessages()
                .addMessage(atlas.findRegion("msg1"), b.format("tutorial.msg1.text"), b.format("tutorial.msg1.action"))
                .addMessage(atlas.findRegion("msg2"), b.format("tutorial.msg2.text"), b.format("tutorial.msg2.action"))
                .addMessage(atlas.findRegion("msg3"), b.format("tutorial.msg3.text"), b.format("tutorial.msg3.action"))
                .addMessage(atlas.findRegion("msg4"), b.format("tutorial.msg4.text"), b.format("tutorial.msg4.action"))
                .addMessage(atlas.findRegion("msg5"), b.format("tutorial.msg5.text"), b.format("tutorial.msg5.action"))
                .addMessage(atlas.findRegion("msg6"), b.format("tutorial.msg6.text"), b.format("tutorial.msg6.action"));
    }
}

// note#1 (@mitrakov, 2017-03-24): even though bool condition "thing==null" is unreliable, an actor won't die because
// waterfall is fake
