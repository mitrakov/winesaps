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
public class DialogLanguage extends DialogFeat {
    private final Label lblLang;

    public DialogLanguage(Winesaps game, Model model, String title, Skin skin, String windowStyleName,
                          TextureAtlas atlas, I18NBundle i18n, AudioManager audioManager) {
        super(title, skin, windowStyleName);

        lblLang = new Label("", skin, "default");
        lblLang.setAlignment(Align.center);

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

    private void buildLangTable(final Winesaps game, final Model model, Table table, Skin skin, TextureAtlas atlas,
                            I18NBundle i18n, AudioManager manager) {
        assert game != null && model != null && table != null && atlas != null && i18n != null && manager != null;

        table.pad(16).add(lblLang).minWidth(250).colspan(2);

        table.row().space(20);
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("en")), manager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = "en";
                    game.updateLocale();
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.english"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "en";
                    game.updateLocale();
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
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.russian"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "ru";
                    game.updateLocale();
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
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.spanish"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "es";
                    game.updateLocale();
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
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.portuguese"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "pt";
                    game.updateLocale();
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
                }
            });
        }}).right();
        table.add(new Label(i18n.format("dialog.settings.lang.french"), skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    model.language = "fr";
                    game.updateLocale();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }}).left();
    }
}
