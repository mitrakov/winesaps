package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.DialogInfo;

/**
 * Created by mitrakov on 01.03.2017
 */
public class ScreenLogin extends LocalizableScreen {

    private final Table tableMain = new Table();
    private final TextureAtlas atlasMenu = new TextureAtlas(Gdx.files.internal("pack/menu.pack"));
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
    private final DialogInfo infoDialog;
    private final Label lblName;
    private final Label lblPassword;
    private final Label lblEmail;
    private final Image imgValid;
    private final Drawable textureEn;
    private final Drawable textureRu;
    private final Drawable textureEs;
    private final Drawable texturePt;
    private final Drawable textureFr;
    private final Drawable textureValid;
    private final Drawable textureInvalid;

    private enum CurDialog {Start, SignIn, SignUp}

    private CurDialog curDialog = CurDialog.Start;
    private I18NBundle i18n;
    private boolean shiftedByKeyboard = false;

    public ScreenLogin(Winesaps game, final Model model, PsObject psObject, Skin skin, AudioManager audioManager,
                       I18NBundle i18nb) {
        super(game, model, psObject, skin, audioManager);
        assert i18nb != null;
        i18n = i18nb;

        TextureRegion regionEn = atlasMenu.findRegion("en");
        TextureRegion regionRu = atlasMenu.findRegion("ru");
        TextureRegion regionEs = atlasMenu.findRegion("es");
        TextureRegion regionPt = atlasMenu.findRegion("pt");
        TextureRegion regionFr = atlasMenu.findRegion("fr");
        TextureRegion regionValid = atlasMenu.findRegion("valid");
        TextureRegion regionInvalid = atlasMenu.findRegion("invalid");
        assert regionEn != null && regionRu != null && regionEs != null && regionPt != null && regionFr != null;
        assert regionValid != null && regionInvalid != null;
        textureEn = new TextureRegionDrawable(regionEn);
        textureRu = new TextureRegionDrawable(regionRu);
        textureEs = new TextureRegionDrawable(regionEs);
        texturePt = new TextureRegionDrawable(regionPt);
        textureFr = new TextureRegionDrawable(regionFr);
        textureValid = new TextureRegionDrawable(regionValid);
        textureInvalid = new TextureRegionDrawable(regionInvalid);

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
                    model.signIn(txtLogin.getText(), txtPassword.getText());
                }
            });
        }};
        btnOkSignUp = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    assert i18n != null;
                    String password = txtPassword.getText();
                    if (password.length() >= 4)
                        model.signUp(txtLogin.getText(), password, txtEmail.getText(), txtPromocode.getText());
                    else infoDialog.setText(i18n.format("dialog.info.incorrect.password")).show(stage);
                }
            });
        }};
        txtLogin = new TextFieldFeat("", skin, "default", null); // ....
        txtPassword = new TextFieldFeat("", skin, "default", null) {{
            setPasswordMode(true);
            setPasswordCharacter('*');
        }};
        txtEmail = new TextFieldFeat("", skin, "default", btnOkSignUp);
        txtPromocode = new TextFieldFeat("", skin, "default", btnOkSignUp) {{
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
        infoDialog = new DialogInfo("", skin, "default");
        lblName = new Label("", skin, "default");
        lblPassword = new Label("", skin, "default");
        lblEmail = new Label("", skin, "default");
        imgValid = new Image(textureInvalid);

        // set up layout
        table.add(createLangTable(audioManager)).right();
        table.row();
        table.add(tableMain).expand();

        // only for Android: handling show/hide OnScreenKeyboard
        if (psObject != null) psObject.setRatioListener(new PsObject.RatioListener() {
            @Override
            public void onRatioChanged(final float ratio) {
                Gdx.app.postRunnable(new Runnable() { // @mitrakov: it is necessary to avoid OutOfSync exceptions!
                    @Override
                    public void run() {
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
                });
            }
        });
    }

    @Override
    public void show() {
        super.show();
        setStartDialog();
    }

    @Override
    public void dispose() {
        atlasMenu.dispose(); // disposing an atlas also disposes all its internal textures
        super.dispose();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;
        this.i18n = bundle;

        btnSignIn.setText(bundle.format("sign.in"));
        btnSignUp.setText(bundle.format("sign.up"));
        btnBack.setText(bundle.format("back"));
        btnOkSignIn.setText(bundle.format("ok"));
        btnOkSignUp.setText(bundle.format("ok"));
        chkPromocode.setText(bundle.format("sign.promocode"));
        lblName.setText(bundle.format("sign.name"));
        lblPassword.setText(bundle.format("sign.password"));
        lblEmail.setText(bundle.format("sign.email"));
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        assert i18n != null;
        if (event instanceof EventBus.AuthorizedChangedEvent) {
            EventBus.AuthorizedChangedEvent ev = (EventBus.AuthorizedChangedEvent) event;
            if (ev.authorized)
                game.setNextScreen();
        }
        if (event instanceof EventBus.IncorrectCredentialsEvent)
            infoDialog.setText(i18n.format("dialog.info.incorrect.credentials")).show(stage);
        if (event instanceof EventBus.IncorrectNameEvent)
            infoDialog.setText(i18n.format("dialog.info.incorrect.name")).show(stage);
        if (event instanceof EventBus.IncorrectEmailEvent)
            infoDialog.setText(i18n.format("dialog.info.incorrect.email")).show(stage);
        if (event instanceof EventBus.DuplicateNameEvent)
            infoDialog.setText(i18n.format("dialog.info.duplicate.name")).show(stage);
        if (event instanceof EventBus.SignUpErrorEvent)
            infoDialog.setText(i18n.format("dialog.info.incorrect.signup")).show(stage);
    }

    @Override
    public void handleEventBackground(EventBus.Event event) {
        if (event instanceof EventBus.PromocodeValidChangedEvent) {
            EventBus.PromocodeValidChangedEvent ev = (EventBus.PromocodeValidChangedEvent) event;
            imgValid.setDrawable(ev.valid ? textureValid : textureInvalid);
        }
    }

    private Actor createLangTable(AudioManager audioManager) {
        assert audioManager != null;
        Table tableLang = new Table().padRight(10).padTop(10);
        tableLang.add(new ImageButtonFeat(textureEn, audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = Model.Language.English;
                    game.updateLocale();
                }
            });
        }}).spaceRight(10);
        tableLang.add(new ImageButtonFeat(textureRu, audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = Model.Language.Russian;
                    game.updateLocale();
                }
            });
        }}).spaceRight(10);
        tableLang.add(new ImageButtonFeat(textureEs, audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = Model.Language.Spanish;
                    game.updateLocale();
                }
            });
        }}).spaceRight(10);
        tableLang.add(new ImageButtonFeat(texturePt, audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = Model.Language.Portuguese;
                    game.updateLocale();
                }
            });
        }}).spaceRight(10);
        tableLang.add(new ImageButtonFeat(textureFr, audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.language = Model.Language.French;
                    game.updateLocale();
                }
            });
        }}).spaceRight(10);
        return tableLang;
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
        tableMain.row().space(10);
        tableMain.add(lblName).align(Align.left);
        tableMain.add(txtLogin).width(305).height(txtHeight).colspan(2);
        tableMain.row().space(10);
        tableMain.add(lblPassword).align(Align.left);
        tableMain.add(txtPassword).width(305).height(txtHeight).colspan(2);
        tableMain.row().space(10);
        tableMain.add(lblEmail).align(Align.left);
        tableMain.add(txtEmail).width(305).height(txtHeight).colspan(2);
        tableMain.row().space(10);
        tableMain.add(chkPromocode);
        tableMain.add(txtPromocode).width(240).height(txtHeight).left();
        tableMain.add(imgValid).width(imgValid.getWidth()).height(imgValid.getHeight());
        tableMain.row().spaceTop(30);
        tableMain.add(buttons).colspan(3);
        if (shiftedByKeyboard)
            shiftUp();

        stage.setKeyboardFocus(focused != null ? focused : txtLogin);
    }

    private void shiftUp() {
        tableMain.row().spaceTop(200);
        tableMain.add(new Image());  // 'blank' image to fill space taken by on-screen keyboard
    }
}
