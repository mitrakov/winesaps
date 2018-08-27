package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * LibGdx ScrollPane with extra features added
 * @see com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
 * @author mitrakov
 */
public class ScrollPaneFeat extends ScrollPane {
    /**
     * Creates a new instance of ScrollPaneFeat
     * @param widget internal widget (may be NULL)
     * @param skin LibGdx skin
     * @param styleName style name (usually just "default")
     */
    public ScrollPaneFeat(Actor widget, Skin skin, String styleName) {
        super(widget, skin, styleName);
        setupFadeScrollBars(0, 0);
    }
}
