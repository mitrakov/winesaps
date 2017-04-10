package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.AudioManager;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogSettings extends DialogFeat {
    private final Model model;

    public DialogSettings(Model model, Skin skin, String windowStyleName, AudioManager audioManager, I18NBundle i18n) {
        super(i18n.format("dialog.settings.header"), skin, windowStyleName);
        assert model != null; // audioManager may be NULL
        this.model = model;

        button(i18n.format("close"));

        init(getContentTable(), skin, audioManager, i18n);
    }

    @Override
    protected void result(Object object) {
        model.saveSettings();
    }

    private void init(Table table, Skin skin, AudioManager audioManager, I18NBundle i18n) {
        assert table != null && skin != null && i18n != null;
        table.pad(30);

        // ....
        ObjectMap<String, CheckBox.CheckBoxStyle> map = skin.getAll(CheckBox.CheckBoxStyle.class);
        String style = map.containsKey("radio") ? "radio" : map.containsKey("default-radio") ? "default-radio"
                : map.keys().next();

        // ....
        Button btnEng = new CheckBox(i18n.format("dialog.settings.lang.english"), skin, style);
        btnEng.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.languageEn = true;
            }
        });
        btnEng.setChecked(model.languageEn);

        Button btnRus = new CheckBox(i18n.format("dialog.settings.lang.russian"), skin, style);
        btnRus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.languageEn = false;
            }
        });
        btnRus.setChecked(!model.languageEn);

        new ButtonGroup<Button>(btnEng, btnRus);

        // ....
        Button btnNotifyYes = new CheckBox(i18n.format("dialog.settings.notify.yes"), skin, style);
        btnNotifyYes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = true;
            }
        });
        btnNotifyYes.setChecked(model.notifyNewBattles);

        Button btnNotifyNo = new CheckBox(i18n.format("dialog.settings.notify.no"), skin, style);
        btnNotifyNo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = false;
            }
        });
        btnNotifyNo.setChecked(!model.notifyNewBattles);

        new ButtonGroup<Button>(btnNotifyYes, btnNotifyNo);

        // ....
        TextButton btnSignOut = new TextButtonFeat(i18n.format("dialog.settings.out"), skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    model.signOut();
                }
            });
        }};

        // ....
        table.add(new Label(i18n.format("dialog.settings.lang.header"), skin, "default")).spaceTop(30);
        table.row();
        table.add(btnEng).left();
        table.row();
        table.add(btnRus).left();
        table.row();
        table.add(new Label(i18n.format("dialog.settings.notify.header"), skin, "default")).spaceTop(30);
        table.row();
        table.add(btnNotifyYes).left();
        table.row();
        table.add(btnNotifyNo).left();
        table.row();
        table.add(new Label(i18n.format("dialog.settings.out"), skin, "default")).spaceTop(30);
        table.row();
        table.add(btnSignOut);
    }
}
