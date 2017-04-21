package ru.mitrakov.self.rush.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mitrakov on 21.04.2017
 */
public class EventBus {
    public static abstract class Event {}

    public static final class GeneralRatingUpdatedEvent extends Event {}
    public static final class WeeklyRatingUpdatedEvent extends Event {}
    public static final class FriendListUpdatedEvent extends Event {}
    public static final class AbilitiesUpdatedEvent extends Event {}
    public static final class InviteEvent extends Event {}
    public static final class StopCallRejectedEvent extends Event {}
    public static final class StopCallMissedEvent extends Event {}
    public static final class StopCallExpiredEvent extends Event {}
    public static final class RoundFinishedEvent extends Event {}
    public static final class GameFinishedEvent extends Event {}
    public static final class PromocodeDoneEvent extends Event {}
    public static final class AggressorBusyEvent extends Event {}
    public static final class DefenderBusyEvent extends Event {}
    public static final class EnemyNotFoundEvent extends Event {}
    public static final class NoFreeUsersEvent extends Event {}
    public static final class AttackedYourselfEvent extends Event {}
    public static final class AddFriendErrorEvent extends Event {}
    public static final class NoCrystalsEvent extends Event {}
    public static final class IncorrectCredentialsEvent extends Event {}
    public static final class IncorrectNameEvent extends Event {}
    public static final class IncorrectEmailEvent extends Event {}
    public static final class DuplicateNameEvent extends Event {}
    public static final class SignUpErrorEvent extends Event {}
    public static final class BattleNotFoundEvent extends Event {}

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
