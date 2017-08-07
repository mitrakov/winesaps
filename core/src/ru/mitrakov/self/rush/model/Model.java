package ru.mitrakov.self.rush.model;

import java.util.*;
import java.security.*;
import java.math.BigInteger;
import java.util.concurrent.*;

import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.Winesaps;
import ru.mitrakov.self.rush.model.Cells.CellObject;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.model.Model.Cmd.*;

/**
 * This class represents a model in the MVC pattern
 * Class is intended to have a single instance
 *
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public class Model {
    /**
     * size of the rating list (defined by server)
     */
    public static final int RATINGS_COUNT = 10;
    public static final int STYLES_COUNT = 4;
    public static final int HISTORY_MAX = 32;

    // ===========================
    // === PUBLIC INTERFACES ===
    // ===========================

    /**
     * interface to send commands to the server
     */
    @SuppressWarnings("unused")
    public interface ISender {
        void send(Cmd cmd);

        void send(int cmd);

        void send(Cmd cmd, int... arg);

        void send(int cmd, int... arg);

        void send(Cmd cmd, String arg);

        void send(int cmd, String arg);

        void reset();
    }

    /**
     * interface to read/write files independent from a platform (Desktop, Android, etc.)
     */
    public interface IFileReader {
        void write(String filename, String s);

        void append(String filename, String s);

        String read(String filename);

        Object deserialize(String filename);

        void serialize(String filename, Object obj);
    }

    // ===========================
    // === PUBLIC ENUMERATIONS ===
    // ===========================

    /**
     * server-specific commands; for more details see docs to the protocol
     */
    public enum Cmd {
        UNSPEC_ERROR,      // 0
        SIGN_UP,           // 1
        SIGN_IN,           // 2
        SIGN_OUT,          // 3
        USER_INFO,         // 4
        CHANGE_CHARACTER,  // 5
        ATTACK,            // 6
        CALL,              // 7
        ACCEPT,            // 8
        REJECT,            // 9
        STOPCALL,          // 10
        CANCEL_CALL,       // 11
        RECEIVE_LEVEL,     // 12
        RANGE_OF_PRODUCTS, // 13
        BUY_PRODUCT,       // 14
        ENEMY_NAME,        // 15
        FULL_STATE,        // 16
        ROUND_INFO,        // 17
        ABILITY_LIST,      // 18
        MOVE,              // 19
        USE_THING,         // 20
        USE_SKILL,         // 21
        GIVE_UP,           // 22
        STATE_CHANGED,     // 23
        SCORE_CHANGED,     // 24
        EFFECT_CHANGED,    // 25
        PLAYER_WOUNDED,    // 26
        THING_TAKEN,       // 27
        OBJECT_APPENDED,   // 28
        FINISHED,          // 29
        RESERVED_1E,       // 30
        RESERVED_1F,       // 31
        RATING,            // 32
        FRIEND_LIST,       // 33
        ADD_FRIEND,        // 34
        REMOVE_FRIEND,     // 35
        CHECK_PROMOCODE,   // 36
        PROMOCODE_DONE,    // 37
        GET_SKU_GEMS,      // 38
        CHECK_PURCHASE,    // 39
        GET_CLIENT_VERSION // 40
    }

    public static final Cmd[] cmdValues = Cmd.values();

    public enum Character {None, Rabbit, Hedgehog, Squirrel, Cat}

    public final Character[] characterValues = Character.values();

    @SuppressWarnings("unused")
    public enum HurtCause {Poisoned, Sunk, Soaked, Devoured, Exploded}
    private final HurtCause[] hurtCauseValues = HurtCause.values();

    public enum Effect {None, Antidote, Dazzle, Afraid, @SuppressWarnings("unused")Attention}

    private final Effect[] effectValues = Effect.values();

    public enum MoveDirection {LeftDown, Left, LeftUp, RightDown, Right, RightUp}

    private final MoveDirection[] moveDirectionValues = MoveDirection.values();

    /**
     * ability list; some abilities are stubs (a7, a8, up to a32), because skills start with an index=33
     */
    public enum Ability {
        None, Snorkel, ClimbingShoes, SouthWester, VoodooMask, Snowshoes, Sunglasses,
        a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19,
        a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32,
        Miner, Builder, Shaman, Grenadier, Spy
    }

    public final Ability[] abilityValues = Ability.values();

    /**
     * rating enumeration (General, Weekly, etc.); constants (0, 1) are specified by the server
     */
    public enum RatingType {
        General, Weekly
    }

    // =============================
    // === PUBLIC STATIC METHODS ===
    // =============================

    public static synchronized String md5(String s) {
        try {
            // @mitrakov: don't use HexBinaryAdapter(): javax is not supported by Android
            byte[] bytes = MessageDigest.getInstance("md5").digest(getBytes(s));
            return String.format("%032x", new BigInteger(1, bytes)); // use "%032x" instead of "%32x"!
        } catch (NoSuchAlgorithmException ignored) {
            return "";
        }
    }

    // ==============================
    // === PUBLIC VOLATILE FIELDS ===
    // ==============================
    // getters are supposed to have a little overhead, so we make the fields "public" for efficiency and "volatile" for
    // memory management inside a multithreading access; be careful! They may be changed OUTSIDE the OpenGL loop
    // ==============================

    public volatile String name = "";
    public volatile String enemy = "";
    public volatile String promocode = "";
    public volatile Character character = Character.None;
    public volatile Character character1 = Character.None;
    public volatile Character character2 = Character.None;
    public volatile boolean connected = false;
    public volatile boolean newbie = true;
    public volatile int roundLengthSec = 90;
    public volatile int stylePack = 0;
    public volatile long abilityExpireTime = 0;
    public volatile long roundStartTime = 0;
    public volatile Field field;
    public volatile CellObject curActor;
    public volatile CellObject enemyActor;

    // ==================================================
    // === PUBLIC NON-VOLATILE CONCURRENT COLLECTIONS ===
    // ==================================================
    // getters are supposed to have a little overhead, so we make the fields "public" for efficiency; these collections
    // are rest upon Java-Concurrent Library, because they may be changed OUTSIDE the OpenGL loop at any moment;
    // all 'foreach' operations are considered to be safe
    // ==================================================

    public final Collection<Product> products = new ConcurrentLinkedQueue<Product>();
    public final Collection<HistoryItem> history = new ConcurrentLinkedQueue<HistoryItem>();
    public final Collection<FriendItem> friends = new ConcurrentLinkedQueue<FriendItem>();
    public final Map<Ability, Integer> abilityExpireMap = new ConcurrentHashMap<Ability, Integer>(); // see note#2

    // ===========================
    // === PUBLIC FINAL FIELDS ===
    // ===========================

    public final EventBus bus = new EventBus();

    // ================
    // === SETTINGS ===
    // ================

    public volatile boolean notifyNewBattles = true;
    public volatile String language = "en";
    public volatile boolean music = true;
    public volatile boolean soundEffects = true;

    // ================================
    // === PRIVATE STATIC CONSTANTS ===
    // ================================

    private static final int AGGRESSOR_ID = 4;
    private static final int DEFENDER_ID = 5;
    private static final int PING_PERIOD_MSEC = 60000;
    private static final int SKILL_OFFSET = 0x20;
    private static final int PROMOCODE_LEN = 5;
    private static final String HISTORY_PREFIX = "history/";
    public /*private*/ static final String SETTINGS_FILE = "settings"; // public for debug purposes only!

    // ======================
    // === PRIVATE EVENTS ===
    // ======================
    // these events are extracted to fields ONLY FOR DECREASING GC PRESSURE during a battle!
    // the other events can be emitted over usual "new EventBus.XxxEvent(...)" notation
    // ==================================================

    private final EventBus.NewFieldEvent newFieldEvent = new EventBus.NewFieldEvent(null, null);
    private final EventBus.ActorResetEvent actorResetEvent = new EventBus.ActorResetEvent(null);
    private final EventBus.EffectAddedEvent effectAddedEvent = new EventBus.EffectAddedEvent(Effect.None);
    private final EventBus.RoundStartedEvent roundStartedEvent = new EventBus.RoundStartedEvent(0);
    private final EventBus.ScoreChangedEvent scoreChangedEvent = new EventBus.ScoreChangedEvent(0, 0);
    private final EventBus.LivesChangedEvent livesChangedEvent = new EventBus.LivesChangedEvent(0, 0);
    private final EventBus.ThingChangedEvent thingChangedEvent = new EventBus.ThingChangedEvent(null, null, false);
    private final EventBus.StyleChangedEvent styleChangedEvent = new EventBus.StyleChangedEvent(0);
    private final EventBus.ObjectRemovedEvent objectRemovedEvent = new EventBus.ObjectRemovedEvent(0, null);
    private final EventBus.RoundFinishedEvent roundFinishedEvent = new EventBus.RoundFinishedEvent(false, "", "", 0, 0);
    private final EventBus.PlayerWoundedEvent playerWoundedEvent = new EventBus.PlayerWoundedEvent(0, null, 0, 0);


    // ============================
    // === USUAL PRIVATE FIELDS ===
    // ============================

    private final Object locker = new Object();
    private final Collection<Ability> abilities = new LinkedList<Ability>();
    private ISender sender;
    private boolean authorized = false;
    private boolean aggressor = true;
    private CellObject curThing;
    private CellObject enemyThing;
    private transient int battleNotFoundGuardCounter;
    private transient int debugCounter;
    public /*private*/ IFileReader fileReader; // public for debug purposes only!
    public /*private*/ String hash = "";       // public for debug purposes only!

    public Model(PsObject psObject) {
        assert psObject != null;
        // create timer to ping the server (otherwise the server will make "signOut due to inaction")
        psObject.runDaemon(PING_PERIOD_MSEC, PING_PERIOD_MSEC, new Runnable() {
            @Override
            public void run() {
                if (authorized)
                    getUserInfo();
            }
        });
    }

    // ==========================
    // === NON-SERVER METHODS ===
    // ==========================

    /**
     * Sets a new sender to the model
     *
     * @param sender - sender (may be NULL)
     */
    public void setSender(ISender sender) {
        this.sender = sender;
    }

    /**
     * Sets a new file reader to the model
     *
     * @param fileReader - file reader (may be NULL)
     */
    public void setFileReader(IFileReader fileReader) {
        this.fileReader = fileReader;
    }

    /**
     * Loads settings from the internal file
     */
    public void loadSettings() {
        if (fileReader != null) {
            String s = fileReader.read(SETTINGS_FILE);
            if (s != null) {
                String[] settings = s.split(" ");
                if (settings.length > 4) {
                    language = settings[0];
                    notifyNewBattles = settings[1].equals("1");
                    music = settings[2].equals("1");
                    soundEffects = settings[3].equals("1");
                    name = settings[4];
                    bus.raise(new EventBus.NameChangedEvent(name));
                    if (settings.length > 5)
                        hash = settings[5];
                }
                newbie = false;
            }
        }
    }

    /**
     * Saves current settings to the internal file
     */
    public void saveSettings() {
        if (fileReader != null) {
            String s = String.format("%s %s %s %s %s %s", language, notifyNewBattles ? "1" : "0", music ? "1" : "0",
                    soundEffects ? "1" : "0", name, hash);
            fileReader.write(SETTINGS_FILE, s);
        }
    }

    /**
     * @param ability - ability (if NULL then returns empty list)
     * @return collection of available products by the given ability (e.g. Snorkel for 1 day, 3 days, 7 days)
     */
    public Collection<Product> getProductsByAbility(Ability ability) {
        Collection<Product> res = new LinkedList<Product>();
        for (Product product : products) {   // in Java 8 may be replaced with lambda
            if (product.ability == ability)
                res.add(product);
        }
        return res;
    }

    @SuppressWarnings("unused")
    public boolean friendExists(String name) {
        for (FriendItem item : friends) { // in Java 8 may be replaced with lambda
            if (item.name.equals(name))
                return true;
        }
        return false;
    }

    public String getDetractor1() {
        return aggressor ? name : enemy;
    }

    public String getDetractor2() {
        return aggressor ? enemy : name;
    }

    // ==============================
    // === SERVER REQUEST METHODS ===
    // ==============================
    // Feel free to call these methods from anywhere
    // ==============================

    public void checkVersion() {
        if (connected && sender != null) {
            sender.send(GET_CLIENT_VERSION);
        }
    }

    public void signIn() {
        assert name != null && hash != null;
        if (name.length() > 0 && hash.length() > 0 && connected && sender != null) { // don't use method 'isEmpty()'
            sender.reset();
            sender.send(SIGN_IN, String.format("\1%s\0%s", name, hash)); // \1 = Local auth
        }
    }

    /**
     * Sends SIGN_IN command to the server
     *
     * @param login    - user name
     * @param password - password
     */
    public void signIn(String login, String password) {
        if (connected && sender != null) {
            hash = md5(password);
            sender.reset();
            sender.send(SIGN_IN, String.format("\1%s\0%s", login, hash)); // \1 = Local auth
        }
    }

    /**
     * Sends SIGN_UP command to the server
     *
     * @param login    - user name
     * @param password - password
     * @param email    - email address
     */
    public void signUp(String login, String password, String email, String promocode) {
        assert login != null && password != null && email != null && promocode != null;
        if (connected && sender != null && password.length() >= 4) {
            hash = md5(password);
            sender.reset();
            sender.send(SIGN_UP, String.format("%s\0%s\0%s\0%s", login, hash, email, promocode));
        }
    }

    /**
     * Sends SIGN_OUT command to the server
     */
    public void signOut() {
        if (connected && sender != null) {
            sender.send(SIGN_OUT);
        }
    }

    public void getUserInfo() {
        if (connected && sender != null) {
            sender.send(USER_INFO);
        }
    }

    /**
     * Sends INVITE command to the server (by name)
     *
     * @param victim - victim user name
     */
    public void invite(String victim) {
        if (connected && sender != null) {
            sender.send(ATTACK, String.format("\0%s", victim));
        }
    }

    /**
     * Sends INVITE command to the server (latest enemy)
     */
    public void inviteLatest() {
        if (connected && sender != null) {
            sender.send(ATTACK, 1);
        }
    }

    /**
     * Sends INVITE command to the server (random enemy)
     */
    public void quickGame() {
        if (connected && sender != null) {
            sender.send(ATTACK, 2);
        }
    }

    /**
     * Sends ACCEPT command to the server (in response to INVITE)
     */
    public void accept(int enemySid) {
        if (connected && sender != null) {
            sender.send(ACCEPT, enemySid / 256, enemySid % 256);
        }
    }

    /**
     * Sends REJECT command to the server (in response to INVITE)
     */
    public void reject(int enemySid) {
        if (connected && sender != null) {
            sender.send(REJECT, enemySid / 256, enemySid % 256);
        }
    }

    public void receiveLevel(String levelName) {
        if (connected && sender != null) {
            sender.send(RECEIVE_LEVEL, levelName);
        }
    }

    public void changeCharacter(Character character) {
        if (character != Character.None) {
            if (connected && sender != null) {
                sender.send(CHANGE_CHARACTER, character.ordinal());
            }
        }
    }

    /**
     * Sends FRIEND_LIST command to the server
     */
    public void getFriends() {
        if (connected && sender != null) {
            sender.send(FRIEND_LIST);
        }
    }

    /**
     * Sends ADD_FRIEND command to the server
     *
     * @param name - friend user name
     */
    public void addFriend(String name) {
        if (connected && sender != null) {
            if (name.length() > 0)
                sender.send(ADD_FRIEND, name);
        }
    }

    /**
     * Sends REMOVE_FRIEND command to the server
     *
     * @param name - quondam friend name
     */
    public void removeFriend(String name) {
        if (connected && sender != null) {
            if (name.length() > 0)
                sender.send(REMOVE_FRIEND, name);
        }
    }

    /**
     * Sends RATING command to the server
     *
     * @param type - type of rating (General, Weekly, etc.)
     */
    public void getRating(RatingType type) {
        assert type != null;
        if (connected && sender != null) {
            sender.send(RATING, type.ordinal());
        }
    }

    public void checkPromocode(String promocode) {
        assert promocode != null;
        if (connected && sender != null && promocode.length() >= PROMOCODE_LEN) {
            sender.send(CHECK_PROMOCODE, promocode);
        }
    }

    /**
     * Sends BUY_PRODUCT command to the server
     *
     * @param product - product to buy
     */
    public void buyProduct(Product product) {
        assert product != null;
        if (connected && sender != null) {
            sender.send(BUY_PRODUCT, product.ability.ordinal(), product.days);
        }
    }

    public void cancelCall() {
        if (connected && sender != null) {
            sender.send(CANCEL_CALL);
        }
    }

    /**
     * Sends MOVE battle command to the server
     */
    public void move(MoveDirection direction) {
        if (connected && sender != null && curActor != null) {
            // simple checks to relieve the server
            // DO NOT use switch(direction)!!! It causes call MoveDirection.values() that produces work for GC!
            if (direction == MoveDirection.LeftDown) {
                if (curActor.getX() == 0 && curActor.getY() == Field.HEIGHT - 1) return;
            } else if (direction == MoveDirection.Left) {
                if (curActor.getX() == 0) return;
            } else if (direction == MoveDirection.LeftUp) {
                if (curActor.getX() == 0 && curActor.getY() == 0) return;
            } else if (direction == MoveDirection.RightDown) {
                if (curActor.getX() == Field.WIDTH - 1 && curActor.getY() == Field.HEIGHT - 1) return;
            } else if (direction == MoveDirection.Right) {
                if (curActor.getX() == Field.WIDTH - 1) return;
            } else if (direction == MoveDirection.RightUp) {
                if (curActor.getX() == Field.WIDTH - 1 && curActor.getY() == 0) return;
            }
            sender.send(MOVE, Arrays.binarySearch(moveDirectionValues, direction)); // avoid "direction.ordinal()" (GC)
        }
    }

    /**
     * Sends USE_THING battle command to the server
     */
    public void useThing() {
        if (connected && sender != null && curThing != null) {
            sender.send(USE_THING);
        }
    }

    /**
     * Sends USE_SKILL battle command to the server
     *
     * @param ability - ability to use (it must be a SKILL, i.e. has a number > SKILL_OFFSET)
     */
    public void useAbility(Ability ability) {
        assert ability != null;
        if (connected && sender != null) {
            int code = Arrays.binarySearch(abilityValues, ability); // don't use "ability.ordinal()" (GC pressure)
            if (code > SKILL_OFFSET) // only skills may be used
                sender.send(USE_SKILL, code);
        }
    }

    public void useAbility(int index) {
        int i = 0;
        for (Ability ability : abilities) {
            if (i++ == index)
                useAbility(ability);
        }
    }

    public void giveUp() {
        field = null; // reset the current field
        if (connected && sender != null) {
            sender.send(GIVE_UP);
        }
    }

    public void requestSkuGems() {
        if (connected && sender != null) {
            sender.send(GET_SKU_GEMS);
        }
    }

    public void checkPurchase(String data, String signature) {
        if (connected && sender != null) {
            sender.send(CHECK_PURCHASE, String.format("%s\0%s", data, signature));
        }
    }

    // ===============================
    // === SERVER RESPONSE METHODS ===
    // ===============================
    // These methods are not expected to be called from external code
    // ===============================

    public void setConnected(boolean value) {
        if (!connected && value) { // if changed "not_connected" -> "connected"
            connected = true;  // we must change it before calling getUserInfo() or signIn()
            if (authorized)
                getUserInfo(); // connected, but already authorized? possibly the server has been restarted: see note#4
            else {
                checkVersion();
                signIn();     // connected and not authorized: try to sign in using stored credentials
            }
        }
        connected = value;
        bus.raise(new EventBus.ConnectedChangeEvent(connected));
    }

    public void setAuthorized(boolean value) {
        authorized = value;
        if (connected && sender != null) {
            if (value) {
                sender.send(RANGE_OF_PRODUCTS);
                sender.send(FRIEND_LIST); // without this "InviteByName" dialog suggests to add everyone to friends
            } else {
                sender.reset(); // clean up sid/token pair
                // hash = ""; saveSettings(); @mitrakov (2017-06-30): Do not clear hash, it may cause unexpected bugs
            }
        }
        bus.raise(new EventBus.AuthorizedChangedEvent(value));
    }

    public void setUserInfo(IIntArray data) {
        assert data != null;
        int i = 0;

        // parse name
        StringBuilder bld = new StringBuilder();
        for (; i < data.length() && data.get(i) != 0; i++) {
            bld.append((char) data.get(i));
        }
        name = bld.toString();
        bus.raise(new EventBus.NameChangedEvent(name));
        i++;

        // parse promo code
        bld = new StringBuilder(name);
        for (; i < data.length() && data.get(i) != 0; i++) {
            bld.append((char) data.get(i));
        }
        promocode = bld.toString();
        i++;

        // parse character
        int ch = data.get(i++);
        if (0 <= ch && ch < characterValues.length && character != characterValues[ch]) {
            character = characterValues[ch];
            bus.raise(new EventBus.CharacterChangedEvent(character));
        }

        // parse crystals
        if (i + 3 < data.length()) {
            int crystals = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | (data.get(i + 3));
            bus.raise(new EventBus.CrystalChangedEvent(crystals));
        }
        i += 4;

        // parse abilities
        synchronized (locker) {
            abilityExpireMap.clear();
            int abilitiesCnt = data.get(i++);
            for (int j = 0; j < abilitiesCnt; j++, i += 3) {
                if (i + 2 < data.length()) {
                    int id = data.get(i);
                    int minutes = data.get(i + 1) * 256 + data.get(i + 2);
                    if (0 <= id && id < abilityValues.length)
                        abilityExpireMap.put(abilityValues[id], minutes);
                }
            }
        }
        abilityExpireTime = System.currentTimeMillis();
        // fire the event (we use TreeSet to implicitly sort the key set; of course we may use ConcurrentSkipListMap
        // for "abilityExpireMap", but it's not supported by API Level 8, so we use ConcurrentHashMap)
        bus.raise(new EventBus.AbilitiesExpireUpdatedEvent(new TreeSet<Ability>(abilityExpireMap.keySet())));

        // now we know valid user name => read the history from a local storage
        if (fileReader != null && history.isEmpty()) {
            Object lst = fileReader.deserialize(String.format("%s%s", HISTORY_PREFIX, name));
            if (lst instanceof Collection) { // stackoverflow.com/questions/2950319
                //noinspection unchecked
                history.addAll((Collection) lst);
            }
        }

        // now we know valid user name => save settings
        saveSettings();
    }

    public void setEnemyName(String name) {
        if (name.length() > 0) { // server can send empty name as a response of Quick Attack
            enemy = name;
            bus.raise(new EventBus.EnemyNameChangedEvent(name));
        }
    }

    public void attacked(int sid, String aggressorName) {
        enemy = aggressorName;
        bus.raise(new EventBus.InviteEvent(aggressorName, sid));
    }

    public void stopCallRejected(String coward) {
        bus.raise(new EventBus.StopCallRejectedEvent(coward));
    }

    public void stopCallMissed(String aggressorName) {
        bus.raise(new EventBus.StopCallMissedEvent(aggressorName));
    }

    public void stopCallExpired(String defenderName) {
        bus.raise(new EventBus.StopCallExpiredEvent(defenderName));
    }

    public void setFriendList(IIntArray data, boolean append) {
        assert data != null;

        synchronized (locker) {
            if (!append)
                friends.clear();
            String s = data.toUTF8();  // example: \3Tommy\0\2Bobby\0\3Billy\0
            if (s.length() > 0) { // be careful! if s == "", then s.split("\0") returns Array("") instead of Array()
                for (String item : s.split("\0")) {
                    byte ch = (byte) item.charAt(0);
                    if (0 <= ch && ch < characterValues.length) {
                        friends.add(new FriendItem(characterValues[ch], item.substring(1)));
                    }
                }
            }
        }
        bus.raise(new EventBus.FriendListUpdatedEvent(friends));
    }

    public void friendAdded(int character, String name) {
        if (0 <= character && character < characterValues.length) {
            FriendItem item = new FriendItem(characterValues[character], name);
            friends.add(item);
            bus.raise(new EventBus.FriendAddedEvent(item));
        }
    }

    public void friendRemoved(String name) {
        synchronized (locker) {
            for (FriendItem item : friends) { // in Java 8 may be replaced with lambda
                if (item.name.equals(name)) {
                    friends.remove(item);
                    break; // to avoid iterator exceptions
                }
            }
        }
        bus.raise(new EventBus.FriendRemovedEvent(name));
    }

    public void setRangeOfProducts(IIntArray data) {
        assert data != null;
        synchronized (locker) {
            products.clear();
            for (int i = 0; i + 2 < data.length(); i += 3) {
                int id = data.get(i);
                int days = data.get(i + 1);
                int cost = data.get(i + 2);
                if (0 <= id && id < abilityValues.length)
                    products.add(new Product(abilityValues[id], days, cost));
            }
        }
    }

    public void setRating(RatingType type, IIntArray data) {
        assert type != null && data != null;
        Collection<RatingItem> rating = new LinkedList<RatingItem>();

        int i = 0;
        while (i < data.length()) {
            // name
            StringBuilder name = new StringBuilder();
            int wins = 0, losses = 0, score_diff = 0;
            for (; data.get(i) != 0 && i < data.length(); i++) {
                name.append((char) data.get(i));
            }
            i++;
            // wins
            if (i + 3 < data.length()) {
                wins = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | (data.get(i + 3));
                i += 4;
            }
            // losses
            if (i + 3 < data.length()) {
                losses = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | (data.get(i + 3));
                i += 4;
            }
            // score_diff
            if (i + 3 < data.length()) {
                score_diff = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | (data.get(i + 3));
                i += 4;
            }
            rating.add(new RatingItem(name.toString(), wins, losses, score_diff));
        }

        bus.raise(new EventBus.RatingUpdatedEvent(type, rating));
    }

    public void setPromocodeValid(boolean valid) {
        bus.raise(new EventBus.PromocodeValidChangedEvent(valid));
    }

    public void setPromocodeDone(String name, boolean inviter, int gems) {
        assert name != null;
        bus.raise(new EventBus.PromocodeDoneEvent(name, inviter, gems));
    }

    public void setSkuGems(IIntArray data) {
        Map<String, Integer> res = new HashMap<String, Integer>(3);
        for (int i = 0; i < data.length(); i += 4) {
            StringBuilder bld = new StringBuilder();
            for (; i < data.length() && data.get(i) != 0; i++) {
                bld.append((char) data.get(i));
            }
            i++; // skip NULL-terminator
            String sku = bld.toString();
            if (i + 3 < data.length()) {
                int gems = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | data.get(i + 3);
                res.put(sku, gems);
            }
        }
        bus.raise(new EventBus.SkuGemsUpdatedEvent(res));
    }

    public void paymentDone(int gems, String coupon) {
        bus.raise(new EventBus.PaymentDoneEvent(gems, coupon));
    }

    public void setRoundInfo(int number, int timeSec, boolean aggressor, int character1, int character2, int myLives,
                             int enemyLives) {
        curThing = enemyThing = curActor = enemyActor = null;
        battleNotFoundGuardCounter = 0;

        if (0 <= character1 && character1 < characterValues.length)
            this.character1 = characterValues[character1];
        if (0 <= character2 && character2 < characterValues.length)
            this.character2 = characterValues[character2];

        roundLengthSec = timeSec;
        this.aggressor = aggressor;
        roundStartTime = System.currentTimeMillis();

        // generate initial events
        roundStartedEvent.number = number;
        scoreChangedEvent.score1 = scoreChangedEvent.score2 = 0;
        livesChangedEvent.myLives = myLives;
        livesChangedEvent.enemyLives = enemyLives;
        thingChangedEvent.oldThing = thingChangedEvent.newThing = null;
        thingChangedEvent.mine = true;
        bus.raise(roundStartedEvent);
        bus.raise(scoreChangedEvent);
        bus.raise(livesChangedEvent);
        bus.raise(thingChangedEvent);
    }

    public void setNewField(IIntArray fieldData) {
        Field field; // for multithreaded safety
        this.field = field = new Field(fieldData);
        // assign curActor (be careful! if "fieldData" doesn't contain actors, curActor will become NULL! it may be
        // assigned later in appendObject() method)
        curActor = field.getObjectById(aggressor ? AGGRESSOR_ID : DEFENDER_ID);
        enemyActor = field.getObjectById(aggressor ? DEFENDER_ID : AGGRESSOR_ID);
        newFieldEvent.actor = curActor;
        newFieldEvent.field = field;
        bus.raise(newFieldEvent);
    }

    public void appendObject(int number, int id, int xy) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            field.appendObject(number, id, xy);
            if (id == AGGRESSOR_ID || id == DEFENDER_ID) {
                curActor = field.getObjectById(aggressor ? AGGRESSOR_ID : DEFENDER_ID);
                enemyActor = field.getObjectById(aggressor ? DEFENDER_ID : AGGRESSOR_ID);
            }
        }
    }

    public void setStylePack(int pack) {
        stylePack = pack;
        styleChangedEvent.stylePack = pack;
        bus.raise(styleChangedEvent);
    }

    public void setXy(int number, int id, int xy, boolean reset) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            if (xy == Field.TRASH_CELL) {
                CellObject obj = field.getObjectByNumber(number);
                if (obj != null) {
                    objectRemovedEvent.oldXy = obj.xy;
                    objectRemovedEvent.obj = obj;
                    bus.raise(objectRemovedEvent);
                }
            }
            if (reset) {
                if (curActor.getNumber() == number) {
                    actorResetEvent.obj = curActor;
                    bus.raise(actorResetEvent);
                } else if (enemyActor.getNumber() == number) {
                    actorResetEvent.obj = enemyActor;
                    bus.raise(actorResetEvent);
                }
            }
            field.setXy(number, id, xy);
        }
    }

    public void setScore(int score1, int score2) {
        scoreChangedEvent.score1 = score1;
        scoreChangedEvent.score2 = score2;
        bus.raise(scoreChangedEvent);
    }

    public void setThing(int thingId) {
        CellObject oldThing = curThing;
        curThing = Cell.newObject(thingId, Field.TRASH_CELL, Field.ZeroNumber);
        thingChangedEvent.oldThing = oldThing;
        thingChangedEvent.newThing = curThing;
        thingChangedEvent.mine = true;
        bus.raise(thingChangedEvent);
    }

    public void setEnemyThing(int thingId) {
        CellObject oldThing = enemyThing;
        enemyThing = Cell.newObject(thingId, Field.TRASH_CELL, Field.ZeroNumber);
        thingChangedEvent.oldThing = oldThing;
        thingChangedEvent.newThing = enemyThing;
        thingChangedEvent.mine = false;
        bus.raise(thingChangedEvent);
    }

    public void setPlayerWounded(boolean me, int cause, int myLives, int enemyLives) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            CellObject actor = field.getObjectById(me == aggressor ? AGGRESSOR_ID : DEFENDER_ID);
            assert actor != null;
            if (0 <= cause && cause < hurtCauseValues.length) {
                playerWoundedEvent.xy = actor.xy;
                playerWoundedEvent.cause = hurtCauseValues[cause];
                playerWoundedEvent.myLives = myLives;
                playerWoundedEvent.enemyLives = enemyLives;
                bus.raise(playerWoundedEvent);
            }
        }
    }

    public void addEffect(int effectId, int objNumber) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            if (0 <= effectId && effectId < effectValues.length) {
                field.setEffect(objNumber, effectValues[effectId]);
                effectAddedEvent.effect = effectValues[effectId];
                bus.raise(effectAddedEvent);
            }
        }
    }

    public void removeEffect(int objNumber) {
        addEffect(0, objNumber);
    }

    public void roundFinished(boolean winner, int totalScore1, int totalScore2) {
        roundFinishedEvent.winner = winner;
        roundFinishedEvent.detractor1 = getDetractor1();
        roundFinishedEvent.detractor2 = getDetractor2();
        roundFinishedEvent.totalScore1 = totalScore1;
        roundFinishedEvent.totalScore2 = totalScore2;
        bus.raise(roundFinishedEvent);
    }

    public void gameFinished(boolean winner, int totalScore1, int totalScore2, int reward) {
        // updating history
        if (enemy.length() > 0) { // it may be empty, e.g. in the Training/Tutorial Level
            // building a history item
            HistoryItem item = new HistoryItem(new Date(), winner, getDetractor1(), getDetractor2(), character1,
                    character2, totalScore1, totalScore2);

            // prepend the item into the current history (and delete old items if necessary)
            List<HistoryItem> lst = new LinkedList<HistoryItem>(history);
            lst.add(0, item);
            while (lst.size() > HISTORY_MAX)
                lst.remove(HISTORY_MAX);
            history.clear();
            history.addAll(lst);

            // store the current history in the local storage
            if (fileReader != null)
                fileReader.serialize(String.format("%s%s", HISTORY_PREFIX, name), lst);
        }

        // reset reference to a field
        field = null;
        bus.raise(new EventBus.GameFinishedEvent(winner, getDetractor1(), getDetractor2(), totalScore1, totalScore2,
                reward));
    }

    public void setAbilities(IIntArray ids) {
        assert ids != null;
        synchronized (locker) {
            abilities.clear();
            for (int i = 0; i < ids.length(); i++) {
                int id = ids.get(i);
                if (0 <= id && id < abilityValues.length)
                    abilities.add(abilityValues[id]);
            }
        }
        bus.raise(new EventBus.AbilitiesChangedEvent(abilities));
    }

    public void setClientVersion(int minVersionH, int minVersionM, int minVersionL,
                                 int curVersionH, int curVersionM, int curVersionL) {
        String minVersion = String.format(Locale.getDefault(), "%d.%d.%d", minVersionH, minVersionM, minVersionL);
        String curVersion = String.format(Locale.getDefault(), "%d.%d.%d", curVersionH, curVersionM, curVersionL);
        boolean versionAllowed = Winesaps.VERSION >= ((minVersionH << 16) | (minVersionM << 8) | minVersionL);
        boolean newVersionAvailable = Winesaps.VERSION < ((curVersionH << 16) | (curVersionM << 8) | curVersionL);
        if (!versionAllowed)
            bus.raise(new EventBus.VersionNotAllowedEvent(minVersion));
        if (newVersionAvailable)
            bus.raise(new EventBus.NewVersionAvailableEvent(curVersion));
    }

    public void setUnsupportedProtocol() {
        bus.raise(new EventBus.UnsupportedProtocolEvent());
    }

    public void setUserBusy(boolean aggressor) {
        bus.raise(aggressor ? new EventBus.AggressorBusyEvent() : new EventBus.DefenderBusyEvent());
    }

    public void setBattleNotFound() {
        // IMPORTANT! this error is possible on a battle finish in case of slow network (when a user sends MOVE, the
        // battle still exists, but when the server receives it (in 40-50ms), the battle is already done)
        // so we make a guard function (if it happens >10 times => ring the alarm)
        if (++battleNotFoundGuardCounter >= 10) {
            battleNotFoundGuardCounter = 0;
            bus.raise(new EventBus.BattleNotFoundEvent());
        }
    }

    public void setEnemyNotFound() {
        bus.raise(new EventBus.EnemyNotFoundEvent());
    }

    public void setWaitingForEnemy() {
        bus.raise(new EventBus.WaitingForEnemyEvent());
    }

    public void setAttackYourself() {
        bus.raise(new EventBus.AttackedYourselfEvent());
    }

    public void setAddFriendError() {
        bus.raise(new EventBus.AddFriendErrorEvent());
    }

    public void setNoCrystals() {
        bus.raise(new EventBus.NoCrystalsEvent());
    }

    public void setIncorrectCredentials() {
        bus.raise(new EventBus.IncorrectCredentialsEvent());
    }

    public void setIncorrectName() {
        bus.raise(new EventBus.IncorrectNameEvent());
    }

    public void setIncorrectEmail() {
        bus.raise(new EventBus.IncorrectEmailEvent());
    }

    public void setDuplicateName() {
        bus.raise(new EventBus.DuplicateNameEvent());
    }

    public void setSignUpError() {
        bus.raise(new EventBus.SignUpErrorEvent());
    }

    public void setServerGonnaStop() {
        bus.raise(new EventBus.ServerGonnaStopEvent());
    }

    private void debugProtocol(PsObject psObject) {
        if (sender != null) {
            psObject.runDaemon(8000, 200, new Runnable() {
                @Override
                public void run() {
                    if (++debugCounter > 10000) System.exit(0);
                    long t0 = System.currentTimeMillis();
                    sender.send(0xF2, debugCounter / 256, debugCounter % 256,
                            (int) ((t0 >> 56) & 0xFF),
                            (int) ((t0 >> 48) & 0xFF),
                            (int) ((t0 >> 40) & 0xFF),
                            (int) ((t0 >> 32) & 0xFF),
                            (int) ((t0 >> 24) & 0xFF),
                            (int) ((t0 >> 16) & 0xFF),
                            (int) ((t0 >> 8) & 0xFF),
                            (int) (t0 & 0xFF),
                            11, 22, 33, 44, 55, 66, 77); // 7b of fake data to reach total 32 bytes;
                }
            });
        }
    }
}

// note#2 (@mitrakov, 2017-04-03): it'd be better use SkipListMap, but it's not supported by Android API 8
//
// note#4 (@mitrakov, 2017-04-21): suppose the server has been suddenly restarted; a user may request smth (e.g. Random
// Opponent); server won't respond and the "Connecting" dialog will be shown; then after re-connecting a user may retry
// its request (Random Opponent), but the server will return "NO_USER_FOUND" so that a client will need to re-sign in.
// it means that the request will also fail; to resolve this problem here we send a fake request (e.g. USER_INFO)
// intentionally to obtain "NO_USER_FOUND" and afterwards re-sign in.
// Someone may ask "what if send SIGN_IN right away?" It's a mistake because client might just been re-connected
// without the server being restarted, so that requesting SIGN_IN would be erroneous
