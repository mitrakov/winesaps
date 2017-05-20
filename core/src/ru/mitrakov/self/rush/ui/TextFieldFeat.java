package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by mitrakov on 27.03.2017
 */
public class TextFieldFeat extends TextField {
    private Actor onEnterActor;
    public TextFieldFeat(String text, Skin skin, String styleName, Actor onEnterPressedActor) {
        super(text, skin, styleName);
        onEnterActor = onEnterPressedActor;

        // add 'enter-pressed' feature
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (onEnterActor != null && keycode == Input.Keys.ENTER) {
                    onEnterActor.fire(new ChangeListener.ChangeEvent()); // programmatically push the button
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    // GENERATED CODE

    @SuppressWarnings("unused")
    public Actor getOnEnterActor() {
        return onEnterActor;
    }

    public void setOnEnterActor(Actor onEnterActor) {
        this.onEnterActor = onEnterActor;
    }
}
