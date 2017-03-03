package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 03.03.2017
 */

class ScreenMain extends ScreenAdapter {
    private final RushClient game;
    private final Model model;
    private final Stage stage = new Stage(new FitViewport(800, 480));
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    private final Table tableMain = new Table();
    private final Table tableLeft = new Table();
    private final Table tableRight = new Table();

    private final TextField txtEnemyName = new TextField("", skin, "default");
    private final TextButton btnInviteByName = new TextButton("Find opponent", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildTables(true);
            }
        });
    }};
    private final TextButton btnInviteRandom = new TextButton("Random opponent", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.inviteRandom();
            }
        });
    }};
    private final TextButton btnInviteLatest = new TextButton("Latest opponent", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.inviteLatest();
            }
        });
    }};
    private final TextButton btnInviteByNameOk = new TextButton("OK", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.invite(txtEnemyName.getText());
                buildTables(false);
            }
        });
    }};
    private final TextButton btnInviteByNameCancel = new TextButton("Cancel", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buildTables(false);
            }
        });
    }};

    ScreenMain(RushClient game, Model model) {
        assert game != null && model != null;
        this.game = game;
        this.model = model;

        tableMain.setFillParent(true);
        tableMain.setDebug(true);
        stage.addActor(tableMain);

        tableMain.add(tableLeft).spaceRight(20);
        tableMain.add(tableRight);
        buildTables(false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.25f, .77f, .81f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
        tableMain.debug();

        if (model.field != null)
            game.setNextScreen();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
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

    private void buildTables(boolean showInputName) {
        tableLeft.clear();

        if (showInputName) {
            tableLeft.add(txtEnemyName).colspan(2);
            tableLeft.row();
            tableLeft.add(btnInviteByNameOk);
            tableLeft.add(btnInviteByNameCancel);
            tableLeft.row();
            tableLeft.add(btnInviteRandom).colspan(2);
            tableLeft.row();
            tableLeft.add(btnInviteLatest).colspan(2);
        } else {
            tableLeft.add(btnInviteByName);
            tableLeft.row();
            tableLeft.add(btnInviteRandom);
            tableLeft.row();
            tableLeft.add(btnInviteLatest);
        }
    }
}
