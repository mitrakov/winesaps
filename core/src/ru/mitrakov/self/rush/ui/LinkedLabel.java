package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by mitrakov on 13.03.2017
 */

public class LinkedLabel extends Table {

    public LinkedLabel(String txtBefore, String txtLink, String txtAfter, Skin skin, String style, final Runnable f) {
        if (txtBefore != null && !txtBefore.isEmpty())
            add(new Label(txtBefore, skin, style));
        Label lblLink = new Label(txtLink, skin, "default-font", Color.BLUE);
        lblLink.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                f.run();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        add(lblLink);
        if (txtAfter != null && !txtAfter.isEmpty())
            add(new Label(txtAfter, skin, style));
    }
}
