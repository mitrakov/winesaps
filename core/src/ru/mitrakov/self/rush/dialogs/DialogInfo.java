package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogInfo extends DialogFeat {
    private final Label lblMessage;

    public DialogInfo(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        lblMessage = new Label("", skin, "default");
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button("OK");
    }

    public Dialog setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }
}
