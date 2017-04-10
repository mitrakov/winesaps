package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;

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

    public Dialog setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }
}
