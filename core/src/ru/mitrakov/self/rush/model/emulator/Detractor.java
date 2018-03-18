package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Created by mitrakov on 09.03.2018
 */
class Detractor {
    final Model.Character character;
    final IIntArray abilities;
    int score;

    Detractor(Model.Character character, IIntArray abilities) {
        this.character = character;
        this.abilities = abilities;
    }
}
