package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogIncoming extends Dialog {

    private enum Result {Accept, Reject, Ignore}

    private final Model model;
    private final Label lblQuestion;
    private final CheckBox chkAddToFriends;

    public DialogIncoming(Model model, Skin skin, String windowStyleName) {
        super("Invitation", skin, windowStyleName);
        assert model != null;
        this.model = model;

        lblQuestion = new Label("", skin, "default");
        chkAddToFriends = new CheckBox(" add to friends", skin, "default"); // not checked by default

        button("Accept", Result.Accept);
        button("Reject", Result.Reject);
        button("Ignore", Result.Ignore);

        Table table = getContentTable();

        table.add(lblQuestion);
        table.row().space(30);
        table.add(chkAddToFriends);
    }

    @Override
    public Dialog show(Stage stage) {
        lblQuestion.setText(String.format("%s wants to attack you! Do you wanna accept a battle?", model.enemy));
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        assert object != null && object instanceof Result;
        switch ((Result) object) {
            case Accept:
                model.accept();
                if (chkAddToFriends.isChecked())
                    model.addFriend(model.enemy);
                hide(null); // null = close immediately (without fadeOut)
                break;
            case Reject:
                model.reject();
                break;
            default:
        }
    }
}
