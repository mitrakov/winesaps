package ru.mitrakov.self.rush.model.emulator;

import java.util.*;

import ru.mitrakov.self.rush.model.*;

import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * Analog of Server Player class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class Player {
    final ActorEx actor;
    final List<Model.Ability> skills;
    private Cells.CellObjectThing thing;
    int score = 0;
    int lives = 2;

    Player(ActorEx actor, List<Model.Ability> skills) {
        this.actor = actor;
        this.skills = skills;
    }

    Cells.CellObjectThing setThing(Cells.CellObjectThing thing) {
        Cells.CellObjectThing oldThing = this.thing;
        this.thing = thing;
        return oldThing;
    }

    Model.Ability getSkill(int id) {
        for (int i = 0; i < skills.size(); i++) {
            Model.Ability ability = skills.get(i);
            int abilityId = Arrays.binarySearch(abilityValues, ability); // don't use "ability.ordinal()"!
            if (abilityId == id) return ability;
        }
        return null;
    }
}
