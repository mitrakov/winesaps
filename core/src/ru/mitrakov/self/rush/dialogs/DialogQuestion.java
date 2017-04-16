package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

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
        lblMessage.setAlignment(Align.center);
        getContentTable().pad(20).add(lblMessage).width(400); // here getContentTable != null

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
