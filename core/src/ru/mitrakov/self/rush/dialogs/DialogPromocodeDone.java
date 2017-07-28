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
    private String name = "";
    private boolean inviter;
    private int crystals;

    public DialogPromocodeDone(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center, Align.center);
        getContentTable().pad(20).add(lblMessage).minWidth(350); // here getContentTable != null

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        setArguments(name, inviter, crystals, bundle);

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.promocode.done.header"));
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

    public Dialog setArguments(String name, boolean inviter, int crystals, I18NBundle i18n) {
        assert name != null && i18n != null;
        this.name = name;
        this.inviter = inviter;
        this.crystals = crystals;
        String s1 = i18n.format("dialog.promocode.done.inviter", name, crystals);
        String s2 = i18n.format("dialog.promocode.done.winner", name, crystals);
        lblMessage.setText(inviter ? s1 : s2);
        pack();
        return this;
    }
}
