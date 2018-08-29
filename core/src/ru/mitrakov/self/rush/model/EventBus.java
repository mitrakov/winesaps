package ru.mitrakov.self.rush.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.Cells.CellObject;

/**
 * Event Bus
 * @author Mitrakov
 */
public class EventBus {
    /** Base event class */
    public static abstract class Event {}

    /** Event: MOVE acknowledge from the Server */
    public static final class MoveResponseEvent extends Event {}
    /** Event: aggressor is busy */
    public static final class AggressorBusyEvent extends Event {}
    /** Event: defender is busy */
    public static final class DefenderBusyEvent extends Event {}
    /** Error: enemy not found */
    public static final class EnemyNotFoundEvent extends Event {}
    /** Event: start waiting for enemy in the Quick Battle mode */
    public static final class WaitingForEnemyEvent extends Event {}
    /** Error: attack yourself*/
    public static final class AttackedYourselfEvent extends Event {}
    /** Error: cannot add a friend */
    public static final class AddFriendErrorEvent extends Event {}
    /** Error: no enough gems to perform an operation */
    public static final class NoGemsEvent extends Event {}
    /** Error: incorrect login/password pair */
    public static final class IncorrectCredentialsEvent extends Event {}
    /** Error: incorrect name provided */
    public static final class IncorrectNameEvent extends Event {}
    /** Error: incorrect email provided */
    public static final class IncorrectEmailEvent extends Event {}
    /** Error: name already exists */
    public static final class DuplicateNameEvent extends Event {}
    /** Error: sign-up error */
    public static final class SignUpErrorEvent extends Event {}
    /** Warning: Server is going to be restarted (soft-reboot) */
    public static final class ServerGonnaStopEvent extends Event {}
    /** Error: battle is not found or already finished*/
    public static final class BattleNotFoundEvent extends Event {}
    /** Error: password provided is too weak */
    public static final class WeakPasswordEvent extends Event {}
    /** Error: unsupported protocol version (client needs to be updated) */
    public static final class UnsupportedProtocolEvent extends Event {}
    /** Error: client version is lower than the minimum version */
    public static final class VersionNotAllowedEvent extends Event {
        /** Minimum version allowed */
        public String minVersion;
        /**
         * Creates a new VersionNotAllowed Event
         * @param minVersion minimum version supported by the Server
         */
        VersionNotAllowedEvent(String minVersion) {
            this.minVersion = minVersion;
        }
    }
    /** Event: new version is available to download */
    public static final class NewVersionAvailableEvent extends Event {
        /** New version */
        public String newVersion;
        /**
         * Creates a new NewVersionAvailable Event
         * @param newVersion new version ready for downloading
         */
        NewVersionAvailableEvent(String newVersion) {
            this.newVersion = newVersion;
        }
    }
    /** Event: name is changed (by loading data from settings file or by the Server); also the name may be the same */
    public static final class NameChangedEvent extends Event {
        /** Username */
        public String name;
        /**
         * Creates new NameChanged Event
         * @param name username
         */
        NameChangedEvent(String name) {
            this.name = name;
        }
    }
    /** Event: gems balance has been changed */
    public static final class GemsChangedEvent extends Event {
        /** Gems count */
        public int gems;
        /**
         * Creates a new Gems Changed Event
         * @param gems updated gems balance
         */
        GemsChangedEvent(int gems) {
            this.gems = gems;
        }
    }
    /** Event: user's abilities have been changed */
    public static final class AbilitiesExpireUpdatedEvent extends Event {
        /** List of abilities */
        public Iterable<Model.Ability> items;
        /**
         * Creates a new AbilitiesExpireUpdated Event
         * @param items updated list of the user's abilities
         */
        AbilitiesExpireUpdatedEvent(Iterable<Model.Ability> items) {
            this.items = items;
        }
    }
    /** Event: friends list has been updated */
    public static final class FriendListUpdatedEvent extends Event {
        /** List of friends */
        public Collection<FriendItem> items;
        /**
         * Creates a new FriendListUpdated Event
         * @param items updated list of user's friends
         */
        FriendListUpdatedEvent(Collection<FriendItem> items) {
            this.items = items;
        }
    }
    /** Event: new friend has been added */
    public static final class FriendAddedEvent extends Event {
        /** New friend's name */
        public FriendItem name;
        /**
         * Creates a new FriendAdded Event
         * @param name new friend's name
         */
        FriendAddedEvent(FriendItem name) {
            this.name = name;
        }
    }
    /** Event: new friend has been removed */
    public static final class FriendRemovedEvent extends Event {
        /** Ex-friend's name */
        public String name;
        /**
         * Creates a new FriendRemoved Event
         * @param name ex-friend's name
         */
        FriendRemovedEvent(String name) {
            this.name = name;
        }
    }
    /** Event: someone invited us to a battle */
    public static final class InviteEvent extends Event {
        /** Enemy name */
        public String enemy;
        /** Enemy Session ID (required by the Server in response) */
        public int enemySid;
        /**
         * Creates a new Invite Event
         * @param enemy enemy name
         * @param enemySid enemy Session ID
         */
        InviteEvent(String enemy, int enemySid) {
            this.enemy = enemy;
            this.enemySid = enemySid;
        }
    }
    /** Event: the enemy rejected our invite for a battle */
    public static final class StopCallRejectedEvent extends Event {
        /** Enemy name, who rejected our invitation */
        public String cowardName;
        /**
         * Creates a new StopCall Rejected Event
         * @param cowardName enemy name, who rejected our invitation
         */
        StopCallRejectedEvent(String cowardName) {
            this.cowardName = cowardName;
        }
    }
    /** Event: we miss someone's call for a battle, and Server wants us to stop ringing */
    public static final class StopCallMissedEvent extends Event {
        /** Aggressor name */
        public String aggressorName;
        /**
         * Creates a new StopCall Missed Event
         * @param aggressorName aggressor name
         */
        StopCallMissedEvent(String aggressorName) {
            this.aggressorName = aggressorName;
        }
    }
    /** Event: we invite someone for a battle, but he/she missed (or ignored) our invitation */
    public static final class StopCallExpiredEvent extends Event {
        /** Defender name */
        public String defenderName;
        /**
         * Creates a new StopCall Expired Event
         * @param defenderName defender name
         */
        StopCallExpiredEvent(String defenderName) {
            this.defenderName = defenderName;
        }
    }
    /** Event: ranking updated */
    @SuppressWarnings("WeakerAccess")
    public static final class RatingUpdatedEvent extends Event {
        /** Rating type (General/Weekly) */
        public Model.RatingType type;
        /** Rating items list */
        public Iterable<RatingItem> items;
        /**
         * Creates a new Rating Updated Event
         * @param type rating type (General/Weekly)
         * @param items updated list of rating items for the given rating type
         */
        RatingUpdatedEvent(Model.RatingType type, Iterable<RatingItem> items) {
            this.type = type;
            this.items = items;
        }
    }
    /** Event: round has been finished */
    public static final class RoundFinishedEvent extends Event {
        /** Winner flag (TRUE if we won) */
        public boolean winner;
        /** Participant #1 */
        public String detractor1;
        /** Participant #2 */
        public String detractor2;
        /** Total score1 (for the whole game) */
        public int totalScore1;
        /** Total score2 (for the whole game) */
        public int totalScore2;
        /**
         * Creates a new Round Finished Event
         * @param winner winner flag (TRUE if we won)
         * @param detractor1 participant #1
         * @param detractor2 participant #2
         * @param totalScore1 total score1
         * @param totalScore2 total score2
         */
        RoundFinishedEvent(boolean winner, String detractor1, String detractor2, int totalScore1, int totalScore2) {
            this.winner = winner;
            this.detractor1 = detractor1;
            this.detractor2 = detractor2;
            this.totalScore1 = totalScore1;
            this.totalScore2 = totalScore2;
        }
    }
    /** Event: battle has been finished */
    public static final class GameFinishedEvent extends Event {
        /** Winner flag (TRUE if we won) */
        public boolean winner;
        /** Participant #1 */
        public String detractor1;
        /** Participant #2 */
        public String detractor2;
        /** Total score1 */
        public int totalScore1;
        /** Total score2 */
        public int totalScore2;
        /** Reward for the battle, in gems */
        public int reward;
        /**
         * Creates a new Game Finished Event
         * @param winner winner flag (TRUE if we won)
         * @param d1 participant #1
         * @param d2 participant #2
         * @param totalScore1 total score1
         * @param totalScore2 total score2
         * @param reward reward for the game, in gems
         */
        GameFinishedEvent(boolean winner, String d1, String d2, int totalScore1, int totalScore2, int reward) {
            this.winner = winner;
            this.detractor1 = d1;
            this.detractor2 = d2;
            this.totalScore1 = totalScore1;
            this.totalScore2 = totalScore2;
            this.reward = reward;
        }
    }
    /** Event: promo code activated */
    public static final class PromocodeDoneEvent extends Event {
        /** Name of our friend */
        public String name;
        /** Inviter flag: whether a current user is inviter (TRUE), or our friend is inviter (FALSE) */
        public boolean inviter;
        /** Reward for promocode activated, in gems */
        public int gems;
        /**
         * Creates a new Promocode Done Event
         * @param name name of our friend
         * @param inviter whether a current user is inviter (TRUE), or our friend is inviter (FALSE)
         * @param gems reward for promocode, in gems
         */
        PromocodeDoneEvent(String name, boolean inviter, int gems) {
            this.name = name;
            this.inviter = inviter;
            this.gems = gems;
        }
    }
    /** Event: Style Pack has been changed */
    public static final class StyleChangedEvent extends Event {
        /** Style Pack number */
        public int stylePack;
        /**
         * Creates a new Style Changed Event
         * @param stylePack style Pack number (currently 0-3)
         */
        StyleChangedEvent(int stylePack) {
            this.stylePack = stylePack;
        }
    }
    /** Event: Character has been changed */
    public static final class CharacterChangedEvent extends Event {
        /** New character (rabbit, hedgehog, etc.) */
        public Model.Character character;
        /**
         * Creates a new Character Changed Event
         * @param character new character
         */
        CharacterChangedEvent(Model.Character character) {
            this.character = character;
        }
    }
    /** Event: Abilities list has been changed */
    public static final class AbilitiesChangedEvent extends Event {
        /** New abilities list */
        public Iterable<Model.Ability> items;
        /**
         * Creates a new Abilities Changed Event
         * @param items new abilities list
         */
        AbilitiesChangedEvent(Iterable<Model.Ability> items) {
            this.items = items;
        }
    }
    /** Event: enemy has been changed */
    public static final class EnemyNameChangedEvent extends Event {
        /** New enemy */
        public String enemy;
        /**
         * Creates a new EnemyName Changed Event
         * @param enemy new enemy
         */
        EnemyNameChangedEvent(String enemy) {
            this.enemy = enemy;
        }
    }
    /** Event: Round started */
    public static final class RoundStartedEvent extends Event {
        /** Round number, starting with 0 */
        public int number;
        /** Level name */
        public String levelName;
        /**
         * Creates a new Round Started Event
         * @param number round number, starting with 0
         * @param levelName level name
         */
        RoundStartedEvent(int number, String levelName) {
            this.number = number;
            this.levelName = levelName;
        }
    }
    /** Event: new battlefield created */
    public static final class NewFieldEvent extends Event {
        /** Actor that corresponds to a current user */
        public CellObject actor;
        /** New battle field */
        public Field field;
        /**
         * Creates a new NewField Event
         * @param actor actor that corresponds to a current user
         * @param field new battlefield
         */
        NewFieldEvent(CellObject actor, Field field) {
            this.actor = actor;
            this.field = field;
        }
    }
    /** Event: Score changed */
    public static final class ScoreChangedEvent extends Event {
        /** Score of detractor1 */
        public int score1;
        /** Score of detractor2 */
        public int score2;
        /**
         * Creates a new Score Changed Event
         * @param score1 score of detractor1
         * @param score2 score of detractor2
         */
        ScoreChangedEvent(int score1, int score2) {
            this.score1 = score1;
            this.score2 = score2;
        }
    }
    /** Event: lives count changed */
    @SuppressWarnings("WeakerAccess")
    public static final class LivesChangedEvent extends Event {
        /** My lives count */
        public int myLives;
        /** Enemy lives count */
        public int enemyLives;
        /**
         * Creates a new Lives Changed Event
         * @param myLives my lives count
         * @param enemyLives enemy lives count
         */
        LivesChangedEvent(int myLives, int enemyLives) {
            this.myLives = myLives;
            this.enemyLives = enemyLives;
        }
    }
    /** Event: player has been wounded */
    public static final class PlayerWoundedEvent extends Event {
        /** Position where a player has been wounded */
        public int xy;
        /** Cause */
        public Model.HurtCause cause;
        /** My lives count */
        public int myLives;
        /** Enemy lives count */
        public int enemyLives;
        /**
         * Creates a new Player Wounded Event
         * @param xy tragedy location
         * @param cause cause
         * @param myLives my lives count
         * @param enemyLives enemy lives count
         */
        PlayerWoundedEvent(int xy, Model.HurtCause cause, int myLives, int enemyLives) {
            this.xy = xy;
            this.cause = cause;
            this.myLives = myLives;
            this.enemyLives = enemyLives;
        }
    }
    /** Event: effect has been applied to an object */
    public static final class EffectAddedEvent extends Event {
        /** Effect */
        public Model.Effect effect;
        /**
         * Creates a new Effect Added Event
         * @param effect effect applied to an object
         */
        EffectAddedEvent(Model.Effect effect) {
            this.effect = effect;
        }
    }
    /** Event: object has been removed from the battlefield */
    public static final class ObjectRemovedEvent extends Event {
        /** Location where the object has been removed from */
        public int oldXy;
        /** Object */
        public CellObject obj;
        /**
         * Creates a new Object Removed Event
         * @param oldXy position where the object has been removed from
         * @param obj object
         */
        ObjectRemovedEvent(int oldXy, CellObject obj) {
            this.oldXy = oldXy;
            this.obj = obj;
        }
    }
    /** Event: actor's position has been reset */
    public static final class ActorResetEvent extends Event {
        /** Actor object (either ours or our enemy) */
        public CellObject obj;
        /**
         * Creates a new Actor Reset Event
         * @param obj actor object (either ours or our enemy)
         */
        ActorResetEvent(CellObject obj) {
            this.obj = obj;
        }
    }
    /** Event: thing has been changed (dropped, used, or taken a new one) */
    public static final class ThingChangedEvent extends Event {
        /** Old thing */
        public CellObject oldThing;
        /** New thing */
        public CellObject newThing;
        /** Possession flag: TRUE for our player, and FALSE for our enemy's actor */
        public boolean mine;
        /**
         * Creates a new ThingChangedEvent
         * @param oldThing old thing (may be NULL)
         * @param newThing new thing (may be NULL)
         * @param mine TRUE for our player, and FALSE for our enemy's actor
         */
        ThingChangedEvent(CellObject oldThing, CellObject newThing, boolean mine) {
            this.oldThing = oldThing;
            this.newThing = newThing;
            this.mine = mine;
        }
    }
    /** Event: authorization status changed (signed in or signed out) */
    public static final class AuthorizedChangedEvent extends Event {
        /** Authorization flag: signed in or signed out */
        public boolean authorized;
        /**
         * Creates a new Authorized Changed Event
         * @param authorized signed in or signed out
         */
        AuthorizedChangedEvent(boolean authorized) {
            this.authorized = authorized;
        }
    }
    /** Event: connection status changed (connected/disconnected) */
    public static final class ConnectedChangeEvent extends Event {
        /** Connection flag: connected/disconnected */
        public boolean connected;
        /**
         * Creates a new Connected Change Event
         * @param connected status (connected/disconnected)
         */
        ConnectedChangeEvent(boolean connected) {
            this.connected = connected;
        }
    }
    /** Event: input promo code status changed: valid/invalid */
    public static final class PromocodeValidChangedEvent extends Event {
        /** Promo code status: valid/invalid */
        public boolean valid;
        /**
         * Creates a new Promocode Valid Changed Event
         * @param valid status (valid/invalid)
         */
        PromocodeValidChangedEvent(boolean valid) {
            this.valid = valid;
        }
    }
    /** Event: SKU prices updated */
    public static final class SkuGemsUpdatedEvent extends Event {
        /** New prices (map: [SKU_name -> gems]) */
        public Map<String, Integer> skuGems;
        /**
         * Creates a new Sku Gems Updated Event
         * @param skuGems new prices (map: [SKU_name -> gems])
         */
        SkuGemsUpdatedEvent(Map<String, Integer> skuGems) {
            this.skuGems = skuGems;
        }
    }
    /** Event: payment successful */
    public static final class PaymentDoneEvent extends Event {
        /** Gems bought by the operation */
        public int gems;
        /** Coupon for the next purchase */
        public String coupon;
        /**
         * Creates a new Payment Done Event
         * @param gems gems bought by the operation
         * @param coupon coupon for the next purchase (may be empty if no coupon generated)
         */
        PaymentDoneEvent(int gems, String coupon) {
            this.gems = gems;
            this.coupon = coupon;
        }
    }


    // =====================================
    // =====================================


    /** Listener interface for the Event Bus */
    public interface Listener {
        /**
         * Invoked on a new event
         * @param event event
         */
        void OnEvent(Event event);
    }

    /** Subscribers */
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    /**
     * Adds a subscriber to the subscribers list
     * @param listener subscriber
     */
    public void addListener(Listener listener) {
        assert listener != null;
        listeners.add(listener);
    }

    /**
     * Triggers the event to the Event Bus
     * @param event event to be transmitted to the listeners
     * @see #addListener(Listener)
     */
    public void raise(Event event) {
        assert event != null;
        for (int i = 0; i < listeners.size(); i++) { // do NOT use iterators! They produces excessive work for GC
            Listener listener = listeners.get(i); assert listener != null;
            listener.OnEvent(event);
        }
    }
}
