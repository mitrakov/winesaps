package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;

import ru.mitrakov.self.rush.AudioManager;

/**
 * Created by mitrakov on 27.03.2017
 */
public class ImageButtonFeat extends ImageButton {
    public ImageButtonFeat(Drawable drawable, final AudioManager audioManager) {
        super(drawable);
        assert audioManager != null;

        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.sound("click");
            }
        });
    }
}
