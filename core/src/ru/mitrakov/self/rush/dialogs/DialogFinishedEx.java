package ru.mitrakov.self.rush.dialogs;

import java.util.Locale;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * "Round/Battle Finished" dialog
 * @author Mitrakov
 */
public class DialogFinishedEx extends DialogFinished {
    /** Table that contains reward labels + gem picture ("Reward is: 5 {gemImage}") */
    private final Table rewardTable = new Table();
    /** Header image (happy if a user won, and sad if lost) */
    private final Image imgHeader;
    /** Enemies label (e.g. "Tommy - Bobby") */
    private final Label lblDetractors;
    /** Total score label */
    private final Label lblScore;
    /** "Reward" label */
    private final Label lblRewardIs;
    /** Reward value label, e.g. "5" */
    private final Label lblReward;

    /** "Round Win" image */
    private final Drawable roundWin;
    /** "Round Loss" image */
    private final Drawable roundLoss;
    /** "Battle Win" image */
    private final Drawable battleWin;
    /** "Battle Loss" image */
    private final Drawable battleLoss;

    /**
     * Creates new "Round/Battle Finished" dialog
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param atlas Texture atlas that contains win/loss pictures needed
     */
    public DialogFinishedEx(Skin skin, String windowStyleName, TextureAtlas atlas) {
        super(skin, windowStyleName, atlas);

        imgHeader = new Image();
        lblDetractors = new Label("", skin, "score");
        lblScore = new Label("", skin, "score");
        lblRewardIs = new Label("", skin, "default");
        lblReward = new Label("", skin, "default");

        roundWin = new TextureRegionDrawable(atlas.findRegion("roundWin"));
        roundLoss = new TextureRegionDrawable(atlas.findRegion("roundLoss"));
        battleWin = new TextureRegionDrawable(atlas.findRegion("battleWin"));
        battleLoss = new TextureRegionDrawable(atlas.findRegion("battleLoss"));

        rewardTable.add(lblRewardIs).expand().right().spaceRight(20);
        rewardTable.add(lblReward);
        rewardTable.add(new Image(atlas.findRegion("gem")));

        rebuildTable(getContentTable(), false);
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        super.onLocaleChanged(bundle);
        lblRewardIs.setText(bundle.format("dialog.finished.reward"));
    }

    /**
     * Sets the theme picture to the dialog
     * @param gameOver TRUE, if battle finished, and FALSE, if just round finished
     * @param winner win/loss flag
     * @return this
     */
    public DialogFinishedEx setPicture(boolean gameOver, boolean winner) {
        imgHeader.setDrawable(gameOver ? (winner ? battleWin : battleLoss) : (winner ? roundWin : roundLoss));
        return this;
    }

    /**
     * Sets total score and enemies' names to the dialog
     * @param detractor1 user1 name (note: it may be either current user or his enemy, depending on aggressor)
     * @param detractor2 user2 name
     * @param score1 total score of a user1
     * @param score2 total score of a user2
     * @return this
     */
    public DialogFinishedEx setScore(String detractor1, String detractor2, int score1, int score2) {
        boolean empty = detractor1.length() == 0 || detractor2.length() == 0;
        String txt1 = detractor1.length() <= 10 ? detractor1 : detractor1.substring(0, 10);
        String txt2 = detractor2.length() <= 10 ? detractor2 : detractor2.substring(0, 10);
        lblDetractors.setText(empty ? "" : String.format("%s-%s", txt1, txt2));
        lblScore.setText(String.format(Locale.getDefault(), "%d-%d", score1, score2));
        return this;
    }

    /**
     * Sets reward for a user (if he's won the battle)
     * @param gems reward (put 0 not to show reward)
     * @return this
     */
    public DialogFinishedEx setReward(int gems) {
        lblReward.setText(String.valueOf(gems));
        rebuildTable(getContentTable(), gems > 0);
        return this;
    }

    /**
     * Rebuilds the current dialog
     * @param table content table
     * @param showReward TRUE to show reward controls, and FALSE - to hide them
     */
    private void rebuildTable(Table table, boolean showReward) {
        assert table != null;

        table.pad(10).clear();
        table.row();
        table.add(imgHeader);
        table.row();
        table.add(lblMessage);
        table.row();
        table.add(lblDetractors);
        table.row();
        table.add(lblScore);
        if (showReward) {
            table.row();
            table.add(rewardTable).fill();
        }
    }
}
