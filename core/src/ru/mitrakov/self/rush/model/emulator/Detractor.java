package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Analog of Server Detractor class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class Detractor {
    /** Character (rabbit, squirrel, etc.) */
    final Model.Character character;
    /** Abilities expressed as binary data (these abilities are static, i.e. "restored" each round) */
    final IIntArray abilities;
    /** Total score */
    int score;

    /**
     * Creates new detractor
     * @param character character (rabbit, squirrel, etc.)
     * @param abilities abilities expressed as binary data
     */
    Detractor(Model.Character character, IIntArray abilities) {
        this.character = character;
        this.abilities = abilities;
    }
}
