package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */

@SuppressWarnings("WeakerAccess")
public class DialogQuestion extends DialogFeat {
    private final Label lblMessage;
    private Runnable action;

    public DialogQuestion(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        lblMessage = new Label("", skin, "default");
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button("Yes", true);
        button("No");
    }

    @Override
    protected void result(Object object) {
        if (object != null) { // 'Yes' pressed
            if (action != null)
                action.run();
        }
    }

    public DialogQuestion setText(String text) {
        assert text != null;
        lblMessage.setText(text);
        return this;
    }

    public DialogQuestion setRunnable(Runnable runnable) {
        action = runnable;
        return this;
    }
}
