package ru.mitrakov.self.rush.dialogs;

import java.util.Locale;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogFinished extends DialogFeat {
    private final Label lblMessage1;
    private final Label lblMessage2;
    private final Label lblTotalScore;
    private final Label lblScore;

    public DialogFinished(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);
        lblMessage1 = new Label("", skin, "default");
        lblMessage2 = new Label("", skin, "default");
        lblTotalScore = new Label("", skin, "default");
        lblScore = new Label("", skin, "default");

        init(getContentTable());
        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblTotalScore.setText(bundle.format("dialog.finished.total.score"));
        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.finished.header"));
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

    private void init(Table table) {
        assert table != null;
        table.pad(30);
        table.row().space(10);
        table.add(lblMessage1);
        table.row().space(10);
        table.add(lblMessage2);
        table.row().space(10);
        table.add(lblTotalScore);
        table.row().space(10);
        table.add(lblScore);
    }
}
