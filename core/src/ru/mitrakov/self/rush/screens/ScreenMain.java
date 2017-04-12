package ru.mitrakov.self.rush.screens;

import java.util.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;


/**
 * Created by mitrakov on 03.03.2017
 */
public class ScreenMain extends LocalizableScreen {
    private final TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));
    private final TextureAtlas atlasMenu = new TextureAtlas(Gdx.files.internal("pack/menu.pack"));
    private final Table tableLeft = new Table();
    private final Table tableRight = new Table();
    private final Table tableLeftInvite = new Table();
    private final Table tableLeftToolbar = new Table();
    private final Table tableRightHeader = new Table();
    private final Table tableRightContent = new Table();
    private final Table tableRightContentAbilities = new Table();
    private final Table tableRightContentRatingBtns = new Table();
    private final Table tableRightContentRating = new Table();
    private final Table tableFriendsControl = new Table();
    private final DialogPromocode promocodeDialog;
    private final DialogFeat moreCrystalsDialog;
    private final DialogFeat incomingDialog;
    private final DialogFeat settingsDialog;
    private final DialogFeat aboutDialog;
    private final DialogBuyAbilities buyAbilitiesDialog;
    private final DialogInfo infoDialog;
    private final DialogDialup dialupDialog;
    private final DialogInvite inviteDialog;
    private final DialogQuestion questionDialog;
    private final DialogFriends friendsDialog;
    private final DialogPromocodeDone promocodeDoneDialog;
    private final DialogConnect connectingDialog;
    private final List<String> lstHistory;
    private final List<String> lstFriends;
    private final ScrollPane lstHistoryScroll;
    private final ScrollPane lstFriendsScroll;
    private final ScrollPane tableRightContentAbilitiesScroll;
    private final TextField txtEnemyName;
    private final TextField txtFriendName;
    private final TextButton btnInviteByName;
    private final TextButton btnInviteRandom;
    private final TextButton btnInviteLatest;
    private final TextButton btnInviteByNameOk;
    private final TextButton btnInviteByNameCancel;
    private final Button btnSettings;
    private final Button btnAbout;
    private final TextButton btnInfo;
    private final TextButton btnRating;
    private final TextButton btnHistory;
    private final TextButton btnFriends;
    private final TextButton btnBuyAbilities;
    private final TextButton btnGeneralRating;
    private final TextButton btnWeeklyRating;
    private final TextButton btnAddFriend;
    private final TextButton btnAddFriendOk;
    private final TextButton btnAddFriendCancel;
    private final LinkedLabel lblMore;
    private final Label lblName;
    private final Label lblCrystalsHeader;
    private final Label lblCrystalsData;
    private final Label lblAbilities;
    private final Label lblAbilityExpireTime;
    private final Label lblRatingName;
    private final Label lblRatingWins;
    private final Label lblRatingLosses;
    private final Label lblRatingScoreDiff;
    private final Label lblRatingDots;

    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);
    private final Array<Label> ratingLabels = new Array<Label>(4 * (Model.RATINGS_COUNT + 1));

    private enum CurDisplayMode {Info, Rating, History, Friends}

    private I18NBundle i18n;
    private long generalRatingTime = 0;
    private long weeklyRatingTime = 0;
    private long inviteTime = 0;
    private long stopCallRejectedTime = 0;
    private long stopCallMissedTime = 0;
    private long stopCallExpiredTime = 0;
    private long friendsListTime = 0;
    private long abilityExpireTime = 0;
    private long promocodeDoneTime = 0;

    public ScreenMain(RushClient game, final Model model, PsObject psObject, Skin skin, AudioManager audioManager,
                      I18NBundle i18nArg) {
        super(game, model, psObject);
        assert skin != null && audioManager != null && i18nArg != null;
        i18n = i18nArg;

        TextureRegion regionSettings = atlasMenu.findRegion("settings");
        TextureRegion regionAbout = atlasMenu.findRegion("about");
        assert regionSettings != null && regionAbout != null;

        promocodeDialog = new DialogPromocode(model, skin, "default");
        moreCrystalsDialog = new DialogMoreCrystals(skin, "default", promocodeDialog, stage);
        incomingDialog = new DialogIncoming(model, skin, "default", audioManager, i18n);
        settingsDialog = new DialogSettings(game, model, skin, "default", audioManager);
        aboutDialog = new DialogAbout(skin, "default");
        buyAbilitiesDialog = new DialogBuyAbilities(model, skin, "default", audioManager);
        infoDialog = new DialogInfo("", skin, "default");
        dialupDialog = new DialogDialup(model, skin, "default", i18n);
        inviteDialog = new DialogInvite(model, skin, "default", dialupDialog, stage, i18n);
        questionDialog = new DialogQuestion("", skin, "default");
        friendsDialog = new DialogFriends(model, skin, "default", inviteDialog, questionDialog, stage, audioManager);
        promocodeDoneDialog = new DialogPromocodeDone(skin, "default");
        connectingDialog = new DialogConnect(skin, "default", stage);
        lstHistory = new List<String>(skin, "default");
        lstFriends = new List<String>(skin, "default") {{
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    assert i18n != null;
                    String friend = lstFriends.getSelected();
                    if (friend != null)
                        friendsDialog.setFriend(friend, i18n).show(stage);
                }
            });
        }};
        lstHistoryScroll = new ScrollPane(lstHistory, skin, "default");
        lstFriendsScroll = new ScrollPane(lstFriends, skin, "default");
        tableRightContentAbilitiesScroll = new ScrollPane(tableRightContentAbilities);
        txtEnemyName = new TextField("", skin, "default");
        txtFriendName = new TextField("", skin, "default");
        btnInviteByName = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildLeftTable(true);
                }
            });
        }};
        btnInviteRandom = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    inviteDialog.setArguments(DialogInvite.InviteType.Random, "").show(stage);
                }
            });
        }};
        btnInviteLatest = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    inviteDialog.setArguments(DialogInvite.InviteType.Latest, "").show(stage);
                }
            });
        }};
        btnInviteByNameOk = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = txtEnemyName.getText();
                    if (name.length() > 0) { // use 'length() > 0' instead of 'isEmpty()' (Android API 8)
                        inviteDialog.setArguments(DialogInvite.InviteType.ByName, name).show(stage);
                        rebuildLeftTable(false);
                    }
                }
            });
        }};
        btnInviteByNameCancel = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildLeftTable(false);
                }
            });
        }};
        btnSettings = new ImageButtonFeat(new TextureRegionDrawable(regionSettings), audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    settingsDialog.show(stage);
                }
            });
        }};
        btnAbout = new ImageButtonFeat(new TextureRegionDrawable(regionAbout), audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    aboutDialog.show(stage);
                }
            });
        }};
        btnInfo = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildRightTable(CurDisplayMode.Info);
                }
            });
        }};
        btnRating = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildRightTable(CurDisplayMode.Rating);
                }
            });
        }};
        btnHistory = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildRightTable(CurDisplayMode.History);
                }
            });
        }};
        btnFriends = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildRightTable(CurDisplayMode.Friends);
                }
            });
        }};
        btnBuyAbilities = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    buyAbilitiesDialog.show(stage);
                }
            });
        }};
        btnGeneralRating = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.getRating(Model.RatingType.General);
                }
            });
        }};
        btnWeeklyRating = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.getRating(Model.RatingType.Weekly);
                }
            });
        }};
        btnAddFriend = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildFriends(true);
                }
            });
        }};
        btnAddFriendOk = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    model.addFriend(txtFriendName.getText());
                    rebuildFriends(false);
                }
            });
        }};
        btnAddFriendCancel = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rebuildFriends(false);
                }
            });
        }};
        lblMore = new LinkedLabel("", "", "", skin, "default", new Runnable() {
            @Override
            public void run() {
                moreCrystalsDialog.show(stage);
            }
        });
        lblName = new Label("", skin, "default");
        lblCrystalsHeader = new Label("", skin, "default");
        lblCrystalsData = new Label("", skin, "default");
        lblAbilities = new Label("", skin, "default");
        lblAbilityExpireTime = new Label("", skin, "default");
        lblRatingName = new Label("", skin, "default");
        lblRatingWins = new Label("", skin, "default");
        lblRatingLosses = new Label("", skin, "default");
        lblRatingScoreDiff = new Label("", skin, "default");
        lblRatingDots = new Label("", skin, "default");

        loadTextures(audioManager);
        initTables(skin);
        rebuildLeftTable(false);
        rebuildRightTable(CurDisplayMode.Info);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        lblName.setText(model.name); // if text is not changed, setText just returns
        lblCrystalsData.setText(String.valueOf(model.crystals));

        connectingDialog.setVisible(!model.connected);

        // updating internal state
        updateInvite();
        updateRatings();
        updateFriends();
        updateStopCall();
        updateAbilities();
        updatePromocodeDone();

        // changing screens
        if (!model.authorized)
            game.setLoginScreen();
        if (model.field != null) {
            dialupDialog.hide();
            game.setNextScreen();
        }

        // checking MENU button on Android
        if (Gdx.input.isKeyJustPressed(Input.Keys.MENU))
            if (psObject != null)
                settingsDialog.show(stage);
    }

    @Override
    public void show() {
        super.show();

        // we should update our timestamps because some model's timestamps may be changed off-stage (e.g. during battle)
        // be careful! it usually concerns dialogs (to avoid their showing) so not all timestamps should be updated here
        inviteTime = model.inviteTime;
        stopCallRejectedTime = model.stopCallRejectedTime;
        stopCallMissedTime = model.stopCallMissedTime;
        stopCallExpiredTime = model.stopCallExpiredTime;
        promocodeDoneTime = model.promocodeDoneTime;
    }

    @Override
    public void dispose() {
        atlasAbility.dispose(); // disposing an atlas also disposes all its internal textures
        atlasMenu.dispose();
        buyAbilitiesDialog.dispose();
        super.dispose();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        promocodeDialog.onLocaleChanged(bundle);
        questionDialog.onLocaleChanged(bundle);
        moreCrystalsDialog.onLocaleChanged(bundle);
        incomingDialog.onLocaleChanged(bundle);
        settingsDialog.onLocaleChanged(bundle);
        aboutDialog.onLocaleChanged(bundle);
        buyAbilitiesDialog.onLocaleChanged(bundle);
        infoDialog.onLocaleChanged(bundle);
        dialupDialog.onLocaleChanged(bundle);
        inviteDialog.onLocaleChanged(bundle);
        friendsDialog.onLocaleChanged(bundle);
        promocodeDoneDialog.onLocaleChanged(bundle);
        connectingDialog.onLocaleChanged(bundle);

        if (infoDialog.getTitleLabel() != null)
            infoDialog.getTitleLabel().setText(bundle.format("dialog.info"));
        if (questionDialog.getTitleLabel() != null)
            questionDialog.getTitleLabel().setText(bundle.format("dialog.question"));

        btnInviteByName.setText(bundle.format("opponent.find"));
        btnInviteRandom.setText(bundle.format("opponent.random"));
        btnInviteLatest.setText(bundle.format("opponent.latest"));
        btnInviteByNameOk.setText(bundle.format("ok"));
        btnInviteByNameCancel.setText(bundle.format("cancel"));
        btnInfo.setText(bundle.format("info.header"));
        btnRating.setText(bundle.format("rating.header"));
        btnHistory.setText(bundle.format("history.header"));
        btnFriends.setText(bundle.format("friends.header"));
        btnBuyAbilities.setText(bundle.format("abilities.buy"));
        btnGeneralRating.setText(bundle.format("rating.general"));
        btnWeeklyRating.setText(bundle.format("rating.weekly"));
        btnAddFriend.setText(bundle.format("friends.add"));
        btnAddFriendOk.setText(bundle.format("ok"));
        btnAddFriendCancel.setText(bundle.format("cancel"));
        lblMore.setText(bundle.format("dialog.crystals.start"), bundle.format("dialog.crystals.link"), "");
        lblCrystalsHeader.setText(bundle.format("info.crystals"));
        lblAbilities.setText(bundle.format("info.abilities"));
        lblRatingName.setText(bundle.format("rating.name"));
        lblRatingWins.setText(bundle.format("rating.wins"));
        lblRatingLosses.setText(bundle.format("rating.losses"));
        lblRatingScoreDiff.setText(bundle.format("rating.score.diff"));
        lblRatingDots.setText(bundle.format("rating.dots"));
    }

    private void loadTextures(AudioManager audioManager) {
        for (final Model.Ability ability : Model.Ability.values()) {
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                ImageButton imageButton = new ImageButtonFeat(new TextureRegionDrawable(region), audioManager) {{
                    addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            assert i18n != null;
                            Integer minutes = model.abilityExpireMap.get(ability); // count of minutes at INITIAL time!
                            if (minutes != null) {
                                long minLeft = minutes - (TimeUtils.millis() - model.abilityExpireTime) / 60000;
                                if (minLeft < 0) // if server's expire checking period is too large, value may be < 0
                                    minLeft = 0;
                                lblAbilityExpireTime.setText(i18n.format("abilities.time", minLeft / 60, minLeft % 60));
                                lblAbilityExpireTime.clearActions();
                                lblAbilityExpireTime.addAction(sequence(fadeIn(.1f), Actions.show(),
                                        fadeOut(2, Interpolation.fade), Actions.hide()));
                            }
                        }
                    });
                }};
                abilities.put(ability, imageButton);
            }
        }
    }

    private void initTables(Skin skin) {
        table.add(tableLeft).pad(20).fill();
        table.add(tableRight).pad(20).expand().fill();

        tableLeft.add(tableLeftInvite).expand().fill();
        tableLeft.row();
        tableLeft.add(tableLeftToolbar);

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
        tableRightContentRating.add(lblRatingName);
        tableRightContentRating.add(lblRatingWins);
        tableRightContentRating.add(lblRatingLosses);
        tableRightContentRating.add(lblRatingScoreDiff);

        final int RATING_COLUMNS = 4;
        for (int i = 0; i < Model.RATINGS_COUNT; i++) {
            tableRightContentRating.row().spaceLeft(20);
            for (int j = 0; j < RATING_COLUMNS; j++) {
                Label label = new Label("", skin, "default");
                tableRightContentRating.add(label);
                ratingLabels.add(label);
            }
        }
        tableRightContentRating.row().spaceLeft(20);
        tableRightContentRating.add(lblRatingDots).colspan(4);
        tableRightContentRating.row().spaceLeft(20);
        for (int j = 0; j < RATING_COLUMNS; j++) {
            Label label = new Label("", skin, "default");
            tableRightContentRating.add(label);
            ratingLabels.add(label);
        }
    }

    private void rebuildLeftTable(boolean showInputName) {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android (shown in "invite enemy by name")
        tableLeftInvite.clear();

        // ...
        if (showInputName) {
            tableLeftInvite.add(txtEnemyName).colspan(2).width(145).height(50);
            tableLeftInvite.row().space(20);
            tableLeftInvite.add(btnInviteByNameOk).width(60).height(40);
            tableLeftInvite.add(btnInviteByNameCancel).width(85).height(40);
            tableLeftInvite.row().space(20);
            tableLeftInvite.add(btnInviteRandom).colspan(2).width(165).height(80);
            tableLeftInvite.row().space(20);
            tableLeftInvite.add(btnInviteLatest).colspan(2).width(165).height(80);
        } else {
            tableLeftInvite.add(btnInviteByName).width(165).height(80);
            tableLeftInvite.row().space(20);
            tableLeftInvite.add(btnInviteRandom).width(165).height(80);
            tableLeftInvite.row().space(20);
            tableLeftInvite.add(btnInviteLatest).width(165).height(80);
        }

        tableLeftToolbar.add(btnSettings).spaceRight(30);
        tableLeftToolbar.add(btnAbout);
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
                tableRightContent.add(tableRightContentAbilitiesScroll).pad(15);
                tableRightContent.row().expandX();
                tableRightContent.add();
                tableRightContent.add(lblAbilityExpireTime);
                tableRightContent.row().expand();
                tableRightContent.add(btnBuyAbilities).colspan(2);
                tableRightContent.row().expand();
                tableRightContent.add(lblMore).colspan(2);
                break;
            case Rating:
                tableRightContent.add(tableRightContentRatingBtns).pad(15);
                tableRightContent.row();
                tableRightContent.add(tableRightContentRating).expand();
                model.getRating(Model.RatingType.General); // we should requery rating each time we choose the tab,
                break;                                     // because it might be updated on the server
            case History:
                lstHistory.setItems(model.history.toArray(new String[0]));
                tableRightContent.add(lstHistoryScroll).fill(.9f, .9f).expand();
                break;
            case Friends:
                tableRightContent.add(tableFriendsControl).pad(15);
                tableRightContent.row();
                tableRightContent.add(lstFriendsScroll).fill(.9f, .9f).expand();
                rebuildFriends(false);
                model.getFriends(); // we should requery friends each time we choose the tab, because friends may be
                break;              // added from different places
            default:
        }
    }

    private void rebuildFriends(boolean showInputName) {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android (shown in "add friend")
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
        if (abilityExpireTime != model.abilityExpireTime) {
            abilityExpireTime = model.abilityExpireTime;
            tableRightContentAbilities.clear();
            // we sort abilities (via TreeSet), because since 2017.04.03 'model.abilityExpireMap' is not SkipListMap
            for (Model.Ability ability : new TreeSet<Model.Ability>(model.abilityExpireMap.keySet())) {
                Button btn = abilities.get(ability);
                if (btn != null)
                    tableRightContentAbilities.add(btn).space(10);
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
        if (friendsListTime != model.friendsListTime) {
            friendsListTime = model.friendsListTime;
            lstFriends.setItems(model.friends.toArray(new String[0]));
        }
    }

    private void updateInvite() {
        if (inviteTime != model.inviteTime) {
            inviteTime = model.inviteTime;
            incomingDialog.show(stage);
        }
    }

    private void updatePromocodeDone() {
        if (promocodeDoneTime != model.promocodeDoneTime) {
            promocodeDoneTime = model.promocodeDoneTime;
            promocodeDoneDialog.setArguments(model.promocodeDoneName, model.promocodeDoneInviter,
                    model.promocodeDoneCrystals, i18n).show(stage);
        }
    }

    private void updateStopCall() {
        assert i18n != null;
        if (stopCallRejectedTime != model.stopCallRejectedTime) {
            stopCallRejectedTime = model.stopCallRejectedTime;
            dialupDialog.hide();
            infoDialog.setText(i18n.format("stopcall.rejected", model.enemy)).show(stage);
        }
        if (stopCallMissedTime != model.stopCallMissedTime) {
            stopCallMissedTime = model.stopCallMissedTime;
            incomingDialog.hide();
            infoDialog.setText(i18n.format("stopcall.missed", model.enemy)).show(stage);
        }
        if (stopCallExpiredTime != model.stopCallExpiredTime) {
            stopCallExpiredTime = model.stopCallExpiredTime;
            dialupDialog.hide();
            infoDialog.setText(i18n.format("stopcall.expired", model.enemy)).show(stage);
        }
    }
}
