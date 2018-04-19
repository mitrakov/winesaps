package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.*;

/**
 * Custom message box dialog
 * @author Mitrakov
 */
public class DialogInfo extends DialogFeat {
    /** Message label */
    private final Label lblMessage;

    /**
     * Creates new message box dialog
     * @param title header title (may be empty and then changed via {@link #setText(String, String)} method)
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogInfo(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        lblMessage = new LabelFeat("", skin, "default", true);
        getContentTable().pad(20).add(lblMessage).minWidth(400); // here getContentTable != null

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

    /**
     * Sets title and message text of the dialog
     * @param header title text
     * @param text message body text
     * @return this
     */
    public Dialog setText(String header, String text) {
        if (getTitleLabel() != null)
            getTitleLabel().setText(header);
        lblMessage.setText(text);
        return this;
    }
}
