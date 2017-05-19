package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.Localizable;

/**
 * Created by mitrakov on 27.03.2017
 */

public abstract class DialogFeat extends Dialog implements Localizable {
    public DialogFeat(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        // set up button styles
        getButtonTable().defaults().minWidth(120).height(46).padLeft(10).padRight(10);

        // add 'close-on-esc' feature
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    // programmatically 'push' the last button
                    Array<Actor> children = getButtonTable().getChildren();
                    if (children.size > 0) {
                        assert children.peek() != null;
                        children.peek().fire(new ChangeListener.ChangeEvent());
                    }
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    @Override
    public void hide() {
        hide(null); // null = close immediately (without fadeOut)
    }
}
