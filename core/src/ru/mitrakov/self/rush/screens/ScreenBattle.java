package ru.mitrakov.self.rush.screens;

import java.util.Locale;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.model.Cells.*;

import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * ScreenBattle shows a battle field with UseThing button, abilities, lives icon, score and timer
 * @author mitrakov
 */
public class ScreenBattle extends LocalizableScreen {
    /** Reference to GUI (takes top 90% of the screen) */
    private final Gui gui;
    /** Table for the current actor's abilities (located in the bottom) */
    private final Table abilityButtons = new Table();
    /** Score label (e.g. "10-8") */
    private final Label lblScore;
    /** Remaining time label (in sec.); located in the right bottom corner */
    private final Label lblTime;
    /** "User1 vs. User2" label */
    private final Label lblVersus;
    /** "3-2-1-Go" Label */
    private final Label lblCountdown;
    /** Invisible Scroll pane for {@link #abilityButtons} */
    private final ScrollPane abilityButtonsScroll;
    /** Actor thing button; located in the left bottom corner */
    private final ImageButton btnThing;
    /** Lives image (1 or 2 hearts) */
    private final Image imgLives;
    /** Reference to the Finished Dialog */
    private final DialogFinishedEx finishedDialog;
    /** Reference to the Hints Dialog */
    private final DialogNoButtons hintDialog;

    /** Picture of 1 heart */
    private final Drawable lives1;
    /** Picture of 2 hearts */
    private final Drawable lives2;

    /** Map: [ThingClass -> ThingDrawable], e.g. UmbrellaThing -> UmbrellaThingIcon */
    private final ObjectMap<Class, Drawable> things = new ObjectMap<Class, Drawable>(3);
    /** Map: [Class -> ClassSimpleName], e.g. classOf(java.util.Locale) -> "Locale" */
    private final ObjectMap<Class, String> simpleNames = new ObjectMap<Class, String>(16);
    /** Map: [Ability -> AbilityDrawable], e.g. Ability.Snorkel -> SnorkelIcon */
    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);
    /** Map: [Number -> String], e.g. 55 -> "55" (it's needed, because .toString() uses "new" to create a new string) */
    private final LongMap<String> seconds = new LongMap<String>(255);

    /** "Out of Sync" string */
    private String outOfSyncStr;
    /** Out-of-Sync flag (if TRUE, then connection was lost ) */
    private transient boolean outOfSync = false;

    /**
     * Creates a new instance of ScreenBattle
     * @param game instance of Winesaps (NON-NULL)
     * @param model model (NON-NULL)
     * @param psObject Platform Specific Object (NON-NULL)
     * @param assetManager asset manager (NON-NULL)
     * @param audioManager audio manager (NON-NULL)
     */
    public ScreenBattle(final Winesaps game, final Model model, PsObject psObject, AssetManager assetManager,
                        final AudioManager audioManager) {
        super(game, model, psObject, assetManager, audioManager);

        loadTextures();
        gui = new Gui(model, assetManager); // do NOT share this GUI with ScreenTutorial (because it's an Actor)

        Skin skin = assetManager.get("skin/uiskin.json");
        TextureAtlas atlas = assetManager.get("pack/icons.pack");
        finishedDialog = new DialogFinishedEx(skin, "default", assetManager.<TextureAtlas>get("pack/menu.pack"));
        hintDialog = new DialogNoButtons("", skin, "panel");
        lblScore = new Label("", skin, "white");
        lblTime = new Label("", skin, "white");
        abilityButtonsScroll = new ScrollPane(abilityButtons);
        lives1 = new TextureRegionDrawable(atlas.findRegion("lives1"));
        lives2 = new TextureRegionDrawable(atlas.findRegion("lives2"));
        imgLives = new Image(lives2);

        btnThing = new ImageButtonFeat(things.get(CellObject.class), audioManager, new Runnable() {
            @Override
            public void run() {
                model.useThing();
            }
        });

        stage.addActor(lblVersus = new Label("", skin, "score"));
        stage.addActor(lblCountdown = new Label("", skin, "score"));

        // @mitrakov: BUG in LibGDX! If a skin is not assigned to a ScrollPane then ScrollPane supposes any upper actor
        // as its scrollbar and makes it invisible after fadeOut; all that remains is to forbid fading
        abilityButtonsScroll.setFadeScrollBars(false);

        buildTable();

        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
        outOfSyncStr = i18n.format("battle.out.of.sync");

        // init seconds map (to decrease GC pressure)
        for (int i = 0; i < 256; i++) {
            seconds.put(i, String.valueOf(i));
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // update time
        long t = (TimeUtils.millis() - model.roundStartTime) / 1000;
        String sec = seconds.get(Math.max(model.roundLengthSec - t, 0), "");
        lblTime.setText(outOfSync ? outOfSyncStr : sec);
        draw321(t);
    }

    @Override
    public void show() {
        super.show();
        reset();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;

        outOfSyncStr = bundle.format("battle.out.of.sync");
        finishedDialog.onLocaleChanged(bundle);
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        // @mitrakov (2017-08-05): do NOT put here local vars like "String.format()" or "i18n.format()". It causes
        // excessive work for GC on each event during a battle (because all screens are subscribed to events)

        if (event instanceof EventBus.NewFieldEvent || event instanceof EventBus.ActorResetEvent ||
                event instanceof EventBus.MoveResponseEvent || event instanceof EventBus.ConnectedChangeEvent) {
            gui.handleEvent(event);
        }
        if (event instanceof EventBus.RoundStartedEvent) {
            String d1 = model.getDetractor1();
            String d2 = model.getDetractor2();
            String s1 = d1.length() <= 10 ? d1 : d1.substring(0, 10);
            String s2 = d2.length() <= 10 ? d2 : d2.substring(0, 10);
            lblVersus.setText(model.enemy.length() > 0 ? String.format("%s vs %s", s1, s2) : "");
            lblVersus.pack();
            lblVersus.setPosition(Winesaps.WIDTH / 2, Winesaps.HEIGHT / 2 + 80, Align.center);
            lblCountdown.setPosition(Winesaps.WIDTH / 2, Winesaps.HEIGHT / 2, Align.center);

            EventBus.RoundStartedEvent ev = (EventBus.RoundStartedEvent) event;
            if ("training".equals(ev.levelName)) {
                I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
                hintDialog.setText(i18n.format("training.hint")).show(stage, false);
            }
        }
        if (event instanceof EventBus.RoundFinishedEvent) {
            EventBus.RoundFinishedEvent ev = (EventBus.RoundFinishedEvent) event;
            audioManager.sound("round");
            reset();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            String header = i18n.format("dialog.finished.header.round");
            String msg = i18n.format(ev.winner ? "dialog.finished.win.round" : "dialog.finished.lose.round");
            finishedDialog.setPicture(false, ev.winner).setText(header, msg);
            finishedDialog.setScore(ev.detractor1, ev.detractor2, ev.totalScore1, ev.totalScore2);
            finishedDialog.setReward(0).setOnResultAction(null).show(stage);
        }
        if (event instanceof EventBus.GameFinishedEvent) {
            EventBus.GameFinishedEvent ev = (EventBus.GameFinishedEvent) event;
            gui.setMovesAllowed(false); // forbid moving to restrict sending useless messages to the server
            audioManager.sound("game");
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            String header = i18n.format("dialog.finished.header.battle");
            String msg = i18n.format(ev.winner ? "dialog.finished.win.battle" : "dialog.finished.lose.battle");
            finishedDialog.setPicture(true, ev.winner).setText(header, msg);
            finishedDialog.setScore(ev.detractor1, ev.detractor2, ev.totalScore1, ev.totalScore2);
            finishedDialog.setReward(ev.reward).setOnResultAction(new Runnable() {
                @Override
                public void run() {
                    game.setNextScreen();
                }
            }).show(stage);
            audioManager.music("theme", false);
        }
        if (event instanceof EventBus.BattleNotFoundEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            String header = i18n.format("battle.out.of.sync");
            String msg = i18n.format("battle.out.of.sync.exit");
            finishedDialog.setPicture(true, false).setScore("", "", 0, 0).setText(header, msg);
            finishedDialog.setReward(0).setOnResultAction(new Runnable() {
                @Override
                public void run() {
                    game.setNextScreen();
                }
            }).show(stage);
            audioManager.music("theme", false);
        }
        if (event instanceof EventBus.ScoreChangedEvent) {
            EventBus.ScoreChangedEvent ev = (EventBus.ScoreChangedEvent) event;
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            lblScore.setText(i18n.format("battle.score", ev.score1, ev.score2));
            if (ev.score1 + ev.score2 > 0)
                audioManager.sound("food");
        }
        if (event instanceof EventBus.LivesChangedEvent) {
            EventBus.LivesChangedEvent ev = (EventBus.LivesChangedEvent) event;
            imgLives.setDrawable(ev.myLives == 2 ? lives2 : ev.myLives == 1 ? lives1 : null);
        }
        if (event instanceof EventBus.PlayerWoundedEvent) {
            EventBus.PlayerWoundedEvent ev = (EventBus.PlayerWoundedEvent) event;
            imgLives.setDrawable(ev.myLives == 2 ? lives2 : ev.myLives == 1 ? lives1 : null);
            audioManager.sound("die");
            audioManager.sound(ev.cause.name());
            gui.handleEvent(event);
        }
        if (event instanceof EventBus.EffectAddedEvent) {
            EventBus.EffectAddedEvent ev = (EventBus.EffectAddedEvent) event;
            if (ev.effect == Model.Effect.Afraid)
                audioManager.sound(ev.effect.name());
        }
        if (event instanceof EventBus.ObjectRemovedEvent) {
            EventBus.ObjectRemovedEvent ev = (EventBus.ObjectRemovedEvent) event;
            if (ev.obj instanceof Mine)
                audioManager.sound(Model.HurtCause.Exploded.name());
            else if (ev.obj instanceof Antidote || ev.obj instanceof Beam || ev.obj instanceof Detector
                    || ev.obj instanceof Flashbang || ev.obj instanceof Teleport)
                audioManager.sound(getSimpleName(ev.obj.getClass()));
            gui.handleEvent(event);
        }
        if (event instanceof EventBus.ThingChangedEvent) {
            EventBus.ThingChangedEvent ev = (EventBus.ThingChangedEvent) event;
            // 1) change button image
            if (ev.mine) {
                Class clazz = ev.newThing != null ? ev.newThing.getClass() : CellObject.class;
                ImageButton.ImageButtonStyle style = btnThing.getStyle();
                if (style != null)
                    style.imageUp = things.get(clazz);
            }
            // 2) play the sound
            if (ev.oldThing != null && ev.newThing == null)
                audioManager.sound(getSimpleName(ev.oldThing.getClass()));
            else if (ev.newThing != null)
                audioManager.sound("thing");
        }
        if (event instanceof EventBus.StyleChangedEvent) {
            EventBus.StyleChangedEvent ev = (EventBus.StyleChangedEvent) event;
            audioManager.music(String.format(Locale.getDefault(), "battle%d", ev.stylePack), true);
        }
        if (event instanceof EventBus.ConnectedChangeEvent) {
            EventBus.ConnectedChangeEvent ev = (EventBus.ConnectedChangeEvent) event;
            outOfSync = ev.connected;
            if (outOfSync)
                model.restoreState();
        }
    }

    @Override
    public void handleEventBackground(EventBus.Event event) {
        if (event instanceof EventBus.AbilitiesChangedEvent) {
            EventBus.AbilitiesChangedEvent ev = (EventBus.AbilitiesChangedEvent) event;
            abilityButtons.clear();
            for (Model.Ability ability : ev.items) {
                abilityButtons.add(abilities.get(ability)).spaceLeft(10);
            }
        }
    }

    /**
     * Prepares the textures
     */
    private void loadTextures() {
        Class[] classes = new Class[]{CellObject.class, UmbrellaThing.class, MineThing.class, AntidoteThing.class,
                BeamThing.class, FlashbangThing.class, TeleportThing.class, DetectorThing.class, BoxThing.class};

        TextureAtlas atlasThing = assetManager.get("pack/thing.pack");
        for (Class clazz : classes) {
            TextureRegion region = atlasThing.findRegion(clazz.getSimpleName());
            if (region != null)
                things.put(clazz, new TextureRegionDrawable(region));
        }

        TextureAtlas atlasAbility = assetManager.get("pack/ability.pack");
        for (final Model.Ability ability : abilityValues) {
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                ImageButton btn = new ImageButtonFeat(new TextureRegionDrawable(region), audioManager, new Runnable() {
                    @Override
                    public void run() {
                        model.useAbility(ability);
                    }
                });
                abilities.put(ability, btn);
            }
        }
    }

    /**
     * Builds main content table
     */
    private void buildTable() {
        table.add(gui).colspan(5);
        table.row().spaceRight(50);
        table.add(btnThing).padLeft(3);
        table.add(abilityButtonsScroll).expandX();
        table.add(lblScore);
        table.add(imgLives).width(imgLives.getWidth()).height(imgLives.getHeight());
        table.add(lblTime).padRight(10);
    }

    /**
     * Resets the screen's state
     */
    private void reset() {
        outOfSync = false;
        hintDialog.remove();
    }

    /**
     * Draws starting pause labels(3, 2, 1, go!)
     * @param sec time to start (sec.)
     */
    private void draw321(long sec) {
        boolean pause = sec < 3;
        lblVersus.setVisible(pause);
        lblCountdown.setVisible(pause);
        gui.setMovesAllowed(!pause);
        if (pause)
            lblCountdown.setText(seconds.get(Math.max(3 - sec, 0), ""));
    }

    /**
     * Analog of clazz.getSimpleName(). Designed to decrease GC pressure because usual way produces new String
     * @param clazz class
     * @return simple name of a class
     */
    private String getSimpleName(Class clazz) {
        String simpleName = simpleNames.get(clazz);
        if (simpleName == null) { // 1-st time only
            simpleName = clazz.getSimpleName();
            simpleNames.put(clazz, simpleName);
        }
        return simpleName;
    }
}
