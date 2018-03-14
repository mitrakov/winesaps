package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Cells;

/**
 * Created by mitrakov on 09.03.2018
 */
class Player {
    final ActorEx actor;
    private Cells.CellObjectThing thing;
    int score = 0;
    int lives = 2;

    Player(ActorEx actor) {
        this.actor = actor;
    }

    Cells.CellObjectThing setThing(Cells.CellObjectThing thing) {
        Cells.CellObjectThing oldThing = this.thing;
        this.thing = thing;
        return oldThing;
    }
}
