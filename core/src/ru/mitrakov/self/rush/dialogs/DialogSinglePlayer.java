package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.lang.Character;
import java.util.Locale;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;
import static ru.mitrakov.self.rush.model.Model.*;

/**
 * Single Player Dialog
 * @author Mitrakov
 * @since 2.0.0
 */
public class DialogSinglePlayer extends DialogFeat {
    /** Paged Scroll Pane for packs */
    private final PagedScrollPane tablePacksScroll = new PagedScrollPane();
    /** Reference to the model */
    private final Model model;
    /** LibGdx skin */
    private final Skin skin;
    /** Sub-Dialog: "This Pack is locked. To unlock it please do the following..." */
    private final DialogUnlockSpPack unlockSpPackDialog;
    /** Map: [char (a, b, c, ...) -> Icon (locked, gold, silver, bronze, ...)] */
    private final ObjectMap<Character, TextureRegion> icons = new ObjectMap<Character, TextureRegion>(5);

    /** LibGdx internationalization bundle */
    private I18NBundle i18n;
    /** Pack number chosen by a user (from 1 to {@link Model#SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT} inclusively) */
    private int chosenPack;
    /** Level number chosen by a user (from 1 to {@link Model#SINGLE_PLAYER_PACK_SIZE PACK_SIZE} inclusively) */
    private int chosenLevel;

    /**
     * Creates new Single Player dialog
     * @param model {@link Model}
     * @param atlas texture atlas that contains all the icons needed
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param unlockDialog sub-dialog to open up when a chosen by a user pack is locked
     * @param i18n LibGdx internationalization bundle
     */
    public DialogSinglePlayer(Model model, TextureAtlas atlas, Skin skin, String windowStyleName,
                              DialogUnlockSpPack unlockDialog, I18NBundle i18n) {
        super("", skin, windowStyleName);
        assert model != null && atlas != null && skin != null && i18n != null && unlockDialog != null;
        this.model = model;
        this.skin = skin;
        this.i18n = i18n;
        this.unlockSpPackDialog = unlockDialog;

        icons.put('e', atlas.findRegion("spGold"));
        icons.put('f', atlas.findRegion("spSilver"));
        icons.put('g', atlas.findRegion("spBronze"));
        icons.put('h', atlas.findRegion("spDone"));
        icons.put('i', atlas.findRegion("spLocked"));

        for (TextureRegion r : icons.values())
            assert r != null;

        getContentTable().add(tablePacksScroll).height(350).width(350).fill();

        button("Play", true); // text will be replaced in onLocaleChanged()
        button("Back");       // text will be replaced in onLocaleChanged()
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildTables();
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        if (object != null) {
            if (model.isSinglePlayerPackAvailable(chosenPack)) {
                String levelName = String.format(Locale.getDefault(), "pack_%02d/level_%02d", chosenPack, chosenLevel);
                model.setSinglePlayer(true);
                model.setChosenSinglePlayerLevel(chosenPack, chosenLevel);
                model.invite(levelName);
            } else {
                cancel(); // prevent the dialog from closing
                unlockSpPackDialog.setPackNumber(chosenPack).show(getStage());
            }
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        i18n = bundle;

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("opponent.none").replaceAll("\n", " "));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 2) {
                Actor play = buttons.first();
                if (play instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) play).setText(bundle.format("play"));
                Actor back = buttons.get(1);
                if (back instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) back).setText(bundle.format("back"));
            }
        }
    }

    /**
     * Rebuilds the content of the dialog according to current SinglePlayer progress
     */
    private void rebuildTables() {
        String progress = model.singlePlayerProgress;
        assert progress.length() == SINGLE_PLAYER_PACKS_COUNT * SINGLE_PLAYER_PACK_SIZE;

        chosenPack = model.getCurSinglePlayerPack();
        chosenLevel = model.getCurSinglePlayerLevel();

        tablePacksScroll.clear();
        for (int i = 1; i <= SINGLE_PLAYER_PACKS_COUNT; i++) {
            Table tablePack = new Table();
            Table tablePackLevels = new Table();
            final ScrollPane pane = new ScrollPaneFeat(tablePackLevels, skin, "default");
            tablePack.add(new LabelFeat(i18n.format("dialog.singleplayer.pack", i), skin, "title", true)).width(350);
            tablePack.row();
            tablePack.add(pane).height(300);
            for (int j = 1; j <= SINGLE_PLAYER_PACK_SIZE; j++) {
                char progressChar = progress.charAt((i-1)*SINGLE_PLAYER_PACK_SIZE + (j-1));
                tablePackLevels.row();
                TextureRegion region = icons.get(progressChar);
                tablePackLevels.add(region != null ? new Image(region) : null).spaceLeft(10);
                tablePackLevels.add(createLabel(i, j, progressChar, tablePacksScroll)).height(32).spaceLeft(10);
            }
            tablePacksScroll.addPage(tablePack);
            scrollToCurrentLevel(pane);
        }
        scrollToCurrentPack();
    }

    /**
     * Creates "Level XX" label
     * @param pack pack number (from 1 to {@link Model#SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT} inclusively)
     * @param level level number (from 1 to {@link Model#SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT} inclusively)
     * @param progressChar char of the progress (see {@link Model#singlePlayerProgress} for more details)
     * @param parent parent table that contains all the other labels
     * @return new Label for the given pack number and level number
     */
    private Actor createLabel(final int pack, final int level, char progressChar, final WidgetGroup parent) {
        boolean isLocked = progressChar == 'i';
        final LabelFeat label = new LabelFeat(i18n.format("dialog.singleplayer.level", level), skin, "default", true);
        if (!isLocked) {
            label.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    chosenPack = pack;
                    chosenLevel = level;
                    unselectAllLabels(parent);
                    label.setBackground(Color.GOLDENROD);
                }
            });
        }
        if (pack == chosenPack && level == chosenLevel)
            label.setBackground(Color.GOLDENROD);
        return label;
    }

    /**
     * Unselects all the labels inside the given parent recursively
     * @param parent container (e.g. Table)
     */
    private void unselectAllLabels(WidgetGroup parent) {
        for (Actor actor : parent.getChildren()) {
            if (actor instanceof LabelFeat)
                ((LabelFeat) actor).setBackground(null);
            else if (actor instanceof WidgetGroup)
                unselectAllLabels((WidgetGroup) actor);
        }
    }

    /**
     * Scrolls the main ScrollPane to make current pack visible
     */
    private void scrollToCurrentPack() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                tablePacksScroll.setScrollPercentX((1f / (SINGLE_PLAYER_PACKS_COUNT - 1)) * (chosenPack - 1));
            }
        });
    }

    /**
     * Scrolls the given <b>pane</b> to make current level visible
     * @param pane ScrollPane to scroll
     */
    private void scrollToCurrentLevel(final ScrollPane pane) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                pane.setScrollPercentY((1f / (SINGLE_PLAYER_PACK_SIZE - 1)) * (chosenLevel - 1));
            }
        });
    }
}
