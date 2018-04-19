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
 * "Choose a language" dialog
 * @author Mitrakov
 */
public class DialogLanguage extends DialogFeat {
    /** "Choose a language" label */
    private final Label lblLang;

    /**
     * Creates new "Choose a language" dialog
     * @param game {@link Winesaps}
     * @param model {@link Model}
     * @param title header title
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param atlas texture atlas containing all the flags needed
     * @param i18n LibGdx internationalization bundle
     * @param audioManager {@link AudioManager}
     */
    public DialogLanguage(Winesaps game, Model model, String title, Skin skin, String windowStyleName,
                          TextureAtlas atlas, I18NBundle i18n, AudioManager audioManager) {
        super(title, skin, windowStyleName);

        lblLang = new LabelFeat("", skin, "default", true);
        buildLangTable(game, model, getContentTable(), skin, atlas, i18n, audioManager);
        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblLang.setText(bundle.format("dialog.settings.lang.text"));
        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.settings.lang.header"));

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    /**
     * Builds table with different languages and country flags
     * @param game {@link Winesaps}
     * @param model {@link Model}
     * @param table content table
     * @param skin LibGdx skin
     * @param atlas texture atlas containing all the flags needed
     * @param i18n LibGdx internationalization bundle
     * @param manager {@link AudioManager}
     */
    private void buildLangTable(final Winesaps game, final Model model, Table table, Skin skin, TextureAtlas atlas,
                            I18NBundle i18n, AudioManager manager) {
        assert game != null && model != null && table != null && atlas != null && i18n != null && manager != null;

        table.pad(16).add(lblLang).minWidth(250).colspan(2);

        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("en")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "en";
                game.updateLocale();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.english"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "en";
                    game.updateLocale();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("ru")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "ru";
                game.updateLocale();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.russian"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "ru";
                    game.updateLocale();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("es")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "es";
                game.updateLocale();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.spanish"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "es";
                    game.updateLocale();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("pt")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "pt";
                game.updateLocale();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.portuguese"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float yn) {
                    model.language = "pt";
                    game.updateLocale();
                }
            });
        }}).left();
        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("fr")), manager, new Runnable() {
            @Override
            public void run() {
                model.language = "fr";
                game.updateLocale();
            }
        })).right();
        table.add(new Label(i18n.format("dialog.settings.lang.french"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    model.language = "fr";
                    game.updateLocale();
                }
            });
        }}).left();
    }
}
