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
    private final Model model;
    private final TextButton btnInvite;
    private final TextButton btnRemove;
    private final DialogQuestion yesNoDialog;
    private final DialogInvite invDialog;

    private String name = "";

    public DialogFriends(Model model, Skin skin, String style, final DialogInvite invDialog,
                         final DialogQuestion yesNoDialog, final Stage stage, AudioManager audioManager) {
        super("", skin, style);
        assert model != null && invDialog != null && yesNoDialog != null && stage != null && audioManager != null;
        this.model = model;
        this.yesNoDialog = yesNoDialog;
        this.invDialog = invDialog;

        btnInvite = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    invDialog.show(stage);
                }
            });
        }};
        btnRemove = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    yesNoDialog.show(stage);
                }
            });
        }};

        Table table = getContentTable();
        assert table != null;
        table.add(btnInvite).width(200);
        table.row();
        table.add(btnRemove).width(200);

        padTop(0);
        button("Close"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        setFriend(name, bundle);

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("close"));
            }
        }
    }

    public Dialog setFriend(final String name, I18NBundle i18n) {
        assert name != null && i18n != null;
        this.name = name;
        btnInvite.setText(i18n.format("dialog.friends.invite", name));
        btnRemove.setText(i18n.format("dialog.friends.remove", name));
        invDialog.setArguments(DialogInvite.InviteType.ByName, name);
        yesNoDialog.setText(i18n.format("dialog.friends.remove.text", name)).setRunnable(new Runnable() {
            @Override
            public void run() {
                model.removeFriend(name);
            }
        });
        return this;
    }
}
