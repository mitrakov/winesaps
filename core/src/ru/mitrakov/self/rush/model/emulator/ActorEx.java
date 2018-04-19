package ru.mitrakov.self.rush.model.emulator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Model.effectValues;

/**
 * Analog of Server Actor class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class ActorEx extends Cells.Actor1 {
    /** Array to store actor effect steps (index is effect ID, value is count of actor steps when effect is active) */
    private final IIntArray effectSteps = new GcResistantIntArray(effectValues.length);
    /** List of effect callback functions (index is effect ID, value is to be run when effect is over) */
    private final List<Runnable> effectCallbacks = new CopyOnWriteArrayList<Runnable>();
    /** List of the actor's abilities */
    private final List<Model.Ability> swaggas = new CopyOnWriteArrayList<Model.Ability>();

    /** Character (rabbit, squirrel, etc.) */
    private Model.Character character;
    /** Direction flag (TRUE - the actor looks to the right, FALSE - the actor looks left) */
    private boolean directionRight = true;

    /**
     * Creates a new Extended Actor
     * @param cell location
     * @param number sequence number of actor on a {@link FieldEx Battlefield}
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    ActorEx(Cell cell, int number) {
        super(cell, number);
        for (int i = 0; i < effectValues.length; i++) {
            effectSteps.add(0);
            effectCallbacks.add(null);
        }
    }

    /**
     * @return character (rabbit, squirrel, etc.)
     */
    public Model.Character getCharacter() {
        return character;
    }

    /**
     * Sets a character to the actor
     * @param character character (rabbit, squirrel, etc.)
     */
    public void setCharacter(Model.Character character) {
        this.character = character;
    }

    /**
     * @return TRUE if the actor looks to the right (FALSE otherwise)
     */
    boolean isDirectedToRight() {
        return directionRight;
    }

    /**
     * Sets the direction of the actor
     * @param directedRight TRUE to set the direction to the right, FALSE - to the left
     */
    void setDirectionRight(boolean directedRight) {
        this.directionRight = directedRight;
    }

    /**
     * Should be called on each step in order to increase internal effect counters
     */
    void addStep() {
        for (int i = 0; i < effectSteps.length(); i++) {
            if (effectSteps.get(i) > 0) {
                effectSteps.set(i, effectSteps.get(i)-1);
                if (effectSteps.get(i) == 0 && effectCallbacks.get(i) != null)
                    effectCallbacks.get(i).run();
            }
        }
    }

    /**
     * Sets an effect to the actor
     * @param effect effect
     * @param steps count of steps that the effect is active
     * @param callback function to be run when the effect is over (may be NULL)
     */
    void setEffect(Model.Effect effect, int steps, Runnable callback) {
        int effectId = Arrays.binarySearch(effectValues, effect);
        effectSteps.set(effectId, steps);
        effectCallbacks.set(effectId, callback);
    }

    /**
     * Checks whether the actor has a given effect
     * @param effect effect
     * @return TRUE, if the actor has a given effect
     */
    boolean hasEffect(Model.Effect effect) {
        int effectId = Arrays.binarySearch(effectValues, effect);
        int steps = effectSteps.get(effectId);
        return steps > 0;
    }

    /**
     * Adds an ability to the actor's ability list
     * @param ability ability
     */
    void addSwagga(Model.Ability ability) {
        swaggas.add(ability);
    }

    /**
     * Checks whether the actor has a given ability
     * @param s ability
     * @return TRUE, if the actor has a given ability
     */
    boolean hasSwagga(Model.Ability s) {
        for (int i = 0; i < swaggas.size(); i++) {
            Model.Ability v = swaggas.get(i); // don't use iterators here (Garbage Collector issues!)
            if (v == s) return true;
        }
        return false;
    }

    /**
     * @return list of all of the actor's abilities
     */
    List<Model.Ability> getSwaggas() {
        return swaggas;
    }
}
