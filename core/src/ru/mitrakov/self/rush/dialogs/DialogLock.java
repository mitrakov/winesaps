package ru.mitrakov.self.rush.dialogs;

import static java.lang.Math.*;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.*;

/**
 * Non-closable Dialog that covers all the screen
 * @see DialogNoButtons
 * @author Mitrakov
 */
public class DialogLock extends Window {
    /** Message label */
    private final Label label;

    /**
     * Creates non-closable dialog that covers all the screen until someone hides it explicitly
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogLock(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        // add widgets
        pad(20);
        add(label = new LabelFeat("", skin, "default", true)).minWidth(300);

        // set up
        setModal(true);
        setMovable(false);
        pack();
    }

    /**
     * Sets the text for this dialog
     * @param text message
     * @return this
     */
    public DialogLock setText(String text) {
        label.setText(text);
        return this;
    }

    /**
     * Shows the dialog making all the other controls non-active.
     * <br><b>ATTENTION:</b> you must {@link #remove()} it on some condition! Otherwise the client will just "hang up"
     * @param stage LibGdx stage
     */
    public void show(Stage stage) {
        // TODO I think we need to implement timeout! Otherwise the full client might get stuck forever
        assert stage != null;
        DialogFeat.hideAll(stage);
        setPosition(round((stage.getWidth() - getWidth()) / 2), round((stage.getHeight() - getHeight()) / 2));
        stage.addActor(this);
    }
}
