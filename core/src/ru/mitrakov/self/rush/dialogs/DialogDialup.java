package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogDialup extends DialogFeat {
    private final Model model;
    private final I18NBundle i18n;
    private final Label lblMessage;

    public DialogDialup(Model model, Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.dialup.header"), skin, windowStyleName);
        assert model != null;
        this.model = model;
        this.i18n = i18n;

        lblMessage = new Label("", skin, "default");
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button("Cancel");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        lblMessage.setText(String.format(i18n.format("dialog.dialup.text"), model.enemy));
        super.draw(batch, parentAlpha);
    }

    @Override
    protected void result(Object object) {
        model.cancelCall();
    }
}
