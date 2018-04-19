package ru.mitrakov.self.rush.model.emulator;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Field.*;
import static ru.mitrakov.self.rush.model.Model.*;
import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.model.Model.Cmd.*;
import static ru.mitrakov.self.rush.model.Model.Ability.*;
import static ru.mitrakov.self.rush.model.Model.HurtCause.*;

/**
 * Analog of Server BattleManager class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class BattleManager {
    /** Reference to the Server emulator */
    private final ServerEmulator emulator;
    /** Lock (analog of Mutex/RWMutes in Go) */
    private final Object lock = new Object();
    /** Lock for battles collection (analog of Mutex/RWMutes in Go) */
    private final ReentrantLock battleLock = new ReentrantLock();
    /** File reader to read levels from the disk */
    private final Model.IFileReader fileReader;
    /** Environment (intended to have only 1 instance per all the battles) */
    private final Environment environment;
    /** Helper array to store binary data and avoid invoking "new" (to decrease Garbage Collector pressure) */
    private final IIntArray array = new GcResistantIntArray(WIDTH * Field.HEIGHT);      // need to be synchronized!

    /** Integer value for {@link Cmd#FULL_STATE} command */
    private final int fullState = Arrays.binarySearch(cmdValues, FULL_STATE);           // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#ROUND_INFO} command */
    private final int roundInfo = Arrays.binarySearch(cmdValues, ROUND_INFO);           // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#ABILITY_LIST} command */
    private final int abilityList = Arrays.binarySearch(cmdValues, ABILITY_LIST);       // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#MOVE} command */
    private final int move = Arrays.binarySearch(cmdValues, MOVE);                      // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#STATE_CHANGED} command */
    private final int stateChanged = Arrays.binarySearch(cmdValues, STATE_CHANGED);     // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#OBJECT_APPENDED} command */
    private final int objectAppended = Arrays.binarySearch(cmdValues, OBJECT_APPENDED); // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#SCORE_CHANGED} command */
    private final int scoreChanged = Arrays.binarySearch(cmdValues, SCORE_CHANGED);     // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#THING_TAKEN} command */
    private final int thingTaken = Arrays.binarySearch(cmdValues, THING_TAKEN);         // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#EFFECT_CHANGED} command */
    private final int effectChanged = Arrays.binarySearch(cmdValues, EFFECT_CHANGED);   // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#PLAYER_WOUNDED} command */
    private final int playerWounded = Arrays.binarySearch(cmdValues, PLAYER_WOUNDED);   // don't use "cmd.ordinal" (GC)
    /** Integer value for {@link Cmd#FINISHED} command */
    private final int finished = Arrays.binarySearch(cmdValues, FINISHED);              // don't use "cmd.ordinal" (GC)

    /** Battle (on Server there is a Map of different battles, but for Emulator there is only one) */
    private Battle battle;

    /**
     * Creates new BattleManager (this class is intended to have a single instance)
     * @param emulator reference to the Server emulator
     * @param fileReader file reader
     */
    BattleManager(ServerEmulator emulator, Model.IFileReader fileReader) {
        assert fileReader != null;
        this.emulator = emulator;
        this.fileReader = fileReader;
        this.environment = new Environment(this);
    }

    /**
     * @return file reader (NON-NULL)
     */
    Model.IFileReader getFileReader() {
        return fileReader;
    }

    /**
     * @return battle Environment (NON-NULL)
     */
    Environment getEnvironment() {
        return environment;
    }

    /**
     * @return battle (in Server returns battle by Sid)
     */
    Battle getBattle() {
        battleLock.lock();
        Battle result = this.battle;
        battleLock.unlock();
        return result;
    }

    /**
     * Accepts the invitation of the aggressor, creates and starts a new battle
     * @param character1 character of the aggressor
     * @param character2 character of the defender
     * @param aggAbilities abilities of the aggressor
     * @param defAbilities abilities of the defender
     * @param levelnames level names
     * @param wins count of round wins to win the entire battle (usually 3 on the Server)
     */
    void accept(Model.Character character1, Model.Character character2, IIntArray aggAbilities, IIntArray defAbilities,
                String[] levelnames, int wins) {
        Battle battle = new Battle(character1, character2, levelnames, wins, aggAbilities, defAbilities, this);
        battleLock.lock();
        this.battle = battle;
        battleLock.unlock();
        startRound(battle.getRound());
    }

    /**
     * Handler for {@link Cmd#MOVE} command
     * @param direction direction expressed as an integer constant (see {@link MoveDirection})
     */
    void move(int direction) {
        assert 0 <= direction && direction < moveDirectionValues.length;
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;
            round.move(moveDirectionValues[direction]);
        }
        // also send Move Ack (in the Server "handler.go" actually does it)
        synchronized (lock) {
            emulator.receive(array.clear().add(move).add(0));
        }
    }

    /**
     * Handler for {@link Cmd#USE_THING} command
     */
    void useThing() {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;
            round.useThing();
            synchronized (lock) {
                emulator.receive(array.clear().add(thingTaken).add(1).add(0));
            }
        }
    }

    /**
     * Handler for {@link Cmd#USE_SKILL} command
     * @param skillId ability ID (see {@link Ability} enumeration starting with 0x20)
     */
    void useSkill(int skillId) {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;
            Cells.CellObjectThing thing = round.useSkill(skillId);
            if (thing != null) { // thing may be NULL (in case skill produced nothing)
                int thingId = thing.getId();
                synchronized (lock) {
                    emulator.receive(array.clear().add(thingTaken).add(1).add(thingId));
                }
            }
            IIntArray abilities = round.getCurrentAbilities();
            synchronized (lock) {
                array.copyFrom(abilities, abilities.length()).prepend(abilities.length()).prepend(abilityList);
                emulator.receive(array);
            }
        }
    }

    /**
     * Shuts the Battle manager down and releases corresponding resources
     */
    @SuppressWarnings("unused")
    void close() {
        //Assert(battleMgr.stop, battleMgr.environment)
        //battleMgr.stop <- true
        environment.close();
    }

    /**
     * Invoked when an object coordinates on a battlefield are changed
     * @param obj object (NON-NULL)
     * @param newXy new position
     * @param reset TRUE if an object resets its position (e.g. teleportation), and FALSE - for smooth moving
     */
    void objChanged(Cells.CellObject obj, int newXy, boolean reset) {
        synchronized (lock) {
            array.clear().add(stateChanged).add(obj.getNumber()).add(obj.getId()).add(newXy).add(reset ? 1 : 0);
            emulator.receive(array);
        }
    }

    /**
     * Invoked when [non-poisoned] food has been eaten by an actor
     */
    void foodEaten() {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;
            Player player = round.player1;

            player.score++;
            synchronized (lock) {
                emulator.receive(array.clear().add(scoreChanged).add(player.score).add(0));
            }
            round.checkRoundFinished();
        }
    }

    /**
     * Invoked when a thing has been taken (dropped) by an actor
     * @param thing thing (may be NULL that means an actor dropped a thing)
     */
    void thingTaken(Cells.CellObjectThing thing) {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;
            round.setThingToPlayer(thing);
            synchronized (lock) {
                emulator.receive(array.clear().add(thingTaken).add(1).add(thing != null ? thing.getId() : 0));
            }
        }
    }

    /**
     * Invoked when a new object have been added to the battlefield (e.g. an actor established an umbrella)
     * @param obj object (NON-NULL)
     */
    void objAppended(Cells.CellObject obj) {
        synchronized (lock) {
            emulator.receive(array.clear().add(objectAppended).add(obj.getId()).add(obj.getNumber()).add(obj.getXy()));
        }
    }

    /**
     * Invoked when a round has been finished
     * @param winner TRUE if we've won the round, and FALSE - if our enemy has won
     */
    void roundFinished(boolean winner) {
        Battle battle = getBattle();
        if (battle != null) {
            boolean gameOver = battle.checkBattle(winner);
            Detractor detractor1 = battle.detractor1;
            Detractor detractor2 = battle.detractor2;
            int score1 = detractor1.score;
            int score2 = detractor2.score;
            synchronized (lock) {
                array.clear().add(finished).add(0).add(winner ? 1 : 0).add(score1).add(score2).add(0).add(0).add(0).add(0);
                emulator.receive(array);
            }

            if (!gameOver) {
                Round round = battle.nextRound();
                startRound(round);
            } else {
                battle.stop();
                battleLock.lock();
                this.battle = null;
                battleLock.unlock();
                emulator.gameOver(winner);
                synchronized (lock) {
                    array.clear();
                    array.add(finished).add(1).add(winner ? 1 : 0).add(score1).add(score2).add(0).add(0).add(0).add(0);
                    emulator.receive(array);
                }
            }
        }
    }

    /**
     * Invoked when an actor has been eaten by a wolf
     * <br><b>Note:</b> This method differs from {@link #hurt(boolean, HurtCause) hurt()} and cannot be generalized
     * @param actor actor (either we or the enemy)
     */
    void eatenByWolf(ActorEx actor) {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;
            Player player = round.getPlayerByActor(actor);
            assert player != null;

            hurt(player == round.player1, Devoured);
        }
    }

    /**
     * Invoked when an actor has been hurt by something, expressed as <b>cause</b>
     * @param me TRUE for us, and FALSE - for our enemy
     * @param cause hurt cause (poisoned, sunk, etc.)
     */
    void hurt(boolean me, Model.HurtCause cause) {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;

            boolean isAlive = round.wound(me);
            int lives1 = round.player1.lives;
            int lives2 = round.player2.lives;
            int causeId = Arrays.binarySearch(hurtCauseValues, cause); // don't use "cause.ordinal()"!
            synchronized (lock) {
                emulator.receive(array.clear().add(playerWounded).add(1).add(causeId).add(lives1).add(lives2));
            }
            if (isAlive) {
                round.restore();
            } else roundFinished(false);
        }
    }

    /**
     * Invoked when the effect have been added/removed from the object
     * @param effect effect
     * @param added TRUE to add, and FALSE to remove effect
     * @param objNumber object to apply the effect on (usually actors and wolves)
     */
    void effectChanged(Model.Effect effect, boolean added, int objNumber) {
        int effectId = Arrays.binarySearch(effectValues, effect); // don't use "effect.ordinal()"!
        synchronized (lock) {
            emulator.receive(array.clear().add(effectChanged).add(effectId).add(added ? 1 : 0).add(objNumber));
        }
    }

    /**
     * Applies the effect on the enemy (for us it's the opponent, but for him - it's ourselves)
     * @param isActor1 TRUE if we're initiator, and FALSE - if the opponent is
     * @param effect effect to apply on our enemy
     */
    void setEffectOnEnemy(boolean isActor1, Model.Effect effect) {
        Battle battle = getBattle();
        if (battle != null) {
            Round round = battle.getRound();
            assert round != null;

            Player player = round.getPlayerBySid(!isActor1);
            ActorEx actor = player.actor;
            assert actor != null;

            if (!actor.hasSwagga(Sunglasses)) {
                actor.setEffect(effect, 1, null); // only formality; in fact it's an empty effect on server-side
                effectChanged(effect, true, actor.getNumber());
            }
        }
    }

    /**
     * Starts the round
     * @param round round
     */
    private void startRound(Round round) {
        assert round != null;
        assert round.player1.actor != null && round.player2.actor != null;

        IIntArray base = round.field.raw;
        Model.Character char1 = round.player1.actor.getCharacter();
        Model.Character char2 = round.player2.actor.getCharacter();
        int char1Id = Arrays.binarySearch(characterValues, char1); // don't use "cmd.ordinal()"
        int char2Id = Arrays.binarySearch(characterValues, char2); // don't use "cmd.ordinal()"
        int lives1 = round.player1.lives;
        int lives2 = round.player2.lives;
        IIntArray abilities1 = round.getCurrentAbilities();
        int t = round.field.timeSec;
        String fname = round.levelname;

        synchronized (lock) {
            array.fromByteArray(getBytes(fname), fname.length()).prepend(lives2).prepend(lives1).prepend(char2Id)
                    .prepend(char1Id).prepend(1).prepend(t).prepend(round.number).prepend(roundInfo);
            emulator.receive(array);
            emulator.receive(base.prepend(fullState));
            array.copyFrom(abilities1, abilities1.length()).prepend(abilities1.length()).prepend(abilityList);
            emulator.receive(array);
        }
    }
}
