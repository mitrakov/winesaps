package ru.mitrakov.self.rush;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
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
    private final Table tableRightHeader = new Table();
    private final Table tableRightContent = new Table();

    private enum CurDisplayMode {Info, Rating, History, Friends}

    private final TextField txtEnemyName = new TextField("", skin, "default");
    private final TextButton btnInviteByName = new TextButton("Find opponent", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildLeftTable(true);
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
                rebuildLeftTable(false);
            }
        });
    }};
    private final TextButton btnInviteByNameCancel = new TextButton("Cancel", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildLeftTable(false);
            }
        });
    }};
    private final TextButton btnInfo = new TextButton("Info", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildRightTable(CurDisplayMode.Info);
            }
        });
    }};
    private final TextButton btnRating = new TextButton("Rating", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildRightTable(CurDisplayMode.Rating);
            }
        });
    }};
    private final TextButton btnHistory = new TextButton("History", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildRightTable(CurDisplayMode.History);
            }
        });
    }};
    private final TextButton btnFriends = new TextButton("Friends", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildRightTable(CurDisplayMode.Friends);
            }
        });
    }};
    private final Label lblName = new Label("aaa", skin, "default");
    private final Label lblCrystalsHeader = new Label("Crystals:", skin, "default");
    private final Label lblCrystalsData = new Label("", skin, "default");
    private final Label lblAbilities = new Label("Abilities:", skin, "default");
    private final Label lblMoreCrystalsPrefix = new Label("Get ", skin, "default");
    private final Label lblMoreCrystals = new Label("more crystals", skin, "default-font", new Color(.4f, .4f, .9f, 1));

    ScreenMain(RushClient game, Model model) {
        assert game != null && model != null;
        this.game = game;
        this.model = model;

        tableMain.setFillParent(true);
        tableMain.setDebug(true);
        stage.addActor(tableMain);

        initTables();
        rebuildLeftTable(false);
        rebuildRightTable(CurDisplayMode.Info);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.25f, .77f, .81f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

        lblName.setText(model.name);
        lblCrystalsData.setText(String.format(Locale.getDefault(), "%d", model.crystals));

        if (model.field != null)
            game.setNextScreen();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    private void initTables() {
        tableMain.add(tableLeft).pad(20);
        tableMain.add(tableRight).pad(20).expand().fill();

        tableLeft.setDebug(true);

        tableRight.setDebug(true);
        tableRight.add(tableRightHeader);
        tableRight.row();
        tableRight.add(tableRightContent).expandY();

        tableRightHeader.setDebug(true);
        tableRightHeader.row().width(90).space(20);
        tableRightHeader.add(btnInfo);
        tableRightHeader.add(btnRating);
        tableRightHeader.add(btnHistory);
        tableRightHeader.add(btnFriends);

        tableRightContent.setDebug(true);
    }

    private void rebuildLeftTable(boolean showInputName) {
        tableLeft.clear();

        // ...
        if (showInputName) {
            tableLeft.add(txtEnemyName).colspan(2).width(140).height(50);
            tableLeft.row().space(20);
            tableLeft.add(btnInviteByNameOk).width(60).height(40);
            tableLeft.add(btnInviteByNameCancel).width(80).height(40);
            tableLeft.row().space(20);
            tableLeft.add(btnInviteRandom).colspan(2).width(160).height(80);
            tableLeft.row().space(20);
            tableLeft.add(btnInviteLatest).colspan(2).width(160).height(80);
        } else {
            tableLeft.add(btnInviteByName).width(160).height(80);
            tableLeft.row().space(20);
            tableLeft.add(btnInviteRandom).width(160).height(80);
            tableLeft.row().space(20);
            tableLeft.add(btnInviteLatest).width(160).height(80);
        }
    }

    private void rebuildRightTable(CurDisplayMode mode) {
        tableRightContent.clear();

        switch (mode) {
            case Info:
                tableRightContent.row().space(40);
                tableRightContent.add(lblName).colspan(3);
                tableRightContent.row().space(40);
                tableRightContent.add(lblCrystalsHeader);
                tableRightContent.add(lblCrystalsData).colspan(2);
                tableRightContent.row().space(40);
                tableRightContent.add(lblAbilities);
                tableRightContent.add(new Label("...", skin, "default")).colspan(2);
                tableRightContent.row().spaceTop(140);
                tableRightContent.add(new Label("", skin, "default"));
                tableRightContent.add(lblMoreCrystalsPrefix).right();
                tableRightContent.add(lblMoreCrystals).left();
                break;
            default:
                tableRightContent.add(new Label(mode.name(), skin, "default"));
        }
    }
}
