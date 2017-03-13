package ru.mitrakov.self.rush.screens;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.dialogs.*;
import ru.mitrakov.self.rush.RushClient;
import ru.mitrakov.self.rush.ui.LinkedLabel;

/**
 * Created by mitrakov on 03.03.2017
 */

public class ScreenMain extends ScreenAdapter {
    private final RushClient game;
    private final Model model;
    private final PsObject psObject;
    private final Stage stage = new Stage(new FitViewport(800, 480));
    private final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    private final Table tableMain = new Table();
    private final Table tableLeft = new Table();
    private final Table tableRight = new Table();
    private final Table tableRightHeader = new Table();
    private final Table tableRightContent = new Table();
    private final Table tableRightContentAbilities = new Table();
    private final Table tableRightContentRatingBtns = new Table();
    private final Table tableRightContentRating = new Table();
    private final Table tableFriendsControl = new Table();
    private final Dialog buyAbilitiesDialog;
    private final Dialog moreCrystalsDialog;
    private final Dialog incomingDialog;
    private final DialogInvite inviteDialog;
    private final DialogInfo infoDialog;
    private final List<String> lstHistory = new List<String>(skin, "default");
    private final List<String> lstFriends = new List<String>(skin, "default");
    private final ScrollPane lstHistoryScroll = new ScrollPane(lstHistory, skin, "default");
    private final ScrollPane lstFriendsScroll = new ScrollPane(lstFriends, skin, "default");

    private final Map<Model.Ability, ImageButton> abilities = new HashMap<Model.Ability, ImageButton>(10);
    private final Array<Label> ratingLabels = new Array<Label>(4 * (Model.RATINGS_COUNT + 1));

    private enum CurDisplayMode {Info, Rating, History, Friends}

    private final TextField txtEnemyName = new TextField("", skin, "default");
    private final TextField txtFriendName = new TextField("", skin, "default");
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
                inviteDialog.setArguments(DialogInvite.InviteType.Random, "").show(stage);
            }
        });
    }};
    private final TextButton btnInviteLatest = new TextButton("Latest opponent", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                inviteDialog.setArguments(DialogInvite.InviteType.Latest, "").show(stage);
            }
        });
    }};
    private final TextButton btnInviteByNameOk = new TextButton("OK", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = txtEnemyName.getText();
                if (!name.isEmpty()) {
                    inviteDialog.setArguments(DialogInvite.InviteType.ByName, name).show(stage);
                    rebuildLeftTable(false);
                }
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
    private final TextButton btnBuyAbilities = new TextButton("Buy abilities", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buyAbilitiesDialog.show(stage);
            }
        });
    }};
    private final TextButton btnGeneralRating = new TextButton("General Rating", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.getRating(Model.RatingType.General);
            }
        });
    }};
    private final TextButton btnWeeklyRating = new TextButton("Weekly Rating", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.getRating(Model.RatingType.Weekly);
            }
        });
    }};
    private final TextButton btnAddFriend = new TextButton("Add new", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildFriends(true);
            }
        });
    }};
    private final TextButton btnAddFriendOk = new TextButton("OK", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                model.addFriend(txtFriendName.getText());
                rebuildFriends(false);
            }
        });
    }};
    private final TextButton btnAddFriendCancel = new TextButton("Cancel", skin, "default") {{
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildFriends(false);
            }
        });
    }};
    private final LinkedLabel lblMore = new LinkedLabel("Get ", "more crystals", "", skin, "default", new Runnable() {
        @Override
        public void run() {
            moreCrystalsDialog.show(stage);
        }
    });
    private final Label lblName = new Label("aaa", skin, "default");
    private final Label lblCrystalsHeader = new Label("Crystals:", skin, "default");
    private final Label lblCrystalsData = new Label("", skin, "default");
    private final Label lblAbilities = new Label("Abilities:", skin, "default");

    private long generalRatingTime = 0;
    private long weeklyRatingTime = 0;
    private long inviteTime = 0;
    private long rejectedTime = 0;
    private long missedTime = 0;

    public ScreenMain(RushClient game, Model model, PsObject psObject) {
        assert game != null && model != null;
        this.game = game;
        this.model = model;
        this.psObject = psObject; // may be NULL

        buyAbilitiesDialog = new DialogBuyAbilities(model, skin, "default");
        moreCrystalsDialog = new DialogMoreCrystals(skin, "default");
        incomingDialog = new DialogIncoming(model, skin, "default");
        inviteDialog = new DialogInvite(model, skin, "default");
        infoDialog = new DialogInfo("Information", skin, "default");

        tableMain.setFillParent(true);
        stage.addActor(tableMain);

        loadTextures();
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
        lblCrystalsData.setText(String.valueOf(model.crystals));

        updateAbilities();
        updateRatings();
        updateFriends();
        updateInvite();
        updateRefused();

        if (model.field != null)
            game.setNextScreen();

        // checking BACK and MENU buttons on Android
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            if (psObject != null)
                psObject.hide();
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

        for (ImageButton button : abilities.values()) {
            assert button.getStyle() != null;
            Drawable drawable = button.getStyle().imageUp;
            if (drawable != null && drawable instanceof TextureRegionDrawable)
                ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose(); // no NULL references here
        }
    }

    private void loadTextures() {
        TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));

        for (final Model.Ability ability : Model.Ability.values()) {
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                ImageButton imageButton = new ImageButton(new TextureRegionDrawable(region));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        ;
                    }
                });
                abilities.put(ability, imageButton);
            }
        }
    }

    private void initTables() {
        tableMain.add(tableLeft).pad(20);
        tableMain.add(tableRight).pad(20).expand().fill();

        tableRight.add(tableRightHeader);
        tableRight.row();
        tableRight.add(tableRightContent).expand().fill();

        tableRightHeader.row().width(90).space(20);
        tableRightHeader.add(btnInfo);
        tableRightHeader.add(btnRating);
        tableRightHeader.add(btnHistory);
        tableRightHeader.add(btnFriends);

        tableRightContentRatingBtns.row().spaceLeft(30);
        tableRightContentRatingBtns.add(btnGeneralRating);
        tableRightContentRatingBtns.add(btnWeeklyRating);

        tableRightContentRating.row().spaceLeft(20).spaceBottom(20); // don't use space(), it breaks the layout
        tableRightContentRating.add(new Label("Name", skin, "default"));
        tableRightContentRating.add(new Label("Wins", skin, "default"));
        tableRightContentRating.add(new Label("Losses", skin, "default"));
        tableRightContentRating.add(new Label("Score diff", skin, "default"));
        for (int i = 0; i < Model.RATINGS_COUNT; i++) {
            tableRightContentRating.row().spaceLeft(20);
            for (int j = 0; j < 4; j++) { // 4 = columns count
                Label label = new Label("", skin, "default");
                tableRightContentRating.add(label);
                ratingLabels.add(label);
            }
        }
        tableRightContentRating.row().spaceLeft(20);
        tableRightContentRating.add(new Label("...", skin, "default")).colspan(4);
        tableRightContentRating.row().spaceLeft(20);
        for (int j = 0; j < 4; j++) { // 4 = columns count
            Label label = new Label("", skin, "default");
            tableRightContentRating.add(label);
            ratingLabels.add(label);
        }
    }

    private void rebuildLeftTable(boolean showInputName) {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android
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
                tableRightContent.add(lblName).colspan(2).expand();
                tableRightContent.row().expand();
                tableRightContent.add(lblCrystalsHeader);
                tableRightContent.add(lblCrystalsData);
                tableRightContent.row().expand();
                tableRightContent.add(lblAbilities);
                tableRightContent.add(tableRightContentAbilities);
                tableRightContent.row().expand();
                tableRightContent.add(btnBuyAbilities).colspan(2);
                tableRightContent.row().expand();
                tableRightContent.add(lblMore).colspan(2);
                break;
            case Rating:
                tableRightContent.add(tableRightContentRatingBtns).pad(15);
                tableRightContent.row();
                tableRightContent.add(tableRightContentRating).expand();
                model.getRating(Model.RatingType.General);
                break;
            case History:
                lstHistory.setItems(model.history.toArray(new String[0]));
                tableRightContent.add(lstHistoryScroll).fill(.9f, .9f).expand();
                break;
            case Friends:
                tableRightContent.add(tableFriendsControl).pad(15);
                tableRightContent.row();
                tableRightContent.add(lstFriendsScroll).fill(.9f, .9f).expand();
                rebuildFriends(false);
                model.getFriends();
                break;
            default:
        }
    }

    private void rebuildFriends(boolean showInputName) {
        tableFriendsControl.clear();

        if (showInputName) {
            tableFriendsControl.add(txtFriendName).width(180).colspan(2);
            tableFriendsControl.row().space(10);
            tableFriendsControl.add(btnAddFriendOk).width(70);
            tableFriendsControl.add(btnAddFriendCancel).width(100);
        } else {
            tableFriendsControl.add(btnAddFriend);
        }
    }

    private void updateAbilities() {
        if (model.abilityExpireTime.size() != tableRightContentAbilities.getColumns()) {
            tableRightContentAbilities.clear();
            for (Map.Entry<Model.Ability, Integer> entry : model.abilityExpireTime.entrySet()) {
                ImageButton btn = abilities.get(entry.getKey());
                if (btn != null) {
                    tableRightContentAbilities.add(btn).space(10);
                }
            }
        }
    }

    private void updateRatings() {
        if (generalRatingTime != model.generalRatingTime) {
            generalRatingTime = model.generalRatingTime;
            updateRating(Model.RatingType.General);
        } else if (weeklyRatingTime != model.weeklyRatingTime) {
            weeklyRatingTime = model.weeklyRatingTime;
            updateRating(Model.RatingType.Weekly);
        }
    }

    private void updateRating(Model.RatingType type) {
        for (Label label : ratingLabels) {
            label.setText("");
        }

        Collection<RatingItem> items = type == Model.RatingType.General ? model.generalRating : model.weeklyRating;
        int i = 0;
        for (RatingItem item : items) {
            if (i + 3 < ratingLabels.size) {
                ratingLabels.get(i++).setText(item.name);
                ratingLabels.get(i++).setText(String.valueOf(item.victories));
                ratingLabels.get(i++).setText(String.valueOf(item.defeats));
                ratingLabels.get(i++).setText(String.valueOf(item.score_diff));
            }
        }
    }

    private void updateFriends() {
        if (lstFriends.getItems().size != model.friends.size())
            lstFriends.setItems(model.friends.toArray(new String[0]));
    }

    private void updateInvite() {
        if (inviteTime != model.inviteTime) {
            inviteTime = model.inviteTime;
            incomingDialog.show(stage);
        }
    }

    private void updateRefused() {
        if (rejectedTime != model.refusedRejectedTime) {
            rejectedTime = model.refusedRejectedTime;
            infoDialog.setText(String.format("%s rejected your invitation", model.enemy)).show(stage);
        }
        if (missedTime != model.refusedMissedTime) {
            missedTime = model.refusedMissedTime;
            infoDialog.setText(String.format("%s missed your invitation", model.enemy)).show(stage);
        }
    }
}
