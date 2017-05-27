package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
    private final TextButton btnEn;
    private final TextButton btnRu;
    private final TextButton btnEs;
    private final TextButton btnPt;
    private final TextButton btnFr;
    private final TextButton btnNotifyYes;
    private final TextButton btnNotifyNo;
    private final TextButton btnSignOut;
    private final Label lblLang;
    private final Label lblNotify;

    public DialogSettings(RushClient game, final Model model, Skin skin, String styleName, AudioManager audioManager) {
        super("", skin, styleName);
        assert game != null && model != null && audioManager != null;
        this.game = game;
        this.model = model;

        // ....
        ObjectMap<String, CheckBox.CheckBoxStyle> map = skin.getAll(CheckBox.CheckBoxStyle.class);
        String style = map.containsKey("radio") ? "radio" : map.containsKey("default-radio") ? "default-radio"
                : map.keys().next();

        btnEn = new CheckBox("", skin, style);
        btnRu = new CheckBox("", skin, style);
        btnEs = new CheckBox("", skin, style);
        btnPt = new CheckBox("", skin, style);
        btnFr = new CheckBox("", skin, style);
        btnNotifyYes = new CheckBox("", skin, style);
        btnNotifyNo = new CheckBox("", skin, style);
        btnSignOut = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide();
                    model.signOut();
                }
            });
        }};
        lblLang = new Label("", skin, "default");
        lblNotify = new Label("", skin, "default");

        button("Close"); // text will be replaced in onLocaleChanged()
        init(getContentTable(), game);
    }

    @Override
    public Dialog show(Stage stage) {
        // 'setChecked()' must be called here (not in constructor), because parameters might be changed outside
        btnEn.setChecked(model.language == Model.Language.English);
        btnRu.setChecked(model.language == Model.Language.Russian);
        btnEs.setChecked(model.language == Model.Language.Spanish);
        btnPt.setChecked(model.language == Model.Language.Portuguese);
        btnFr.setChecked(model.language == Model.Language.French);
        btnNotifyYes.setChecked(model.notifyNewBattles);
        btnNotifyNo.setChecked(!model.notifyNewBattles);

        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        model.saveSettings();
        game.updateLocale();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        btnEn.setText(bundle.format("dialog.settings.lang.english"));
        btnRu.setText(bundle.format("dialog.settings.lang.russian"));
        btnEs.setText(bundle.format("dialog.settings.lang.spanish"));
        btnPt.setText(bundle.format("dialog.settings.lang.portuguese"));
        btnFr.setText(bundle.format("dialog.settings.lang.french"));
        btnNotifyYes.setText(bundle.format("dialog.settings.notify.yes"));
        btnNotifyNo.setText(bundle.format("dialog.settings.notify.no"));
        btnSignOut.setText(bundle.format("dialog.settings.sign.out"));
        lblLang.setText(bundle.format("dialog.settings.lang.header"));
        lblNotify.setText(bundle.format("dialog.settings.notify.header"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.settings.header"));
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

    private void init(Table table, final RushClient game) {
        assert table != null && game != null;
        table.pad(20);

        // ....
        btnEn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.language = Model.Language.English;
                game.updateLocale();
            }
        });
        btnRu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.language = Model.Language.Russian;
                game.updateLocale();
            }
        });
        btnEs.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.language = Model.Language.Spanish;
                game.updateLocale();
            }
        });
        btnPt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.language = Model.Language.Portuguese;
                game.updateLocale();
            }
        });
        btnFr.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.language = Model.Language.French;
                game.updateLocale();
            }
        });
        new ButtonGroup<Button>(btnEn, btnRu, btnEs, btnPt, btnFr);

        // ....
        btnNotifyYes.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = true;
            }
        });
        btnNotifyNo.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.notifyNewBattles = false;
            }
        });
        new ButtonGroup<Button>(btnNotifyYes, btnNotifyNo);

        Table tableLeft = new Table();
        Table tableRight = new Table();

        // ....
        tableLeft.add(lblLang).spaceTop(30);
        tableLeft.row();
        tableLeft.add(btnEn).left();
        tableLeft.row();
        tableLeft.add(btnRu).left();
        tableLeft.row();
        tableLeft.add(btnEs).left();
        tableLeft.row();
        tableLeft.add(btnPt).left();
        tableLeft.row();
        tableLeft.add(btnFr).left();
        tableLeft.row();

        // ....
        tableRight.add(lblNotify).spaceTop(30);
        tableRight.row();
        tableRight.add(btnNotifyYes).left();
        tableRight.row();
        tableRight.add(btnNotifyNo).left();
        tableRight.row();
        tableRight.add(btnSignOut).spaceTop(20).expandY();

        // ....
        table.add(tableLeft).top().spaceRight(30);
        table.add(tableRight).top().spaceRight(30);
    }
}
