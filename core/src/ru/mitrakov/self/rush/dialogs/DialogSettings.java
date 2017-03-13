package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.RushClient;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogSettings extends Dialog {
    private final Model model;

    public DialogSettings(Model model, RushClient game, Skin skin, String windowStyleName) {
        super("Settings", skin, windowStyleName);
        assert model != null && game != null;
        this.model = model;

        button("Close");

        init(getContentTable(), skin);
    }

    private void init(Table table, Skin skin) {
        assert table != null && skin != null;

        TextButton btnNotifyYes = new TextButton("Notify about new battle", skin, "toggle");
        btnNotifyYes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = true;
            }
        });
        btnNotifyYes.setChecked(model.notifyNewBattles);

        TextButton btnNotifyNo = new TextButton("Don't notify about new battle", skin, "toggle");
        btnNotifyYes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = false;
            }
        });
        btnNotifyNo.setChecked(!model.notifyNewBattles);

        TextButton btnSignOut = new TextButton("Sign out", skin, "default");
        btnSignOut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide(null);  // immediately close the dialog
                model.signOut();
            }
        });

        new ButtonGroup<TextButton>(btnNotifyYes, btnNotifyNo);

        table.add(btnNotifyYes).left();
        table.row();
        table.add(btnNotifyNo).left();
        table.row().space(30);
        table.add(btnSignOut);
    }
}
