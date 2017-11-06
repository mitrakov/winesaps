package ru.mitrakov.self.rush.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.Cells.CellObject;

/**
 * Created by mitrakov on 21.04.2017
 */
public class EventBus {
    public static abstract class Event {}

    public static final class MoveResponseEvent extends Event {}
    public static final class AggressorBusyEvent extends Event {}
    public static final class DefenderBusyEvent extends Event {}
    public static final class EnemyNotFoundEvent extends Event {}
    public static final class WaitingForEnemyEvent extends Event {}
    public static final class AttackedYourselfEvent extends Event {}
    public static final class AddFriendErrorEvent extends Event {}
    public static final class NoCrystalsEvent extends Event {}
    public static final class IncorrectCredentialsEvent extends Event {}
    public static final class IncorrectNameEvent extends Event {}
    public static final class IncorrectEmailEvent extends Event {}
    public static final class DuplicateNameEvent extends Event {}
    public static final class SignUpErrorEvent extends Event {}
    public static final class ServerGonnaStopEvent extends Event {}
    public static final class BattleNotFoundEvent extends Event {}
    public static final class WeakPasswordEvent extends Event {}
    public static final class UnsupportedProtocolEvent extends Event {}
    public static final class VersionNotAllowedEvent extends Event {
        public String minVersion;
        VersionNotAllowedEvent(String minVersion) {
            this.minVersion = minVersion;
        }
    }
    public static final class NewVersionAvailableEvent extends Event {
        public String newVersion;
        NewVersionAvailableEvent(String newVersion) {
            this.newVersion = newVersion;
        }
    }
    public static final class NameChangedEvent extends Event {
        public String name;
        NameChangedEvent(String name) {
            this.name = name;
        }
    }
    public static final class CrystalChangedEvent extends Event {
        public int crystals;
        CrystalChangedEvent(int crystals) {
            this.crystals = crystals;
        }
    }
    public static final class AbilitiesExpireUpdatedEvent extends Event {
        public Iterable<Model.Ability> items;
        AbilitiesExpireUpdatedEvent(Iterable<Model.Ability> items) {
            this.items = items;
        }
    }
    public static final class FriendListUpdatedEvent extends Event {
        public Collection<FriendItem> items;
        FriendListUpdatedEvent(Collection<FriendItem> items) {
            this.items = items;
        }
    }
    public static final class FriendAddedEvent extends Event {
        public FriendItem name;
        FriendAddedEvent(FriendItem name) {
            this.name = name;
        }
    }
    public static final class FriendRemovedEvent extends Event {
        public String name;
        FriendRemovedEvent(String name) {
            this.name = name;
        }
    }
    public static final class InviteEvent extends Event {
        public String enemy;
        public int enemySid;
        InviteEvent(String enemy, int enemySid) {
            this.enemy = enemy;
            this.enemySid = enemySid;
        }
    }
    public static final class StopCallRejectedEvent extends Event {
        public String cowardName;
        StopCallRejectedEvent(String cowardName) {
            this.cowardName = cowardName;
        }
    }
    public static final class StopCallMissedEvent extends Event {
        public String aggressorName;
        StopCallMissedEvent(String aggressorName) {
            this.aggressorName = aggressorName;
        }
    }
    public static final class StopCallExpiredEvent extends Event {
        public String defenderName;
        StopCallExpiredEvent(String defenderName) {
            this.defenderName = defenderName;
        }
    }
    @SuppressWarnings("WeakerAccess")
    public static final class RatingUpdatedEvent extends Event {
        public Model.RatingType type;
        public Iterable<RatingItem> items;
        RatingUpdatedEvent(Model.RatingType type, Iterable<RatingItem> items) {
            this.type = type;
            this.items = items;
        }
    }
    public static final class RoundFinishedEvent extends Event {
        public boolean winner;
        public String detractor1;
        public String detractor2;
        public int totalScore1;
        public int totalScore2;
        RoundFinishedEvent(boolean winner, String detractor1, String detractor2, int totalScore1, int totalScore2) {
            this.winner = winner;
            this.detractor1 = detractor1;
            this.detractor2 = detractor2;
            this.totalScore1 = totalScore1;
            this.totalScore2 = totalScore2;
        }
    }
    public static final class GameFinishedEvent extends Event {
        public boolean winner;
        public String detractor1;
        public String detractor2;
        public int totalScore1;
        public int totalScore2;
        public int reward;
        GameFinishedEvent(boolean winner, String d1, String d2, int totalScore1, int totalScore2, int reward) {
            this.winner = winner;
            this.detractor1 = d1;
            this.detractor2 = d2;
            this.totalScore1 = totalScore1;
            this.totalScore2 = totalScore2;
            this.reward = reward;
        }
    }
    public static final class PromocodeDoneEvent extends Event {
        public String name;
        public boolean inviter;
        public int crystals;
        PromocodeDoneEvent(String name, boolean inviter, int crystals) {
            this.name = name;
            this.inviter = inviter;
            this.crystals = crystals;
        }
    }
    public static final class StyleChangedEvent extends Event {
        public int stylePack;
        StyleChangedEvent(int stylePack) {
            this.stylePack = stylePack;
        }
    }
    public static final class CharacterChangedEvent extends Event {
        public Model.Character character;
        CharacterChangedEvent(Model.Character character) {
            this.character = character;
        }
    }
    public static final class AbilitiesChangedEvent extends Event {
        public Iterable<Model.Ability> items;
        AbilitiesChangedEvent(Iterable<Model.Ability> items) {
            this.items = items;
        }
    }
    public static final class EnemyNameChangedEvent extends Event {
        public String enemy;
        EnemyNameChangedEvent(String enemy) {
            this.enemy = enemy;
        }
    }
    public static final class RoundStartedEvent extends Event {
        public int number;
        public String levelName;
        RoundStartedEvent(int number, String levelName) {
            this.number = number;
            this.levelName = levelName;
        }
    }
    public static final class NewFieldEvent extends Event {
        public CellObject actor;
        public Field field;
        NewFieldEvent(CellObject actor, Field field) {
            this.actor = actor;
            this.field = field;
        }
    }
    public static final class ScoreChangedEvent extends Event {
        public int score1;
        public int score2;
        ScoreChangedEvent(int score1, int score2) {
            this.score1 = score1;
            this.score2 = score2;
        }
    }
    @SuppressWarnings("WeakerAccess")
    public static final class LivesChangedEvent extends Event {
        public int myLives;
        public int enemyLives;
        LivesChangedEvent(int myLives, int enemyLives) {
            this.myLives = myLives;
            this.enemyLives = enemyLives;
        }
    }
    @SuppressWarnings("WeakerAccess")
    public static final class PlayerWoundedEvent extends Event {
        public int xy;
        public Model.HurtCause cause;
        public int myLives;
        public int enemyLives;
        PlayerWoundedEvent(int xy, Model.HurtCause cause, int myLives, int enemyLives) {
            this.xy = xy;
            this.cause = cause;
            this.myLives = myLives;
            this.enemyLives = enemyLives;
        }
    }
    public static final class EffectAddedEvent extends Event {
        public Model.Effect effect;
        EffectAddedEvent(Model.Effect effect) {
            this.effect = effect;
        }
    }
    public static final class ObjectRemovedEvent extends Event {
        public int oldXy;
        public CellObject obj;
        ObjectRemovedEvent(int oldXy, CellObject obj) {
            this.oldXy = oldXy;
            this.obj = obj;
        }
    }
    public static final class ActorResetEvent extends Event {
        public CellObject obj;
        ActorResetEvent(CellObject obj) {
            this.obj = obj;
        }
    }
    public static final class ThingChangedEvent extends Event {
        public CellObject oldThing;
        public CellObject newThing;
        public boolean mine;
        ThingChangedEvent(CellObject oldThing, CellObject newThing, boolean mine) {
            this.oldThing = oldThing;
            this.newThing = newThing;
            this.mine = mine;
        }
    }
    public static final class AuthorizedChangedEvent extends Event {
        public boolean authorized;
        AuthorizedChangedEvent(boolean authorized) {
            this.authorized = authorized;
        }
    }
    public static final class ConnectedChangeEvent extends Event {
        public boolean connected;
        ConnectedChangeEvent(boolean connected) {
            this.connected = connected;
        }
    }
    public static final class PromocodeValidChangedEvent extends Event {
        public boolean valid;
        PromocodeValidChangedEvent(boolean valid) {
            this.valid = valid;
        }
    }
    public static final class SkuGemsUpdatedEvent extends Event {
        public Map<String, Integer> skuGems;
        SkuGemsUpdatedEvent(Map<String, Integer> skuGems) {
            this.skuGems = skuGems;
        }
    }
    public static final class PaymentDoneEvent extends Event {
        public int gems;
        public String coupon;
        PaymentDoneEvent(int gems, String coupon) {
            this.gems = gems;
            this.coupon = coupon;
        }
    }


    // =====================================
    // =====================================


    public interface Listener {
        void OnEvent(Event event);
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public void addListener(Listener listener) {
        assert listener != null;
        listeners.add(listener);
    }

    void raise(Event event) {
        assert event != null;
        for (int i = 0; i < listeners.size(); i++) { // do NOT use iterators! They produces excessive work for GC
            Listener listener = listeners.get(i); assert listener != null;
            listener.OnEvent(event);
        }
    }
}
