package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ScrollPaneFeat extends ScrollPane {
    public ScrollPaneFeat(Actor widget, Skin skin, String styleName) {
        super(widget, skin, styleName);
        setupFadeScrollBars(0, 0);
    }
}
