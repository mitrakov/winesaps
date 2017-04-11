package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogInvite extends DialogFeat {

    public enum InviteType {ByName, Random, Latest}

    private final Model model;
    private final Dialog dialup;
    private final Stage stage;
    private final Label lblQuestion;
    private final CheckBox chkAddToFriends;

    private InviteType type = InviteType.Random;
    private String name = "";
    private I18NBundle i18n;

    public DialogInvite(Model model, Skin skin, String windowStyleName, Dialog dialup, Stage stage, I18NBundle i18n) {
        super("", skin, windowStyleName);
        assert model != null && dialup != null && stage != null && i18n != null;
        this.model = model;
        this.dialup = dialup;
        this.stage = stage;
        this.i18n = i18n;

        lblQuestion = new Label("", skin, "default");
        chkAddToFriends = new CheckBox("", skin, "default"); // not checked by default

        button("OK", true); // text will be replaced in onLocaleChanged()
        button("Cancel");   // text will be replaced in onLocaleChanged()
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildContent();
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        assert type != null;
        if (object != null) {                // "OK" is pressed
            switch (type) {
                case ByName:
                    model.invite(name);
                    if (chkAddToFriends.isChecked())
                        model.addFriend(name);
                    break;
                case Random:
                    model.inviteRandom();
                    break;
                case Latest:
                    model.inviteLatest();
                    break;
                default:
            }
            dialup.show(stage);
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        chkAddToFriends.setText(bundle.format("dialog.friends.add"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.invite.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 2) {
                Actor ok = buttons.first();
                if (ok != null && ok instanceof TextButton)
                    ((TextButton) ok).setText(bundle.format("ok"));
                Actor cancel = buttons.get(1);
                if (cancel != null && cancel instanceof TextButton)
                    ((TextButton) cancel).setText(bundle.format("cancel"));
            }
        }
    }

    public Dialog setArguments(InviteType type, String name) {
        assert type != null;
        this.type = type;
        this.name = name;
        chkAddToFriends.setChecked(!model.friends.contains(name));
        chkAddToFriends.setVisible(!model.friends.contains(name));
        return this;
    }

    private void rebuildContent() {
        Table table = getContentTable();
        assert type != null && table != null && i18n != null;

        table.pad(20).clear();
        switch (type) {
            case ByName:
                lblQuestion.setText(i18n.format("dialog.invite.name", name));
                table.add(lblQuestion);
                table.row().space(30);
                table.add(chkAddToFriends);
                break;
            case Random:
                lblQuestion.setText(i18n.format("dialog.invite.random"));
                table.add(lblQuestion);
                break;
            case Latest:
                lblQuestion.setText(i18n.format("dialog.invite.latest"));
                table.add(lblQuestion);
                break;
            default:
        }
    }
}
