package ru.mitrakov.self.rush.model.emulator;

import java.util.Arrays;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Field.*;
import static ru.mitrakov.self.rush.model.Model.*;
import static ru.mitrakov.self.rush.utils.Utils.*;
import static ru.mitrakov.self.rush.model.Model.Cmd.*;
import static ru.mitrakov.self.rush.model.Model.HurtCause.*;

/**
 * Created by mitrakov on 04.02.2018
 */
class BattleManager {
    private final ServerEmulator emulator;
    private final Object lock = new Object();
    private final Model.IFileReader fileReader;
    private final Environment environment;
    private final IIntArray array = new GcResistantIntArray(WIDTH * Field.HEIGHT);      // need to be synchronized!

    private final int fullState = Arrays.binarySearch(cmdValues, FULL_STATE);           // don't use "cmd.ordinal()"
    private final int roundInfo = Arrays.binarySearch(cmdValues, ROUND_INFO);           // don't use "cmd.ordinal()"
    private final int abilityList = Arrays.binarySearch(cmdValues, ABILITY_LIST);       // don't use "cmd.ordinal()"
    private final int move = Arrays.binarySearch(cmdValues, MOVE);                      // don't use "cmd.ordinal()"
    private final int stateChanged = Arrays.binarySearch(cmdValues, STATE_CHANGED);     // don't use "cmd.ordinal()"
    private final int objectAppended = Arrays.binarySearch(cmdValues, OBJECT_APPENDED); // don't use "cmd.ordinal()"
    private final int scoreChanged = Arrays.binarySearch(cmdValues, SCORE_CHANGED);     // don't use "cmd.ordinal()"
    private final int thingTaken = Arrays.binarySearch(cmdValues, THING_TAKEN);         // don't use "cmd.ordinal()"
    private final int effectChanged = Arrays.binarySearch(cmdValues, EFFECT_CHANGED);   // don't use "cmd.ordinal()"
    private final int playerWounded = Arrays.binarySearch(cmdValues, PLAYER_WOUNDED);   // don't use "cmd.ordinal()"
    private final int finished = Arrays.binarySearch(cmdValues, FINISHED);              // don't use "cmd.ordinal()"

    private Battle battle;

    BattleManager(ServerEmulator emulator, Model.IFileReader fileReader) {
        assert fileReader != null;
        this.emulator = emulator;
        this.fileReader = fileReader;
        this.environment = new Environment(this);
    }

    Model.IFileReader getFileReader() {
        return fileReader;
    }

    Environment getEnvironment() {
        return environment;
    }

    Battle getBattle() {
        return battle;
    }

    void accept(Model.Character character1, Model.Character character2, IIntArray aggAbilities, IIntArray defAbilities,
                String[] levelnames, int timeSec, int wins) {
        battle = new Battle(character1, character2, levelnames, timeSec, wins, aggAbilities, defAbilities, this);
        startRound(battle.getRound());
    }

    void move(int direction) {
        assert battle != null && 0 <= direction && direction < moveDirectionValues.length;
        Round round = battle.getRound();
        assert round != null;

        round.move(moveDirectionValues[direction]);
        synchronized (lock){
            emulator.receive(array.clear().add(move).add(0));
        }
    }

    void useThing() {
        assert battle != null;
        Round round = battle.getRound();
        assert round != null;
        round.useThing();
        synchronized (lock) {
            emulator.receive(array.clear().add(thingTaken).add(1).add(0));
        }
    }

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

    void close() {
        //Assert(battleMgr.stop, battleMgr.environment)
        //battleMgr.stop <- true
        environment.close();
    }

    void objChanged(Cells.CellObject obj, int newXy, boolean reset) {
        synchronized (lock) {
            array.clear().add(stateChanged).add(obj.getNumber()).add(obj.getId()).add(newXy).add(reset ? 1 : 0);
            emulator.receive(array);
        }
    }

    void foodEaten() {
        assert battle != null;
        Round round = battle.getRound();
        assert round != null;
        Player player = round.player1;

        player.score++;
        synchronized (lock) {
            emulator.receive(array.clear().add(scoreChanged).add(player.score).add(0));
        }
        round.checkRoundFinished();
    }

    void thingTaken(Cells.CellObjectThing thing) {
        assert battle != null;
        Round round = battle.getRound();
        assert round != null;
        round.setThingToPlayer(thing);
        synchronized (lock) {
            emulator.receive(array.clear().add(thingTaken).add(1).add(thing != null ? thing.getId() : 0));
        }
    }

    void objAppended(Cells.CellObject obj) {
        synchronized (lock) {
            emulator.receive(array.clear().add(objectAppended).add(obj.getId()).add(obj.getNumber()).add(obj.getXy()));
        }
    }

    void roundFinished(boolean winner) {
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
            synchronized (lock) {
                array.clear();
                array.add(finished).add(1).add(winner ? 1 : 0).add(score1).add(score2).add(0).add(0).add(0).add(0);
                emulator.receive(array);
            }
        }
    }

    void eatenByWolf(ActorEx actor) {
        Round round = battle.getRound();
        assert round != null;
        Player player = round.getPlayerByActor(actor);
        assert player != null;

        hurt(player == round.player1, Devoured);
    }

    void hurt(boolean me, Model.HurtCause cause) {
        assert battle != null;
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

    void effectChanged(Model.Effect effect, boolean added, int objNumber) {
        int effectId = Arrays.binarySearch(effectValues, effect); // don't use "effect.ordinal()"!
        synchronized (lock) {
            emulator.receive(array.clear().add(effectChanged).add(effectId).add(added ? 1 : 0).add(objNumber));
        }
    }

    @SuppressWarnings("UnusedParameters")
    void setEffectOnEnemy(Model.Effect effect) {
        // TODO
    }

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
        int t = round.timeSec;
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
