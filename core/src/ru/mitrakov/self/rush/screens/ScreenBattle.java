package ru.mitrakov.self.rush.screens;

import java.util.Locale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 01.03.2017
 */
public class ScreenBattle extends LocalizableScreen {
    private final TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("pack/thing.pack"));
    private final TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));
    private final Gui gui;
    private final Table abilityButtons = new Table();
    private final Label lblScore;
    private final Label lblTime;
    private final ScrollPane abilityButtonsScroll;
    private final ImageButton btnThing;
    private final DialogFinished finishedDialog;
    private final DialogInfo infoDialog;

    private final ObjectMap<Class, Drawable> things = new ObjectMap<Class, Drawable>(3);
    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);

    private boolean connected = true;
    private boolean outOfSync = false;
    private I18NBundle i18n;

    public ScreenBattle(RushClient game, final Model model, PsObject psObject, Skin skin, AudioManager audioManager,
                        I18NBundle i18n) {
        super(game, model, psObject, skin, audioManager);
        assert i18n != null;
        this.i18n = i18n;

        // ....
        glClearR = glClearG = glClearB = 0;

        loadTextures();
        gui = new Gui(model);
        finishedDialog = new DialogFinished(game, skin, "default");
        infoDialog = new DialogInfo("", skin, "default");
        lblScore = new Label("", skin, "white");
        lblTime = new Label("", skin, "white");
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
        super.render(delta);

        // updating labels
        long t = model.roundLengthSec - (TimeUtils.millis() - model.roundStartTime) / 1000;
        lblTime.setText(outOfSync ? i18n.format("battle.out.of.sync") : String.valueOf(t >= 0 ? t : 0));

        // checking
        checkConnected();
    }

    @Override
    public void show() {
        super.show();
        gui.setMovesAllowed(true); // enable moves (in case they possibly were disabled before)
        reset();
    }

    @Override
    public void dispose() {
        atlasThing.dispose();   // disposing an atlas also disposes all its internal textures
        atlasAbility.dispose();
        gui.dispose();
        super.dispose();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;
        this.i18n = bundle;

        finishedDialog.onLocaleChanged(bundle);
        infoDialog.onLocaleChanged(bundle);

        infoDialog.setText(bundle.format("battle.out.of.sync.text"));
        if (infoDialog.getTitleLabel() != null)
            infoDialog.getTitleLabel().setText(bundle.format("dialog.warning"));
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        assert i18n != null;
        if (event instanceof EventBus.RoundFinishedEvent) {
            EventBus.RoundFinishedEvent ev = (EventBus.RoundFinishedEvent) event;
            audioManager.sound("round");
            reset();
            String msg = ev.winner ? i18n.format("battle.win.header") : i18n.format("battle.lose.header");
            finishedDialog.setText("", msg).setScore(model.totalScore1, model.totalScore2).setQuitOnResult(false);
            finishedDialog.show(stage);
        }
        if (event instanceof EventBus.GameFinishedEvent) {
            EventBus.GameFinishedEvent ev = (EventBus.GameFinishedEvent) event;
            gui.setMovesAllowed(false); // forbid moving to restrict sending useless messages to the server
            audioManager.sound("game");
            reset();
            String header = i18n.format("battle.finish");
            String msg = ev.winner ? i18n.format("battle.win.text") : i18n.format("battle.lose.text");
            finishedDialog.setText(header, msg).setScore(model.totalScore1, model.totalScore2).setQuitOnResult(true);
            finishedDialog.show(stage);
            audioManager.music("theme");
        }
        if (event instanceof EventBus.ScoreChangedEvent) {
            EventBus.ScoreChangedEvent ev = (EventBus.ScoreChangedEvent) event;
            lblScore.setText(i18n.format("battle.score", ev.score1, ev.score2));
            if (ev.score1 + ev.score2 > 0)
                audioManager.sound("food");
        }
        if (event instanceof EventBus.LivesChangedEvent) {
            EventBus.LivesChangedEvent ev = (EventBus.LivesChangedEvent) event;
            if (!ev.reset)
                audioManager.sound("die");
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
                audioManager.sound(ev.oldThing.getClass().getSimpleName());
            else audioManager.sound("thing");
        }
        if (event instanceof EventBus.AbilitiesChangedEvent) {
            EventBus.AbilitiesChangedEvent ev = (EventBus.AbilitiesChangedEvent) event;
            abilityButtons.clear();
            for (Model.Ability ability : ev.items) {
                abilityButtons.add(abilities.get(ability)).spaceLeft(10);
            }
        }
        if (event instanceof EventBus.BattleNotFoundEvent) {
            gui.setMovesAllowed(false); // forbid moving to restrict sending useless messages to the server
            //audioManager.sound("..."); find appropriate sound!
            reset();
            String header = i18n.format("battle.out.of.sync");
            String msg = i18n.format("battle.out.of.sync.exit");
            finishedDialog.setText(header, msg).setScore(0, 0).setQuitOnResult(true).show(stage);
            audioManager.music("theme");
        }
        if (event instanceof EventBus.StyleChangedEvent) {
            EventBus.StyleChangedEvent ev = (EventBus.StyleChangedEvent) event;
            audioManager.music(String.format(Locale.getDefault(), "battle%d", ev.stylePack));
        }
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
        table.add(btnThing).align(Align.left).padLeft(2);
        table.add(abilityButtonsScroll).colspan(2);
        table.add(lblScore);
        table.add(lblTime);
    }

    private void reset() {
        outOfSync = false;
        infoDialog.hide();
    }

    private void checkConnected() {
        if (connected != model.connected) {
            if (!connected && model.connected) {
                outOfSync = true;
                infoDialog.show(stage);
            } else outOfSync = false;
            connected = model.connected;
        }
    }
}
