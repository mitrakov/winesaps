package ru.mitrakov.self.rush.model.emulator;

import java.util.concurrent.locks.ReentrantLock;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 09.03.2018
 */
class Battle {
    private final BattleManager battleManager;
    private final ReentrantLock lock = new ReentrantLock();
    private final int wins;
    private final int roundTimeSec;
    private final String[] levelnames;

    final Detractor detractor1;
    final Detractor detractor2;
    Round curRound;

    Battle(Model.Character character1, Model.Character character2, String[] levelnames, int timeSec, int wins,
           BattleManager battleManager) {
        assert character1 != null && character2 != null && levelnames != null && battleManager != null;

        if (levelnames.length > 0) {
            detractor1 = new Detractor(character1);
            detractor2 = new Detractor(character2);
            //skills1, swaggas1 := extractAbilities(aggressorAbilities)
            //skills2, swaggas2 := extractAbilities(defenderAbilities)
            curRound = new Round(character1, character2, 0, levelnames[0], timeSec, battleManager);
            this.levelnames = levelnames;
            this.roundTimeSec = timeSec;
            this.wins = wins;
            this.battleManager = battleManager;
        } else throw new IllegalArgumentException("Empty levels list");
    }


    Round getRound() {
        lock.lock();
        Round round = curRound;
        lock.unlock();
        return round;
    }

    boolean checkBattle(boolean meWinner) {
        // increase score
        if (meWinner) {
            detractor1.score++;
        } else detractor2.score++;
        return detractor1.score >= wins || detractor2.score >= wins;

    }

    Round nextRound() {
        Round round;
        stop();

        // copy parameters from the previous round
        Round oldRound = getRound();
        int number = 0;
        if (oldRound != null)
            number = oldRound.number + 1;
        if (number < levelnames.length) {
            String levelname = levelnames[number];
            // get parameters from within battle
            // skills1, swaggas1 := extractAbilities(detractor1.abilities) // see note below
            // skills2, swaggas2 := extractAbilities(detractor2.abilities) // see note below
            // create a new round
            round = new Round(detractor1.character, detractor2.character, number, levelname, roundTimeSec,
                    battleManager);
            lock.lock();
            curRound = round;
            lock.unlock();
        } else throw new IllegalArgumentException("Incorrect levels length");
        return round;
    }

    void stop() {

    }
}
