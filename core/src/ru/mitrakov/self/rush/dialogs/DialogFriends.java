package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.AudioManager;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogFriends extends DialogFeat {
    private final I18NBundle i18n;
    private final TextButton btnInvite;
    private final TextButton btnRemove;
    private String name = "";

    public DialogFriends(final Model model, Skin skin, String style, final DialogInvite invDialog,
                         final DialogQuestion yesNoDialog, final Stage stage, AudioManager audioManager,
                         final I18NBundle i18n) {
        super("", skin, style);
        assert model != null && invDialog != null && yesNoDialog != null && stage != null && i18n != null;
        this.i18n = i18n;

        btnInvite = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    invDialog.setArguments(DialogInvite.InviteType.ByName, name).show(stage);
                }
            });
        }};
        btnRemove = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    yesNoDialog.setText(i18n.format("dialog.friends.remove.text", name)).setRunnable(new Runnable() {
                        @Override
                        public void run() {
                            model.removeFriend(name);
                        }
                    }).show(stage);
                }
            });
        }};

        Table table = getContentTable();
        assert table != null;
        table.add(btnInvite).width(200);
        table.row();
        table.add(btnRemove).width(200);

        padTop(0);
        button("Close");
    }

    public Dialog setFriend(String name) {
        assert name != null;
        this.name = name;
        btnInvite.setText(i18n.format("dialog.friends.invite", name));
        btnRemove.setText(i18n.format("dialog.friends.remove", name));
        return this;
    }
}
