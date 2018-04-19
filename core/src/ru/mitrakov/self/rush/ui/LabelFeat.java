package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class LabelFeat extends Label {
    private final LabelStyle originalStyle;
    private final Pixmap pixmap;

    public LabelFeat(CharSequence text, Skin skin, String styleName, boolean centered) {
        super(text, skin, styleName);
        originalStyle = getStyle();
        pixmap = new Pixmap(512, 512, Pixmap.Format.RGB888); // do NOT use getWidth/getHeight! it'll fail on text change
        if (centered)
            setAlignment(Align.center, Align.center);
    }

    public LabelFeat setBackground(Color color) {
        if (color == null)
            setStyle(originalStyle);
        else {
            pixmap.setColor(color);
            pixmap.fill();
            Label.LabelStyle style = new Label.LabelStyle(originalStyle);
            style.background = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
            setStyle(style);
        }
        return this;
    }
}
