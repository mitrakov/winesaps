package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;

import ru.mitrakov.self.rush.AudioManager;

/**
 * Created by mitrakov on 27.03.2017
 */
@SuppressWarnings("WeakerAccess")
public class ImageButtonFeat extends ImageButton {
    public ImageButtonFeat(Drawable drawable, AudioManager audioManager) {
        super(drawable);
        addSfx(audioManager);
    }

    public ImageButtonFeat(Drawable drawableOn, Drawable drawableOff, boolean checked, AudioManager audioManager) {
        super(drawableOff, null, drawableOn);
        setChecked(checked);
        addSfx(audioManager); // add listener AFTER setChecked()!
    }

    private void addSfx(final AudioManager audioManager) {
        assert audioManager != null;
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.sound("click");
            }
        });
    }
}
