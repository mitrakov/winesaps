package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;

/**
 * "Unlock next SinglePlayer Levels Pack" dialog
 * @author Mitrakov
 */
@SuppressWarnings("UnusedReturnValue")
public class DialogUnlockSpPack extends DialogFeat {
    /** Possible cases to close this dialog */
    private enum QuitAction {JustClose, BuyAndClose, GotoGetMoreGemsScreen}

    /** Reference to the model */
    private final Model model;
    /** Sub-Dialog: "How to get more gems" */
    private final Dialog moreGemsDialog;
    /** "Unlocking this Pack costs N gems" label */
    private final Label lblPrice;
    /** "Do you wanna proceed?" label */
    private final Label lblQuestion;
    /** "How to get more gems" label */
    private final LinkedLabel lblMoreGems;
    /** Gem icon */
    private final Image gemIcon;

    /** LibGdx internationalization bundle */
    private I18NBundle i18n;
    /** Current count of user's gems */
    private int userGems;
    /** Current pack number (from 1 to {@link Model#SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT} inclusively) */
    private int packNumber = 2;

    /**
     * Creates a new "Unlock next SinglePlayer Levels Pack" dialog
     * @param title header title
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogUnlockSpPack(Model model, String title, Skin skin, String windowStyleName, final Dialog moreGemsDialog,
                              I18NBundle i18n, TextureRegion gem) {
        super(title, skin, windowStyleName);
        assert model != null && moreGemsDialog != null && i18n != null;
        this.model = model;
        this.i18n = i18n;
        this.moreGemsDialog = moreGemsDialog;

        lblPrice = new Label("", skin, "default");
        lblQuestion = new Label("", skin, "default");
        gemIcon = new Image(gem);

        lblMoreGems = new LinkedLabel("", "", "", skin, "default", "link", new Runnable() {
            @Override
            public void run() {
                moreGemsDialog.show(getStage());
                hide();
            }
        });
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildTables();
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        if (object instanceof QuitAction) { // stackoverflow.com/questions/2950319
            switch ((QuitAction)object) {
                case BuyAndClose:
                    model.buySinglePlayerPack(packNumber);
                    hideAll(getStage()); // close all dialogs (including parent) to get a user to re-open SinglePlayer
                    break;
                case GotoGetMoreGemsScreen:
                    moreGemsDialog.show(getStage());
                    break;
                default:
            }
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        i18n = bundle;
    }

    /**
     * Sets total user's gems count
     * @param gems count of gems
     * @return this
     */
    public DialogUnlockSpPack setUserGems(int gems) {
        this.userGems = gems;
        return this;
    }

    /**
     * Sets the pack number for this dialog
     * @param packNumber pack number (from 1 to {@link Model#SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT} inclusively)
     * @return this
     */
    public DialogUnlockSpPack setPackNumber(int packNumber) {
        this.packNumber = packNumber;
        return this;
    }

    /**
     * Rebuilds the content of the dialog according to user's gems count and pack number
     * <br><b>Note:</b> don't forget to set the arguments with {@link #setUserGems(int)} and {@link #setPackNumber(int)}
     * methods
     */
    private void rebuildTables() {
        Table table = getContentTable();
        assert table != null;

        int price = model.getSinglePlayerPackPrice(packNumber);

        if (getTitleLabel() != null)
            getTitleLabel().setText(i18n.format("dialog.singleplayer.unlock.header"));
        lblPrice.setText(i18n.format("dialog.singleplayer.unlock.text", price));
        lblMoreGems.setText(i18n.format("dialog.gems.start"), i18n.format("dialog.gems.link"), "");

        table.clear();
        table.pad(16);
        table.add(lblPrice).space(5).right();
        table.add(gemIcon).left();
        table.row();
        table.add(lblQuestion).colspan(2);

        getButtonTable().clearChildren(); // don't use "clear()" method
        if (userGems >= price) {
            lblQuestion.setText(i18n.format("proceed"));
            button(i18n.format("yes"), QuitAction.BuyAndClose);
            button(i18n.format("no"), QuitAction.JustClose);
        } else {
            lblQuestion.setText(i18n.format("dialog.info.no.gems"));
            table.row();
            table.add(lblMoreGems).colspan(2).right().spaceTop(50);
            button(i18n.format("back"), QuitAction.GotoGetMoreGemsScreen);
        }
    }
}
