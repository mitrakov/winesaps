package ru.mitrakov.self.rush.model.emulator;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Model.Ability.*;
import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * Analog of Server Battle class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class Battle {
    /** Reference to the Battle manager */
    private final BattleManager battleManager;
    /** Lock (analog of Mutex/RWMutex in Go) */
    private final ReentrantLock lock = new ReentrantLock();
    /** Count of round wins to win the entire battle (usually 3 on the Server) */
    private final int wins;
    /** Array of level names */
    private final String[] levelnames;
    /** List of skills available for a battle (note: skills belong to an actor for a battle, not to a user) */
    private final List<Model.Ability> skills = new CopyOnWriteArrayList<Model.Ability>();
    /** List of swaggas available for a battle (note: swaggas belong to an actor for a battle, not to a user) */
    private final List<Model.Ability> swaggas = new CopyOnWriteArrayList<Model.Ability>();

    /** Participant 1 */
    final Detractor detractor1;
    /** Participant 2 */
    final Detractor detractor2;

    /** Reference to a current round (not intended to be NULL, but I recommend to check for non-null anyway) */
    private Round curRound;

    /**
     * Creates new battle
     * @param character1 character of aggressor
     * @param character2 character of defender
     * @param levelnames array of level names
     * @param wins count of round wins to win the entire battle
     * @param aggressorAbilities ability list of aggressor
     * @param defenderAbilities ability list of defender
     * @param battleManager {@link BattleManager}
     */
    Battle(Model.Character character1, Model.Character character2, String[] levelnames, int wins,
           IIntArray aggressorAbilities, IIntArray defenderAbilities, BattleManager battleManager) {
        assert character1 != null && character2 != null && levelnames != null && battleManager != null;

        if (levelnames.length > 0) {
            detractor1 = new Detractor(character1, aggressorAbilities);
            detractor2 = new Detractor(character2, defenderAbilities);
            List<Model.Ability> skills1 = extractAbilitiesSkills(aggressorAbilities);
            List<Model.Ability> swaggas1 = extractAbilitiesSwaggas(aggressorAbilities);
            List<Model.Ability> empty = Collections.emptyList();
            curRound = new Round(character1, character2, 0, levelnames[0], skills1, empty, swaggas1, empty,
                    battleManager);
            this.levelnames = levelnames;
            this.wins = wins;
            this.battleManager = battleManager;
        } else throw new IllegalArgumentException("Empty levels list");
    }

    /**
     * Extracts only swaggas from the abilities array
     * @see #extractAbilitiesSkills(IIntArray)
     * @param abilities abilities array
     * @return list of swaggas
     */
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

    /**
     * Extracts only skills from the abilities array
     * @see #extractAbilitiesSwaggas(IIntArray)
     * @param abilities abilities array
     * @return list of skills
     */
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

    /**
     * @return current round
     */
    Round getRound() {
        lock.lock();
        Round round = curRound;
        lock.unlock();
        return round;
    }

    /**
     * Checks whether the battle is finished
     * @param meWinner TRUE if we've won the round, and FALSE - if our enemy has won
     * @return true, if the battle is over
     */
    boolean checkBattle(boolean meWinner) {
        // increase score
        if (meWinner) {
            detractor1.score++;
        } else detractor2.score++;
        return detractor1.score >= wins || detractor2.score >= wins;
    }

    /**
     * Starts the next round of the battle
     * @return reference to a new Round
     */
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
            round = new Round(detractor1.character, detractor2.character, number, levelname, skills1,
                    empty, swaggas1, empty, battleManager);
            lock.lock();
            curRound = round;
            lock.unlock();
        } else throw new IllegalArgumentException("Incorrect levels length");
        return round;
    }

    /**
     * Stops the battle (this method must be called to release the acquired resources)
     */
    void stop() {
        Round round = getRound();
        assert round != null;
        round.stop.cancel();

        Environment env = battleManager.getEnvironment();
        assert env != null;
        env.removeField();
    }
}
