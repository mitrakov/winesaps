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
 * Settings dialog
 * @author Mitrakov
 */
public class DialogSettings extends DialogFeat {
    /** Winesaps instance */
    private final Winesaps game;
    /** Reference to the model */
    private final Model model;

    /** "Notify about new battles" YES button */
    private final TextButton btnNotifyYes;
    /** "Notify about new battles" NO button */
    private final TextButton btnNotifyNo;
    /** "Sign out" button */
    private final TextButton btnSignOut;
    /** "Notify about new battles" label */
    private final Label lblLang;
    /** "Choose a language" label */
    private final Label lblNotify;

    /** Music On/Off image button */
    private /*final*/ Button btnMusic;
    /** Sound effects On/Off image button */
    private /*final*/ Button btnSfx;

    /**
     * Creates a new Settings dialog
     * @param game {@link Winesaps}
     * @param model {@link Model}
     * @param skin LibGdx skin
     * @param styleName style name (usually just "default")
     * @param atlas texture atlas that contains icons needed
     * @param i18n LibGdx internationalization bundle
     * @param audioManager {@link AudioManager}
     */
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
        btnSignOut = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                hide();
                model.signOut();
            }
        });
        lblLang = new LabelFeat("", skin, "default", true);
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

    /**
     * Initializes components for this dialog
     * @param table content table
     * @param skin LibGdx skin
     * @param atlas texture atlas that contains icons needed
     * @param i18n LibGdx internationalization bundle
     * @param audioManager {@link AudioManager}
     */
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

        btnMusic = new ImageButtonFeat(musicOn, musicOff, model.music, audioManager, new Runnable() {
            @Override
            public void run() {
                model.music = !model.music;
                audioManager.muteMusic(!model.music);
                btnMusic.setChecked(model.music);
            }
        });

        btnSfx = new ImageButtonFeat(sfxOn, sfxOff, model.soundEffects, audioManager, new Runnable() {
            @Override
            public void run() {
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

    /**
     * Builds table with different languages and country flags
     * <br><b>Note:</b> this method is copy-paste from {@link DialogLanguage}
     * @param table content table
     * @param skin LibGdx skin
     * @param atlas texture atlas containing all the flags needed
     * @param i18n LibGdx internationalization bundle
     * @param manager {@link AudioManager}
     */
    private void buildLangTable(Table table, Skin skin, TextureAtlas atlas, I18NBundle i18n, AudioManager manager) {
        assert table != null && atlas != null && i18n != null && manager != null;
        final DialogSettings self = this;

        table.add(lblLang).colspan(2);

        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("en")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "en";
                game.updateLocale();
                self.pack();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.english"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "en";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("ru")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "ru";
                game.updateLocale();
                self.pack();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.russian"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "ru";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("es")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "es";
                game.updateLocale();
                self.pack();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.spanish"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "es";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("pt")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "pt";
                game.updateLocale();
                self.pack();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.portuguese"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "pt";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("fr")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "fr";
                game.updateLocale();
                self.pack();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.french"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "fr";
                    game.updateLocale();
                    self.pack();
                }
            });
        }}).left();
    }
}
