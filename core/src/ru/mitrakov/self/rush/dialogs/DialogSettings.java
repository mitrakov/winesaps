package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogSettings extends DialogFeat {
    private final Winesaps game;
    private final Model model;
    private final TextButton btnNotifyYes;
    private final TextButton btnNotifyNo;
    private final TextButton btnSignOut;
    private final Label lblLang;
    private final Label lblNotify;

    public DialogSettings(Winesaps game, final Model model, Skin skin, String styleName, TextureAtlas atlas,
                          I18NBundle i18n, AudioManager audioManager) {
        super("", skin, styleName);
        assert model != null;
        this.game = game;
        this.model = model;

        // ....
        ObjectMap<String, CheckBox.CheckBoxStyle> map = skin.getAll(CheckBox.CheckBoxStyle.class);
        String style = map.containsKey("radio") ? "radio" : map.containsKey("default-radio") ? "default-radio"
                : map.keys().next();

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
        lblLang.setAlignment(Align.center);
        lblNotify = new Label("", skin, "default");

        button("Close"); // text will be replaced in onLocaleChanged()
        init(getContentTable(), skin, atlas, i18n, audioManager);
    }

    @Override
    public Dialog show(Stage stage) {
        // 'setChecked()' must be called here (not in constructor), because parameters might be changed outside
        btnNotifyYes.setChecked(model.notifyNewBattles);
        btnNotifyNo.setChecked(!model.notifyNewBattles);

        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        model.saveSettings();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

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

    private void init(Table table, Skin skin, TextureAtlas atlas, I18NBundle i18n, final AudioManager audioManager) {
        assert table != null && atlas != null;
        table.pad(20);

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

        final Drawable musicOn = new TextureRegionDrawable(atlas.findRegion("musicOn"));
        final Drawable musicOff = new TextureRegionDrawable(atlas.findRegion("musicOff"));
        final Drawable sfxOn = new TextureRegionDrawable(atlas.findRegion("sfxOn"));
        final Drawable sfxOff = new TextureRegionDrawable(atlas.findRegion("sfxOff"));

        final Button btnMusic = new ImageButtonFeat(musicOn, musicOff, model.music, audioManager);
        btnMusic.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.music = !model.music;
                audioManager.muteMusic(!model.music);
                btnMusic.setChecked(model.music);
            }
        });

        final Button btnSfx = new ImageButtonFeat(sfxOn, sfxOff, model.soundEffects, audioManager);
        btnSfx.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.soundEffects = !model.soundEffects;
                audioManager.muteSound(!model.soundEffects);
                btnSfx.setChecked(model.soundEffects);
            }
        });

        Table tableLeft = new Table();
        Table tableRight = new Table();

        // ....
        buildLangTable(tableLeft, skin, atlas, i18n, audioManager);

        // ....
        tableRight.add(lblNotify).colspan(2).spaceTop(30);
        tableRight.row();
        tableRight.add(btnNotifyYes).colspan(2).left();
        tableRight.row();
        tableRight.add(btnNotifyNo).colspan(2).left();
        tableRight.row();
        tableRight.add(btnMusic).space(20);
        tableRight.add(btnSfx).space(20);
        tableRight.row();
        tableRight.add(btnSignOut).colspan(2).spaceTop(30).expandY();

        // ....
        table.add(tableLeft).top().spaceRight(30);
        table.add(tableRight).top().spaceRight(30);
    }

    // note: copy-paste from DialogLanguage
    private void buildLangTable(Table table, Skin skin, TextureAtlas atlas, I18NBundle i18n, AudioManager manager) {
        assert table != null && atlas != null && i18n != null && manager != null;
        final DialogSettings self = this;

        table.add(lblLang).colspan(2);

        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("en")), manager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = "en";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.english"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "en";
                    game.updateLocale();
                    self.pack();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("ru")), manager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = "ru";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.russian"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "ru";
                    game.updateLocale();
                    self.pack();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("es")), manager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = "es";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.spanish"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "es";
                    game.updateLocale();
                    self.pack();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("pt")), manager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = "pt";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.portuguese"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "pt";
                    game.updateLocale();
                    self.pack();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("fr")), manager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = "fr";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.french"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "fr";
                    game.updateLocale();
                    self.pack();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }}).left();
    }
}
