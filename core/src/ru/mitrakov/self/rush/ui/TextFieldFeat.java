package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.PsObject;

/**
 * Gdx single line text box with extra features added
 * @see com.badlogic.gdx.scenes.scene2d.ui.TextField
 * @author mitrakov
 */
public class TextFieldFeat extends TextField {

    /**
     * Creates a new instance of TextFieldFeat
     * @param text current text (default is "")
     * @param skin LibGdx skin
     * @param styleName style name (default is "default")
     * @param psObject Platform Specific Object (NON-NULL)
     * @param onEnterActor actor that is programmatically "pushed" when a user presses ENTER key (may be NULL)
     */
    public TextFieldFeat(String text, Skin skin, String styleName, PsObject psObject, final Actor onEnterActor) {
        super(text, skin, styleName);
        assert psObject != null;

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
        if (psObject.getKeyboardVendor().contains("Samsung")) { // for Samsung show native inputBox (see note#9 below)
            setOnscreenKeyboard(new OnscreenKeyboard() {
                @Override
                public void show(boolean visible) {
                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            setText(text);
                        }
                        @Override
                        public void canceled() {}
                    }, "", getText(), "");
                }
            });
        }
    }
}

// note#9 (@mitrakov, 2017-08-08): FUCKING SAMSUNG KEYBOARD! WOE BE TO IT! On modern Samsung devices (Galaxy
// S5, S6, S6 Edge, Note 5, etc.) there is a trouble with keyboard: if the suggestions are turned on, an input text
// doesn't appear in the TextField until a user puts space, enter or any suggestion.
// Flag InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS is ignored by Samsung Keyboard developers...
// See https://github.com/libgdx/libgdx/issues/3896 for more details.
// Also see MY OWN COMMENT (github.com/libgdx/libgdx/issues/3896#issuecomment-321046989) where I described that an
// existing workaround WORKS, but it works until a user changes the input language; after that everything crashes again
