package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by mitrakov on 13.03.2017
 */
public class LinkedLabel extends Table {

    private final Label lblBefore;
    private final Label lblLink;
    private final Label lblAfter;

    public LinkedLabel(String txtBefore, String txtLink, String txtAfter, Skin skin, String style, final Runnable f) {
        assert txtBefore != null && txtLink != null && txtAfter != null;

        ObjectMap<String, BitmapFont> fonts = skin.getAll(BitmapFont.class);
        String font = fonts.containsKey("default-font") ? "default-font" : fonts.containsKey("font") ? "font"
                : fonts.keys().next();

        lblBefore = new Label(txtBefore, skin, style);
        lblLink = new Label(txtLink, skin, font, Color.CYAN);
        lblAfter = new Label(txtAfter, skin, style);

        add(lblBefore);
        lblLink.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                f.run();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        add(lblLink);
        add(lblAfter);
    }

    public void setText(String txtBefore, String txtLink, String txtAfter) {
        assert txtBefore != null && txtLink != null && txtAfter != null;
        lblBefore.setText(txtBefore);
        lblLink.setText(txtLink);
        lblAfter.setText(txtAfter);
    }
}
