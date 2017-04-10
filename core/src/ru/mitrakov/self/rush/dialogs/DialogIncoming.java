package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.AudioManager;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogIncoming extends DialogFeat {

    private enum Result {Accept, Reject, Ignore}

    private final Model model;
    private final I18NBundle i18n;
    private final Label lblQuestion;
    private final CheckBox chkAddToFriends;
    private final AudioManager audioManager;

    public DialogIncoming(Model model, Skin skin, String windowStyleName, AudioManager audioManager, I18NBundle i18n) {
        super(i18n.format("dialog.incoming.header"), skin, windowStyleName);
        assert model != null && audioManager != null;
        this.model = model;
        this.audioManager = audioManager;
        this.i18n = i18n;

        lblQuestion = new Label("", skin, "default");
        chkAddToFriends = new CheckBox(i18n.format("dialog.friends.add"), skin, "default"); // not checked by default

        button(i18n.format("dialog.incoming.accept"), Result.Accept);
        button(i18n.format("dialog.incoming.reject"), Result.Reject);
        button(i18n.format("dialog.incoming.ignore"), Result.Ignore);

        Table table = getContentTable();

        table.add(lblQuestion);
        table.row().space(30);
        table.add(chkAddToFriends);
    }

    @Override
    public Dialog show(Stage stage) {
        audioManager.music("call");
        chkAddToFriends.setVisible(!model.friends.contains(model.enemy));
        lblQuestion.setText(i18n.format("dialog.incoming.text", model.enemy));
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
                break;
            case Reject:
                model.reject();
                break;
            default:
        }
    }

    @Override
    public void hide() {
        audioManager.music("theme");
        super.hide();
    }
}
