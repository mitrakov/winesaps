package ru.mitrakov.self.rush.dialogs;

import java.util.Locale;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogPromocodeDone extends DialogFeat {
    private final Label lblMessage;

    public DialogPromocodeDone(Skin skin, String windowStyleName) {
        super("You've got extra crystals!", skin, windowStyleName);

        lblMessage = new Label("", skin, "default");
        lblMessage.setAlignment(Align.center, Align.center);
        getContentTable().pad(20).add(lblMessage); // here getContentTable != null

        button("OK");
    }

    public Dialog setArguments(String name, boolean inviter, int crystals) {
        assert name != null;
        String s1 = String.format(Locale.getDefault(),
                "%s has won with your promo code,\nso you've got %d extra crystals!", name, crystals);
        String s2 = String.format(Locale.getDefault(),
                "You have won with a promo code provided by %s,\nso you've got %d extra crystals!", name, crystals);
        lblMessage.setText(inviter ? s1 : s2);
        return this;
    }
}
