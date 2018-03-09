package ru.mitrakov.self.rush.model.emulator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 09.03.2018
 */
class Detractor {
    final Model.Character character;
    int score;
    public final Map<Model.Ability, Integer> abilityExpireMap = new ConcurrentHashMap<Model.Ability, Integer>();

    Detractor(Model.Character character) {
        this.character = character;
    }
}
