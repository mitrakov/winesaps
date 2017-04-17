package ru.mitrakov.self.rush.model;

import java.util.*;
import java.security.*;
import java.math.BigInteger;
import java.util.concurrent.*;

import ru.mitrakov.self.rush.model.object.CellObject;

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

    /**
     * interface to send commands to the server
     */
    public interface ISender {
        void send(Cmd cmd);

        void send(Cmd cmd, int arg);

        void send(Cmd cmd, byte[] data);

        void reset();
    }

    /**
     * interface to read/write files independent from a platform (Desktop, Android, etc.)
     */
    public interface IFileReader {
        void write(String filename, String s);

        String read(String filename);

        Object deserialize(String filename);

        void serialize(String filename, Object obj);
    }

    /**
     * server-specific commands; for more details see docs to the protocol
     */
    public enum Cmd {
        UNSPEC_ERROR, SIGN_UP, SIGN_IN, SIGN_OUT, USER_INFO, ATTACK, CALL, ACCEPT, REJECT, STOPCALL, CANCEL_CALL,
        RANGE_OF_PRODUCTS, BUY_PRODUCT, RECEIVE_TRAINING, CHANGE_CHARACTER, RESERVED_0F, FULL_STATE, ABILITY_LIST,
        MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN, USE_THING, USE_SKILL, STATE_CHANGED, SCORE_CHANGED, PLAYER_WOUNDED,
        THING_TAKEN, OBJECT_APPENDED, FINISHED, GIVE_UP, ROUND_INFO, RATING, FRIEND_LIST, ADD_FRIEND, REMOVE_FRIEND,
        CHECK_PROMOCODE, PROMOCODE_DONE
    }

    public enum Character {None, Rabbit, Hedgehog, Squirrel, Cat}

    /**
     * ability list; some abilities are stubs (a7, a8, up to a32), because skills start with an index=33
     */
    public enum Ability {
        None, Snorkel, Shoes, SouthWester, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16,
        a17, a18, a19, a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32, Sapper
    }

    /**
     * rating enumeration (General, Weekly, etc.); constants (0, 1) are specified by the server
     */
    public enum RatingType {
        General, Weekly
    }

    public String md5(String s) {
        try {
            // @mitrakov: don't use HexBinaryAdapter(): javax is not supported by Android
            byte[] bytes = MessageDigest.getInstance("md5").digest(s.getBytes());
            return String.format("%X", new BigInteger(1, bytes)).toLowerCase();
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
    public volatile String hash = "";
    public volatile String enemy = "";
    public volatile String promocode = "";
    public volatile String promocodeDoneName = "";
    public volatile Character character = Character.None;
    public volatile Character character1 = Character.None;
    public volatile Character character2 = Character.None;
    public volatile boolean connected = true;
    public volatile boolean authorized = false;
    public volatile boolean roundWinner = false;
    public volatile boolean promocodeValid = false;
    public volatile boolean promocodeDoneInviter = false;
    public volatile boolean newbie = true;
    public volatile int crystals = 0;
    public volatile int score1 = 0;
    public volatile int score2 = 0;
    public volatile int totalScore1 = 0;
    public volatile int totalScore2 = 0;
    public volatile int myLives = 2;
    public volatile int enemyLives = 2;
    public volatile int promocodeDoneCrystals = 0;
    public volatile int roundNumber = 0;
    public volatile int roundLengthSec = 60;
    public volatile Field field;
    public volatile CellObject curActor;
    public volatile CellObject curThing;
    public volatile CellObject enemyThing;

    public volatile long abilityExpireTime = 0;
    public volatile long generalRatingTime = 0;
    public volatile long weeklyRatingTime = 0;
    public volatile long inviteTime = 0;
    public volatile long stopCallRejectedTime = 0;
    public volatile long stopCallMissedTime = 0;
    public volatile long stopCallExpiredTime = 0;
    public volatile long roundStartTime = 0;
    public volatile long roundFinishedTime = 0;
    public volatile long gameFinishedTime = 0;
    public volatile long friendsListTime = 0;
    public volatile long promocodeDoneTime = 0;
    public volatile long aggressorBusyTime = 0;
    public volatile long defenderBusyTime = 0;
    public volatile long enemyNotFoundTime = 0;
    public volatile long noFreeUsersTime = 0;
    public volatile long attackYourselfTime = 0;

    // ==================================================
    // === PUBLIC NON-VOLATILE CONCURRENT COLLECTIONS ===
    // ==================================================
    // getters are supposed to have a little overhead, so we make the fields "public" for efficiency; these collections
    // are rest upon Java-Concurrent Library, because they may be changed OUTSIDE the OpenGL loop at any moment;
    // all 'foreach' operations are considered to be safe
    // ==================================================

    public final Collection<Ability> abilities = new ConcurrentLinkedQueue<Ability>();
    public final Collection<Product> products = new ConcurrentLinkedQueue<Product>();
    public final Collection<RatingItem> generalRating = new ConcurrentLinkedQueue<RatingItem>();
    public final Collection<RatingItem> weeklyRating = new ConcurrentLinkedQueue<RatingItem>();
    public final Collection<HistoryItem> history = new ConcurrentLinkedQueue<HistoryItem>();
    public final Collection<String> friends = new ConcurrentLinkedQueue<String>();
    public final Map<Ability, Integer> abilityExpireMap = new ConcurrentHashMap<Ability, Integer>(); // see note#1

    // ================
    // === SETTINGS ===
    // ================

    public volatile boolean notifyNewBattles = true;
    public volatile boolean languageEn = true; // convert to enum in the future

    // ================================
    // === PRIVATE STATIC CONSTANTS ===
    // ================================

    private static final int AGGRESSOR_ID = 1;
    private static final int DEFENDER_ID = 2;
    private static final int PING_PERIOD_MSEC = 60000;
    private static final int SKILL_OFFSET = 0x20;
    private static final int HISTORY_MAX = 32;
    private static final int PROMOCODE_LEN = 5;
    private static final String SETTINGS_FILE = "settings";
    private static final String HISTORY_PREFIX = "history/";

    // ============================
    // === USUAL PRIVATE FIELDS ===
    // ============================

    private ISender sender;
    private IFileReader fileReader;
    private int enemySid = 0;
    private boolean aggressor = true;

    public Model() {
        // create timer to ping the server (otherwise the server will make "signOut due to inaction")
        new Timer("Ping timer", true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (connected && authorized && sender != null)
                    sender.send(USER_INFO);
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
                    languageEn = settings[0].equals("e");
                    notifyNewBattles = settings[1].equals("1");
                    name = settings[2];
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
            String s = String.format("%s %s %s %s", languageEn ? "e" : "r", notifyNewBattles ? "1" : "0", name, hash);
            fileReader.write(SETTINGS_FILE, s);
        }
    }

    /**
     * @param ability - ability (if NULL then returns empty list)
     * @return collection of available products by the given ability (e.g. Snorkel for 1 day, 3 days, 7 days)
     */
    public Collection<Product> getProductsByAbility(Ability ability) {
        List<Product> res = new LinkedList<Product>();
        for (Product product : products) {   // in Java 8 may be replaced with lambda
            if (product.ability == ability)
                res.add(product);
        }
        return res;
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
            sender.send(SIGN_IN, String.format("\1%s\0%s", name, hash).getBytes()); // \1 = Local auth
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
            sender.send(SIGN_IN, String.format("\1%s\0%s", login, hash).getBytes()); // \1 = Local auth
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
        if (connected && sender != null) {
            hash = md5(password);
            sender.reset();
            sender.send(SIGN_UP, String.format("%s\0%s\0%s\0%s", login, hash, email, promocode).getBytes());
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
     * Sends INVITE command to the server (by name)
     *
     * @param victim - victim user name
     */
    public void invite(String victim) {
        if (connected && sender != null) {
            sender.send(ATTACK, String.format("\0%s", victim).getBytes());
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
    public void inviteRandom() {
        if (connected && sender != null) {
            sender.send(ATTACK, 2);
        }
    }

    /**
     * Sends ACCEPT command to the server (in response to INVITE)
     */
    public void accept() {
        if (connected && sender != null) {
            sender.send(ACCEPT, new byte[]{(byte) (enemySid / 256), (byte) (enemySid % 256)});
        }
    }

    /**
     * Sends REJECT command to the server (in response to INVITE)
     */
    public void reject() {
        if (connected && sender != null) {
            sender.send(REJECT, new byte[]{(byte) (enemySid / 256), (byte) (enemySid % 256)});
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
            sender.send(ADD_FRIEND, name.getBytes());
        }
    }

    /**
     * Sends REMOVE_FRIEND command to the server
     *
     * @param name - quondam friend name
     */
    public void removeFriend(String name) {
        if (connected && sender != null) {
            sender.send(REMOVE_FRIEND, name.getBytes());
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
            sender.send(CHECK_PROMOCODE, promocode.getBytes());
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
            sender.send(BUY_PRODUCT, new byte[]{(byte) product.ability.ordinal(), (byte) product.days});
        }
    }

    public void cancelCall() {
        if (connected && sender != null) {
            sender.send(CANCEL_CALL);
        }
    }

    /**
     * Sends MOVE_LEFT battle command to the server
     */
    public void moveLeft() {
        if (connected && sender != null && curActor != null) {
            if (curActor.getX() > 0)
                sender.send(MOVE_LEFT);
        }
    }

    /**
     * Sends MOVE_RIGHT battle command to the server
     */
    public void moveRight() {
        if (connected && sender != null && curActor != null) {
            if (curActor.getX() < Field.WIDTH - 1)
                sender.send(MOVE_RIGHT);
        }
    }

    /**
     * Sends MOVE_UP battle command to the server
     */
    public void moveUp() {
        if (connected && sender != null && curActor != null) {
            if (curActor.getY() > 0)
                sender.send(MOVE_UP);
        }
    }

    /**
     * Sends MOVE_DOWN battle command to the server
     */
    public void moveDown() {
        if (connected && sender != null && curActor != null) {
            if (curActor.getY() < Field.HEIGHT - 1)
                sender.send(MOVE_DOWN);
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

    public void stopBattle() {
        if (connected && sender != null) {
            sender.send(GIVE_UP);
        }
    }

    // ===============================
    // === SERVER RESPONSE METHODS ===
    // ===============================
    // These methods are not expected to be called from external code
    // ===============================

    public void setConnected(boolean value) {
        connected = value;
        if (connected && !authorized)
            signIn(); // try to sign in using stored credentials
    }

    public void setAuthorized(boolean value) {
        authorized = value;
        if (connected && sender != null) {
            if (authorized) {
                sender.send(USER_INFO);
                sender.send(RANGE_OF_PRODUCTS);
                sender.send(FRIEND_LIST); // without this "InviteByName" dialog suggests to add everyone to friends
            } else {
                hash = "";
                saveSettings(); // to write empty hash to a local storage
            }
        }
    }

    public synchronized void setUserInfo(int[] data) {
        assert data != null;
        int i = 0;

        // parse name
        StringBuilder bld = new StringBuilder();
        for (; i < data.length && data[i] != 0; i++) {
            bld.append((char) data[i]);
        }
        name = bld.toString();
        i++;

        // parse promo code
        bld = new StringBuilder(name);
        for (; i < data.length && data[i] != 0; i++) {
            bld.append((char) data[i]);
        }
        promocode = bld.toString();
        i++;

        // parse character
        Character[] characters = Character.values();
        int ch = data[i++];
        if (0 <= ch && ch < characters.length)
            character = characters[ch];

        // parse crystals
        if (i + 3 < data.length)
            crystals = (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | (data[i + 3]); // what if > 2*10^9?
        i += 4;

        // parse abilities
        Ability[] array = Ability.values();
        abilityExpireMap.clear();
        int abilitiesCnt = data[i++];
        for (int j = 0; j < abilitiesCnt; j++, i += 3) {
            if (i + 2 < data.length) {
                int id = data[i];
                int minutes = data[i + 1] * 256 + data[i + 2];
                if (0 <= id && id < array.length)
                    abilityExpireMap.put(array[id], minutes);
            }
        }
        abilityExpireTime = System.currentTimeMillis();

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
        assert victimName != null;
        enemy = victimName;
    }

    public void attacked(int sid, String aggressorName) {
        assert aggressorName != null;
        enemySid = sid;
        enemy = aggressorName;
        inviteTime = System.currentTimeMillis();
    }

    public void stopCallRejected(String coward) {
        assert coward != null;
        enemy = coward;
        stopCallRejectedTime = System.currentTimeMillis();
    }

    public void stopCallMissed(String aggressorName) {
        assert aggressorName != null;
        enemy = aggressorName;
        stopCallMissedTime = System.currentTimeMillis();
    }

    public void stopCallExpired(String defenderName) {
        assert defenderName != null;
        enemy = defenderName;
        stopCallExpiredTime = System.currentTimeMillis();
    }

    public synchronized void setFriendList(int[] data) {
        assert data != null;
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) data[i];
        }
        friends.clear();
        String s = new String(bytes);
        if (s.length() > 0) // be careful! if s == "", then s.split("\0") returns Array("") instead of Array()
            Collections.addAll(friends, s.split("\0"));
        friendsListTime = System.currentTimeMillis();
    }

    public void friendAdded(String name) {
        friends.add(name);
        friendsListTime = System.currentTimeMillis();
    }

    public void friendRemoved(String name) {
        friends.remove(name);
        friendsListTime = System.currentTimeMillis();
    }

    public synchronized void setRangeOfProducts(final int[] data) {
        assert data != null;
        Ability[] abs = Ability.values();
        products.clear();
        for (int i = 0; i + 2 < data.length; i += 3) {
            int id = data[i];
            int days = data[i + 1];
            int cost = data[i + 2];
            if (0 <= id && id < abs.length)
                products.add(new Product(abs[id], days, cost));
        }
    }

    public synchronized void setRating(RatingType type, int[] data) {
        assert type != null && data != null;
        Collection<RatingItem> rating = type == RatingType.General ? generalRating : weeklyRating;
        rating.clear();

        int i = 0;
        while (i < data.length) {
            // name
            StringBuilder name = new StringBuilder();
            int wins = 0, losses = 0, score_diff = 0;
            for (; data[i] != 0 && i < data.length; i++) {
                name.append((char) data[i]);
            }
            i++;
            // wins
            if (i + 3 < data.length) {
                wins = (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | (data[i + 3]); // if > 2*10^9?
                i += 4;
            }
            // losses
            if (i + 3 < data.length) {
                losses = (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | (data[i + 3]); // if > 2*10^9?
                i += 4;
            }
            // score_diff
            if (i + 3 < data.length) {
                score_diff = (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | (data[i + 3]); // if > 2*10^9?
                i += 4;
            }
            rating.add(new RatingItem(name.toString(), wins, losses, score_diff));
        }

        // update current rating time to make the 'subscribers' update their states
        if (type == RatingType.General)
            generalRatingTime = System.currentTimeMillis();
        else if (type == RatingType.Weekly)
            weeklyRatingTime = System.currentTimeMillis();
    }

    public void setPromocodeValid(boolean valid) {
        promocodeValid = valid;
    }

    public void setPromocodeDone(String name, boolean inviter, int crystals) {
        assert name != null;
        promocodeDoneName = name;
        promocodeDoneInviter = inviter;
        promocodeDoneCrystals = crystals;
        promocodeDoneTime = System.currentTimeMillis();
    }

    public void setRoundInfo(int number, int timeSec, boolean aggressor, int character1, int character2, int myLives,
                             int enemyLives) {
        curThing = enemyThing = curActor = null;
        score1 = score2 = 0;

        Character[] characters = Character.values();
        if (0 <= character1 && character1 < characters.length)
            this.character1 = characters[character1];
        if (0 <= character2 && character2 < characters.length)
            this.character2 = characters[character2];

        this.myLives = myLives;
        this.enemyLives = enemyLives;
        roundNumber = number;
        roundLengthSec = timeSec;
        this.aggressor = aggressor;
        roundStartTime = System.currentTimeMillis();
    }

    public void setNewField(int[] fieldData) {
        field = new Field(fieldData);
    }

    public synchronized void appendObject(int number, int id, int xy) {
        if (field != null) { // "synchronized" needed
            field.appendObject(number, id, xy);
            if (id == AGGRESSOR_ID || id == DEFENDER_ID)
                curActor = aggressor ? field.getObject(AGGRESSOR_ID) : field.getObject(DEFENDER_ID);
        }
    }

    public synchronized void setXy(int number, int xy) {
        if (field != null) // "synchronized" needed
            field.setXy(number, xy);
    }

    public void setScore(int score1, int score2) {
        this.score1 = score1;
        this.score2 = score2;
    }

    public void setThing(int thingId) {
        curThing = Cell.newObject(thingId, 0xFF, new Field.NextNumber() {
            @Override
            public int next() {
                return 0;
            }
        });
    }

    public void setEnemyThing(int thingId) {
        enemyThing = Cell.newObject(thingId, 0xFF, new Field.NextNumber() {
            @Override
            public int next() {
                return 0;
            }
        });
    }

    public void setLives(int myLives, int enemyLives) {
        this.myLives = myLives;
        this.enemyLives = enemyLives;
    }

    public void roundFinished(boolean winner, int totalScore1, int totalScore2) {
        this.totalScore1 = totalScore1;
        this.totalScore2 = totalScore2;
        roundWinner = winner;
        roundFinishedTime = System.currentTimeMillis();
    }

    public synchronized void gameFinished(boolean winner) {
        // updating history
        if (enemy.length() > 0) { // it may be empty, e.g. in the Training Level
            // building a history item
            HistoryItem item = new HistoryItem(new Date(), winner, aggressor ? name : enemy, aggressor ? enemy : name,
                    totalScore1, totalScore2);

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
        field = null; // "synchronized" needed
        gameFinishedTime = System.currentTimeMillis();
    }

    public synchronized void setAbilities(int[] ids) {
        assert ids != null;
        abilities.clear();
        Ability[] array = Ability.values();
        for (int id : ids) {
            if (0 <= id && id < array.length)
                abilities.add(array[id]);
        }
    }

    public void setUserBusy(boolean aggressor) {
        if (aggressor)
            aggressorBusyTime = System.currentTimeMillis();
        else defenderBusyTime = System.currentTimeMillis();
    }

    public void setEnemyNotFound() {
        enemyNotFoundTime = System.currentTimeMillis();
    }

    public void setNoFreeUsers() {
        noFreeUsersTime = System.currentTimeMillis();
    }

    public void setAttackYourself() {
        attackYourselfTime = System.currentTimeMillis();
    }
}

// note#2 (@mitrakov, 2017-04-03): it'd be better use SkipListMap, but it's not supported by Android API 8
