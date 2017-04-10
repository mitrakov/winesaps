package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
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
    private final TextButton btnInvite;
    private final TextButton btnRemove;

    private I18NBundle i18n;
    private String name = "";

    public DialogFriends(final Model model, Skin skin, String style, final DialogInvite invDialog,
                         final DialogQuestion yesNoDialog, final Stage stage, AudioManager audioManager,
                         I18NBundle i18nb) {
        super("", skin, style);
        assert model != null && invDialog != null && yesNoDialog != null && stage != null && i18nb != null;
        i18n = i18nb;

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
                    assert i18n != null;
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
        button(i18n.format("close"));
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        btnInvite.setText(bundle.format("dialog.friends.invite", name));
        btnRemove.setText(bundle.format("dialog.friends.remove", name));

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor != null && actor instanceof TextButton)
                    ((TextButton) actor).setText(bundle.format("close"));
            }
        }
    }

    public Dialog setFriend(String name) {
        assert name != null && i18n != null;
        this.name = name;
        btnInvite.setText(i18n.format("dialog.friends.invite", name));
        btnRemove.setText(i18n.format("dialog.friends.remove", name));
        return this;
    }
}
