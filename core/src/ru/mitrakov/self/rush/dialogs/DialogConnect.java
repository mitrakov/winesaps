package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogConnect extends Window {
    private final Model model;

    public DialogConnect(Model model, Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);
        assert model != null;
        this.model = model;

        setModal(true);
        setMovable(false);
        pad(20);
        add(new Label("Connecting...", skin, "default"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (model.connected) {
            remove();
            model.signIn(); // try to sign in using stored credentials
        }
    }

    public Window show(Stage stage) {
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        stage.addActor(this);
        return this;
    }
}
