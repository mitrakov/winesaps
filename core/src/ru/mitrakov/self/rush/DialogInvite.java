package ru.mitrakov.self.rush;

import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

class DialogInvite extends Dialog {

    enum InviteType {ByName, Random, Latest}

    private final Model model;
    private final String name;
    private final InviteType type;
    private final CheckBox chkAddToFriends;

    DialogInvite(InviteType type, String name, Model model, Skin skin, String windowStyleName) {
        super("Invite", skin, windowStyleName);
        assert type != null && model != null;
        this.model = model;
        this.name = name;
        this.type = type;

        chkAddToFriends = new CheckBox(" add to friends", skin, "default");
        chkAddToFriends.setChecked(true);

        button("OK", true).setWidth(100);
        button("Cancel").setWidth(100);

        Table table = getContentTable();
        switch (type) {
            case ByName:
                table.add(new Label(String.format("Do you wanna invite '%s'?", name), skin, "default"));
                table.row().space(30);
                table.add(chkAddToFriends);
                break;
            case Random:
                table.add(new Label("Do you wanna invite random opponent?", skin, "default"));
                break;
            case Latest:
                table.add(new Label("Do you wanna invite latest opponent?", skin, "default"));
                break;
            default:
        }
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
        }
    }
}
