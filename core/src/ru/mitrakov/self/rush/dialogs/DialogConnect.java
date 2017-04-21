package ru.mitrakov.self.rush.dialogs;

import static java.lang.Math.*;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.Localizable;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogConnect extends Window implements Localizable {

    private final Label label;

    public DialogConnect(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        // add widgets
        pad(20);
        add(label = new Label("", skin, "default")).width(200);
        label.setAlignment(Align.center);

        // set up
        setModal(true);
        setMovable(false);
        pack();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        label.setText(bundle.format("dialog.connecting"));
    }

    public void show(Stage stage) {
        assert stage != null;
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Window)
                actor.remove(); // Attention! Is it safe for iterator?
        }
        setPosition(round((stage.getWidth() - getWidth()) / 2), round((stage.getHeight() - getHeight()) / 2));
        stage.addActor(this);
    }
}
