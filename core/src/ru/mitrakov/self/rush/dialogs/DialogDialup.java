package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogDialup extends DialogFeat {
    private final Model model;
    private final Label lblMessage;

    public DialogDialup(Model model, Skin skin, String windowStyleName) {
        super("Attack", skin, windowStyleName);
        assert model != null;
        this.model = model;

        lblMessage = new Label("Attempting to find enemy ...", skin, "default");
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button("Cancel");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        lblMessage.setText(String.format("Daring %s to a fight ...", model.enemy));
        super.draw(batch, parentAlpha);
    }

    @Override
    protected void result(Object object) {
        model.cancelCall();
    }
}
