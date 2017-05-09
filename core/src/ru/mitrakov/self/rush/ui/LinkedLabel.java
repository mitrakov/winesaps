package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by mitrakov on 13.03.2017
 */
public class LinkedLabel extends Table {
    private final Label lblBefore;
    private final Label lblLink;
    private final Label lblAfter;

    public LinkedLabel(String txtBefore, String txtLink, String txtAfter, Skin skin, String style, String styleLink,
                       final Runnable f) {
        assert txtBefore != null && txtLink != null && txtAfter != null && style != null && styleLink != null;

        add(lblBefore = new Label(txtBefore, skin, style)).bottom();
        add(lblLink = new Label(txtLink, skin, styleLink) {{
            addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    f.run();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }});
        add(lblAfter = new Label(txtAfter, skin, style)).bottom();
    }

    public void setText(String txtBefore, String txtLink, String txtAfter) {
        assert txtBefore != null && txtLink != null && txtAfter != null;
        lblBefore.setText(txtBefore);
        lblLink.setText(txtLink);
        lblAfter.setText(txtAfter);
    }
}
