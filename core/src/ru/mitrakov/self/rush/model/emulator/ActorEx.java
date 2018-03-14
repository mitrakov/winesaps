package ru.mitrakov.self.rush.model.emulator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Model.effectValues;

/**
 * Created by mitrakov on 08.03.2018
 */
class ActorEx extends Cells.Actor1 {
    private final IIntArray effectSteps = new GcResistantIntArray(effectValues.length);
    private final List<Runnable> effectCallbacks = new CopyOnWriteArrayList<Runnable>();

    private Model.Character character;
    private boolean directionRight = true;

    @SuppressWarnings("ForLoopReplaceableByForEach")
    ActorEx(Cell cell, int number) {
        super(cell, number);
        for (int i = 0; i < effectValues.length; i++) {
            effectSteps.add(0);
            effectCallbacks.add(null);
        }
    }

    public Model.Character getCharacter() {
        return character;
    }

    public void setCharacter(Model.Character character) {
        this.character = character;
    }

    boolean isDirectedToRight() {
        return directionRight;
    }

    void setDirectionRight(boolean directedRight) {
        this.directionRight = directedRight;
    }

    void addStep() {
        for (int i = 0; i < effectSteps.length(); i++) {
            if (effectSteps.get(i) > 0) {
                effectSteps.set(i, effectSteps.get(i)-1);
                if (effectSteps.get(i) == 0 && effectCallbacks.get(i) != null)
                    effectCallbacks.get(i).run();
            }
        }
    }

    void setEffect(Model.Effect effect, int steps, Runnable callback) {
        int effectId = Arrays.binarySearch(effectValues, effect);
        effectSteps.set(effectId, steps);
        effectCallbacks.set(effectId, callback);
    }

    boolean hasEffect(Model.Effect effect) {
        int effectId = Arrays.binarySearch(effectValues, effect);
        int steps = effectSteps.get(effectId);
        return steps > 0;
    }

    void addSwagga(Model.Ability ability) {
        // TODO
    }

    boolean hasSwagga(Model.Ability ability) {
        return false; // TODO
    }
}
