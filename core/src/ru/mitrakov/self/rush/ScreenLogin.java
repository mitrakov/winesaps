package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Created by mitrakov on 01.03.2017
 */

class ScreenLogin extends ScreenAdapter {
    private final Stage stage = new Stage(new FitViewport(800, 480), new SpriteBatch());
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    private final Table table = new Table();

    ScreenLogin(PsObject psObject) {
        Gdx.input.setInputProcessor(stage);
        table.setFillParent(true);
        stage.addActor(table);
        setStartDialog();

        if (psObject != null) psObject.setListener(new PsObject.Listener() {
            @Override
            public void onRatioChanged(float ratio) {
                if (ratio > 2)
                    ; // redraw the screen
                else setSignInDialog();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.25f, .77f, .81f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose(); // batch will also be disposed
        skin.dispose();
    }

    private void setStartDialog() {
        TextButton btnSignIn = new TextButton("Sign in", skin, "default");
        btnSignIn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setSignInDialog();
            }
        });
        TextButton btnSignUp = new TextButton("Sign up", skin, "default");

        table.clear();
        table.add(btnSignIn).width(200).height(60).space(30);
        table.row();
        table.add(btnSignUp).width(200).height(60);
    }

    private void setSignInDialog() {
        TextField txtLogin = new TextField("", skin, "default");
        TextField txtPassword = new TextField("", skin, "default");
        txtPassword.setPasswordMode(true);
        txtPassword.setPasswordCharacter('*');

        TextButton btnBack = new TextButton("Back", skin, "default");
        btnBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setStartDialog();
            }
        });
        TextButton btnForth = new TextButton("OK", skin, "default");


        table.clear();
        table.add(new Label("Name", skin, "default")).align(Align.left);
        table.add(txtLogin);
        table.row().spaceTop(20);
        table.add(new Label("Password", skin, "default")).align(Align.left);
        table.add(txtPassword);
        table.row().spaceTop(30);
        table.add(btnBack).width(100).height(40);
        table.add(btnForth).width(100).height(40);
    }

    private void setSignUpDialog() {

    }
}
