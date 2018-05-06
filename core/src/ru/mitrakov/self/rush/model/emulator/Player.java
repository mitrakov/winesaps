package ru.mitrakov.self.rush.model.emulator;

import java.util.*;

import ru.mitrakov.self.rush.model.*;

import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * Analog of Server Player class (reconstructed from Server v.1.3.6)
 * <br>Player is just an actor with additional attributes (skills, score, lives and other stuff)
 * @author Mitrakov
 */
class Player {
    /** Actor on the battlefield */
    final ActorEx actor;
    /** Skills list */
    final List<Model.Ability> skills;
    /** Thing that actor may or may not possess */
    private Cells.CellObjectThing thing;
    /** Score inside a round (don't mix up with Total Score) */
    int score = 0;
    /** Lives count (default is 2) */
    int lives = 2;

    /**
     * Creates a new player based on the given actor with the given abilities
     * @param actor actor
     * @param skills skills list
     */
    Player(ActorEx actor, List<Model.Ability> skills) {
        this.actor = actor;
        this.skills = skills;
    }

    /**
     * Sets a given thing to the player
     * @param thing thing
     * @return old thing, if a player had the other thing before, or NULL - if a player had nothing
     */
    Cells.CellObjectThing setThing(Cells.CellObjectThing thing) {
        Cells.CellObjectThing oldThing = this.thing;
        this.thing = thing;
        return oldThing;
    }

    /**
     * @param id skill ID
     * @return skill by its ID
     */
    Model.Ability getSkill(int id) {
        for (int i = 0; i < skills.size(); i++) {
            Model.Ability ability = skills.get(i);
            int abilityId = Arrays.binarySearch(abilityValues, ability); // don't use "ability.ordinal()"!
            if (abilityId == id) return ability;
        }
        return null;
    }
}
