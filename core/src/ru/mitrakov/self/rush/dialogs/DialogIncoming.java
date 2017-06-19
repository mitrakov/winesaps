package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.AudioManager;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogIncoming extends DialogFeat {

    private enum Result {Accept, Reject, Ignore}

    private final Model model;
    private final Label lblQuestion;
    private final CheckBox chkAddToFriends;
    private final AudioManager audioManager;

    private String enemy = "";
    private int enemySid = 0;
    private I18NBundle i18n;

    public DialogIncoming(Model model, Skin skin, String windowStyleName, AudioManager audioManager, I18NBundle i18n) {
        super("", skin, windowStyleName);
        assert model != null && audioManager != null && i18n != null;
        this.model = model;
        this.audioManager = audioManager;
        this.i18n = i18n;

        lblQuestion = new Label("", skin, "default");
        lblQuestion.setAlignment(Align.center, Align.center);
        chkAddToFriends = new CheckBox("", skin, "default"); // not checked by default

        button("Accept", Result.Accept); // text will be replaced in onLocaleChanged()
        button("Reject", Result.Reject); // text will be replaced in onLocaleChanged()
        button("Ignore", Result.Ignore); // text will be replaced in onLocaleChanged()

        Table table = getContentTable();

        table.pad(10).add(lblQuestion).width(300);
        table.row().space(20);
        table.add(chkAddToFriends);
    }

    @Override
    public Dialog show(Stage stage) {
        audioManager.sound("call");
        chkAddToFriends.setVisible(!model.friendExists(enemy));
        lblQuestion.setText(i18n.format("dialog.incoming.text", enemy)); // i18n != NULL (assert omitted)
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        assert object instanceof Result; // stackoverflow.com/questions/2950319
        switch ((Result) object) {
            case Accept:
                model.accept(enemySid);
                if (chkAddToFriends.isChecked())
                    model.addFriend(enemy);
                break;
            case Reject:
                model.reject(enemySid);
                break;
            default:
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        chkAddToFriends.setText(bundle.format("dialog.friends.add"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.incoming.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 3) {
                Actor accept = buttons.first();
                if (accept instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) accept).setText(bundle.format("dialog.incoming.accept"));
                Actor reject = buttons.get(1);
                if (reject instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) reject).setText(bundle.format("dialog.incoming.reject"));
                Actor ignore = buttons.get(2);
                if (ignore instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) ignore).setText(bundle.format("dialog.incoming.ignore"));
            }
        }
    }

    public Dialog setArguments(String enemy, int enemySid) {
        this.enemy = enemy;
        this.enemySid = enemySid;
        return this;
    }
}
