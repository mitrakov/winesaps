package ru.mitrakov.self.rush;

import com.badlogic.gdx.scenes.scene2d.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */

class DialogInfo extends Dialog {
    private final Label lblMessage;

    DialogInfo(Skin skin, String windowStyleName) {
        super("Information", skin, windowStyleName);
        lblMessage = new Label("", skin, "default");
        getContentTable().add(lblMessage); // no NULL here

        button("OK");
    }

    Dialog setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }
}
