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
 * Created by mitrakov on 01.03.2017
 */
public class ScreenLogin extends LocalizableScreen {

    private final Table tableMain;
    private final TextFieldFeat txtLogin;
    private final TextFieldFeat txtPassword;
    private final TextFieldFeat txtEmail;
    private final TextFieldFeat txtPromocode;
    private final TextButton btnSignIn;
    private final TextButton btnSignUp;
    private final TextButton btnBack;
    private final TextButton btnOkSignIn;
    private final TextButton btnOkSignUp;
    private final CheckBox chkPromocode;
    private final DialogLanguage langDialog;
    private final DialogInfo infoDialog;
    private final Label lblName;
    private final Label lblNameAsterisk;
    private final Label lblPassword;
    private final Label lblPasswordAsterisk;
    private final Label lblEmail;
    private final Image imgValid;
    private final Drawable textureValid;
    private final Drawable textureInvalid;
    private final DialogLock connectingDialog;

    private enum CurDialog {Start, SignIn, SignUp}

    private CurDialog curDialog = CurDialog.Start;
    private boolean shiftedByKeyboard = false;

    public ScreenLogin(final Winesaps game, final Model model, PsObject psObject, final AssetManager assetManager,
                       AudioManager audioManager) {
        super(game, model, psObject, assetManager, audioManager);

        TextureAtlas atlasMenu = assetManager.get("pack/menu.pack");
        textureValid = new TextureRegionDrawable(atlasMenu.findRegion("valid"));
        textureInvalid = new TextureRegionDrawable(atlasMenu.findRegion("invalid"));

        Skin skin = assetManager.get("skin/uiskin.json");
        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));

        tableMain = new Table(skin).pad(20);
        tableMain.setBackground("panel-maroon");

        btnSignIn = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setSignInDialog();
                }
            });
        }};
        btnSignUp = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setSignUpDialog(false);
                }
            });
        }};
        btnBack = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setStartDialog();
                }
            });
        }};
        btnOkSignIn = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = txtLogin.getText();
                    if (name.startsWith("#!")) {
                        infoDialog.setText(name, game.getDebugInfo(name)).show(stage);
                        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android
                    } else {
                        connectingDialog.show(stage);
                        model.signIn(name, txtPassword.getText());
                    }
                }
            });
        }};
        btnOkSignUp = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
                    String password = txtPassword.getText();
                    if (password.length() >= 4) {
                        connectingDialog.show(stage);
                        model.signUp(txtLogin.getText(), password, txtEmail.getText(), txtPromocode.getText());
                    }
                    else {
                        infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.incorrect.password"));
                        infoDialog.show(stage);
                    }
                }
            });
        }};
        txtLogin = new TextFieldFeat("", skin, "default", psObject, null); // ....
        txtPassword = new TextFieldFeat("", skin, "default", psObject, null) {{
            setPasswordMode(true);
            setPasswordCharacter('*');
        }};
        txtEmail = new TextFieldFeat("", skin, "default", psObject, btnOkSignUp);
        txtPromocode = new TextFieldFeat("", skin, "default", psObject, btnOkSignUp) {{
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
                    setSignUpDialog(chkPromocode.isChecked());
                }
            });
        }};
        langDialog = new DialogLanguage(game, model, "", skin, "default", atlasMenu, i18n, audioManager);
        infoDialog = new DialogInfo("", skin, "default");
        lblName = new Label("", skin, "default");
        lblNameAsterisk = new Label("*", skin, "default");
        lblPassword = new Label("", skin, "default");
        lblPasswordAsterisk = new Label("*", skin, "default");
        lblEmail = new Label("", skin, "default");
        imgValid = new Image(textureInvalid);
        connectingDialog = new DialogLock(skin, "panel-lock");

        // set up layout
        // table.add(createLangTable(audioManager)).right(); table.row(); @mitrakov: removed after Newbie-Beta-Testing
        table.add(new ImageButtonFeat(new TextureRegionDrawable(atlasMenu.findRegion("shutdown")), audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.exit(); // NOTE! DO NOT use it on iOS (see javaDocs)
                }
            });
        }}).left();
        table.row();
        table.add(tableMain).expand();
        table.setBackground(new Image(assetManager.<Texture>get("back/login.jpg")).getDrawable());
    }

    @Override
    public void show() {
        super.show();
        setStartDialog();
        if (model.newbie)
            langDialog.show(stage);
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;

        langDialog.onLocaleChanged(bundle);

        btnSignIn.setText(bundle.format("sign.in"));
        btnSignUp.setText(bundle.format("sign.up"));
        btnBack.setText(bundle.format("back"));
        btnOkSignIn.setText(bundle.format("ok"));
        btnOkSignUp.setText(bundle.format("ok"));
        chkPromocode.setText(bundle.format("sign.promocode"));
        lblName.setText(bundle.format("sign.name"));
        lblPassword.setText(bundle.format("sign.password"));
        lblEmail.setText(bundle.format("sign.email"));
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
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.duplicate.name")).show(stage);
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

    public void setRatio(float ratio) {
        shiftedByKeyboard = ratio > 2;
        switch (curDialog) {
            case SignIn:
                setSignInDialog();
                break;
            case SignUp:
                setSignUpDialog(chkPromocode.isChecked());
                break;
            default:
        }
    }

    private void setStartDialog() {
        curDialog = CurDialog.Start;
        chkPromocode.setChecked(false);              // clear checking
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android

        tableMain.clear();
        tableMain.add(btnSignIn).width(222).height(85).space(30);
        tableMain.row();
        tableMain.add(btnSignUp).width(222).height(85);
    }

    private void setSignInDialog() {
        curDialog = CurDialog.SignIn;
        Actor focused = stage.getKeyboardFocus();

        txtLogin.setOnEnterActor(btnOkSignIn);
        txtPassword.setOnEnterActor(btnOkSignIn);

        Table buttons = new Table();
        buttons.add(btnBack).width(120).height(46).space(20);
        buttons.add(btnOkSignIn).width(120).height(46).space(20);

        tableMain.clear();
        tableMain.row().space(20);
        tableMain.add(lblName).align(Align.left);
        tableMain.add(txtLogin).width(305).height(50);
        tableMain.row().space(20);
        tableMain.add(lblPassword).align(Align.left);
        tableMain.add(txtPassword).width(305).height(50);
        tableMain.row().spaceTop(30);
        tableMain.add(buttons).colspan(2);
        if (shiftedByKeyboard)
            shiftUp();

        stage.setKeyboardFocus(focused != null ? focused : txtLogin);
    }

    private void setSignUpDialog(boolean havePromocode) {
        curDialog = CurDialog.SignUp;
        Actor focused = stage.getKeyboardFocus();

        txtLogin.setOnEnterActor(btnOkSignUp);
        txtPassword.setOnEnterActor(btnOkSignUp);
        txtPromocode.setVisible(havePromocode);
        imgValid.setVisible(havePromocode);

        Table buttons = new Table();
        buttons.add(btnBack).width(120).height(46).space(20);
        buttons.add(btnOkSignUp).width(120).height(46).space(20);

        final int txtHeight = shiftedByKeyboard ? 40 : 50; // make textboxes smaller when OnScreen Keyboard is visible

        tableMain.clear();
        tableMain.row().spaceTop(10).spaceLeft(5);
        tableMain.add(lblNameAsterisk).width(10);
        tableMain.add(lblName).expandX().align(Align.left);
        tableMain.add(txtLogin).width(305).height(txtHeight).colspan(2);

        tableMain.row().spaceTop(10).spaceLeft(5);
        tableMain.add(lblPasswordAsterisk).width(10);
        tableMain.add(lblPassword).align(Align.left);
        tableMain.add(txtPassword).width(305).height(txtHeight).colspan(2);

        tableMain.row().spaceTop(10).spaceLeft(5);
        tableMain.add(lblEmail).align(Align.left).colspan(2);
        tableMain.add(txtEmail).width(305).height(txtHeight).colspan(2);

        tableMain.row().spaceTop(10).spaceLeft(5);
        tableMain.add(chkPromocode).colspan(2);
        tableMain.add(txtPromocode).expandX().fillX().height(txtHeight);
        tableMain.add(imgValid).width(imgValid.getWidth()).height(imgValid.getHeight());

        tableMain.row().spaceTop(30);
        tableMain.add(buttons).colspan(4);
        if (shiftedByKeyboard)
            shiftUp();

        stage.setKeyboardFocus(focused != null ? focused : txtLogin);
    }

    private void shiftUp() {
        tableMain.row().spaceTop(200);
        tableMain.add(new Image());  // 'blank' image to fill space taken by on-screen keyboard
    }
}
