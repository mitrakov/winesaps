package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;

/**
 * ScreenLogin shows SignIn and SignUp UI controls: login, password, promocode, etc.
 * @author mitrakov
 */
public class ScreenLogin extends LocalizableScreen {
    // see note#10 below

    private final Table tableMain;
    private final TextFieldFeat txtLogin;
    private final TextFieldFeat txtPassword;
    private final TextFieldFeat txtPromocode;

    private final TextButton btnOk;
    private final TextButton btnBack;
    private final CheckBox chkPromocode;
    private final DialogLanguage langDialog;
    private final DialogInfo infoDialog;
    private final Label lblName;
    private final Label lblPassword;
    private final Label lblDuplicateName;
    private final LinkedLabel lblHaveAccount;
    private final Image imgValid;
    private final Drawable textureValid;
    private final Drawable textureInvalid;
    private final DialogLock connectingDialog;

    private transient boolean shiftedByKeyboard = false;
    private transient boolean signInMode = false;

    /**
     * Creates a new instance of ScreenLogin
     * @param game - instance of Winesaps (NON-NULL)
     * @param model - model (NON-NULL)
     * @param psObject - Platform Specific Object (NON-NULL)
     * @param assetManager - asset manager (NON-NULL)
     * @param audioManager - audio manager (NON-NULL)
     */
    public ScreenLogin(final Winesaps game, final Model model, PsObject psObject, final AssetManager assetManager,
                       AudioManager audioManager) {
        super(game, model, psObject, assetManager, audioManager);

        TextureAtlas atlasMenu = assetManager.get("pack/menu.pack");
        textureValid = new TextureRegionDrawable(atlasMenu.findRegion("valid"));
        textureInvalid = new TextureRegionDrawable(atlasMenu.findRegion("invalid"));
        Drawable textureShutdown = new TextureRegionDrawable(atlasMenu.findRegion("shutdown"));

        Skin skin = assetManager.get("skin/uiskin.json");
        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));

        tableMain = new Table(skin).pad(20);
        tableMain.setBackground("panel-maroon");

        btnOk = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android
                String name = txtLogin.getText();
                if (name.startsWith("#!")) {
                    infoDialog.setText(name, game.getDebugInfo(name)).show(stage);
                } else if (signInMode) {
                    connectingDialog.show(stage);
                    model.signIn(name, txtPassword.getText());
                } else { // signUpMode
                    connectingDialog.show(stage);
                    model.signUp(txtLogin.getText(), "1234", "", txtPromocode.getText());
                    rebuildDialog(chkPromocode.isChecked(), false);
                }
            }
        });
        btnBack = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildDialog(chkPromocode.isChecked(), false);
            }
        });
        txtLogin = new TextFieldFeat("", skin, "default", psObject, btnOk); // ....
        txtPassword = new TextFieldFeat("", skin, "default", psObject, btnOk) {{
            setPasswordMode(true);
            setPasswordCharacter('*');
        }};
        txtPromocode = new TextFieldFeat("", skin, "default", psObject, btnOk) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.checkPromocode(txtPromocode.getText());
                }
            });
        }};
        chkPromocode = new CheckBox("", skin, "default") {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildDialog(chkPromocode.isChecked(), false);
                }
            });
        }};
        lblDuplicateName = new Label("", skin, "white") {{
            setColor(1, 0, 0, 1);
            setAlignment(Align.center);
        }};
        lblHaveAccount = new LinkedLabel("", "", "", skin, "default", "link", new Runnable() {
            @Override
            public void run() {
                rebuildDialogPassword();
            }
        });
        langDialog = new DialogLanguage(game, model, "", skin, "default", atlasMenu, i18n, audioManager);
        infoDialog = new DialogInfo("", skin, "default");
        lblName = new Label("", skin, "default");
        lblPassword = new Label("", skin, "default");
        imgValid = new Image(textureInvalid);
        connectingDialog = new DialogLock(skin, "panel-lock");

        // set up layout
        // table.add(createLangTable(audioManager)).right(); table.row(); @mitrakov: removed after Newbie-Beta-Testing
        table.add(new ImageButtonFeat(textureShutdown, audioManager, new Runnable() {
            @Override
            public void run() {
                Gdx.app.exit(); // NOTE! DO NOT use it on iOS (see javaDocs)
            }
        })).left();
        table.row();
        table.add(tableMain).expand();
        table.setBackground(new Image(assetManager.<Texture>get("back/login.jpg")).getDrawable());
    }

    @Override
    public void show() {
        super.show();
        rebuildDialog(false, false);
        if (model.newbie)
            langDialog.show(stage);
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;

        langDialog.onLocaleChanged(bundle);

        btnOk.setText(bundle.format("ok"));
        btnBack.setText(bundle.format("back"));
        chkPromocode.setText(bundle.format("sign.promocode"));
        lblName.setText(bundle.format("sign.prompt"));
        lblPassword.setText(bundle.format("sign.password"));
        lblDuplicateName.setText(bundle.format("dialog.info.duplicate.name"));
        connectingDialog.setText(bundle.format("dialog.connecting"));
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        // @mitrakov (2017-08-05): do NOT put here local vars like "String.format()" or "i18n.format()". It causes
        // excessive work for GC on each event during a battle (because all screens are subscribed to events)

        if (event instanceof EventBus.AuthorizedChangedEvent) {
            EventBus.AuthorizedChangedEvent ev = (EventBus.AuthorizedChangedEvent) event;
            if (ev.authorized)
                game.setNextScreen();
        }
        if (event instanceof EventBus.IncorrectCredentialsEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.incorrect.credentials")).show(stage);
        }
        if (event instanceof EventBus.IncorrectNameEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.incorrect.name")).show(stage);
        }
        if (event instanceof EventBus.IncorrectEmailEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.incorrect.email")).show(stage);
        }
        if (event instanceof EventBus.DuplicateNameEvent) {
            rebuildDialog(chkPromocode.isChecked(), true);
        }
        if (event instanceof EventBus.SignUpErrorEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.incorrect.signup")).show(stage);
        }
    }

    @Override
    public void handleEventBackground(EventBus.Event event) {
        connectingDialog.remove();
        if (event instanceof EventBus.PromocodeValidChangedEvent) {
            EventBus.PromocodeValidChangedEvent ev = (EventBus.PromocodeValidChangedEvent) event;
            imgValid.setDrawable(ev.valid ? textureValid : textureInvalid);
        }
    }

    /**
     * Sets the screen ratio
     * @param ratio - float value of width/height
     */
    public void setRatio(float ratio) {
        shiftedByKeyboard = ratio > 2;
        if (signInMode)
            rebuildDialogPassword();
        else rebuildDialog(chkPromocode.isChecked(), false);
    }

    /**
     * Reconstructs the screen to show SignUp controls
     * @param havePromocode - true, if a user clicked "I have a promo code"
     * @param showDuplicateNameError - true, if a user chose an already existing login
     */
    private void rebuildDialog(boolean havePromocode, boolean showDuplicateNameError) {
        Actor focused = stage.getKeyboardFocus();
        signInMode = false;

        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
        lblName.setText(i18n.format("sign.prompt"));

        tableMain.clear();
        tableMain.add(lblName).colspan(2).space(8);
        tableMain.row();
        tableMain.add(txtLogin).colspan(2).width(305).height(50).space(8);

        if (showDuplicateNameError) {
            lblHaveAccount.setText(i18n.format("sign.have.account"), txtLogin.getText(), "");
            tableMain.row();
            tableMain.add(lblDuplicateName).colspan(2).space(8);
            tableMain.row();
            tableMain.add(lblHaveAccount).colspan(2).left().space(8);
        }

        tableMain.row();
        tableMain.add(chkPromocode).colspan(2).left().space(8);

        if (havePromocode) {
            tableMain.row();
            tableMain.add(txtPromocode).expandX().fillX().height(50).space(8);
            tableMain.add(imgValid).width(imgValid.getWidth()).height(imgValid.getHeight());
        }

        tableMain.row();
        tableMain.add(btnOk).colspan(2).minWidth(131).height(50).spaceTop(30);

        if (shiftedByKeyboard)
            shiftUp();

        stage.setKeyboardFocus(focused != null ? focused : txtLogin);
    }

    /**
     * Reconstructs the screen to show SignIn controls (login/password)
     */
    private void rebuildDialogPassword() {
        Actor focused = stage.getKeyboardFocus();
        signInMode = true;

        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
        lblName.setText(i18n.format("sign.name"));

        Table buttons = new Table();
        buttons.add(btnBack).width(131).height(50).space(20);
        buttons.add(btnOk).width(131).height(50).space(20);

        tableMain.clear();
        tableMain.add(lblName).colspan(2).space(8);
        tableMain.row();
        tableMain.add(txtLogin).colspan(2).width(305).height(50).space(8);

        tableMain.row();
        tableMain.add(lblPassword).colspan(2).space(8);
        tableMain.row();
        tableMain.add(txtPassword).colspan(2).width(305).height(50).space(8);

        tableMain.row();
        tableMain.add(buttons).colspan(2).spaceTop(30);

        if (shiftedByKeyboard)
            shiftUp();

        stage.setKeyboardFocus(focused != null ? focused : txtPassword);
    }

    /**
     * Shifts all the controls up to free bottom space for Soft Keyboard (only Android)
     */
    private void shiftUp() {
        tableMain.row().spaceTop(200);
        tableMain.add(new Image());  // 'blank' image to fill space taken by on-screen keyboard
    }
}

// note#10 (@mitrakov, 2017-08-16): I decided to replace usual registration (Login/Password) with a "soft" one
// (only ask for Name, Password is always default); after several battles the game will suggest to provide a new
// password; I hope it'll help to decrease the number of registration-scared newbies
//
