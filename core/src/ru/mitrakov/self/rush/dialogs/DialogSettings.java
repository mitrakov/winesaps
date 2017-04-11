package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogSettings extends DialogFeat {
    private final RushClient game;
    private final Model model;
    private final TextButton btnEng;
    private final TextButton btnRus;
    private final TextButton btnNotifyYes;
    private final TextButton btnNotifyNo;
    private final TextButton btnSignOut;
    private final Label lblLang;
    private final Label lblNotify;
    private final Label lblSignOut;

    public DialogSettings(RushClient game, final Model model, Skin skin, String styleName, AudioManager audioManager,
                          I18NBundle i18n) {
        super(i18n.format("dialog.settings.header"), skin, styleName);
        assert game != null && model != null && audioManager != null;
        this.game = game;
        this.model = model;

        // ....
        ObjectMap<String, CheckBox.CheckBoxStyle> map = skin.getAll(CheckBox.CheckBoxStyle.class);
        String style = map.containsKey("radio") ? "radio" : map.containsKey("default-radio") ? "default-radio"
                : map.keys().next();

        btnEng = new CheckBox(i18n.format("dialog.settings.lang.english"), skin, style);
        btnRus = new CheckBox(i18n.format("dialog.settings.lang.russian"), skin, style);
        btnNotifyYes = new CheckBox(i18n.format("dialog.settings.notify.yes"), skin, style);
        btnNotifyNo = new CheckBox(i18n.format("dialog.settings.notify.no"), skin, style);
        btnSignOut = new TextButtonFeat(i18n.format("dialog.settings.sign.out"), skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    model.signOut();
                }
            });
        }};
        lblLang = new Label(i18n.format("dialog.settings.lang.header"), skin, "default");
        lblNotify = new Label(i18n.format("dialog.settings.notify.header"), skin, "default");
        lblSignOut = new Label(i18n.format("dialog.settings.sign.out"), skin, "default");

        button(i18n.format("close"));
        init(getContentTable(), game);
    }

    @Override
    protected void result(Object object) {
        model.saveSettings();
        game.updateLocale();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        btnEng.setText(bundle.format("dialog.settings.lang.english"));
        btnRus.setText(bundle.format("dialog.settings.lang.russian"));
        btnNotifyYes.setText(bundle.format("dialog.settings.notify.yes"));
        btnNotifyNo.setText(bundle.format("dialog.settings.notify.no"));
        btnSignOut.setText(bundle.format("dialog.settings.sign.out"));
        lblLang.setText(bundle.format("dialog.settings.lang.header"));
        lblNotify.setText(bundle.format("dialog.settings.notify.header"));
        lblSignOut.setText(bundle.format("dialog.settings.sign.out"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.settings.header"));
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

    private void init(Table table, final RushClient game) {
        assert table != null && game != null;
        table.pad(30);

        // ....
        btnEng.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.languageEn = true;
            }
        });
        btnEng.setChecked(model.languageEn);
        btnRus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.languageEn = false;
            }
        });
        btnRus.setChecked(!model.languageEn);
        new ButtonGroup<Button>(btnEng, btnRus);

        // ....
        btnNotifyYes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = true;
            }
        });
        btnNotifyYes.setChecked(model.notifyNewBattles);
        btnNotifyNo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = false;
            }
        });
        btnNotifyNo.setChecked(!model.notifyNewBattles);
        new ButtonGroup<Button>(btnNotifyYes, btnNotifyNo);

        // ....
        table.add(lblLang).spaceTop(30);
        table.row();
        table.add(btnEng).left();
        table.row();
        table.add(btnRus).left();
        table.row();

        table.add(lblNotify).spaceTop(30);
        table.row();
        table.add(btnNotifyYes).left();
        table.row();
        table.add(btnNotifyNo).left();
        table.row();

        table.add(lblSignOut).spaceTop(30);
        table.row();
        table.add(btnSignOut);
    }
}
