package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.*;

/**
 * Standard Yes/No dialog
 * @author Mitrakov
 */
public class DialogQuestion extends DialogFeat {
    /** Question message label */
    private final Label lblMessage;
    /** OnYes function */
    private Runnable action;

    /**
     * Creates a new Yes/No dialog
     * @param title header title
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogQuestion(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName, true);

        lblMessage = new LabelFeat("", skin, "default", true);
        getContentTable().pad(20).add(lblMessage).minWidth(400); // here getContentTable != null

        button("Yes", true); // text will be replaced in onLocaleChanged()
        button("No");        // text will be replaced in onLocaleChanged()
    }

    @Override
    protected void result(Object object) {
        if (object != null) { // 'Yes' pressed
            if (action != null)
                action.run();
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 2) {
                Actor yes = buttons.first();
                if (yes instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) yes).setText(bundle.format("yes"));
                Actor no = buttons.get(1);
                if (no instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) no).setText(bundle.format("no"));
            }
        }
    }

    /**
     * Sets text for this dialog
     * @param header title header text
     * @param text message body
     * @return this
     */
    public DialogQuestion setText(String header, String text) {
        if (getTitleLabel() != null)
            getTitleLabel().setText(header);
        lblMessage.setText(text);
        return this;
    }

    /**
     * Sets a function to run when 'Yes' is clicked
     * @param runnable function to run
     * @return this
     */
    public DialogQuestion setRunnable(Runnable runnable) {
        action = runnable;
        return this;
    }
}
