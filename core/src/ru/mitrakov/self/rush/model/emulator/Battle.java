package ru.mitrakov.self.rush.model.emulator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Model.abilityValues;
import static ru.mitrakov.self.rush.model.Model.Ability.*;

/**
 * Created by mitrakov on 09.03.2018
 */
class Battle {
    private final BattleManager battleManager;
    private final ReentrantLock lock = new ReentrantLock();
    private final int roundTimeSec;
    private final int wins;
    private final String[] levelnames;
    private final List<Model.Ability> skills = new CopyOnWriteArrayList<Model.Ability>();
    private final List<Model.Ability> swaggas = new CopyOnWriteArrayList<Model.Ability>();

    final Detractor detractor1;
    final Detractor detractor2;
    private Round curRound;

    Battle(Model.Character character1, Model.Character character2, String[] levelnames, int timeSec, int wins,
           IIntArray aggressorAbilities, IIntArray defenderAbilities, BattleManager battleManager) {
        assert character1 != null && character2 != null && levelnames != null && battleManager != null;

        if (levelnames.length > 0) {
            detractor1 = new Detractor(character1, aggressorAbilities);
            detractor2 = new Detractor(character2, defenderAbilities);
            List<Model.Ability> skills1 = extractAbilitiesSkills(aggressorAbilities);
            List<Model.Ability> swaggas1 = extractAbilitiesSwaggas(aggressorAbilities);
            List<Model.Ability> empty = Collections.emptyList();
            curRound = new Round(character1, character2, 0, levelnames[0], timeSec, skills1, empty, swaggas1, empty,
                    battleManager);
            this.levelnames = levelnames;
            this.roundTimeSec = timeSec;
            this.wins = wins;
            this.battleManager = battleManager;
        } else throw new IllegalArgumentException("Empty levels list");
    }

    private synchronized List<Model.Ability> extractAbilitiesSwaggas(IIntArray abilities) {
        swaggas.clear();
        for (int i = 0; i < abilities.length(); i++) {
            int ability = abilities.get(i);
            assert 0 <= ability && ability < abilityValues.length;
            switch (abilityValues[ability]) {
                case Snorkel:
                case ClimbingShoes:
                case SouthWester:
                case VoodooMask:
                case SapperShoes:
                case Sunglasses:
                    swaggas.add(abilityValues[ability]);
                    break;
                default:
            }
        }
        return swaggas;
    }

    private synchronized List<Model.Ability> extractAbilitiesSkills(IIntArray abilities) {
        skills.clear();
        for (int i = 0; i < abilities.length(); i++) {
            int ability = abilities.get(i);
            switch (ability) {
                case 0x21:
                    skills.add(Miner);
                    break;
                case 0x22:
                    skills.add(Builder);
                    break;
                case 0x23:
                    skills.add(Shaman);
                    break;
                case 0x24:
                    skills.add(Grenadier);
                    break;
                case 0x25:
                    skills.add(TeleportMan);
                    break;
                default:
            }
        }
        return skills;
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
            List<Model.Ability> skills1 = extractAbilitiesSkills(detractor1.abilities);
            List<Model.Ability> swaggas1 = extractAbilitiesSwaggas(detractor1.abilities);
            // create a new round
            List<Model.Ability> empty = Collections.emptyList();
            round = new Round(detractor1.character, detractor2.character, number, levelname, roundTimeSec, skills1,
                    empty, swaggas1, empty, battleManager);
            lock.lock();
            curRound = round;
            lock.unlock();
        } else throw new IllegalArgumentException("Incorrect levels length");
        return round;
    }

    void stop() {
        Round round = getRound();
        assert round != null;
        round.stop.cancel();

        Environment env = battleManager.getEnvironment();
        assert env != null;
        env.removeField();
    }
}
