package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Gdx label with extra features added
 * @see com.badlogic.gdx.scenes.scene2d.ui.Label
 * @author mitrakov
 */
public class LabelFeat extends Label {
    /** Original style (for restoring original background of the label) */
    private final LabelStyle originalStyle;
    /** Auxiliary PixelMap object */
    private final Pixmap pixmap;

    /**
     * Creates a new instance of LabelFeat
     * @param text label message
     * @param skin LibGdx skin
     * @param styleName style name
     * @param centered whether the label should be centered or not
     */
    public LabelFeat(CharSequence text, Skin skin, String styleName, boolean centered) {
        super(text, skin, styleName);
        originalStyle = getStyle();
        pixmap = new Pixmap(512, 512, Pixmap.Format.RGB888); // do NOT use getWidth/getHeight! it'll fail on text change
        if (centered)
            setAlignment(Align.center, Align.center);
    }

    /**
     * Changes the background colour of the label
     * @param color colour (may be NULL that means to restore the original state)
     * @return this
     * @see <a href="https://stackoverflow.com/questions/18166556">https://stackoverflow.com/questions/18166556</a>
     */
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
