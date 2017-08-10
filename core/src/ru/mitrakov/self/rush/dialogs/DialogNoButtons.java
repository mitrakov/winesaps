package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogNoButtons extends Window {
    private final Label lblMessage;

    public DialogNoButtons(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center);
        pad(20).add(lblMessage).minWidth(400); // here getContentTable != null
        setMovable(false);
    }

    public DialogNoButtons setText(String text) {
        lblMessage.setText(text);
        return this;
    }

    public void show(Stage stage, boolean centered) {
        assert stage != null;
        pack();
        if (centered)
            setPosition(0, 0, Align.center);
        else setPosition(.5f * stage.getWidth() - .5f * getWidth(), stage.getHeight() - getHeight() - 5);
        stage.addActor(this);
    }
}
