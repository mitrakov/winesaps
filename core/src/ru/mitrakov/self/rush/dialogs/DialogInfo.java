package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogInfo extends DialogFeat {
    private final Label lblMessage;

    public DialogInfo(String title, Skin skin, String windowStyleName, I18NBundle i18n) {
        super(title, skin, windowStyleName);
        assert i18n != null;

        lblMessage = new Label("", skin, "default");
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button(i18n.format("ok"));
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor != null && actor instanceof TextButton)
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    public Dialog setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }
}
