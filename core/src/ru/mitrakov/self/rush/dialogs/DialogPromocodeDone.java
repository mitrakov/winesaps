package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogPromocodeDone extends DialogFeat {
    private final I18NBundle i18n;
    private final Label lblMessage;

    public DialogPromocodeDone(Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.promocode.done.header"), skin, windowStyleName);
        this.i18n = i18n;

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center, Align.center);
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button(i18n.format("ok"));
    }

    public Dialog setArguments(String name, boolean inviter, int crystals) {
        assert name != null;
        String s1 = i18n.format("dialog.promocode.done.inviter", name, crystals);
        String s2 = i18n.format("dialog.promocode.done.winner", name, crystals);
        lblMessage.setText(inviter ? s1 : s2);
        return this;
    }
}
