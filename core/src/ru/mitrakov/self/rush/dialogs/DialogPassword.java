package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.assets.AssetManager;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.model.Model;

/**
 * "Change weak password" dialog
 * @author Mitrakov
 */
public class DialogPassword extends DialogFeat {
    /** Reference to the model */
    private final Model model;
    /** LibGdx Assets Manager */
    private final AssetManager assetManager;

    /** "Please provide a new password to protect your account" label */
    private final Label lblMessage;
    /** "Password" label */
    private final Label lblPassword1;
    /** "Repeat password" label */
    private final Label lblPassword2;
    /** Text area for a new password */
    private final TextField txtPassword;
    /** Text area for repeating the new password */
    private final TextField txtPasswordAgain;
    /** Message box to show errors and warnings */
    private final DialogInfo infoDialog;

    /**
     * Creates new "Change weak password" dialog
     * @param model {@link Model}
     * @param title header title
     * @param skin LibGdx skin
     * @param style style name (usually just "default")
     * @param psObject Platform Specific object
     * @param assetManager LibGdx Assets Manager
     */
    public DialogPassword(Model model, String title, Skin skin, String style, PsObject psObject,
                          AssetManager assetManager) {
        super(title, skin, style);
        assert model != null && assetManager != null && skin != null && psObject != null;

        this.model = model;
        this.assetManager = assetManager;

        lblMessage = new LabelFeat("", skin, "default", true);
        txtPassword = new TextFieldFeat("", skin, "default", psObject, null);
        txtPassword.setPasswordMode(true);
        txtPassword.setPasswordCharacter('*');
        txtPasswordAgain = new TextFieldFeat("", skin, "default", psObject, null);
        txtPasswordAgain.setPasswordMode(true);
        txtPasswordAgain.setPasswordCharacter('*');
        infoDialog = new DialogInfo("", skin, "default");

        Table table = new Table();
        table.add(lblPassword1 = new Label("", skin, "default")).left();
        table.add(txtPassword);
        table.row().space(8);
        table.add(lblPassword2 = new Label("", skin, "default")).left();
        table.add(txtPasswordAgain);

        getContentTable().pad(20).add(lblMessage).minWidth(400);
        getContentTable().row();
        getContentTable().add(table);

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblMessage.setText(bundle.format("dialog.password.weak.text"));
        lblPassword1.setText(bundle.format("sign.password"));
        lblPassword2.setText(bundle.format("sign.password.repeat"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.password.weak.header"));

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

    @Override
    protected void result(Object object) {
        super.result(object);
        String password1 = txtPassword.getText();
        String password2 = txtPasswordAgain.getText();
        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));

        if (password1.length() < 4 || password2.length() < 4) {
            cancel();
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.incorrect.password")).show(getStage());
        } else if (!password1.equals(password2)) {
            cancel();
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.different.passwords")).show(getStage());
        } else model.changePassword(password1);
    }
}
