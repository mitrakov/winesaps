package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogSettings extends Dialog {
    private final Model model;

    public DialogSettings(Model model, Skin skin, String windowStyleName) {
        super("Settings", skin, windowStyleName);
        assert model != null;
        this.model = model;

        button("Close");

        init(getContentTable(), skin);
    }

    @Override
    protected void result(Object object) {
        model.saveSettings();
    }

    private void init(Table table, Skin skin) {
        assert table != null && skin != null;
        table.pad(30);

        // ....
        ObjectMap<String, CheckBox.CheckBoxStyle> map = skin.getAll(CheckBox.CheckBoxStyle.class);
        String style = map.containsKey("radio") ? "radio" : map.containsKey("default-radio") ? "default-radio"
                : map.keys().next();

        // ....
        Button btnEng = new CheckBox(" English", skin, style);
        btnEng.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.languageEn = true;
            }
        });
        btnEng.setChecked(model.languageEn);

        Button btnRus = new CheckBox(" Russian", skin, style);
        btnRus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.languageEn = false;
            }
        });
        btnRus.setChecked(!model.languageEn);

        new ButtonGroup<Button>(btnEng, btnRus);

        // ....
        Button btnNotifyYes = new CheckBox(" Notify about new battle", skin, style);
        btnNotifyYes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = true;
            }
        });
        btnNotifyYes.setChecked(model.notifyNewBattles);

        Button btnNotifyNo = new CheckBox(" Don't notify about new battle", skin, style);
        btnNotifyNo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = false;
            }
        });
        btnNotifyNo.setChecked(!model.notifyNewBattles);

        new ButtonGroup<Button>(btnNotifyYes, btnNotifyNo);

        // ....
        TextButton btnSignOut = new TextButton("Sign out", skin, "default");
        btnSignOut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide(null);  // immediately close the dialog
                model.signOut();
            }
        });


        // ....
        table.add(new Label("Language", skin, "default")).spaceTop(30);
        table.row();
        table.add(btnEng).left();
        table.row();
        table.add(btnRus).left();
        table.row();
        table.add(new Label("Notifications", skin, "default")).spaceTop(30);
        table.row();
        table.add(btnNotifyYes).left();
        table.row();
        table.add(btnNotifyNo).left();
        table.row();
        table.add(new Label("Sign out", skin, "default")).spaceTop(30);
        table.row();
        table.add(btnSignOut);
    }
}
