package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

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

    public DialogInvite(Model model, Skin skin, String windowStyleName, Dialog dialup, Stage stage) {
        super("Invite", skin, windowStyleName);
        assert model != null && dialup != null && stage != null;
        this.model = model;
        this.dialup = dialup;
        this.stage = stage;

        lblQuestion = new Label("", skin, "default");
        chkAddToFriends = new CheckBox(" add to friends", skin, "default");

        button("OK", true);
        button("Cancel");
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildContent();
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
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
        assert type != null && table != null;

        table.pad(20).clear();
        switch (type) {
            case ByName:
                lblQuestion.setText(String.format("Do you wanna invite '%s'?", name));
                table.add(lblQuestion);
                table.row().space(30);
                table.add(chkAddToFriends);
                break;
            case Random:
                lblQuestion.setText("Do you wanna invite random opponent?");
                table.add(lblQuestion);
                break;
            case Latest:
                lblQuestion.setText("Do you wanna invite latest opponent?");
                table.add(lblQuestion);
                break;
            default:
        }
    }
}
