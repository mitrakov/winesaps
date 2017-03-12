package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.RushClient;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 01.03.2017
 */

public class ScreenLogin extends ScreenAdapter {
    private final RushClient game;
    private final Model model;
    private final PsObject psObject;
    private final Stage stage = new Stage(new FitViewport(800, 480));
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    private final Table table = new Table();

    private final TextField txtLogin = new TextField("Tommy", skin, "default"); // ....
    private final TextField txtPassword = new TextField("Tommy", skin, "default") {{
        setPasswordMode(true);
        setPasswordCharacter('*');
    }};
    private final TextField txtEmail = new TextField("", skin, "default");

    private enum CurDialog {Start, SignIn, SignUp}

    private CurDialog curDialog = CurDialog.Start;

    public ScreenLogin(RushClient game, Model model, PsObject psObject) {
        assert game != null && model != null;
        this.game = game;
        this.model = model;
        this.psObject = psObject; // may be NULL

        table.setFillParent(true);
        stage.addActor(table);

        setStartDialog();

        // only for Android: handling show/hide OnScreenKeyboard
        if (psObject != null) psObject.setListener(new PsObject.Listener() {
            @Override
            public void onRatioChanged(final float ratio) {
                Gdx.app.postRunnable(new Runnable() { // @mitrakov: it is necessary to avoid OutOfSync exceptions!
                    @Override
                    public void run() {
                        switch (curDialog) {
                            case SignIn:
                                setSignInDialog(ratio > 2);
                                break;
                            case SignUp:
                                setSignUpDialog(ratio > 2);
                                break;
                            default:
                        }
                    }
                });
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.25f, .77f, .81f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        if (model.authorized)
            game.setNextScreen();

        // checking BACK and MENU buttons on Android
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            if (psObject != null)
                psObject.hide();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show () {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    private void setStartDialog() {
        curDialog = CurDialog.Start;
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android

        TextButton btnSignIn = new TextButton("Sign in", skin, "default");
        btnSignIn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setSignInDialog(false);
            }
        });
        TextButton btnSignUp = new TextButton("Sign up", skin, "default");
        btnSignUp.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setSignUpDialog(false);
            }
        });

        table.clear();
        table.add(btnSignIn).width(300).height(80).space(30);
        table.row();
        table.add(btnSignUp).width(300).height(80);
    }

    private void setSignInDialog(boolean shiftForKeyboard) {
        curDialog = CurDialog.SignIn;
        Actor focused = stage.getKeyboardFocus();

        TextButton btnBack = new TextButton("Back", skin, "default");
        btnBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setStartDialog();
            }
        });

        TextButton btnForth = new TextButton("OK", skin, "default");
        btnForth.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.signIn(txtLogin.getText(), txtPassword.getText());
            }
        });

        table.clear();
        table.add(new Label("Name", skin, "default")).align(Align.left);
        table.add(txtLogin);
        table.row().spaceTop(20);
        table.add(new Label("Password", skin, "default")).align(Align.left);
        table.add(txtPassword);
        table.row().spaceTop(30);
        table.add(btnBack).width(100).height(40);
        table.add(btnForth).width(100).height(40);
        if (shiftForKeyboard) shiftUp();

        stage.setKeyboardFocus(focused);
    }

    private void setSignUpDialog(boolean shiftForKeyboard) {
        curDialog = CurDialog.SignUp;
        Actor focused = stage.getKeyboardFocus();

        TextButton btnBack = new TextButton("Back", skin, "default");
        btnBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setStartDialog();
            }
        });

        TextButton btnForth = new TextButton("OK", skin, "default");
        btnForth.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.signUp(txtLogin.getText(), txtPassword.getText(), txtEmail.getText());
            }
        });

        table.clear();
        table.add(new Label("Name", skin, "default")).align(Align.left);
        table.add(txtLogin);
        table.row().spaceTop(20);
        table.add(new Label("Password", skin, "default")).align(Align.left);
        table.add(txtPassword);
        table.row().spaceTop(20);
        table.add(new Label("Email", skin, "default")).align(Align.left);
        table.add(txtEmail);
        table.row().spaceTop(30);
        table.add(btnBack).width(100).height(40);
        table.add(btnForth).width(100).height(40);
        if (shiftForKeyboard) shiftUp();

        stage.setKeyboardFocus(focused);
    }

    private void shiftUp() {
        table.row().spaceTop(200);
        table.add(new Label("", skin, "default"));  // WTF: empty label? are you serious?
    }
}
