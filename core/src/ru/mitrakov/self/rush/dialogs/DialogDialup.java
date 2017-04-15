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
        super("", skin, windowStyleName);
        assert model != null && i18n != null;
        this.model = model;
        this.i18n = i18n;

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center);
        getContentTable().pad(20).add(lblMessage).width(250); // here getContentTable != null

        button("Cancel"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        lblMessage.setText(i18n.format("dialog.dialup.text", model.enemy)); // here i18n != NULL (assert omitted)
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
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("cancel"));
            }
        }
    }
}
