package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * "Invite to battle" dialog
 * @author Mitrakov
 */
public class DialogInvite extends DialogFeat {
    /** Invite types */
    public enum InviteType {@Deprecated Training, ByName, Quick, Latest} // Training moved to SinglePlayer (since 2.0.0)

    /** Reference to the model */
    private final Model model;
    /** Question label (e.g. "Do you wanna invite {0}?") */
    private final Label lblQuestion;
    // private final CheckBox chkAddToFriends; @mitrakov (2017-07-19): decided to remove this possibility

    /** Current invite type */
    private InviteType type = InviteType.Quick;
    /** Current enemy name */
    private String name = "";
    /** LibGdx internationalization bundle */
    private I18NBundle i18n;

    /**
     * Creates new "Invite to battle" dialog
     * @param model {@link Model}
     * @param skin LibGdx skin
     * @param styleName style name (usually just "default")
     * @param i18n LibGdx internationalization bundle
     */
    public DialogInvite(Model model, Skin skin, String styleName, I18NBundle i18n) {
        super("", skin, styleName, true);
        assert model != null && i18n != null;
        this.model = model;
        this.i18n = i18n;

        lblQuestion = new Label("", skin, "default");

        button("OK", true); // text will be replaced in onLocaleChanged()
        button("Cancel");   // text will be replaced in onLocaleChanged()
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildContent();
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        assert type != null;
        if (object != null) {                // "OK" is pressed
            switch (type) {
                case Training:
                    model.receiveLevel("training.level");
                    break;
                case ByName:
                    model.invite(name);
                    break;
                case Quick:
                    model.quickGame();
                    break;
                case Latest:
                    model.inviteLatest();
                    break;
                default:
            }
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.invite.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 2) {
                Actor ok = buttons.first();
                if (ok instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) ok).setText(bundle.format("ok"));
                Actor cancel = buttons.get(1);
                if (cancel instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) cancel).setText(bundle.format("cancel"));
            }
        }
    }

    /**
     * Sets arguments for this dialog
     * @param type invite type
     * @param name enemy name
     * @return this
     */
    public Dialog setArguments(InviteType type, String name) {
        assert type != null;
        this.type = type;
        this.name = name;
        return this;
    }

    /** Rebuilds content according to invite type chosen */
    private void rebuildContent() {
        Table table = getContentTable();
        assert type != null && table != null && i18n != null;

        table.pad(20).clear();
        table.add(lblQuestion);
        switch (type) {
            case Training:
                lblQuestion.setText(i18n.format("dialog.invite.none"));
                break;
            case ByName:
                lblQuestion.setText(i18n.format("dialog.invite.name", name));
                break;
            case Quick:
                lblQuestion.setText(i18n.format("dialog.invite.quick"));
                break;
            case Latest:
                lblQuestion.setText(i18n.format("dialog.invite.latest"));
                break;
            default:
        }
        pack();
    }
}
