package ru.mitrakov.self.rush.model;

import java.util.*;
import java.security.*;
import java.math.BigInteger;
import java.util.concurrent.*;

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
    public interface ISender {
        void send(Cmd cmd);

        void send(Cmd cmd, int arg);

        void send(Cmd cmd, int arg1, int arg2);

        void send(Cmd cmd, String arg);

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
        RECEIVE_TRAINING,  // 12
        RANGE_OF_PRODUCTS, // 13
        BUY_PRODUCT,       // 14
        RESERVED_0F,       // 15
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
        CHECK_PURCHASE     // 39
    }

    public enum Character {None, Rabbit, Hedgehog, Squirrel, Cat}

    public enum HurtCause {Poisoned, Sunk, Soaked, Devoured, Exploded}

    public enum Effect {None, Antidote, Dazzle, Afraid, Attention}

    public enum MoveDirection {LeftDown, Left, LeftUp, RightDown, Right, RightUp}

    /**
     * ability list; some abilities are stubs (a7, a8, up to a32), because skills start with an index=33
     */
    public enum Ability {
        None, Snorkel, ClimbingShoes, SouthWester, VoodooMask, Snowshoes, Sunglasses,
        a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19,
        a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32,
        Miner, Builder, Shaman, Grenadier, Spy
    }

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
    public volatile boolean connected = true;
    public volatile boolean newbie = true;
    public volatile int totalScore1 = 0;
    public volatile int totalScore2 = 0;
    public volatile int roundLengthSec = 60;
    public volatile int stylePack = 0;
    public volatile long abilityExpireTime = 0;
    public volatile long roundStartTime = 0;
    public volatile Field field;
    public volatile CellObject curActor;

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
    public /*private*/ IFileReader fileReader; // public for debug purposes only!
    public /*private*/ String hash = "";       // public for debug purposes only!

    public Model() {
        // create timer to ping the server (otherwise the server will make "signOut due to inaction")
        new Timer("Ping timer", true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (authorized)
                    getUserInfo();
            }
        }, PING_PERIOD_MSEC, PING_PERIOD_MSEC);
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
                if (settings.length > 2) {
                    language = settings[0];
                    notifyNewBattles = settings[1].equals("1");
                    name = settings[2];
                    bus.raise(new EventBus.NameChangedEvent(name));
                    if (settings.length > 3)
                        hash = settings[3];
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
            String s = String.format("%s %s %s %s", language, notifyNewBattles ? "1" : "0", name, hash);
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

    public boolean friendExists(String name) {
        for (FriendItem item : friends) { // in Java 8 may be replaced with lambda
            if (item.name.equals(name))
                return true;
        }
        return false;
    }

    // ==============================
    // === SERVER REQUEST METHODS ===
    // ==============================
    // Feel free to call these methods from anywhere
    // ==============================

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

    public void receiveTraining() {
        if (connected && sender != null) {
            sender.send(RECEIVE_TRAINING);
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
            switch (direction) {
                case LeftDown:
                    if (curActor.getX() == 0 && curActor.getY() == Field.HEIGHT - 1) return;
                    break;
                case Left:
                    if (curActor.getX() == 0) return;
                    break;
                case LeftUp:
                    if (curActor.getX() == 0 && curActor.getY() == 0) return;
                    break;
                case RightDown:
                    if (curActor.getX() == Field.WIDTH - 1 && curActor.getY() == Field.HEIGHT - 1) return;
                    break;
                case Right:
                    if (curActor.getX() == Field.WIDTH - 1) return;
                    break;
                case RightUp:
                    if (curActor.getX() == Field.WIDTH - 1 && curActor.getY() == 0) return;
                    break;
            }
            sender.send(MOVE, direction.ordinal());
        }
    }

    /**
     * Sends USE_THING battle command to the server
     */
    public void useThing() {
        if (connected && sender != null && curThing != null) {
            sender.send(USE_THING, curThing.getId());
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
            if (ability.ordinal() > SKILL_OFFSET) // only skills may be used
                sender.send(USE_SKILL, ability.ordinal());
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
            else signIn();     // connected and not authorized: try to sign in using stored credentials
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
        Character[] characters = Character.values();
        int ch = data.get(i++);
        if (0 <= ch && ch < characters.length && character != characters[ch]) {
            character = characters[ch];
            bus.raise(new EventBus.CharacterChangedEvent(character));
        }

        // parse crystals
        if (i + 3 < data.length()) {
            int crystals = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | (data.get(i + 3));
            bus.raise(new EventBus.CrystalChangedEvent(crystals));
        }
        i += 4;

        // parse abilities
        Ability[] array = Ability.values();
        synchronized (locker) {
            abilityExpireMap.clear();
            int abilitiesCnt = data.get(i++);
            for (int j = 0; j < abilitiesCnt; j++, i += 3) {
                if (i + 2 < data.length()) {
                    int id = data.get(i);
                    int minutes = data.get(i + 1) * 256 + data.get(i + 2);
                    if (0 <= id && id < array.length)
                        abilityExpireMap.put(array[id], minutes);
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

    public void setVictim(String victimName) {
        enemy = victimName;
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
        Character[] characters = Character.values();

        synchronized (locker) {
            if (!append)
                friends.clear();
            String s = data.toUTF8();  // example: \3Tommy\0\2Bobby\0\3Billy\0
            if (s.length() > 0) { // be careful! if s == "", then s.split("\0") returns Array("") instead of Array()
                for (String item : s.split("\0")) {
                    byte ch = (byte) item.charAt(0);
                    if (0 <= ch && ch < characters.length) {
                        friends.add(new FriendItem(characters[ch], item.substring(1)));
                    }
                }
            }
        }
        bus.raise(new EventBus.FriendListUpdatedEvent(friends));
    }

    public void friendAdded(int character, String name) {
        Character[] characters = Character.values();
        if (0 <= character && character < characters.length) {
            FriendItem item = new FriendItem(characters[character], name);
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
        Ability[] abs = Ability.values();
        synchronized (locker) {
            products.clear();
            for (int i = 0; i + 2 < data.length(); i += 3) {
                int id = data.get(i);
                int days = data.get(i + 1);
                int cost = data.get(i + 2);
                if (0 <= id && id < abs.length)
                    products.add(new Product(abs[id], days, cost));
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

    public void setPromocodeDone(String name, boolean inviter, int crystals) {
        assert name != null;
        bus.raise(new EventBus.PromocodeDoneEvent(name, inviter, crystals));
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
        curThing = enemyThing = curActor = null;

        Character[] characters = Character.values();
        if (0 <= character1 && character1 < characters.length)
            this.character1 = characters[character1];
        if (0 <= character2 && character2 < characters.length)
            this.character2 = characters[character2];

        roundLengthSec = timeSec;
        this.aggressor = aggressor;
        roundStartTime = System.currentTimeMillis();

        bus.raise(new EventBus.RoundStartedEvent(number));
        bus.raise(new EventBus.ScoreChangedEvent(0, 0));
        bus.raise(new EventBus.LivesChangedEvent(myLives, enemyLives));
        bus.raise(new EventBus.ThingChangedEvent(null, null, true));
        bus.raise(new EventBus.ThingChangedEvent(null, null, false));
    }

    public void setNewField(IIntArray fieldData) {
        Field field; // for multithreaded safety
        this.field = field = new Field(fieldData);
        // assign curActor (be careful! if "fieldData" doesn't contain actors, curActor will become NULL! it may be
        // assigned later in appendObject() method)
        curActor = field.getObjectById(aggressor ? AGGRESSOR_ID : DEFENDER_ID);
    }

    public void appendObject(int number, int id, int xy) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            field.appendObject(number, id, xy);
            if (id == AGGRESSOR_ID || id == DEFENDER_ID)
                curActor = field.getObjectById(aggressor ? AGGRESSOR_ID : DEFENDER_ID);
        }
    }

    public void setStylePack(int pack) {
        stylePack = pack;
        bus.raise(new EventBus.StyleChangedEvent(pack));
    }

    public void setXy(int number, int xy) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            if (xy == Field.TRASH_CELL) {
                CellObject obj = field.getObjectByNumber(number);
                if (obj instanceof Cells.Mine)
                    bus.raise(new EventBus.MineExplodedEvent(obj.xy));
            }
            field.setXy(number, xy);
        }
    }

    public void setScore(int score1, int score2) {
        bus.raise(new EventBus.ScoreChangedEvent(score1, score2));
    }

    public void setThing(int thingId) {
        CellObject oldThing = curThing;
        curThing = Cell.newObject(thingId, 0xFF, Field.ZeroNumber);
        bus.raise(new EventBus.ThingChangedEvent(oldThing, curThing, true));
    }

    public void setEnemyThing(int thingId) {
        CellObject oldThing = enemyThing;
        enemyThing = Cell.newObject(thingId, 0xFF, Field.ZeroNumber);
        bus.raise(new EventBus.ThingChangedEvent(oldThing, enemyThing, false));
    }

    public void setPlayerWounded(boolean me, int cause, int myLives, int enemyLives) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            CellObject actor = field.getObjectById(me == aggressor ? AGGRESSOR_ID : DEFENDER_ID);
            assert actor != null;
            HurtCause[] causes = HurtCause.values();
            if (0 <= cause && cause < causes.length)
                bus.raise(new EventBus.PlayerWoundedEvent(actor.xy, causes[cause], myLives, enemyLives));
        }
    }

    public void addEffect(int effectId, int objNumber) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            Effect[] effects = Effect.values();
            if (0 <= effectId && effectId < effects.length)
                field.setEffect(objNumber, effects[effectId]);
        }
    }

    public void removeEffect(int objNumber) {
        addEffect(0, objNumber);
    }

    public void roundFinished(boolean winner, int totalScore1, int totalScore2) {
        this.totalScore1 = totalScore1;
        this.totalScore2 = totalScore2;
        bus.raise(new EventBus.RoundFinishedEvent(winner));
    }

    public void gameFinished(boolean winner) {
        // updating history
        if (enemy.length() > 0) { // it may be empty, e.g. in the Training Level
            // building a history item
            HistoryItem item = new HistoryItem(new Date(), winner, aggressor ? name : enemy, aggressor ? enemy : name,
                    character1, character2, totalScore1, totalScore2);

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
        bus.raise(new EventBus.GameFinishedEvent(winner));
    }

    public void setAbilities(IIntArray ids) {
        assert ids != null;
        synchronized (locker) {
            abilities.clear();
            Ability[] array = Ability.values();
            for (int i = 0; i < ids.length(); i++) {
                int id = ids.get(i);
                if (0 <= id && id < array.length)
                    abilities.add(array[id]);
            }
        }
        bus.raise(new EventBus.AbilitiesChangedEvent(abilities));
    }

    public void setUserBusy(boolean aggressor) {
        bus.raise(aggressor ? new EventBus.AggressorBusyEvent() : new EventBus.DefenderBusyEvent());
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
