package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogPromocodeDone extends DialogFeat {
    private final Label lblMessage;
    private I18NBundle i18n;

    public DialogPromocodeDone(Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.promocode.done.header"), skin, windowStyleName);
        this.i18n = i18n;

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center, Align.center);
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button(i18n.format("ok"));
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.promocode.done.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor != null && actor instanceof TextButton)
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    public Dialog setArguments(String name, boolean inviter, int crystals) {
        assert name != null && i18n != null;
        String s1 = i18n.format("dialog.promocode.done.inviter", name, crystals);
        String s2 = i18n.format("dialog.promocode.done.winner", name, crystals);
        lblMessage.setText(inviter ? s1 : s2);
        return this;
    }
}
