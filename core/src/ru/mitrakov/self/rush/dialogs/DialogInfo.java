package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogInfo extends Dialog {
    private final Label lblMessage;

    public DialogInfo(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);
        lblMessage = new Label("", skin, "default");
        getContentTable().add(lblMessage); // no NULL here

        button("OK");
    }

    public Dialog setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }
}
