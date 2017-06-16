package ru.mitrakov.self.rush.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import ru.mitrakov.self.rush.model.Cells.CellObject;

/**
 * Created by mitrakov on 21.04.2017
 */
public class EventBus {
    public static abstract class Event {}

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
    public static final class BattleNotFoundEvent extends Event {}
    public static final class NameChangedEvent extends Event {
        public final String name;
        NameChangedEvent(String name) {
            this.name = name;
        }
    }
    public static final class CrystalChangedEvent extends Event {
        public final int crystals;
        CrystalChangedEvent(int crystals) {
            this.crystals = crystals;
        }
    }
    public static final class AbilitiesExpireUpdatedEvent extends Event {
        public final Iterable<Model.Ability> items;
        AbilitiesExpireUpdatedEvent(Iterable<Model.Ability> items) {
            this.items = items;
        }
    }
    public static final class FriendListUpdatedEvent extends Event {
        public final Collection<FriendItem> items;
        FriendListUpdatedEvent(Collection<FriendItem> items) {
            this.items = items;
        }
    }
    public static final class FriendAddedEvent extends Event {
        public final FriendItem name;
        FriendAddedEvent(FriendItem name) {
            this.name = name;
        }
    }
    public static final class FriendRemovedEvent extends Event {
        public final String name;
        FriendRemovedEvent(String name) {
            this.name = name;
        }
    }
    public static final class InviteEvent extends Event {
        public final String enemy;
        public final int enemySid;
        InviteEvent(String enemy, int enemySid) {
            this.enemy = enemy;
            this.enemySid = enemySid;
        }
    }
    public static final class StopCallRejectedEvent extends Event {
        public final String cowardName;
        StopCallRejectedEvent(String cowardName) {
            this.cowardName = cowardName;
        }
    }
    public static final class StopCallMissedEvent extends Event {
        public final String aggressorName;
        StopCallMissedEvent(String aggressorName) {
            this.aggressorName = aggressorName;
        }
    }
    public static final class StopCallExpiredEvent extends Event {
        public final String defenderName;
        StopCallExpiredEvent(String defenderName) {
            this.defenderName = defenderName;
        }
    }
    public static final class RatingUpdatedEvent extends Event {
        final Model.RatingType type;
        public final Iterable<RatingItem> items;
        RatingUpdatedEvent(Model.RatingType type, Iterable<RatingItem> items) {
            this.type = type;
            this.items = items;
        }
    }
    public static final class RoundFinishedEvent extends Event {
        public final boolean winner;
        RoundFinishedEvent(boolean winner) {
            this.winner = winner;
        }
    }
    public static final class GameFinishedEvent extends Event {
        public boolean winner;
        GameFinishedEvent(boolean winner) {
            this.winner = winner;
        }
    }
    public static final class PromocodeDoneEvent extends Event {
        public final String name;
        public final boolean inviter;
        public final int crystals;
        PromocodeDoneEvent(String name, boolean inviter, int crystals) {
            this.name = name;
            this.inviter = inviter;
            this.crystals = crystals;
        }
    }
    public static final class StyleChangedEvent extends Event {
        public final int stylePack;
        StyleChangedEvent(int stylePack) {
            this.stylePack = stylePack;
        }
    }
    public static final class CharacterChangedEvent extends Event {
        public final Model.Character character;
        CharacterChangedEvent(Model.Character character) {
            this.character = character;
        }
    }
    public static final class AbilitiesChangedEvent extends Event {
        public final Iterable<Model.Ability> items;
        AbilitiesChangedEvent(Iterable<Model.Ability> items) {
            this.items = items;
        }
    }
    public static final class RoundStartedEvent extends Event {
        public final int number;
        RoundStartedEvent(int number) {
            this.number = number;
        }
    }
    public static final class ScoreChangedEvent extends Event {
        public final int score1;
        public final int score2;
        ScoreChangedEvent(int score1, int score2) {
            this.score1 = score1;
            this.score2 = score2;
        }
    }
    @SuppressWarnings("WeakerAccess")
    public static final class LivesChangedEvent extends Event {
        public final int myLives;
        public final int enemyLives;
        LivesChangedEvent(int myLives, int enemyLives) {
            this.myLives = myLives;
            this.enemyLives = enemyLives;
        }
    }
    @SuppressWarnings("WeakerAccess")
    public static final class PlayerWoundedEvent extends Event {
        public final int xy;
        public final Model.HurtCause cause;
        public final int myLives;
        public final int enemyLives;
        public PlayerWoundedEvent(int xy, Model.HurtCause cause, int myLives, int enemyLives) {
            this.xy = xy;
            this.cause = cause;
            this.myLives = myLives;
            this.enemyLives = enemyLives;
        }
    }
    public static final class ThingChangedEvent extends Event {
        public final CellObject oldThing;
        public final CellObject newThing;
        public final boolean mine;
        ThingChangedEvent(CellObject oldThing, CellObject newThing, boolean mine) {
            this.oldThing = oldThing;
            this.newThing = newThing;
            this.mine = mine;
        }
    }
    public static final class AuthorizedChangedEvent extends Event {
        public final boolean authorized;
        AuthorizedChangedEvent(boolean authorized) {
            this.authorized = authorized;
        }
    }
    public static final class ConnectedChangeEvent extends Event {
        public final boolean connected;
        ConnectedChangeEvent(boolean connected) {
            this.connected = connected;
        }
    }
    public static final class PromocodeValidChanged extends Event {
        public final boolean valid;
        PromocodeValidChanged(boolean valid) {
            this.valid = valid;
        }
    }


    // =====================================
    // =====================================


    public interface Listener {
        void OnEvent(Event event);
    }

    private final Collection<Listener> listeners = new ConcurrentLinkedQueue<Listener>();

    public void addListener(Listener listener) {
        assert listener != null;
        listeners.add(listener);
    }

    void raise(Event event) {
        assert event != null;
        for (Listener listener : listeners) {
            listener.OnEvent(event);
        }
    }
}
