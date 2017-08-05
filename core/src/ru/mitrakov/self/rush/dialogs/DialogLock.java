package ru.mitrakov.self.rush.dialogs;

import static java.lang.Math.*;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogLock extends Window {
    private final Label label;

    public DialogLock(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        // add widgets
        pad(20);
        add(label = new Label("", skin, "default")).minWidth(300);
        label.setAlignment(Align.center);

        // set up
        setModal(true);
        setMovable(false);
        pack();
    }

    public DialogLock setText(String text) {
        label.setText(text);
        return this;
    }

    public void show(Stage stage) {
        assert stage != null;
        DialogFeat.hideAll(stage);
        setPosition(round((stage.getWidth() - getWidth()) / 2), round((stage.getHeight() - getHeight()) / 2));
        stage.addActor(this);
    }
}
