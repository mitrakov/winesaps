package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.*;

/**
 * "Promo code activated" dialog
 * @author Mitrakov
 */
public class DialogPromocodeDone extends DialogFeat {
    /** Main message label (e.g. "You won with {0}’s promo code") */
    private final Label lblMessage;
    /** Name of a friend who gave or took promo code */
    private String name = "";
    /** Flag of promo code inviter (TRUE if we invited a friend, and FALSE if we're invited by our friend) */
    private boolean inviter;
    /** Reward for activating promo code */
    private int crystals;

    /**
     * Creates a new "Promo code activated" dialog
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogPromocodeDone(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        lblMessage = new LabelFeat("", skin, "default", true);
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

    /**
     * Sets the arguments for this dialog
     * @param name name of a friend who gave or took promo code
     * @param inviter TRUE if we invited a friend, and FALSE if we're invited by our friend
     * @param crystals reward for activating promo code
     * @param i18n LibGdx internationalization bundle
     * @return this
     */
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
