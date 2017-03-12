package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogInvite extends Dialog {

    public enum InviteType {ByName, Random, Latest}

    private final Model model;
    private final Label lblQuestion;
    private final CheckBox chkAddToFriends;

    private InviteType type = InviteType.Random;
    private String name = "";

    public DialogInvite(Model model, Skin skin, String windowStyleName) {
        super("Invite", skin, windowStyleName);
        assert model != null;
        this.model = model;

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
            hide(null); // default hiding uses fadeout Action 400 ms long that may be undesirable when screens change
        }
    }

    public Dialog setArguments(InviteType type, String name) {
        assert type != null;
        this.type = type;
        this.name = name;
        chkAddToFriends.setChecked(!model.friends.contains(name));
        return this;
    }

    private void rebuildContent() {
        assert type != null;
        Table table = getContentTable();
        table.clear();
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
