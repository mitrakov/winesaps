package ru.mitrakov.self.rush.screens;

import java.text.*;
import java.util.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static ru.mitrakov.self.rush.model.Model.abilityValues;
import static ru.mitrakov.self.rush.model.Model.characterValues;
import static ru.mitrakov.self.rush.utils.SimpleLogger.log;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.*;


/**
 * ScreenMain shows the main screen and comprises tabs (Info, Rating, History, Friends), buttons to fight, "Settings"
 * button, "About" button and so on
 * @author mitrakov
 */
public class ScreenMain extends LocalizableScreen {

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
    private final Table tableRightContentHistory = new Table();
    private final Table tableRightContentFriends = new Table();
    private final Table tableFriendsControl = new Table();
    private final Table tableGemsData = new Table();
    private final DialogPromocode promocodeDialog;
    private final DialogPurchase purchaseDialog;
    private final DialogFeat moreGemsDialog;
    private final DialogIncoming incomingDialog;
    private final DialogFeat settingsDialog;
    private final DialogFeat aboutDialog;
    private final DialogBuyAbilities buyAbilitiesDialog;
    private final DialogInfo infoDialog;
    private final DialogLock lockDialog;
    private final DialogDialup dialupDialog;
    private final DialogInvite inviteDialog;
    private final DialogUnlockSpPack unlockSpPackDialog;
    private final DialogSinglePlayer singlePlayerDialog;
    private final DialogQuestion questionDialog;
    private final DialogPromocodeDone promocodeDoneDialog;
    private final DialogPassword passwordDialog;

    private final ScrollPane tableHistoryScroll;
    private final ScrollPane tableFriendsScroll;
    private final ScrollPane tableRightContentAbilitiesScroll;
    private final TextFieldFeat txtEnemyName;
    private final TextFieldFeat txtFriendName;
    private final TextButton btnSinglePlayer;
    private final TextButton btnInviteByName;
    private final TextButton btnQuickBattle;
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
    private final Button btnAddFriendOk;
    private final Button btnAddFriendCancel;
    private final LinkedLabel lblNewVersion;
    private final LinkedLabel lblMore;
    private final Label lblName;
    private final Label lblGemsHeader;
    private final Label lblGemsData;
    private final Label lblAbilities;
    private final Label lblAbilityExpireTime;
    private final Label lblRatingName;
    private final Label lblRatingWins;
    private final Label lblRatingLosses;
    private final Label lblRatingScoreDiff;
    private final Label lblRatingDots;
    private final Image imgCharacter;

    private final Drawable drawableWin;
    private final Drawable drawableLoss;
    private final Drawable drawableInvite;
    private final Drawable drawableRemove;
    private final Drawable drawableStatusOn; // Server API 1.2.0+ supports statuses
    private final Drawable drawableStatusOff; // Server API 1.2.0+ supports statuses

    private final ObjectMap<Model.Ability, ImageButton> abilities = new ObjectMap<Model.Ability, ImageButton>(10);
    private final ObjectMap<Model.Character, Drawable> characters = new ObjectMap<Model.Character, Drawable>(4);
    private final Array<LabelFeat> ratingLabels = new Array<LabelFeat>(4 * (Model.RATINGS_COUNT + 1));
    private final Format dateFmt = new SimpleDateFormat("HH:mm\nyyyy.MM.dd", Locale.getDefault());

    private enum CurDisplayMode {Info, Rating, History, Friends}
    private String newVersion = "";

    /**
     * Creates a new instance of ScreenMain
     * @param game - instance of Winesaps (NON-NULL)
     * @param model - model (NON-NULL)
     * @param psObject - Platform Specific Object (NON-NULL)
     * @param assetManager - asset manager (NON-NULL)
     * @param audioManager - audio manager (NON-NULL)
     */
    public ScreenMain(final Winesaps game, final Model model, PsObject psObject, AssetManager assetManager,
                      AudioManager audioManager) {
        super(game, model, psObject, assetManager, audioManager);

        TextureAtlas atlasMenu = assetManager.get("pack/menu.pack");
        TextureRegionDrawable valid = new TextureRegionDrawable(atlasMenu.findRegion("valid"));
        TextureRegionDrawable back = new TextureRegionDrawable(atlasMenu.findRegion("back"));
        TextureRegionDrawable cancel = new TextureRegionDrawable(atlasMenu.findRegion("back"));
        TextureRegionDrawable settings = new TextureRegionDrawable(atlasMenu.findRegion("settings"));
        TextureRegionDrawable about = new TextureRegionDrawable(atlasMenu.findRegion("about"));
        TextureRegionDrawable add = new TextureRegionDrawable(atlasMenu.findRegion("add"));

        drawableWin = new TextureRegionDrawable(atlasMenu.findRegion("win"));
        drawableLoss = new TextureRegionDrawable(atlasMenu.findRegion("loss"));
        drawableInvite = new TextureRegionDrawable(atlasMenu.findRegion("invite"));
        drawableRemove = new TextureRegionDrawable(atlasMenu.findRegion("remove"));
        drawableStatusOn = new TextureRegionDrawable(atlasMenu.findRegion("statusOn"));
        drawableStatusOff = new TextureRegionDrawable(atlasMenu.findRegion("statusOff"));

        Skin skin = assetManager.get("skin/uiskin.json");
        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));

        promocodeDialog = new DialogPromocode(model, skin, "default", audioManager);
        purchaseDialog = new DialogPurchase(skin, "default", i18n);
        moreGemsDialog = new DialogMoreGems(model, skin, "default", assetManager, audioManager, promocodeDialog,
                purchaseDialog);
        incomingDialog = new DialogIncoming(model, skin, "default", audioManager, i18n);
        settingsDialog = new DialogSettings(game, model, skin, "default", atlasMenu, i18n, audioManager);
        aboutDialog = new DialogAbout(skin, "default");
        buyAbilitiesDialog = new DialogBuyAbilities(model, assetManager, skin, "default", audioManager, i18n);
        infoDialog = new DialogInfo("", skin, "default");
        lockDialog = new DialogLock(skin, "panel-lock");
        dialupDialog = new DialogDialup(model, skin, "default");
        unlockSpPackDialog = new DialogUnlockSpPack(model, "", skin, "default", moreGemsDialog, i18n, atlasMenu.findRegion("gem"));
        singlePlayerDialog = new DialogSinglePlayer(model, atlasMenu, skin, "default", unlockSpPackDialog, i18n);
        inviteDialog = new DialogInvite(model, skin, "default", i18n);
        questionDialog = new DialogQuestion("", skin, "default");
        promocodeDoneDialog = new DialogPromocodeDone(skin, "default");
        passwordDialog = new DialogPassword(model, "", skin, "default", psObject, assetManager);

        tableRight = new Table(skin);
        tableHistoryScroll = new ScrollPaneFeat(tableRightContentHistory, skin, "default");
        tableFriendsScroll = new ScrollPaneFeat(tableRightContentFriends, skin, "default");
        tableRightContentAbilitiesScroll = new ScrollPane(tableRightContentAbilities);

        btnSinglePlayer = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                singlePlayerDialog.show(stage);
            }
        });

        btnInviteByName = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildLeftTable(true);
            }
        });
        btnQuickBattle = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                inviteDialog.setArguments(DialogInvite.InviteType.Quick, "").show(stage);
            }
        });
        btnInviteLatest = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                inviteDialog.setArguments(DialogInvite.InviteType.Latest, "").show(stage);
            }
        });
        btnInviteByNameOk = new ImageButtonFeat(valid, audioManager, new Runnable() {
            @Override
            public void run() {
                String name = txtEnemyName.getText();
                if (name.startsWith("#!")) {
                    infoDialog.setText(name, game.getDebugInfo(name)).show(stage);
                    Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android
                } else if (name.length() > 0) { // use 'length() > 0' instead of 'isEmpty()' (Android API 8)
                    inviteDialog.setArguments(DialogInvite.InviteType.ByName, name).show(stage);
                    rebuildLeftTable(false);
                }
            }
        });
        btnInviteByNameCancel = new ImageButtonFeat(back, audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildLeftTable(false);
            }
        });
        btnSettings = new ImageButtonFeat(settings, audioManager, new Runnable() {
            @Override
            public void run() {
                settingsDialog.show(stage);
            }
        });
        btnAbout = new ImageButtonFeat(about, audioManager, new Runnable() {
            @Override
            public void run() {
                aboutDialog.show(stage);
            }
        });
        btnInfo = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildRightTable(CurDisplayMode.Info);
            }
        });
        btnRating = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildRightTable(CurDisplayMode.Rating);
            }
        });
        btnHistory = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildRightTable(CurDisplayMode.History);
            }
        });
        btnFriends = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildRightTable(CurDisplayMode.Friends);
            }
        });
        btnBuyAbilities = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                buyAbilitiesDialog.show(stage);
            }
        });
        btnGeneralRating = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                model.getRating(Model.RatingType.General);
            }
        });
        btnWeeklyRating = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                model.getRating(Model.RatingType.Weekly);
            }
        });
        btnAddFriend = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildFriendsButtons(true);
            }
        });
        btnAddFriendOk = new ImageButtonFeat(add, audioManager, new Runnable() {
            @Override
            public void run() {
                String name = txtFriendName.getText();
                if (name.length() > 0) {
                    model.addFriend(name);
                    rebuildFriendsButtons(false);
                }
            }
        });
        btnAddFriendCancel = new ImageButtonFeat(cancel, audioManager, new Runnable() {
            @Override
            public void run() {
                rebuildFriendsButtons(false);
            }
        });

        txtEnemyName = new TextFieldFeat("", skin, "default", psObject, btnInviteByNameOk);
        txtFriendName = new TextFieldFeat("", skin, "default", psObject, btnAddFriendOk);

        lblNewVersion = new LinkedLabel("", "", "", skin, "default", "link", new Runnable() {
            @Override
            public void run() {
                Gdx.net.openURI(Winesaps.URL);
                Gdx.app.exit(); // NOTE! DO NOT use it on iOS (see javaDocs)
            }
        });
        lblMore = new LinkedLabel("", "", "", skin, "default", "link", new Runnable() {
            @Override
            public void run() {
                moreGemsDialog.show(stage);
            }
        });
        lblName = new Label("", skin, "title");
        lblGemsHeader = new Label("", skin, "default");
        lblGemsData = new Label("", skin, "default");
        lblAbilities = new Label("", skin, "default");
        lblAbilityExpireTime = new Label("", skin, "default");
        lblRatingName = new Label("", skin, "default");
        lblRatingWins = new Label("", skin, "default");
        lblRatingLosses = new Label("", skin, "default");
        lblRatingScoreDiff = new Label("", skin, "default");
        lblRatingDots = new Label("", skin, "default");
        imgCharacter = new Image();

        lblNewVersion.setVisible(false);

        loadTextures();
        initTables();
        // rebuilding tables moved to 'show()'
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // checking MENU button on Android
        if (Gdx.input.isKeyJustPressed(Input.Keys.MENU))
            settingsDialog.show(stage);
    }

    @Override
    public void show() {
        super.show();
        model.setSinglePlayer(false); // MultiPlayer mode by default
        model.getUserInfo();
        rebuildLeftTable(false);
        rebuildRightTable(CurDisplayMode.Info);
        if (model.newbie) {
            model.newbie = false;
            // put some code for newbies here, e.g. psObject.runTask(2500, new Runnable {...});
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;
        String updateStr = bundle.format("dialog.about.new.version", newVersion);

        promocodeDialog.onLocaleChanged(bundle);
        purchaseDialog.onLocaleChanged(bundle);
        moreGemsDialog.onLocaleChanged(bundle);
        incomingDialog.onLocaleChanged(bundle);
        settingsDialog.onLocaleChanged(bundle);
        aboutDialog.onLocaleChanged(bundle);
        buyAbilitiesDialog.onLocaleChanged(bundle);
        infoDialog.onLocaleChanged(bundle);
        dialupDialog.onLocaleChanged(bundle);
        inviteDialog.onLocaleChanged(bundle);
        unlockSpPackDialog.onLocaleChanged(bundle);
        singlePlayerDialog.onLocaleChanged(bundle);
        questionDialog.onLocaleChanged(bundle);
        promocodeDoneDialog.onLocaleChanged(bundle);
        passwordDialog.onLocaleChanged(bundle);

        btnSinglePlayer.setText(bundle.format("opponent.none"));
        btnInviteByName.setText(bundle.format("opponent.find"));
        btnQuickBattle.setText(bundle.format("opponent.quick"));
        btnInviteLatest.setText(bundle.format("opponent.latest"));
        btnInfo.setText(bundle.format("info.header"));
        btnRating.setText(bundle.format("rating.header"));
        btnHistory.setText(bundle.format("history.header"));
        btnFriends.setText(bundle.format("friends.header"));
        btnBuyAbilities.setText(bundle.format("abilities.buy"));
        btnGeneralRating.setText(bundle.format("rating.general"));
        btnWeeklyRating.setText(bundle.format("rating.weekly"));
        btnAddFriend.setText(bundle.format("friends.add"));
        lblMore.setText(bundle.format("dialog.gems.start"), bundle.format("dialog.gems.link"), "");
        lblGemsHeader.setText(bundle.format("info.gems"));
        lblAbilities.setText(bundle.format("info.abilities"));
        lblRatingName.setText(bundle.format("rating.name"));
        lblRatingWins.setText(bundle.format("rating.wins"));
        lblRatingLosses.setText(bundle.format("rating.losses"));
        lblRatingScoreDiff.setText(bundle.format("rating.score.diff"));
        lblRatingDots.setText(bundle.format("rating.dots"));
        lblNewVersion.setText(updateStr, bundle.format("dialog.about.update"), "");

        rebuildRightTable(CurDisplayMode.Info); // forward user to the main tab (to avoid artifacts, e.g. in History)
    }

    @Override
    public void handleEvent(EventBus.Event event) {
        // @mitrakov (2017-08-05): do NOT put here local vars like "String.format()" or "i18n.format()". It causes
        // excessive work for GC on each event during a battle (because all screens are subscribed to events)

        if (event instanceof EventBus.AuthorizedChangedEvent) {
            EventBus.AuthorizedChangedEvent ev = (EventBus.AuthorizedChangedEvent) event;
            if (!ev.authorized)
                game.setLoginScreen();
        }
        if (event instanceof EventBus.RoundStartedEvent) {
            dialupDialog.hide();
            lockDialog.remove();
            game.setNextScreen();
        }
        if (event instanceof EventBus.EnemyNameChangedEvent) {
            EventBus.EnemyNameChangedEvent ev = (EventBus.EnemyNameChangedEvent) event;
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            dialupDialog.setText(i18n.format("dialog.dialup.text", ev.enemy)).show(stage);
        }
        if (event instanceof EventBus.InviteEvent) {
            EventBus.InviteEvent ev = (EventBus.InviteEvent) event;
            incomingDialog.setArguments(ev.enemy, ev.enemySid).show(stage);
        }
        if (event instanceof EventBus.AddFriendErrorEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.add.friend.error")).show(stage);
        }
        if (event instanceof EventBus.NoGemsEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("dialog.info.no.gems")).show(stage);
        }
        if (event instanceof EventBus.AggressorBusyEvent) {
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("dialog.info.aggressor.busy")).show(stage);
        }
        if (event instanceof EventBus.DefenderBusyEvent) {
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("dialog.info.busy")).show(stage);
        }
        if (event instanceof EventBus.EnemyNotFoundEvent) {
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("dialog.info.no.enemy")).show(stage);
        }
        if (event instanceof EventBus.WaitingForEnemyEvent) {
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            lockDialog.setText(i18n.format("dialog.waiting")).show(stage);
        }
        if (event instanceof EventBus.AttackedYourselfEvent) {
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("error"), i18n.format("dialog.info.yourself")).show(stage);
        }
        if (event instanceof EventBus.ServerGonnaStopEvent) {
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.warning"), i18n.format("dialog.info.server.stop")).show(stage);
        }
    }

    @Override
    public void handleEventBackground(EventBus.Event event) {
        // @mitrakov (2017-08-05): do NOT put here local vars like "String.format()" or "i18n.format()". It causes
        // excessive work for GC on each event during a battle (because all screens are subscribed to events)

        if (event instanceof EventBus.NameChangedEvent) {
            EventBus.NameChangedEvent ev = (EventBus.NameChangedEvent) event;
            String txt = ev.name.length() <= 18 ? ev.name : String.format("%s...", ev.name.substring(0, 15));
            lblName.setText(txt);
        }
        if (event instanceof EventBus.GemsChangedEvent) {
            EventBus.GemsChangedEvent ev = (EventBus.GemsChangedEvent) event;
            lblGemsData.setText(String.valueOf(ev.gems));
            buyAbilitiesDialog.setGems(ev.gems);
            unlockSpPackDialog.setUserGems(ev.gems);
        }
        if (event instanceof EventBus.CharacterChangedEvent) {
            EventBus.CharacterChangedEvent ev = (EventBus.CharacterChangedEvent) event;
            TextureAtlas atlasIcons = assetManager.get("pack/icons.pack");
            imgCharacter.setDrawable(new TextureRegionDrawable(atlasIcons.findRegion(ev.character.name())));
        }
        if (event instanceof EventBus.RatingUpdatedEvent) {
            EventBus.RatingUpdatedEvent ev = (EventBus.RatingUpdatedEvent) event;
            updateRating(ev.items);
        }
        if (event instanceof EventBus.FriendListUpdatedEvent) {
            EventBus.FriendListUpdatedEvent ev = (EventBus.FriendListUpdatedEvent) event;
            updateFriends(ev.items);
        }
        if (event instanceof EventBus.FriendAddedEvent) {
            updateFriends(model.friends);
        }
        if (event instanceof EventBus.FriendRemovedEvent) {
            updateFriends(model.friends);
        }
        if (event instanceof EventBus.AbilitiesExpireUpdatedEvent) {
            EventBus.AbilitiesExpireUpdatedEvent ev = (EventBus.AbilitiesExpireUpdatedEvent) event;
            updateAbilities(ev.items);
        }
        if (event instanceof EventBus.PromocodeDoneEvent) {
            EventBus.PromocodeDoneEvent ev = (EventBus.PromocodeDoneEvent) event;
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            promocodeDoneDialog.setArguments(ev.name, ev.inviter, ev.gems, i18n).show(stage);
        }
        if (event instanceof EventBus.StopCallRejectedEvent) {
            EventBus.StopCallRejectedEvent ev = (EventBus.StopCallRejectedEvent) event;
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("stopcall.rejected", ev.cowardName));
            infoDialog.show(stage);
        }
        if (event instanceof EventBus.StopCallMissedEvent) {
            EventBus.StopCallMissedEvent ev = (EventBus.StopCallMissedEvent) event;
            incomingDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("stopcall.missed", ev.aggressorName));
            infoDialog.show(stage);
        }
        if (event instanceof EventBus.StopCallExpiredEvent) {
            EventBus.StopCallExpiredEvent ev = (EventBus.StopCallExpiredEvent) event;
            dialupDialog.hide();
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            infoDialog.setText(i18n.format("dialog.info"), i18n.format("stopcall.expired", ev.defenderName));
            infoDialog.show(stage);
        }
        if (event instanceof EventBus.SkuGemsUpdatedEvent) {
            IBillingProvider provider = psObject.getBillingProvider();
            if (provider != null) {
                EventBus.SkuGemsUpdatedEvent ev = (EventBus.SkuGemsUpdatedEvent) event;
                for (IBillingProvider.Sku sku : provider.getProducts()) {
                    if (ev.skuGems.containsKey(sku.id))
                        sku.value = ev.skuGems.get(sku.id);
                }
                I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
                purchaseDialog.updateSkuButtons(provider, model.name, i18n, assetManager, audioManager);
            }
        }
        if (event instanceof EventBus.PaymentDoneEvent) {
            EventBus.PaymentDoneEvent ev = (EventBus.PaymentDoneEvent) event;
            DialogFeat.hideAll(stage);
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            String header = i18n.format("dialog.promocode.done.header");
            infoDialog.setText(header, i18n.format("dialog.gems.done", ev.gems)).show(stage);
            log("Coupon = ", ev.coupon);
        }
        if (event instanceof EventBus.NewVersionAvailableEvent) {
            EventBus.NewVersionAvailableEvent ev = (EventBus.NewVersionAvailableEvent) event;
            newVersion = ev.newVersion;
            I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
            String start = i18n.format("dialog.about.new.version", ev.newVersion);
            lblNewVersion.setText(start, i18n.format("dialog.about.update"), "");
            lblNewVersion.setVisible(true);
        }
        if (event instanceof EventBus.WeakPasswordEvent) {
            passwordDialog.show(stage);
        }
    }

    /**
     * Prepares textures
     */
    private void loadTextures() {
        TextureAtlas atlasAbility = assetManager.get("pack/ability.pack");
        for (final Model.Ability ability : abilityValues) {
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                ImageButton btn = new ImageButtonFeat(new TextureRegionDrawable(region), audioManager, new Runnable() {
                    @Override
                    public void run() {
                        I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
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
                abilities.put(ability, btn);
            }
        }

        TextureAtlas atlasIcons = assetManager.get("pack/icons.pack");
        for (Model.Character character : characterValues) {
            TextureRegion region = atlasIcons.findRegion(String.format("%s48", character));
            if (region != null)
                characters.put(character, new TextureRegionDrawable(region));
        }
    }

    /**
     * Initializes the content tables
     */
    private void initTables() {
        TextureAtlas atlasMenu = assetManager.get("pack/menu.pack");
        Skin skin = assetManager.get("skin/uiskin.json");
        Drawable gem = new TextureRegionDrawable(atlasMenu.findRegion("gem"));

        table.add(tableLeft).pad(4).width(222).fill();
        table.add(tableRight).pad(4).expand().fill();
        table.setBackground(new Image(assetManager.<Texture>get("back/main.jpg")).getDrawable());

        tableLeft.add(tableLeftHeader).height(80).pad(10).fill();
        tableLeft.row();
        tableLeft.add(tableLeftInvite).expand().fill();
        tableLeft.row();
        tableLeft.add(tableLeftToolbar).height(64).fill();

        tableRight.setBackground("panel-maroon");
        tableRight.add(tableRightHeader).padTop(16).height(55).fill();
        tableRight.row();
        tableRight.add(tableRightContent).expand().fill();

        tableLeftHeader.add(new Image(atlasMenu.findRegion("logo")));

        tableRightHeader.add(btnInfo).width(138).expand().fill();
        tableRightHeader.add(btnRating).width(138).expand().fill();
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

        // === Ratings ===
        final int RATING_COLUMNS = 4;
        tableRightContentRating.row();
        tableRightContentRating.add(new Image(skin, "splitpane")).colspan(RATING_COLUMNS).width(351).height(2);

        for (int i = 0; i < Model.RATINGS_COUNT; i++) {
            tableRightContentRating.row().spaceLeft(20);
            for (int j = 0; j < RATING_COLUMNS; j++) {
                LabelFeat label = new LabelFeat("", skin, "small", true);
                tableRightContentRating.add(label);
                ratingLabels.add(label);
            }
        }
        tableRightContentRating.row().spaceLeft(20);
        tableRightContentRating.add(lblRatingDots).colspan(RATING_COLUMNS);
        tableRightContentRating.row().spaceLeft(20);
        for (int j = 0; j < RATING_COLUMNS; j++) {
            LabelFeat label = new LabelFeat("", skin, "small", true);
            tableRightContentRating.add(label);
            ratingLabels.add(label);
        }

        // === History ===
        for (int i = 0; i < Model.HISTORY_MAX; i++) {
            tableRightContentHistory.row().spaceLeft(10).spaceTop(5);
            tableRightContentHistory.add(new LabelFeat("", skin, "small", true));
            tableRightContentHistory.add(new Image());
            tableRightContentHistory.add(new Label("", skin, "small")).width(88).maxWidth(88).spaceLeft(5);
            tableRightContentHistory.add(new Label("", skin, "title"));
            tableRightContentHistory.add(new Image());
            tableRightContentHistory.add(new Label("", skin, "small")).width(88).maxWidth(88).spaceLeft(5);
            tableRightContentHistory.add(new Label("", skin, "title"));
            tableRightContentHistory.add(new Image());
        }

        // === Gems Data ===
        tableGemsData.add(lblGemsData).spaceRight(5);
        tableGemsData.add(new ImageButtonFeat(gem, audioManager, new Runnable() {
            @Override
            public void run() {
                moreGemsDialog.show(stage);
            }
        }));
    }

    /**
     * Rebuilds left table with 4 buttons ("Training", "Find enemy", "Quick fight", "Latest enemy")
     * @param showInputName - true to show text box, false to show only 4 buttons
     */
    private void rebuildLeftTable(boolean showInputName) {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android (shown in "invite enemy by name")
        tableLeftInvite.clear();

        // ...
        if (showInputName) {
            tableLeftInvite.add(txtEnemyName).colspan(2).width(190).height(50);
            tableLeftInvite.row();
            tableLeftInvite.add(btnInviteByNameOk).spaceTop(8);
            tableLeftInvite.add(btnInviteByNameCancel).spaceTop(8);
            tableLeftInvite.row();
            tableLeftInvite.add(btnQuickBattle).colspan(2).width(190).height(68).spaceTop(45);
            tableLeftInvite.row();
            tableLeftInvite.add(btnInviteLatest).colspan(2).width(190).height(68).spaceTop(8);
            stage.setKeyboardFocus(txtEnemyName);
        } else {
            tableLeftInvite.add(btnSinglePlayer).width(190).height(68);
            tableLeftInvite.row();
            tableLeftInvite.add(btnInviteByName).width(190).height(68).spaceTop(8);
            tableLeftInvite.row();
            tableLeftInvite.add(btnQuickBattle).width(190).height(68).spaceTop(8);
            tableLeftInvite.row();
            tableLeftInvite.add(btnInviteLatest).width(190).height(68).spaceTop(8);
        }

        tableLeftToolbar.add(btnSettings).spaceRight(30);
        tableLeftToolbar.add(btnAbout);
    }

    /**
     * Rebuilds right table with 4 tabs ("Info", "Rating", "History", "Friends")
     * @param mode - current tab to show
     */
    private void rebuildRightTable(CurDisplayMode mode) {
        tableRightContent.clear();

        switch (mode) {
            case Info:
                tableRightContent.add(tableRightContentName).colspan(2).padTop(16);
                tableRightContent.row();
                tableRightContent.add().expandY();
                tableRightContent.row();
                tableRightContent.add(lblGemsHeader);
                tableRightContent.add(tableGemsData);
                tableRightContent.row().spaceTop(16);
                tableRightContent.add(lblAbilities);
                tableRightContent.add(tableRightContentAbilitiesScroll).pad(4).expandX().fill();
                tableRightContent.row();
                tableRightContent.add().width(217);
                tableRightContent.add(lblAbilityExpireTime);
                tableRightContent.row();
                tableRightContent.add().expandY();
                tableRightContent.row();
                tableRightContent.add(btnBuyAbilities).colspan(2).minWidth(217).height(50); // minWidth for French Lang
                tableRightContent.row();
                tableRightContent.add(lblMore).colspan(2).spaceTop(20);
                tableRightContent.row();
                tableRightContent.add(lblNewVersion).colspan(2);
                break;
            case Rating:
                tableRightContent.add(tableRightContentRatingBtns).pad(8);
                tableRightContent.row();
                tableRightContent.add(tableRightContentRating).expand();
                model.getRating(Model.RatingType.General); // we should requery rating each time we choose the tab,
                break;                                     // because it might be updated on the server
            case History:
                updateHistory();
                tableRightContent.add(tableHistoryScroll).padTop(4).fill().expand();
                break;
            case Friends:
                tableRightContent.add(tableFriendsControl).padTop(16).fill();
                tableRightContent.row();
                tableRightContent.add(tableFriendsScroll).pad(4).fill();
                tableRightContent.row();
                tableRightContent.add().fill().expand(); // fake cell to expand
                rebuildFriendsButtons(false);
                model.getFriends(); // we should requery friends each time we choose the tab, because friends may be
                break;              // added from different places
            default:
        }
    }

    /**
     * Rebuilds friends buttons
     * @param showInputName - true to show text box, false to show a button ("Add friend")
     */
    private void rebuildFriendsButtons(boolean showInputName) {
        Gdx.input.setOnscreenKeyboardVisible(false); // hide keyboard on Android (shown in "add friend")
        tableFriendsControl.clear();

        if (showInputName) {
            tableFriendsControl.add(txtFriendName).width(161).height(50);
            tableFriendsControl.add(btnAddFriendOk).spaceLeft(20);
            tableFriendsControl.add(btnAddFriendCancel).spaceLeft(20);
            stage.setKeyboardFocus(txtFriendName);
        } else {
            tableFriendsControl.add(btnAddFriend).width(120).height(46);
        }
    }

    /**
     * Updates the rating content
     * @param items - collection of rating items
     */
    private void updateRating(Iterable<RatingItem> items) {
        for (Label label : ratingLabels) {
            label.setText("");
        }

        int i = 0;
        for (RatingItem item : items) {
            if (i + 3 < ratingLabels.size) {
                String txt = item.name.length() <= 18 ? item.name : String.format("%s...", item.name.substring(0, 15));
                ratingLabels.get(i++).setBackground(model.name.equals(item.name) ? Color.GOLDENROD : null).setText(txt);
                ratingLabels.get(i++).setText(String.valueOf(item.victories));
                ratingLabels.get(i++).setText(String.valueOf(item.defeats));
                ratingLabels.get(i++).setText(String.valueOf(item.score_diff));
            }
        }
    }

    /**
     * Updates the history content
     */
    private void updateHistory() {
        Array<Actor> cells = tableRightContentHistory.getChildren();
        assert cells != null;
        final int COLUMNS = 8;

        int i = 0;
        for (HistoryItem it : model.history) {
            if (COLUMNS * i + 7 < cells.size) {
                Label lblDate = (Label) cells.get(COLUMNS * i);
                Image imgChar1 = (Image) cells.get(COLUMNS * i + 1);
                Label lblUsr1 = (Label) cells.get(COLUMNS * i + 2);
                Label lblVs = (Label) cells.get(COLUMNS * i + 3);
                Image imgChar2 = (Image) cells.get(COLUMNS * i + 4);
                Label lblUsr2 = (Label) cells.get(COLUMNS * i + 5);
                Label lblScore = (Label) cells.get(COLUMNS * i + 6);
                Image imgWin = (Image) cells.get(COLUMNS * i + 7);

                lblDate.setText(dateFmt.format(it.date));
                imgChar1.setDrawable(characters.get(it.character1));
                lblUsr1.setText(it.name1.length() <= 11 ? it.name1 : String.format("%s...", it.name1.substring(0, 8)));
                lblVs.setText("-");
                imgChar2.setDrawable(characters.get(it.character2));
                lblUsr2.setText(it.name2.length() <= 11 ? it.name2 : String.format("%s...", it.name2.substring(0, 8)));
                lblScore.setText(String.format(Locale.getDefault(), "%d-%d", it.score1, it.score2));
                imgWin.setDrawable(it.win ? drawableWin : drawableLoss);
            }
            i++;
        }
    }

    /**
     * Updates the ability list in "Info" tab
     * @param abilityList - ability list
     */
    private void updateAbilities(Iterable<Model.Ability> abilityList) {
        tableRightContentAbilities.clear();
        for (Model.Ability ability : abilityList) {
            Button btn = abilities.get(ability);
            if (btn != null)
                tableRightContentAbilities.add(btn).space(10);
        }
    }

    /**
     * Updates the friends content
     * @param items - collection of friend items
     */
    private void updateFriends(Collection<? extends FriendItem> items) {
        adjustFriendsTable();
        Array<Actor> cells = tableRightContentFriends.getChildren();
        assert cells != null;
        final int COLUMNS = 5;

        int i = 0;
        // fill the rows with friends
        for (FriendItem item : items) {
            if (COLUMNS * i + 4 < cells.size) {
                Image imgStatus = (Image) cells.get(COLUMNS * i);
                Image imgChar = (Image) cells.get(COLUMNS * i + 1);
                Label lbl = (Label) cells.get(COLUMNS * i + 2);
                ImageButton btnInvite = (ImageButton) cells.get(COLUMNS * i + 3);
                ImageButton btnRemove = (ImageButton) cells.get(COLUMNS * i + 4);

                imgStatus.setDrawable(item.status > 1 ? drawableStatusOn : drawableStatusOff);
                imgChar.setDrawable(characters.get(item.character));
                lbl.setText(item.name.length() <= 27 ? item.name : String.format("%s...", item.name.substring(0, 24)));
                btnInvite.setVisible(true);
                btnRemove.setVisible(true);
            }
            i++;
        }
        // fill the rest of rows with blank values
        for (int j = i; j < tableRightContentFriends.getRows(); j++) {
            if (COLUMNS * j + 4 < cells.size) {
                Image imgStatus = (Image) cells.get(COLUMNS * j);
                Image imgChar = (Image) cells.get(COLUMNS * j + 1);
                Label lblName = (Label) cells.get(COLUMNS * j + 2);
                ImageButton btnInvite = (ImageButton) cells.get(COLUMNS * j + 3);
                ImageButton btnRemove = (ImageButton) cells.get(COLUMNS * j + 4);

                imgStatus.setDrawable(null);
                imgChar.setDrawable(null);
                lblName.setText("");
                btnInvite.setVisible(false);
                btnRemove.setVisible(false);
            }
        }
    }

    /**
     * Appends new items to the friends content table (if necessary)
     */
    private void adjustFriendsTable() {
        final Skin skin = assetManager.get("skin/uiskin.json");

        int curFriendsCount = tableRightContentFriends.getRows();
        int modelFriendsCount = model.friends.size();
        int n = modelFriendsCount - curFriendsCount;
        for (int i = 0; i < n; i++) {
            final Label label = new Label("", skin, "default");

            tableRightContentFriends.row().padTop(2);
            tableRightContentFriends.add(new Image());
            tableRightContentFriends.add(new Image()).spaceLeft(5);
            tableRightContentFriends.add(label).expandX().left().spaceLeft(5);
            tableRightContentFriends.add(new ImageButtonFeat(drawableInvite, audioManager, new Runnable() {
                @Override
                public void run() {
                    final String name = label.getText().toString();
                    inviteDialog.setArguments(DialogInvite.InviteType.ByName, name).show(stage);
                }
            }));
            tableRightContentFriends.add(new ImageButtonFeat(drawableRemove, audioManager, new Runnable() {
                @Override
                public void run() {
                    I18NBundle i18n = assetManager.get(String.format("i18n/bundle_%s", model.language));
                    final String name = label.getText().toString();
                    String txt = i18n.format("dialog.friends.remove.text", name);
                    questionDialog.setText(i18n.format("dialog.warning"), txt).setRunnable(new Runnable() {
                        @Override
                        public void run() {
                            model.removeFriend(name);
                        }
                    }).show(stage);
                }
            })).spaceLeft(20);
        }
    }
}
