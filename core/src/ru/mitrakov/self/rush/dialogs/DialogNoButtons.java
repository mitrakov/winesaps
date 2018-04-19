package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ru.mitrakov.self.rush.ui.LabelFeat;

/**
 * Dialog with a single text message without any buttons (usually used for hints, e.g. in BattleTraining)
 * @see DialogLock
 * @author Mitrakov
 */
public class DialogNoButtons extends Window {
    /** Message label */
    private final Label lblMessage;

    /**
     * Creates new non-closable dialog with a single text message without any buttons
     * @param title header title
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogNoButtons(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        lblMessage = new LabelFeat("", skin, "default", true);
        pad(20).add(lblMessage).minWidth(400); // here getContentTable != null
        setMovable(false);
    }

    /**
     * Sets text message to the dialog body
     * @param text message
     * @return this
     */
    public DialogNoButtons setText(String text) {
        lblMessage.setText(text);
        return this;
    }

    /**
     * Shows the dialog. As opposed to {@link DialogLock#show(Stage)} this action will NOT block the other controls.
     * <br><b>Note:</b> the only way to close this dialog is to {@link #remove()} it programmatically
     * @param stage LibGdx stage
     * @param centered TRUE to show in the center of a screen, and FALSE - to show on top
     */
    public void show(Stage stage, boolean centered) {
        assert stage != null;
        pack();
        if (centered)
            setPosition(0, 0, Align.center);
        else setPosition(.5f * stage.getWidth() - .5f * getWidth(), stage.getHeight() - getHeight() - 5);
        stage.addActor(this);
    }
}
