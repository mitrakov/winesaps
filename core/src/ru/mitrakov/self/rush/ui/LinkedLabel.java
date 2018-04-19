package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Label with a link (analog of "a" tag in HTML)
 * In fact consists of 3 single labels
 * @author mitrakov
 */
public class LinkedLabel extends Table {
    private final Label lblBefore;
    private final Label lblLink;
    private final Label lblAfter;

    /**
     * Creates a new instance of LinkedLabel
     * @param txtBefore - text before the link
     * @param txtLink - text for the link
     * @param txtAfter - text after the link
     * @param skin - skin
     * @param style - style name for usual text
     * @param styleLink - style name for linked text
     * @param f - onClick function (called when a user click a link)
     */
    public LinkedLabel(String txtBefore, String txtLink, String txtAfter, Skin skin, String style, String styleLink,
                       final Runnable f) {
        assert style != null && styleLink != null;

        add(lblBefore = new Label(txtBefore, skin, style)).bottom();
        add(lblLink = new Label(txtLink, skin, styleLink) {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    f.run();
                }
            });
        }}).spaceRight(2);
        add(lblAfter = new Label(txtAfter, skin, style)).bottom();
    }

    /**
     * Sets the texts
     * @param txtBefore - text before the link
     * @param txtLink - text for the link
     * @param txtAfter - text after the link
     */
    public void setText(String txtBefore, String txtLink, String txtAfter) {
        lblBefore.setText(txtBefore);
        lblLink.setText(txtLink);
        lblAfter.setText(txtAfter);
    }
}
