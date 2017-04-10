package ru.mitrakov.self.rush.dialogs;

import java.util.Locale;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.RushClient;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogFinished extends DialogFeat {
    private final RushClient game;
    private final Label lblMessage1;
    private final Label lblMessage2;
    private final Label lblScore;

    private boolean quitOnResult = false;

    public DialogFinished(RushClient game, Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.finished.header"), skin, windowStyleName);
        assert game != null;
        this.game = game;
        lblMessage1 = new Label("", skin, "default");
        lblMessage2 = new Label("", skin, "default");
        lblScore = new Label("", skin, "default");

        init(getContentTable(), skin, i18n);
        button("OK");
    }

    @Override
    protected void result(Object object) {
        if (quitOnResult) {
            game.setNextScreen();
        }
    }

    public DialogFinished setText(String text1, String text2) {
        assert text1 != null && text2 != null;
        lblMessage1.setText(text1);
        lblMessage2.setText(text2);
        return this;
    }

    public DialogFinished setScore(int score1, int score2) {
        lblScore.setText(String.format(Locale.getDefault(), "%d - %d", score1, score2));
        return this;
    }

    public DialogFinished setQuitOnResult(boolean value) {
        quitOnResult = value;
        return this;
    }

    private void init(Table table, Skin skin, I18NBundle i18n) {
        assert table != null && i18n != null;
        table.pad(30);
        table.row().space(10);
        table.add(lblMessage1);
        table.row().space(10);
        table.add(lblMessage2);
        table.row().space(10);
        table.add(new Label(i18n.format("dialog.finished.total.score"), skin, "default"));
        table.row().space(10);
        table.add(lblScore);
    }
}
