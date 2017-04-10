package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogDialup extends DialogFeat {
    private final Model model;
    private final Label lblMessage;
    private I18NBundle i18n;

    public DialogDialup(Model model, Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.dialup.header"), skin, windowStyleName);
        assert model != null;
        this.model = model;
        this.i18n = i18n;

        lblMessage = new Label("", skin, "default");
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button(i18n.format("cancel"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        lblMessage.setText(String.format(i18n.format("dialog.dialup.text"), model.enemy)); //i18n!=NULL (assert omitted)
        super.draw(batch, parentAlpha);
    }

    @Override
    protected void result(Object object) {
        model.cancelCall();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.dialup.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor != null && actor instanceof TextButton)
                    ((TextButton) actor).setText(bundle.format("cancel"));
            }
        }
    }
}
