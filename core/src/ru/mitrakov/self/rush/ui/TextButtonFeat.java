package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.AudioManager;

/**
 * Gdx text button with extra features added
 * @see com.badlogic.gdx.scenes.scene2d.ui.TextButton
 * @author mitrakov
 */
public class TextButtonFeat extends TextButton {
    /**
     * Creates a new instance of TextButtonFeat
     * @param text text
     * @param skin LibGdx skin
     * @param styleName style name (default is "default")
     * @param audioManager audio manager
     * @param f onClick function (may be NULL)
     */
    public TextButtonFeat(String text, Skin skin, String styleName, final AudioManager audioManager, final Runnable f) {
        super(text, skin, styleName);
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
