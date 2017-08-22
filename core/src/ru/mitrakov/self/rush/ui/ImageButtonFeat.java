package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;

import ru.mitrakov.self.rush.AudioManager;

/**
 * Gdx image button with extra features added
 * @see com.badlogic.gdx.scenes.scene2d.ui.ImageButton
 * @author mitrakov
 */
public class ImageButtonFeat extends ImageButton {
    /**
     * Creates a new instance of ImageButtonFeat with a usual drawable
     * @param drawable - drawable
     * @param audioManager - audio manager
     * @param f - onClick function (may be NULL)
     */
    public ImageButtonFeat(Drawable drawable, AudioManager audioManager, Runnable f) {
        super(drawable);
        init(audioManager, f);
    }

    /**
     * Creates a new instance of ImageButtonFeat with 2 toggle drawables: for "on" and "off" state correspondingly
     * @param drawableOn - drawable to represent the On state of a toggle button
     * @param drawableOff - drawable to represent the Off state of a toggle button
     * @param checked - initial state
     * @param audio - audio manager
     * @param f - onClick function
     */
    public ImageButtonFeat(Drawable drawableOn, Drawable drawableOff, boolean checked, AudioManager audio, Runnable f) {
        super(drawableOff, null, drawableOn);
        setChecked(checked);
        init(audio, f); // add listener AFTER setChecked()!
    }

    /**
     * Adds the onClick listener
     * @param audioManager - audio manager
     * @param f - onClick function
     */
    private void init(final AudioManager audioManager, final Runnable f) {
        assert audioManager != null;
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.sound("click");
                if (f != null)
                    f.run();
            }
        });
    }
}
