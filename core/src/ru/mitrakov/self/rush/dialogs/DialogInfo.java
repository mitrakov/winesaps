package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogInfo extends DialogFeat {
    private final Label lblMessage;

    public DialogInfo(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center);
        getContentTable().pad(20).add(lblMessage).width(400); // here getContentTable != null

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    public Dialog setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }
}
