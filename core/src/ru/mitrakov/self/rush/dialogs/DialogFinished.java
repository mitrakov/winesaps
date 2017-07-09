package ru.mitrakov.self.rush.dialogs;

import java.util.Locale;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogFinished extends DialogFeat {

    private final Table rewardTable = new Table();
    private final Image imgHeader;
    private final Label lblMessage;
    private final Label lblTotalScore;
    private final Label lblScore;
    private final Label lblRewardIs;
    private final Label lblReward;

    private final Drawable roundWin;
    private final Drawable roundLoss;
    private final Drawable battleWin;
    private final Drawable battleLoss;

    public DialogFinished(Skin skin, String windowStyleName, TextureAtlas atlas) {
        super("", skin, windowStyleName);
        assert atlas != null;

        imgHeader = new Image();
        lblMessage = new Label("", skin, "default");
        lblTotalScore = new Label("", skin, "default");
        lblScore = new Label("", skin, "score");
        lblRewardIs = new Label("", skin, "default");
        lblReward = new Label("", skin, "default");

        roundWin = new TextureRegionDrawable(atlas.findRegion("roundWin"));
        roundLoss = new TextureRegionDrawable(atlas.findRegion("roundLoss"));
        battleWin = new TextureRegionDrawable(atlas.findRegion("battleWin"));
        battleLoss = new TextureRegionDrawable(atlas.findRegion("battleLoss"));

        rewardTable.add(lblRewardIs).expand();
        rewardTable.add(lblReward);
        rewardTable.add(new Image(atlas.findRegion("gem")));

        rebuildTable(getContentTable(), false);
        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblTotalScore.setText(bundle.format("dialog.finished.total.score"));
        lblRewardIs.setText(bundle.format("dialog.finished.reward"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    public DialogFinished setPicture(boolean gameOver, boolean winner) {
        imgHeader.setDrawable(gameOver ? (winner ? battleWin : battleLoss) : (winner ? roundWin : roundLoss));
        return this;
    }

    public DialogFinished setText(String header, String msg) {
        if (getTitleLabel() != null)
            getTitleLabel().setText(header);
        lblMessage.setText(msg);
        return this;
    }

    public DialogFinished setScore(int score1, int score2) {
        lblScore.setText(String.format(Locale.getDefault(), "%d-%d", score1, score2));
        return this;
    }

    public DialogFinished setReward(int gems) {
        lblReward.setText(String.valueOf(gems));
        rebuildTable(getContentTable(), gems > 0);
        return this;
    }

    private void rebuildTable(Table table, boolean showReward) {
        assert table != null;

        table.pad(16).clear();
        table.row();
        table.add(imgHeader).spaceTop(8);
        table.row();
        table.add(lblMessage).spaceTop(8);
        table.row();
        table.add(lblTotalScore).spaceTop(8);
        table.row();
        table.add(lblScore);
        if (showReward) {
            table.row();
            table.add(rewardTable).fill();
        }
    }
}
