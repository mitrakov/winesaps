package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;
import ru.mitrakov.self.rush.ui.LabelFeat;

/**
 * "Dialup" dialog
 * @author Mitrakov
 */
public class DialogDialup extends DialogFeat {
    /** Reference to the Model */
    private final Model model;
    /** "Challenging {0} to battle" label */
    private final Label lblMessage;

    /**
     * Creates new "Dialup" dialog (waiting for enemy to respond)
     * @param model {@link Model}
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogDialup(Model model, Skin skin, String windowStyleName) {
        super("", skin, windowStyleName, true);
        assert model != null;
        this.model = model;

        lblMessage = new LabelFeat("", skin, "default", true);
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

    /**
     * Sets the dialogs text
     * @param text content text
     * @return this
     */
    public Dialog setText(String text) {
        lblMessage.setText(text);
        pack();
        return this;
    }
}
