package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.AudioManager;

/**
 * Created by mitrakov on 27.03.2017
 */
public class TextButtonFeat extends TextButton {
    protected TextButtonFeat(String text, Skin skin, String styleName, final AudioManager audioManager) {
        super(text, skin, styleName);

        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (audioManager != null)
                    audioManager.sound("click");
            }
        });
    }
}
