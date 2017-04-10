package ru.mitrakov.self.rush.dialogs;

import static java.lang.Math.*;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogConnect extends Window {
    public DialogConnect(Skin skin, String windowStyleName, Stage stage, I18NBundle i18n) {
        super("", skin, windowStyleName);
        assert stage != null && i18n != null;

        // add widgets
        pad(20);
        add(new Label(i18n.format("dialog.connecting"), skin, "default"));

        // set up
        setModal(true);
        setMovable(false);

        // prepare to show
        pack();
        setPosition(round((stage.getWidth() - getWidth()) / 2), round((stage.getHeight() - getHeight()) / 2));
        stage.addActor(this);
        setVisible(false);
    }
}
