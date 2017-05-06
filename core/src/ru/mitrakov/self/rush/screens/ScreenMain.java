package ru.mitrakov.self.rush.screens;

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
    private final TextureAtlas atlasIcons = new TextureAtlas(Gdx.files.internal("pack/icons.pack"));
    private final Table tableLeft = new Table();
    private final Table tableRight;
    private final Table tableLeftHeader = new Table();
    private final Table tableLeftInvite = new Table();
    private final Table tableLeftToolbar = new Table();
    private final Table tableRightHeader = new Table();
    private final Table tableRightContent = new Table();
    private final Table tableRightContentName = new Table();
    private final Table tableRightContentAbilities = new Table();
    private final Table tableRightContentRatingBtns = new Table();
    private final Table tableRightContentRating = new Table();
    private final Table tableFriendsControl = new Table();
    private final DialogPromocode promocodeDialog;
    private final DialogFeat moreCrystalsDialog;
    private final DialogIncoming incomingDialog;
    private final DialogFeat settingsDialog;
    private final DialogFeat aboutDialog;
    private final DialogBuyAbilities buyAbilitiesDialog;
    private final DialogInfo infoDialog;
    private final DialogDialup dialupDialog;
    private final DialogInvite inviteDialog;
    private final DialogQuestion questionDialog;
    private final DialogFriends friendsDialog;
    private final DialogPromocodeDone promocodeDoneDialog;

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
    private final Button btnInviteByNameOk;
    private final Button btnInviteByNameCancel;
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
    private final Image imgCharacter;

    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);
    private final Array<Label> ratingLabels = new Array<Label>(4 * (Model.RATINGS_COUNT + 1));

    private enum CurDisplayMode {Info, Rating, History, Friends}

    private I18NBundle i18n;

    public ScreenMain(RushClient game, final Model model, PsObject psObject, Skin skin, AudioManager audioManager,
                      I18NBundle i18nArg) {
        super(game, model, psObject, skin, audioManager);
        assert i18nArg != null;
        i18n = i18nArg;

        TextureRegion regionSettings = atlasMenu.findRegion("settings");
        TextureRegion regionAbout = atlasMenu.findRegion("about");
        TextureRegion regionOk = atlasMenu.findRegion("valid");
        TextureRegion regionCancel = atlasMenu.findRegion("invalid");
        assert regionSettings != null && regionAbout != null;

        promocodeDialog = new DialogPromocode(model, skin, "default");
        moreCrystalsDialog = new DialogMoreCrystals(skin, "default", promocodeDialog, stage);
        incomingDialog = new DialogIncoming(model, skin, "default", audioManager, i18n);
        settingsDialog = new DialogSettings(game, model, skin, "default", audioManager);
        aboutDialog = new DialogAbout(skin, "default");
        buyAbilitiesDialog = new DialogBuyAbilities(model, skin, "default", audioManager, i18n);
        infoDialog = new DialogInfo("", skin, "default");
        dialupDialog = new DialogDialup(model, skin, "default", i18n);
        inviteDialog = new DialogInvite(model, skin, "default", dialupDialog, stage, i18n);
        questionDialog = new DialogQuestion("", skin, "default");
        friendsDialog = new DialogFriends(model, skin, "default", inviteDialog, questionDialog, stage, audioManager);
        promocodeDoneDialog = new DialogPromocodeDone(skin, "default");

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
        lstHistoryScroll = new ScrollPane(lstHistory, skin, "default") {{
            setupFadeScrollBars(0, 0);
        }};
        lstFriendsScroll = new ScrollPane(lstFriends, skin, "default") {{
            setupFadeScrollBars(0, 0);
        }};
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
        btnInviteByNameOk = new ImageButtonFeat(new TextureRegionDrawable(regionOk), audioManager) {{
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
        btnInviteByNameCancel = new ImageButtonFeat(new TextureRegionDrawable(regionCancel), audioManager) {{
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
        tableRight = new Table(skin);
        lblMore = new LinkedLabel("", "", "", skin, "default", new Runnable() {
            @Override
            public void run() {
                moreCrystalsDialog.show(stage);
            }
        });
        lblName = new Label("", skin, "title");
        lblCrystalsHeader = new Label("", skin, "default");
        lblCrystalsData = new Label("", skin, "default");
        lblAbilities = new Label("", skin, "default");
        lblAbilityExpireTime = new Label("", skin, "default");
        lblRatingName = new Label("", skin, "default");
        lblRatingWins = new Label("", skin, "default");
        lblRatingLosses = new Label("", skin, "default");
        lblRatingScoreDiff = new Label("", skin, "default");
        lblRatingDots = new Label("", skin, "default");
        imgCharacter = new Image();

        loadTextures();
        initTables();
        // rebuilding tables moved to 'show()'
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        lblName.setText(model.name); // if text is not changed, setText just returns
        lblCrystalsData.setText(String.valueOf(model.crystals));

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
        model.getUserInfo();
        rebuildLeftTable(false);
        rebuildRightTable(CurDisplayMode.Info);
    }

    @Override
    public void dispose() {
        atlasAbility.dispose(); // disposing an atlas also disposes all its internal textures
        atlasMenu.dispose();
        atlasIcons.dispose();
        buyAbilitiesDialog.dispose();
        super.dispose();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
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

        if (infoDialog.getTitleLabel() != null)
            infoDialog.getTitleLabel().setText(bundle.format("dialog.info"));
        if (questionDialog.getTitleLabel() != null)
            questionDialog.getTitleLabel().setText(bundle.format("dialog.question"));

        btnInviteByName.setText(bundle.format("opponent.find"));
        btnInviteRandom.setText(bundle.format("opponent.random"));
        btnInviteLatest.setText(bundle.format("opponent.latest"));
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

        rebuildRightTable(CurDisplayMode.Info); // forward user to the main tab (to avoid artifacts, e.g. in History)
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        assert i18n != null;
        if (event instanceof EventBus.RatingUpdatedEvent) {
            EventBus.RatingUpdatedEvent ev = (EventBus.RatingUpdatedEvent) event;
            updateRating(ev.items);
        }
        if (event instanceof EventBus.FriendListUpdatedEvent) {
            EventBus.FriendListUpdatedEvent ev = (EventBus.FriendListUpdatedEvent) event;
            lstFriends.setItems(ev.items.toArray(new String[0]));
        }
        if (event instanceof EventBus.FriendAddedEvent) {
            EventBus.FriendAddedEvent ev = (EventBus.FriendAddedEvent) event;
            Array<String> items = lstFriends.getItems();
            items.add(ev.name);
            lstFriends.setItems(items.toArray()); // toArray() is NEEDED! otherwise setItems() misbehaves
        }
        if (event instanceof EventBus.FriendRemovedEvent) {
            EventBus.FriendRemovedEvent ev = (EventBus.FriendRemovedEvent) event;
            Array<String> items = lstFriends.getItems();
            items.removeValue(ev.name, false);
            lstFriends.setItems(items.toArray()); // toArray() is NEEDED! otherwise setItems() misbehaves
        }
        if (event instanceof EventBus.AbilitiesUpdatedEvent) {
            EventBus.AbilitiesUpdatedEvent ev = (EventBus.AbilitiesUpdatedEvent) event;
            updateAbilities(ev.items);
        }
        if (event instanceof EventBus.InviteEvent) {
            EventBus.InviteEvent ev = (EventBus.InviteEvent) event;
            incomingDialog.setArguments(ev.enemy, ev.enemySid).show(stage);
        }
        if (event instanceof EventBus.PromocodeDoneEvent) {
            EventBus.PromocodeDoneEvent ev = (EventBus.PromocodeDoneEvent) event;
            promocodeDoneDialog.setArguments(ev.name, ev.inviter, ev.crystals, i18n).show(stage);
        }
        if (event instanceof EventBus.StopCallRejectedEvent) {
            EventBus.StopCallRejectedEvent ev = (EventBus.StopCallRejectedEvent) event;
            dialupDialog.hide();
            infoDialog.setText(i18n.format("stopcall.rejected", ev.cowardName)).show(stage);
        }
        if (event instanceof EventBus.StopCallMissedEvent) {
            EventBus.StopCallMissedEvent ev = (EventBus.StopCallMissedEvent) event;
            incomingDialog.hide();
            infoDialog.setText(i18n.format("stopcall.missed", ev.aggressorName)).show(stage);
        }
        if (event instanceof EventBus.StopCallExpiredEvent) {
            EventBus.StopCallExpiredEvent ev = (EventBus.StopCallExpiredEvent) event;
            dialupDialog.hide();
            infoDialog.setText(i18n.format("stopcall.expired", ev.defenderName)).show(stage);
        }
        if (event instanceof EventBus.AddFriendErrorEvent)
            infoDialog.setText(i18n.format("dialog.info.add.friend.error")).show(stage);
        if (event instanceof EventBus.NoCrystalsEvent)
            infoDialog.setText(i18n.format("dialog.info.no.crystals")).show(stage);
        if (event instanceof EventBus.AggressorBusyEvent) {
            dialupDialog.hide();
            infoDialog.setText(i18n.format("dialog.info.aggressor.busy")).show(stage);
        }
        if (event instanceof EventBus.DefenderBusyEvent) {
            dialupDialog.hide();
            infoDialog.setText(i18n.format("dialog.info.busy")).show(stage);
        }
        if (event instanceof EventBus.EnemyNotFoundEvent) {
            dialupDialog.hide();
            infoDialog.setText(i18n.format("dialog.info.no.enemy")).show(stage);
        }
        if (event instanceof EventBus.NoFreeUsersEvent) {
            dialupDialog.hide();
            infoDialog.setText(i18n.format("dialog.info.no.free.users")).show(stage);
        }
        if (event instanceof EventBus.AttackedYourselfEvent) {
            dialupDialog.hide();
            infoDialog.setText(i18n.format("dialog.info.yourself")).show(stage);
        }
        if (event instanceof EventBus.CharacterChangedEvent) {
            EventBus.CharacterChangedEvent ev = (EventBus.CharacterChangedEvent) event;
            imgCharacter.setDrawable(new TextureRegionDrawable(atlasIcons.findRegion(ev.character + "64")));
        }
    }

    private void loadTextures() {
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

    private void initTables() {
        table.add(tableLeft).pad(4).width(222).fill();
        table.add(tableRight).pad(4).expand().fill();

        tableLeft.add(tableLeftHeader).height(90).fill();
        tableLeft.row();
        tableLeft.add(tableLeftInvite).expand().fill();
        tableLeft.row();
        tableLeft.add(tableLeftToolbar).height(64).fill();

        tableRight.setBackground("panel-maroon");
        tableRight.add(tableRightHeader).padTop(16).height(55).fill();
        tableRight.row();
        tableRight.add(tableRightContent).expand().fill();

        tableRightHeader.add(btnInfo)   .width(138).expand().fill();
        tableRightHeader.add(btnRating) .width(138).expand().fill();
        tableRightHeader.add(btnHistory).width(138).expand().fill();
        tableRightHeader.add(btnFriends).width(138).expand().fill();

        tableRightContentName.add(imgCharacter).spaceLeft(20);
        tableRightContentName.add(lblName).spaceLeft(20);

        tableRightContentRatingBtns.row().spaceLeft(30);
        tableRightContentRatingBtns.add(btnGeneralRating);
        tableRightContentRatingBtns.add(btnWeeklyRating);

        tableRightContentRating.row().spaceLeft(20);
        tableRightContentRating.add(lblRatingName);
        tableRightContentRating.add(lblRatingWins);
        tableRightContentRating.add(lblRatingLosses);
        tableRightContentRating.add(lblRatingScoreDiff);

        final int RATING_COLUMNS = 4;
        tableRightContentRating.row();
        tableRightContentRating.add(new Image(skin, "splitpane")).colspan(RATING_COLUMNS).width(351).height(2);

        for (int i = 0; i < Model.RATINGS_COUNT; i++) {
            tableRightContentRating.row().spaceLeft(20);
            for (int j = 0; j < RATING_COLUMNS; j++) {
                Label label = new Label("", skin, "small");
                tableRightContentRating.add(label);
                ratingLabels.add(label);
            }
        }
        tableRightContentRating.row().spaceLeft(20);
        tableRightContentRating.add(lblRatingDots).colspan(4);
        tableRightContentRating.row().spaceLeft(20);
        for (int j = 0; j < RATING_COLUMNS; j++) {
            Label label = new Label("", skin, "small");
            tableRightContentRating.add(label);
            ratingLabels.add(label);
        }
    }

    private void rebuildLeftTable(boolean showInputName) {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android (shown in "invite enemy by name")
        tableLeftInvite.clear();

        // ...
        if (showInputName) {
            tableLeftInvite.add(txtEnemyName).colspan(2).width(190).height(50);
            tableLeftInvite.row().spaceTop(16);
            tableLeftInvite.add(btnInviteByNameOk);
            tableLeftInvite.add(btnInviteByNameCancel);
            tableLeftInvite.row().spaceTop(16);
            tableLeftInvite.add(btnInviteRandom).colspan(2).width(190).height(73);
            tableLeftInvite.row().spaceTop(16);
            tableLeftInvite.add(btnInviteLatest).colspan(2).width(190).height(73);
            stage.setKeyboardFocus(txtEnemyName);
        } else {
            tableLeftInvite.add(btnInviteByName).width(190).height(73).spaceTop(16);
            tableLeftInvite.row();
            tableLeftInvite.add(btnInviteRandom).width(190).height(73).spaceTop(16);
            tableLeftInvite.row();
            tableLeftInvite.add(btnInviteLatest).width(190).height(73).spaceTop(16);
        }

        tableLeftToolbar.add(btnSettings).spaceRight(30);
        tableLeftToolbar.add(btnAbout);
    }

    private void rebuildRightTable(CurDisplayMode mode) {
        tableRightContent.clear();

        switch (mode) {
            case Info:
                tableRightContent.add(tableRightContentName).colspan(2).height(72);
                tableRightContent.row();
                tableRightContent.add(lblCrystalsHeader);
                tableRightContent.add(lblCrystalsData);
                tableRightContent.row().spaceTop(16);
                tableRightContent.add(lblAbilities);
                tableRightContent.add(tableRightContentAbilitiesScroll).pad(4).expandX().fill();
                tableRightContent.row();
                tableRightContent.add().width(217);
                tableRightContent.add(lblAbilityExpireTime);
                tableRightContent.row();
                tableRightContent.add().expandY();
                tableRightContent.row();
                tableRightContent.add(btnBuyAbilities).colspan(2).width(217).height(50);
                tableRightContent.row();
                tableRightContent.add(lblMore).colspan(2).height(53);
                break;
            case Rating:
                tableRightContent.add(tableRightContentRatingBtns).pad(8);
                tableRightContent.row();
                tableRightContent.add(tableRightContentRating).expand();
                model.getRating(Model.RatingType.General); // we should requery rating each time we choose the tab,
                break;                                     // because it might be updated on the server
            case History:
                lstHistory.setItems(getHistory());
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

    private void updateRating(Iterable<RatingItem> items) {
        for (Label label : ratingLabels) {
            label.setText("");
        }

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

    private void updateAbilities(Iterable<Model.Ability> abilityList) {
        tableRightContentAbilities.clear();
        for (Model.Ability ability : abilityList) {
            Button btn = abilities.get(ability);
            if (btn != null)
                tableRightContentAbilities.add(btn).space(10);
        }
    }

    private Array<String> getHistory() {
        assert i18n != null;

        Array<String> items = new Array<String>(model.history.size());
        for (HistoryItem it : model.history) {
            items.add(i18n.format("history.item", it.date, it.win ? 1 : 0, it.name1, it.name2, it.score1, it.score2));
        }
        return items;
    }
}
