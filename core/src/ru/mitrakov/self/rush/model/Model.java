package ru.mitrakov.self.rush.model;

import java.util.*;
import java.security.*;
import java.math.BigInteger;
import java.util.concurrent.*;

import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.Winesaps;
import ru.mitrakov.self.rush.model.Cells.CellObject;
import ru.mitrakov.self.rush.utils.collections.IIntArray;
import ru.mitrakov.self.rush.model.emulator.ServerEmulator;

import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.model.Model.Cmd.*;

/**
 * This class represents a model in the MVC pattern
 * Class is intended to have a single instance
 * @author mitrakov
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Model {
    /** Size of the rating list (defined by server) */
    public static final int RATINGS_COUNT = 10;
    /** Total count of style packs */
    public static final int STYLES_COUNT = 4;
    /** Maximum items to keep in battle history */
    public static final int HISTORY_MAX = 32;
    /** Default length of md5 hash string */
    public static final int HASH_LENGTH = md5("hello world").length();

    // ===========================
    // ===  PUBLIC INTERFACES  ===
    // ===========================

    /**
     * Interface to send commands to the server
     */
    public interface ISender {
        /**
         * Sends single command to the server.
         * @see #send(int)
         * @param cmd command
         */
        void send(Cmd cmd);
        /**
         * Sends single command to the server.
         * @see #send(Cmd)
         * @param cmd command expressed as an integer
         */
        void send(int cmd);
        /**
         * Sends command with given byte arguments to the server.
         * @see #send(int, int...)
         * @param cmd command
         * @param arg arguments (please note they must be 0-255)
         */
        void send(Cmd cmd, int... arg);
        /**
         * Sends command with given byte arguments to the server.
         * @see #send(Cmd, int...)
         * @param cmd command expressed as an integer
         * @param arg arguments (please note they must be 0-255)
         */
        void send(int cmd, int... arg);
        /**
         * Sends command with a given string to the server.
         * @see #send(int, String)
         * @param cmd command
         * @param arg string argument that will be converted to UTF-8 byte array
         */
        void send(Cmd cmd, String arg);
        /**
         * Sends command with a given string to the server.
         * @see #send(Cmd, String)
         * @param cmd command expressed as an integer
         * @param arg string argument that will be converted to UTF-8 byte array
         */
        void send(int cmd, String arg);
        /**
         * Resets the internal state of the sender.
         */
        void reset();
    }

    /**
     * Interface to read/write files independent from a platform (Desktop, Android, etc.)
     */
    public interface IFileReader {
        /**
         * Writes a string to a file. The file may be overwritten by this operation.
         * @see #append(String, String) append
         * @see #serialize(String, Object) serialize
         * @param filename file name
         * @param s string to be written
         */
        void write(String filename, String s);
        /**
         * Appends a string to the end of a file.
         * @see #write(String, String) write
         * @see #serialize(String, Object) serialize
         * @param filename file name
         * @param s string to be written
         */
        void append(String filename, String s);
        /**
         * Reads a string from the file.
         * @see #readAsByteArray(String) readAsByteArray
         * @see #deserialize(String) deserialize
         * @param filename file name
         * @return content of the file represented as a String (may be NULL if the file doesn't exist)
         */
        String read(String filename);
        /**
         * Reads a byte array from the file.
         * @see #read(String) read
         * @see #deserialize(String) deserialize
         * @param filename file name
         * @return content of the file represented as a Byte Array (may be empty if the file doesn't exist)
         */
        byte[] readAsByteArray(String filename);
        /**
         * Reads an object from the file.
         * @see #read(String) read
         * @see #readAsByteArray(String) readAsByteArray
         * @param filename file name
         * @return content of the file represented as an arbitrary object (may be NULL if the file doesn't exist)
         */
        Object deserialize(String filename);
        /**
         * Writes an object to a file. The file may be overwritten by this operation.
         * @see #append(String, String) append
         * @see #write(String, String) write
         * @param filename file name
         * @param obj object (must be serializable)
         */
        void serialize(String filename, Object obj);
    }

    // ===========================
    // === PUBLIC ENUMERATIONS ===
    // ===========================

    /** Server-specific commands; for more details see docs to the protocol */
    public enum Cmd {
        UNSPEC_ERROR,       // 0
        SIGN_UP,            // 1
        SIGN_IN,            // 2
        SIGN_OUT,           // 3
        USER_INFO,          // 4
        CHANGE_CHARACTER,   // 5
        ATTACK,             // 6
        CALL,               // 7
        ACCEPT,             // 8
        REJECT,             // 9
        STOPCALL,           // 10
        CANCEL_CALL,        // 11
        RECEIVE_LEVEL,      // 12
        RANGE_OF_PRODUCTS,  // 13
        BUY_PRODUCT,        // 14
        ENEMY_NAME,         // 15
        FULL_STATE,         // 16
        ROUND_INFO,         // 17
        ABILITY_LIST,       // 18
        MOVE,               // 19
        USE_THING,          // 20
        USE_SKILL,          // 21
        GIVE_UP,            // 22
        STATE_CHANGED,      // 23
        SCORE_CHANGED,      // 24
        EFFECT_CHANGED,     // 25
        PLAYER_WOUNDED,     // 26
        THING_TAKEN,        // 27
        OBJECT_APPENDED,    // 28
        FINISHED,           // 29
        RESTORE_STATE,      // 30
        RESERVED_1F,        // 31
        RATING,             // 32
        FRIEND_LIST,        // 33
        ADD_FRIEND,         // 34
        REMOVE_FRIEND,      // 35
        CHECK_PROMOCODE,    // 36
        PROMOCODE_DONE,     // 37
        GET_SKU_GEMS,       // 38
        CHECK_PURCHASE,     // 39
        GET_CLIENT_VERSION, // 40
        CHANGE_PASSWORD,    // 41
    }

    /** Ability list; some abilities are stubs (a7, a8, up to a32), because skills start with an index=33 */
    public enum Ability {
        None, Snorkel, ClimbingShoes, SouthWester, VoodooMask, SapperShoes, Sunglasses,
        a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, SpPack2 /* since 2.0.0 */, a19,
        a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32,
        Miner, Builder, Shaman, Grenadier, TeleportMan
    }

    /** Characters (defined by Server, started with 1) */
    public enum Character {
        None, Rabbit, Hedgehog, Squirrel, Cat
    }

    /** Hurt causes, useful for playing sounds, animation effects, etc. (defined by Server) */
    public enum HurtCause {
        Poisoned, Sunk, Soaked, Devoured, Exploded
    }

    /** Effect that may be applied to an actor (defined by Server, 0 means no effect) */
    public enum Effect {
        None, Antidote, Dazzle, Afraid, Attention
    }

    /** Move direction constants (defined by Server) */
    public enum MoveDirection {
        LeftDown, Left, LeftUp, RightDown, Right, RightUp
    }

    /** Rating enumeration (General, Weekly, etc.); constants (0, 1) are specified by the server */
    public enum RatingType {
        General, Weekly
    }

    // ===========================
    // === ENUMERATIONS VALUES ===
    // ===========================
    // these are intended to decrease Garbage Collector pressure, e.g. by replacing 'foreach' loop with a usual 'for'
    // (because foreach loops create new iterators that are gonna be subject to GC)
    // ===========================

    public static final Cmd[] cmdValues = Cmd.values();
    public static final Effect[] effectValues = Effect.values();
    public static final Ability[] abilityValues = Ability.values();
    public static final Character[] characterValues = Character.values();
    public static final HurtCause[] hurtCauseValues = HurtCause.values();
    public static final MoveDirection[] moveDirectionValues = MoveDirection.values();

    // =============================
    // === PUBLIC STATIC METHODS ===
    // =============================

    /**
     * @param s input string
     * @return md5 hash of string <b>s</b> represented as a HEX String
     */
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

    /** My name */
    public volatile String name = "";
    /** Current enemy's name (may be empty) */
    public volatile String enemy = "";
    /** Promo code */
    public volatile String promocode = "";
    /** My character */
    public volatile Character character = Character.None;
    /** Character 1 in a battle (usually on the left side of the battlefield) */
    public volatile Character character1 = Character.None;
    /** Character 2 in a battle (usually on the right side of the battlefield) */
    public volatile Character character2 = Character.None;
    /** "Connection to the Server" flag */
    public volatile boolean connected = false;
    /** Newbie flag (detected only on client side, may be incorrectly set to TRUE if a customer re-installed the app) */
    public volatile boolean newbie = true;
    /** Current round duration in sec (set by the Server in the beginning of the round) */
    public volatile int roundLengthSec = 90;
    /** Current style pack (level appearance, music, etc.) set by the Server in the beginning of the round */
    public volatile int stylePack = 0;
    /** Value that shows how long the chosen ability remains active (in msec) */
    public volatile long abilityExpireTime = 0;
    /** Start time of the round (useful to calculate the time left) */
    public volatile long roundStartTime = 0;
    /** Current battle field, may be NULL */
    public volatile Field field;
    /** Current actor in the battle mode, may be NULL outside the battle */
    public volatile CellObject curActor;
    /** Current enemy actor in the MultiPlayer battle mode, may be NULL outside the battle and in SinglePlayer mode */
    public volatile CellObject enemyActor;

    // ==================================================
    // === PUBLIC NON-VOLATILE CONCURRENT COLLECTIONS ===
    // ==================================================
    // getters are supposed to have a little overhead, so we make the fields "public" for efficiency; these collections
    // are rest upon Java-Concurrent Library, because they may be changed OUTSIDE the OpenGL loop at any moment;
    // all 'foreach' operations are considered to be safe
    // ==================================================

    /** Collection of products (product is a triple: [ability-price-days]) */
    public final Collection<Product> products = new ConcurrentLinkedQueue<Product>();
    /** Collection of user's history items) */
    public final Collection<HistoryItem> history = new ConcurrentLinkedQueue<HistoryItem>();
    /** Collection of user's friends */
    public final Collection<FriendItem> friends = new ConcurrentLinkedQueue<FriendItem>();
    /** Collection of user's abilities (the same as abilityExpireMap.keySet() but it helps to reduce GC pressure) */
    public final Collection<Ability> userAbilities = new ConcurrentLinkedQueue<Ability>();
    /** Map: user ability -> minutes left (these abilities belong to a user, not to an actor in the battle) */
    public final Map<Ability, Integer> abilityExpireMap = new ConcurrentHashMap<Ability, Integer>(); // see note#2

    // ===========================
    // === PUBLIC FINAL FIELDS ===
    // ===========================

    /** Event Bus */
    public final EventBus bus = new EventBus();

    // ================
    // === SETTINGS ===
    // ================

    /** If TRUE, a user will be notified about new invitations */
    public volatile boolean notifyNewBattles = true;
    /** GUI language (supported: en, es, pt, fr, ru) */
    public volatile String language = "en";
    /** If TRUE, music is turned on */
    public volatile boolean music = true;
    /** If TRUE, sound effects are turned on */
    public volatile boolean soundEffects = true;

    // ================================
    // === PRIVATE STATIC CONSTANTS ===
    // ================================

    /** Aggressor ID by Server API */
    private static final int AGGRESSOR_ID = 4;
    /** Defender ID by Server API */
    private static final int DEFENDER_ID = 5;
    /** Duration to ping the Server for UserInfo (msec) */
    private static final int PING_PERIOD_MSEC = 60000;
    /** Offset for skills (swaggas (1-15), spPacks (16-31), skills (32+)) */
    private static final int SKILL_OFFSET = 0x20;
    /** Standard promo code length (not necessary, just extra check to decrease Server calls) */
    private static final int PROMOCODE_LEN = 5;
    /** History path prefix */
    private static final String HISTORY_PREFIX = "history/";
    /** Settings filename */
    public /*private*/ static final String SETTINGS_FILE = "settings"; // public for debug purposes only!

    // ======================
    // === PRIVATE EVENTS ===
    // ======================
    // these events are extracted to fields ONLY FOR DECREASING GC PRESSURE during a battle!
    // the other events can be emitted over usual "new EventBus.XxxEvent(...)" notation
    // ==================================================

    private final EventBus.NewFieldEvent newFieldEvent = new EventBus.NewFieldEvent(null, null);
    private final EventBus.MoveResponseEvent moveResponseEvent = new EventBus.MoveResponseEvent();
    private final EventBus.ActorResetEvent actorResetEvent = new EventBus.ActorResetEvent(null);
    private final EventBus.EffectAddedEvent effectAddedEvent = new EventBus.EffectAddedEvent(Effect.None);
    private final EventBus.RoundStartedEvent roundStartedEvent = new EventBus.RoundStartedEvent(0, "");
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

    /** Platform Specific Object */
    private final PsObject psObject;
    /** Locker object (only for synchronization purposes) */
    private final Object locker = new Object();
    /** Abilities that belong to an ACTOR in the battle (don't mix up with "userAbilities" that related to a USER) */
    private final Collection<Ability> abilities = new ConcurrentLinkedQueue<Ability>(); // hotfix: must be concurrent!
    /** List of senders (e.g sender to Emulator for SinglePlayer, sender to the Server for MultiPlayer, etc.) */
    private final List<ISender> senders = new LinkedList<ISender>();

    /** Current locale */
    private Locale locale = Locale.getDefault();
    /** Current sender (one of senders in "senders" collection) */
    private ISender sender;
    /** Authorized flag (if TRUE then the authorization to the Server passed) */
    private boolean authorized = false;
    /** Aggressor/defender flag */
    private boolean aggressor = true;
    /** Current thing (actual only during the battle) */
    private CellObject curThing;
    /** Current enemy thing (actual only during the battle in MultiPlayer) */
    private CellObject enemyThing;
    /** Only for debugging */
    private transient int debugCounter;
    /** External file reader */
    public /*private final*/ IFileReader fileReader;        // public for debug purposes only!
    /** User's password hashed with MD5 */
    public /*private*/ String hash = "";                    // public for debug purposes only!

    /**
     * Creates a new instance of Model
     * @param psObject platform specific object (NON-NULL)
     */
    public Model(PsObject psObject) {
        assert psObject != null;
        this.psObject = psObject;
        // create timer to ping the server (otherwise the server will make "signOut due to inaction")
        psObject.runDaemon(PING_PERIOD_MSEC, PING_PERIOD_MSEC, new Runnable() {
            @Override
            public void run() {
                if (authorized)
                    getUserInfo();
            }
        });
        // @note: for debugging the protocol comment runDaemon above and run debugProtocol(psObject);
    }

    // ==========================
    // === NON-SERVER METHODS ===
    // ==========================

    /**
     * Sets a new sender to the model
     * @param senders list of senders (first sender becomes default)
     */
    public void setSenders(ISender... senders) {
        assert senders.length > 0;
        this.senders.clear();
        this.senders.addAll(Arrays.asList(senders));
        this.sender = senders[0];
    }

    /**
     * Sets a new file reader to the model
     * @param fileReader file reader (may be NULL)
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
                    // settings 0-4 are always fixed
                    language = settings[0];
                    notifyNewBattles = settings[1].equals("1");
                    music = settings[2].equals("1");
                    soundEffects = settings[3].equals("1");
                    name = settings[4];
                    bus.raise(new EventBus.NameChangedEvent(name));
                    // other settings may differ from version to version (since 2.0.0)
                    for (int i = 5; i < settings.length; i++) {
                        String st = settings[i];
                        if (st.length() == HASH_LENGTH)
                            hash = st;
                        else if (st.length() % SINGLE_PLAYER_PACK_SIZE == 0)
                            singlePlayerProgress = st;
                    }
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
            String s = String.format(locale,"%s %s %s %s %s %s %s", language, notifyNewBattles ? "1" : "0",
                    music ? "1" : "0", soundEffects ? "1" : "0", name, hash, singlePlayerProgress);
            fileReader.write(SETTINGS_FILE, s);
        }
    }

    /**
     * @param ability ability (if NULL then returns empty list)
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

    /**
     * @param name friend's name
     * @return TRUE if the user has got a friend with the given name
     */
    public boolean friendExists(String name) {
        for (FriendItem item : friends) { // in Java 8 may be replaced with lambda
            if (item.name.equals(name))
                return true;
        }
        return false;
    }

    /**
     * @return string representation of a current agent (e.g. OS, platform, GUI language, client version etc.)
     */
    public String getAgentInfo() {
        String os = System.getProperty("os.name");
        // String.format("1.%s.%s.%s.%s", language, Winesaps.VERSION_STR, psObject.getPlatform(), os);
        return String.format("2;%s;%s;%s;%s;%s",
                language, Winesaps.VERSION_STR, psObject.getPlatform(), os, singlePlayerProgress);
    }

    /**
     * @return name of detractor1 in current battle
     */
    public String getDetractor1() {
        return aggressor ? name : enemy;
    }

    /**
     * @return name of detractor2 in current battle
     */
    public String getDetractor2() {
        return aggressor ? enemy : name;
    }

    // ==============================
    // === SERVER REQUEST METHODS ===
    // ==============================
    // feel free to call these methods from anywhere
    // ==============================

    /**
     * Sends GET_CLIENT_VERSION command to the server
     */
    public void checkVersion() {
        if (connected && sender != null) {
            sender.send(GET_CLIENT_VERSION);
        }
    }

    /**
     * Sends SIGN_IN command to the server with current username and current password.
     * Please note that this method does nothing if username/password are undefined
     */
    public void signIn() {
        assert name != null && hash != null;
        if (name.length() > 0 && hash.length() > 0 && connected && sender != null) { // don't use method 'isEmpty()'
            sender.reset();
            sender.send(SIGN_IN, String.format("\1%s\0%s\0%s", name, hash, getAgentInfo())); // \1 = Local auth
        }
    }

    /**
     * Sends SIGN_IN command to the server
     * @param login user name
     * @param password password
     */
    public void signIn(String login, String password) {
        if (connected && sender != null) {
            hash = md5(password);
            sender.reset();
            sender.send(SIGN_IN, String.format("\1%s\0%s\0%s", login, hash, getAgentInfo())); // \1 = Local auth
        }
    }

    /**
     * Sends SIGN_UP command to the server
     * @param login user name
     * @param password password
     * @param email email address
     */
    public void signUp(String login, String password, String email, String promocode) {
        assert login != null && password != null && email != null && promocode != null;
        if (connected && sender != null && password.length() >= 4) {
            hash = md5(password);
            sender.reset();
            sender.send(SIGN_UP, String.format("%s\0%s\0%s\0%s\0%s", login, hash, getAgentInfo(), email, promocode));
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

    /**
     * Sends USER_INFO command to the server
     */
    public void getUserInfo() {
        if (connected && sender != null) {
            sender.send(USER_INFO);
        }
    }

    /**
     * Sends INVITE command to the server (by name)
     * @param victim victim user name
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

    /**
     * Sends RECEIVE_LEVEL command to the server
     * @param levelName level name, e.g. "training.level"
     */
    public void receiveLevel(String levelName) {
        if (connected && sender != null) {
            sender.send(RECEIVE_LEVEL, levelName);
        }
    }

    /**
     * Sends CHANGE_CHARACTER command to the server
     * @param character new character (must be neither NULL nor None)
     */
    public void changeCharacter(Character character) {
        if (character != null && character != Character.None) {
            this.character = character; // assign character right away to avoid bugs in Emulator tutorial (since 2.0.0)
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
            sender.send(FRIEND_LIST, 1); // 1 = show_statuses (Server API 1.2.0+ supports statuses)
        }
    }

    /**
     * Sends ADD_FRIEND command to the server
     * @param name friend user name
     */
    public void addFriend(String name) {
        if (connected && sender != null) {
            if (name.length() > 0)
                sender.send(ADD_FRIEND, name);
        }
    }

    /**
     * Sends REMOVE_FRIEND command to the server
     * @param name quondam friend name
     */
    public void removeFriend(String name) {
        if (connected && sender != null) {
            if (name.length() > 0)
                sender.send(REMOVE_FRIEND, name);
        }
    }

    /**
     * Sends RATING command to the server
     * @param type type of rating (General, Weekly, etc.)
     */
    public void getRating(RatingType type) {
        assert type != null;
        if (connected && sender != null) {
            sender.send(RATING, type.ordinal());
        }
    }

    /**
     * Sends CHECK_PROMOCODE command to the server
     * (note: if the length of promo code is less that {@link Model#PROMOCODE_LEN}, the method does nothing)
     * @param promocode promo code
     */
    public void checkPromocode(String promocode) {
        assert promocode != null;
        if (connected && sender != null && promocode.length() >= PROMOCODE_LEN) {
            sender.send(CHECK_PROMOCODE, promocode);
        }
    }

    /**
     * Sends BUY_PRODUCT command to the server
     * @param product product to buy
     */
    public void buyProduct(Product product) {
        assert product != null;
        if (connected && sender != null) {
            sender.send(BUY_PRODUCT, product.ability.ordinal(), product.days);
        }
    }

    /**
     * Sends CANCEL_CALL command to the server
     */
    public void cancelCall() {
        if (connected && sender != null) {
            sender.send(CANCEL_CALL);
        }
    }

    /**
     * Sends MOVE battle command to the server
     * @param direction MoveDirection (Left, Right and so on)
     * @return true, if MOVE sent successfully (otherwise false, e.g. if this action is found to be useless)
     */
    public boolean move(MoveDirection direction) {
        if (connected && sender != null && curActor != null) {
            // simple checks to relieve the server
            // DO NOT use switch(direction)!!! It causes call MoveDirection.values() that produces work for GC!
            if (direction == MoveDirection.LeftDown) {
                if (curActor.getX() == 0 && curActor.getY() == Field.HEIGHT - 1) return false;
            } else if (direction == MoveDirection.Left) {
                if (curActor.getX() == 0) return false;
            } else if (direction == MoveDirection.LeftUp) {
                if (curActor.getX() == 0 && curActor.getY() == 0) return false;
            } else if (direction == MoveDirection.RightDown) {
                if (curActor.getX() == Field.WIDTH - 1 && curActor.getY() == Field.HEIGHT - 1) return false;
            } else if (direction == MoveDirection.Right) {
                if (curActor.getX() == Field.WIDTH - 1) return false;
            } else if (direction == MoveDirection.RightUp) {
                if (curActor.getX() == Field.WIDTH - 1 && curActor.getY() == 0) return false;
            }
            sender.send(MOVE, Arrays.binarySearch(moveDirectionValues, direction)); // avoid "direction.ordinal()" (GC)
            return true;
        }
        return false;
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
     * @param ability ability to use (it must be a SKILL, i.e. has a number > SKILL_OFFSET)
     */
    public void useAbility(Ability ability) {
        assert ability != null;
        if (connected && sender != null) {
            int code = Arrays.binarySearch(abilityValues, ability); // don't use "ability.ordinal()" (GC pressure)
            if (code > SKILL_OFFSET) // only skills may be used
                sender.send(USE_SKILL, code);
        }
    }

    /**
     * Sends USE_SKILL battle command to the server
     * @param index ability index
     */
    public void useAbility(int index) {
        int i = 0;
        for (Ability ability : abilities) {
            if (i++ == index)
                useAbility(ability);
        }
    }

    /**
     * Sends GIVE_UP battle command to the server<br>
     * <b>NOTE:</b> also resets the current battle field
     */
    public void giveUp() {
        field = null; // reset the current field
        if (connected && sender != null) {
            sender.send(GIVE_UP);
        }
    }

    /**
     * Sends GET_SKU_GEMS command to the server
     */
    public void requestSkuGems() {
        if (connected && sender != null) {
            sender.send(GET_SKU_GEMS);
        }
    }

    /**
     * Sends CHECK_PURCHASE command to the server
     * @param data data
     * @param signature signature
     */
    public void checkPurchase(String data, String signature) {
        if (connected && sender != null) {
            sender.send(CHECK_PURCHASE, String.format("%s\0%s", data, signature));
        }
    }

    /**
     * Sends CHANGE_PASSWORD command to the server
     * @param newPassword new password (must have at least 4 characters)
     */
    public void changePassword(String newPassword) {
        if (connected && sender != null && newPassword.length() >= 4) {
            String newHash = md5(newPassword);
            sender.send(CHANGE_PASSWORD, String.format("%s\0%s", hash, newHash));
            hash = newHash;
            saveSettings();
        }
    }

    /**
     * Sends RESTORE_STATE command to the server;
     * Please use this method ONLY when a client was disconnected and connected again in a battle
     * @since ServerAPI 1.3.0
     */
    public void restoreState() {
        if (connected && sender != null) {
            sender.send(FULL_STATE);
            sender.send(RESTORE_STATE);
        }
    }

    // ===============================
    // === SERVER RESPONSE METHODS ===
    // ===============================
    // these methods are not expected to be called from external code
    // ===============================

    /**
     * Sets the current Model state connected/disconnected
     * @param value TRUE for connected, and FALSE for disconnected
     */
    public void setConnected(boolean value) {
        if (!connected && value) { // if changed "not_connected" -> "connected"
            connected = true;  // we must change it before calling getUserInfo() or signIn()
            if (authorized)
                getUserInfo(); // connected, but already authorized? possibly the server has been restarted: see note#4
            else {
                checkVersion();
                signIn();      // connected and not authorized: try to sign in using stored credentials
            }
        }
        connected = value;
        bus.raise(new EventBus.ConnectedChangeEvent(connected));
    }

    /**
     * Sets the current Model state authorized/not_authorized.
     * Also, as a side effect, if authorized, sends RANGE_OF_PRODUCTS and FRIEND_LIST commands to the server
     * @param value TRUE for authorized, FALSE for not authorized
     */
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

    /**
     * Sets the current user info (character, abilities, gems, etc.)
     * @param data binary data from the server
     */
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

        // parse gems
        if (i + 3 < data.length()) {
            int gems = (data.get(i) << 24) | (data.get(i + 1) << 16) | (data.get(i + 2) << 8) | (data.get(i + 3));
            bus.raise(new EventBus.GemsChangedEvent(gems));
        }
        i += 4;

        // parse abilities
        synchronized (locker) {
            userAbilities.clear();
            abilityExpireMap.clear();
            int abilitiesCnt = data.get(i++);
            for (int j = 0; j < abilitiesCnt; j++, i += 3) {
                if (i + 2 < data.length()) {
                    int id = data.get(i);
                    int minutes = data.get(i + 1) * 256 + data.get(i + 2);
                    if (0 <= id && id < abilityValues.length) {
                        userAbilities.add(abilityValues[id]);
                        abilityExpireMap.put(abilityValues[id], minutes);
                    }
                }
            }
        }
        abilityExpireTime = System.currentTimeMillis();
        // fire the event
        bus.raise(new EventBus.AbilitiesExpireUpdatedEvent(userAbilities));

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

    /**
     * Sets the enemy name and shows a DialUp dialog
     * @param name enemy name
     */
    public void waitForEnemy(String name) {
        if (name.length() > 0) { // server can send empty name as a response of Quick Attack
            setEnemyName(name);
            bus.raise(new EventBus.DialUpEvent(name));
        }
    }

    /**
     * Sets the current enemy's name
     * @param name enemy name
     */
    public void setEnemyName(String name) {
        enemy = name;
    }

    /**
     * Invoked when a user is attacked by <b>aggressorName</b>
     * @param sid enemy Session ID (SID)
     * @param aggressorName enemy name
     */
    public void attacked(int sid, String aggressorName) {
        setEnemyName(aggressorName);
        bus.raise(new EventBus.InviteEvent(aggressorName, sid));
    }

    /**
     * Invoked when we had attacked an enemy but he/she rejected our invitation
     * @param coward enemy name
     */
    public void stopCallRejected(String coward) {
        bus.raise(new EventBus.StopCallRejectedEvent(coward));
    }

    /**
     * Invoked, as a reminder, when an enemy had attacked us, but we did not respond during the given timeout
     * @param aggressorName aggressor name
     */
    public void stopCallMissed(String aggressorName) {
        bus.raise(new EventBus.StopCallMissedEvent(aggressorName));
    }

    /**
     * Invoked when we had attacked an enemy but he/she did not respond during the given timeout
     * @param defenderName defender name
     */
    public void stopCallExpired(String defenderName) {
        bus.raise(new EventBus.StopCallExpiredEvent(defenderName));
    }

    /**
     * Sets the friends list (since Server API 1.2.0 also sets their statuses: online/offline)
     * @param data binary data from the server
     * @param append if TRUE, appends new item(s) to the list, if FALSE - overwrites the full list
     */
    public void setFriendList(IIntArray data, boolean append) {
        assert data != null;

        synchronized (locker) {
            if (!append)
                friends.clear();
            String s = data.toUTF8();  // example: \1\3Tommy\0\1\2Bobby\0\1\3Billy\0
            if (s.length() > 0) { // be careful! if s == "", then s.split("\0") returns Array("") instead of Array()
                for (String item : s.split("\0")) {
                    byte status = (byte) item.charAt(0); // Server API 1.2.0+ supports statuses
                    byte ch = (byte) item.charAt(1);
                    if (0 <= ch && ch < characterValues.length) {
                        friends.add(new FriendItem(characterValues[ch], item.substring(2), status));
                    }
                }
            }
        }
        bus.raise(new EventBus.FriendListUpdatedEvent(friends));
    }

    /**
     * Invoked when a new friend added
     * @param character friend's character (rabbit, hedgehog, squirrel, cat)
     * @param name friend's name
     */
    public void friendAdded(int character, String name) {
        if (0 <= character && character < characterValues.length) {
            FriendItem item = new FriendItem(characterValues[character], name, 0);
            friends.add(item);
            bus.raise(new EventBus.FriendAddedEvent(item));
        }
    }

    /**
     * Invoked when a friend is removed from the friends list
     * @param name friend's name
     */
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

    /**
     * Sets the range of products available (product is a triple: [ability-price-days])
     * @param data binary data from the server
     */
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

    /**
     * Sets the rating
     * @param type rating type (General, Weekly)
     * @param data binary data from the server
     */
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
            // add item to table
            rating.add(new RatingItem(name.toString(), wins, losses, score_diff));
            // additionally check if the user has a weak password (we will ping him if he's got 5 wins!)
            if (name.toString().equals(this.name))
                checkWeakPassword(wins);
        }

        bus.raise(new EventBus.RatingUpdatedEvent(type, rating));
    }

    /**
     * Sets whether the last sent to the server promo code is valid
     * @param valid TRUE if valid, FALSE - if invalid
     */
    public void setPromocodeValid(boolean valid) {
        bus.raise(new EventBus.PromocodeValidChangedEvent(valid));
    }

    /**
     * Invoked if either a user has successfully finished promo code, or we're inviter, and our friend has successfully
     * finished promo code
     * @param name name of our friend (either inviter, or newbie)
     * @param inviter whether a current user is inviter (TRUE), or our friend is inviter (FALSE)
     * @param gems reward
     */
    public void setPromocodeDone(String name, boolean inviter, int gems) {
        assert name != null;
        bus.raise(new EventBus.PromocodeDoneEvent(name, inviter, gems));
    }

    /**
     * Sets the SKU gems available (for small pack, standard pack, big pack etc.)
     * @param data binary data from the server
     */
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

    /**
     * Invoked when a payment request is completed
     * @param gems gems bought
     * @param coupon coupon for the next purchase
     */
    public void paymentDone(int gems, String coupon) {
        bus.raise(new EventBus.PaymentDoneEvent(gems, coupon));
    }

    /**
     * Sets the new round info in the battle mode
     * @param number number of the round (starting with 0)
     * @param timeSec round duration, in seconds
     * @param levelName name of the battle field
     * @param aggressor TRUE, if we're aggressor, FALSE otherwise
     * @param character1 character1 (represented by index, e.g. 1 is rabbit, 2 is hedgehog, etc.)
     * @param character2 character2 (represented by index, e.g. 1 is rabbit, 2 is hedgehog, etc.)
     * @param myLives my lives count (usually 2 per round)
     * @param enemyLives enemy's lives count (usually 2 per round)
     */
    public void setRoundInfo(int number, int timeSec, String levelName, boolean aggressor, int character1,
            int character2, int myLives, int enemyLives) {
        curThing = enemyThing = curActor = enemyActor = null;

        if (0 <= character1 && character1 < characterValues.length)
            this.character1 = characterValues[character1];
        if (0 <= character2 && character2 < characterValues.length)
            this.character2 = characterValues[character2];

        roundLengthSec = timeSec;
        this.aggressor = aggressor;
        roundStartTime = System.currentTimeMillis();

        // generate initial events
        roundStartedEvent.number = number;
        roundStartedEvent.levelName = levelName;
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

    /**
     * Sets the new battle field, that in turn switches the GUI to the battle mode
     * @param fieldData binary field data from the server
     */
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

    /**
     * Response on MOVE command
     */
    public void moveResponse() {
        bus.raise(moveResponseEvent);
    }

    /**
     * Invoked in the battle mode, when a new object is added to the battle field
     * @param number object number
     * @param id object ID
     * @param xy object coordinate, 0-255
     */
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

    /**
     * Sets a new style pack (level appearance, music, etc.)
     * @param pack style pack
     */
    public void setStylePack(int pack) {
        stylePack = pack;
        styleChangedEvent.stylePack = pack;
        bus.raise(styleChangedEvent);
    }

    /**
     * Set new position of an object in the battle mode
     * @param number object number
     * @param id object ID (just an additional check)
     * @param xy new coordinate (0-255)
     * @param reset if TRUE, then the object should be replaced immediately (without animations and so on)
     */
    public void setXy(int number, int id, int xy, boolean reset) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            if (xy == Field.TRASH_XY) {
                CellObject obj = field.getObjectByNumber(number);
                if (obj != null) {
                    objectRemovedEvent.oldXy = obj.getXy();
                    objectRemovedEvent.obj = obj;
                    bus.raise(objectRemovedEvent);
                }
            }
            if (reset) {
                if (curActor.getNumber() == number) {
                    actorResetEvent.obj = curActor;
                    bus.raise(actorResetEvent);
                } else if (enemyActor != null && enemyActor.getNumber() == number) {
                    actorResetEvent.obj = enemyActor;
                    bus.raise(actorResetEvent);
                }
            }
            field.setXy(number, id, xy);
        }
    }

    /**
     * Sets score in the battle mode (inside a round)
     * @param score1 score1
     * @param score2 score2
     */
    public void setScore(int score1, int score2) {
        scoreChangedEvent.score1 = score1;
        scoreChangedEvent.score2 = score2;
        bus.raise(scoreChangedEvent);
    }

    /**
     * Sets a new thing to an our actor in the battle mode
     * @param thingId thing ID
     */
    public void setThing(int thingId) {
        CellObject oldThing = curThing;
        curThing = Cell.newObject(thingId, Field.TRASH_CELL, null, 0);
        thingChangedEvent.oldThing = oldThing;
        thingChangedEvent.newThing = curThing;
        thingChangedEvent.mine = true;
        bus.raise(thingChangedEvent);
    }

    /**
     * Sets a new thing to an enemy actor in the battle mode
     * @param thingId thing ID
     */
    public void setEnemyThing(int thingId) {
        CellObject oldThing = enemyThing;
        enemyThing = Cell.newObject(thingId, Field.TRASH_CELL, null, 0);
        thingChangedEvent.oldThing = oldThing;
        thingChangedEvent.newThing = enemyThing;
        thingChangedEvent.mine = false;
        bus.raise(thingChangedEvent);
    }

    /**
     * Invoked when a player is wounded in the battle mode
     * @param me TRUE if me, FALSE if enemy
     * @param cause hurt cause, represented as Int
     * @param myLives my lives left
     * @param enemyLives enemy lives left
     */
    public void setPlayerWounded(boolean me, int cause, int myLives, int enemyLives) {
        Field field;
        synchronized (locker) {
            field = this.field;
        }
        if (field != null) {
            CellObject actor = field.getObjectById(me == aggressor ? AGGRESSOR_ID : DEFENDER_ID);
            assert actor != null;
            if (0 <= cause && cause < hurtCauseValues.length) {
                playerWoundedEvent.xy = actor.getXy();
                playerWoundedEvent.cause = hurtCauseValues[cause];
                playerWoundedEvent.myLives = myLives;
                playerWoundedEvent.enemyLives = enemyLives;
                bus.raise(playerWoundedEvent);
            }
        }
    }

    /**
     * Adds an effect on the given object in the battle mode
     * @param effectId effect ID
     * @param objNumber object number
     */
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

    /**
     * Removes effects from the given object in the battle mode
     * @param objNumber object number
     */
    public void removeEffect(int objNumber) {
        addEffect(0, objNumber);
    }

    /**
     * Invoked when a round is finished
     * @param winner TRUE, if we've won the round, FALSE - if the enemy has
     * @param totalScore1 total battle score1
     * @param totalScore2 total battle score2
     */
    public void roundFinished(boolean winner, int totalScore1, int totalScore2) {
        roundFinishedEvent.winner = winner;
        roundFinishedEvent.detractor1 = getDetractor1();
        roundFinishedEvent.detractor2 = getDetractor2();
        roundFinishedEvent.totalScore1 = totalScore1;
        roundFinishedEvent.totalScore2 = totalScore2;
        bus.raise(roundFinishedEvent);
    }

    /**
     * Invoked when a game finished. Please note that {@link Model#roundFinished(boolean, int, int)} is also invoked
     * @param winner TRUE, if we've won the battle, FALSE - if the enemy has
     * @param totalScore1 total battle score1
     * @param totalScore2 total battle score2
     * @param reward reward, in gems
     */
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

    /**
     * Sets the abilities list in the battle mode. Note that these abilities belong to an actor in the battle, not to a
     * user in general
     * @param ids ability IDs
     */
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

    /**
     * Informs that the server sends invitation to the enemy
     */
    public void setWaitingForEnemy() {
        bus.raise(new EventBus.WaitingForEnemyEvent());
    }

    /**
     * Invoked when a new client version is available
     * @param minVersionH minimal version accepted by the server (major)
     * @param minVersionM minimal version accepted by the server (minor)
     * @param minVersionL minimal version accepted by the server (maintenance)
     * @param curVersionH latest version available (major)
     * @param curVersionM latest version available (minor)
     * @param curVersionL latest version available (maintenance)
     */
    public void setClientVersion(int minVersionH, int minVersionM, int minVersionL,
            int curVersionH, int curVersionM, int curVersionL) {
        String minVersion = String.format(locale, "%d.%d.%d", minVersionH, minVersionM, minVersionL);
        String curVersion = String.format(locale, "%d.%d.%d", curVersionH, curVersionM, curVersionL);
        boolean versionAllowed = Winesaps.VERSION >= ((minVersionH << 16) | (minVersionM << 8) | minVersionL);
        boolean newVersionAvailable = Winesaps.VERSION < ((curVersionH << 16) | (curVersionM << 8) | curVersionL);
        if (!versionAllowed)
            bus.raise(new EventBus.VersionNotAllowedEvent(minVersion));
        if (newVersionAvailable)
            bus.raise(new EventBus.NewVersionAvailableEvent(curVersion));
    }

    // ======================
    // === ERROR HANDLING ===
    // ======================

    /**
     * Invoked on error: battle not found. Also resets the current battle field to NULL
     */
    public void setEmptyField() {
        field = null;
        bus.raise(new EventBus.BattleNotFoundEvent());
    }

    /**
     * Invoked on error: unsupported protocol
     */
    public void setUnsupportedProtocol() {
        bus.raise(new EventBus.UnsupportedProtocolEvent());
    }

    /**
     * Invoked on error: user is already in a battle and is not able to accept invitation
     * @param aggressor TRUE if aggressor is busy (just in case, in theory impossible), FALSE - if defender
     */
    public void setUserBusy(boolean aggressor) {
        bus.raise(aggressor ? new EventBus.AggressorBusyEvent() : new EventBus.DefenderBusyEvent());
    }

    /**
     * Invoked on error: enemy not found
     */
    public void setEnemyNotFound() {
        bus.raise(new EventBus.EnemyNotFoundEvent());
    }

    /**
     * Invoked on error: a user tried to attack himself
     */
    public void setAttackYourself() {
        bus.raise(new EventBus.AttackedYourselfEvent());
    }

    /**
     * Invoked on add friend error (e.g. a user is already our friend, or if no users found with a given name)
     */
    public void setAddFriendError() {
        bus.raise(new EventBus.AddFriendErrorEvent());
    }

    /**
     * Invoked on error: no gems enough to complete a given action
     */
    public void setNoGems() {
        bus.raise(new EventBus.NoGemsEvent());
    }

    /**
     * Invoked on error: username/password pair is incorrect
     */
    public void setIncorrectCredentials() {
        bus.raise(new EventBus.IncorrectCredentialsEvent());
    }

    /**
     * Invoked on error: invalid username (e.g. too small or contains incorrect characters)
     */
    public void setIncorrectName() {
        bus.raise(new EventBus.IncorrectNameEvent());
    }

    /**
     * Invoked on error: invalid e-mail address
     */
    public void setIncorrectEmail() {
        bus.raise(new EventBus.IncorrectEmailEvent());
    }

    /**
     * Invoked on error: username is already in use
     */
    public void setDuplicateName() {
        bus.raise(new EventBus.DuplicateNameEvent());
    }

    /**
     * Invoked on error: sign up error (on DB level, please refer to the Server docs for more details)
     */
    public void setSignUpError() {
        bus.raise(new EventBus.SignUpErrorEvent());
    }

    /**
     * Invoked on the server soft-shutdown
     */
    public void setServerGonnaStop() {
        bus.raise(new EventBus.ServerGonnaStopEvent());
    }

    // =======================
    // === PRIVATE METHODS ===
    // =======================

    /**
     * Checks whether the password is still "1234" and should be changed.
     * <b>Note:</b> method does nothing if a user has less than <b>winsCount</b> wins in order not to disturb the user
     * @param winsCount minimum count of wins, starting with which we should suggest a user to change default password
     */
    private void checkWeakPassword(int winsCount) {
        boolean passwordIs1234 = hash.equals("81dc9bdb52d04dc20036dbd8313ed055");
        if (passwordIs1234 && winsCount >= 5)
            bus.raise(new EventBus.WeakPasswordEvent());
    }

    /**
     * Only for internal debugging purposes
     * @param psObject Platform Specific Object
     */
    private void debugProtocol(PsObject psObject) {
        psObject.runDaemon(8000, 200, new Runnable() {
            @Override
            public void run() {
                if (++debugCounter > 2500) System.exit(0);
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

    // =============================
    // === SINGLE PLAYER SUPPORT ===
    // =============================
    // Fields and methods for the new feature: Single Player mode.
    // The idea is to introduce "Server Emulator" that simulates the actual server responses inside the client
    // Since 2.0.0
    // =============================

    /**
     * Total count of SinglePlayer levels packs
     * @since 2.0.0
     */
    public static final int SINGLE_PLAYER_PACKS_COUNT = 2;
    /**
     * Count of levels in SinglePlayer levels packs
     * @since 2.0.0
     */
    public static final int SINGLE_PLAYER_PACK_SIZE = 15;
    /**
     * Offset for SinglePlayer Levels Packs (swaggas (1-15), spPacks (16-31), skills (32+))
     * @since 2.0.0
     */
    private static final int SP_PACK_OFFSET = 0x10;

    /**
     * Server emulator (for SinglePlayer mode)
     * @since 2.0.0
     */
    public /*private final*/ ServerEmulator serverEmulator;

    /**
     * SinglePlayer progress (one char corresponds to a single level)
     * <ul>
     * <li>a = opened, 0 attempts</li>
     * <li>b = opened, 1 attempt</li>
     * <li>c = opened, 2 attempts</li>
     * <li>d = opened, 3+ attempts</li>
     * <li>e = done, gold budge</li>
     * <li>f = done, silver budge</li>
     * <li>g = done, bronze budge</li>
     * <li>h = done, no budge</li>
     * <li>i = closed</li>
     * </ul>
     * @since 2.0.0
     */
    public volatile String singlePlayerProgress = "aiiiiiiiiiiiiiiiiiiiiiiiiiiiii";

    /**
     * "Latest" flag for SinglePlayer (if TRUE, then a user has chosen the latest level, FALSE - otherwise)
     * <br>Note that the user might choose any previously completed level just for fun
     * @since 2.0.0
     */
    private boolean singlePlayerLatest;

    /**
     * Sets a new Server Emulator to the model (for SinglePlayer mode).
     * <b>NOTE:</b> call to this method DOES NOT switch the Model to SinglePlayer/MultiPlayer mode
     * @param emulator server emulator (may be NULL)
     * @since 2.0.0
     */
    public void setEmulator(ServerEmulator emulator) {
        this.serverEmulator = emulator;
    }

    /**
     * Switches the SinglePlayer/MultiPlayer mode
     * @param value TRUE for SinglePlayer mode, FALSE - for MultiPlayer
     * @since 2.0.0
     */
    public void setSinglePlayer(boolean value) {
        if (senders.size() == 2) {
            sender = value ? senders.get(1) : senders.get(0);
        }
    }

    /**
     * @return current SinglePlayer Pack number (from 1 to {@link #SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT})
     * @since 2.0.0
     */
    public int getCurSinglePlayerPack() {
        for (char c : "abcd".toCharArray()) {
            int result = singlePlayerProgress.indexOf(c);
            if (result >= 0)
                return (result / SINGLE_PLAYER_PACK_SIZE) + 1;
        }
        return 1;
    }

    /**
     * @return current SinglePlayer Level number (from 1 to {@link #SINGLE_PLAYER_PACK_SIZE PACK_SIZE})
     * @since 2.0.0
     */
    public int getCurSinglePlayerLevel() {
        for (char c : "abcd".toCharArray()) {
            int result = singlePlayerProgress.indexOf(c);
            if (result >= 0)
                return (result % SINGLE_PLAYER_PACK_SIZE) + 1;
        }
        return 1;
    }

    /**
     * @param packNumber pack number (from 1 to {@link #SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT})
     * @return TRUE, if a given Levels Pack is available for the user in SinglePlayer mode, FALSE - if it's not
     * @since 2.0.0
     */
    public boolean isSinglePlayerPackAvailable(int packNumber) {
        if (packNumber < 2) return true;                         // pack 1 is always available
        else for (Ability ability : userAbilities) {             // in Java 8 may be replaced with lambda
            if (ability.ordinal() == SP_PACK_OFFSET + packNumber)
                return true;
        }
        return false;
    }

    /**
     * Sets chosen by a user Pack number and Level number in SinglePlayer mode (note that a user might choose any of
     * the previously completed levels)
     * @param pack pack number (from 1 to {@link #SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT})
     * @param level level number (from 1 to {@link #SINGLE_PLAYER_PACK_SIZE PACK_SIZE})
     * @since 2.0.0
     */
    public void setChosenSinglePlayerLevel(int pack, int level) {
        singlePlayerLatest = getCurSinglePlayerPack() == pack && getCurSinglePlayerLevel() == level;
    }

    /**
     * Moves forward the SinglePlayer progress. Method has no effect if a user chose not the latest level
     * <br><b>Note:</b> method should be called regardless of a user won or lost
     * @see #setChosenSinglePlayerLevel(int, int)
     * @param winner TRUE if a user has won, and FALSE - if lost
     * @since 2.0.0
     */
    public void moveForwardSinglePlayerProgress(boolean winner) {
        if (singlePlayerLatest) { // if a user re-plays an old level just for fun => we won't move forward the progress
            for (char c : "abcd".toCharArray()) {
                int result = singlePlayerProgress.indexOf(c);
                if (result >= 0) {
                    char[] newProgress = singlePlayerProgress.toCharArray();
                    // replace current level character
                    switch (c) {
                        case 'a': newProgress[result] = winner ? 'e' : 'b'; break;
                        case 'b': newProgress[result] = winner ? 'f' : 'c'; break;
                        case 'c': newProgress[result] = winner ? 'g' : 'd'; break;
                        case 'd': newProgress[result] = winner ? 'h' : 'd'; break;
                        default:
                    }
                    // if win => also replace (i.e. "unblock") the next character
                    if (winner && result + 1 < newProgress.length)
                        newProgress[result+1] = 'a';
                    // update progress
                    singlePlayerProgress = new String(newProgress);
                    saveSettings();
                    return;
                }
            }
        }
    }

    /**
     * @param packNumber pack number (from 1 to {@link #SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT})
     * @return price of unlocking the given Levels Pack for SinglePlayer
     * @since 2.0.0
     */
    public int getSinglePlayerPackPrice(int packNumber) {
        if (packNumber < 2) return 0;           // pack 1 is for free
        else for (Product product : products) { // in Java 8 may be replaced with lambda
            if (product.ability.ordinal() == SP_PACK_OFFSET + packNumber)
                return product.gems;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Sends BUY_PRODUCT command to the server in order to unlock a given Levels Pack for SinglePlayer mode
     * @see #buyProduct(Product)
     * @param packNumber pack number (from 1 to {@link #SINGLE_PLAYER_PACKS_COUNT PACKS_COUNT})
     * @since 2.0.0
     */
    public void buySinglePlayerPack(int packNumber) {
        if (packNumber >= 2) {
            for (Product product : products) { // in Java 8 may be replaced with lambda
                if (product.ability.ordinal() == SP_PACK_OFFSET + packNumber)
                    buyProduct(product);
            }
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
