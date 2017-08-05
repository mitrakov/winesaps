package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
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

    public DialogDialup(Model model, Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);
        assert model != null;
        this.model = model;

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center);
        getContentTable().pad(20).add(lblMessage).minWidth(350); // here getContentTable != null

        button("Cancel"); // text will be replaced in onLocaleChanged()
    }

    @Override
    protected void result(Object object) {
        model.cancelCall();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

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

    public Dialog setText(String text) {
        lblMessage.setText(text);
        pack();
        return this;
    }
}
