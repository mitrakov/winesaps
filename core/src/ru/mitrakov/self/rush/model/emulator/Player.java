package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Cells;
import ru.mitrakov.self.rush.model.Model;

import static ru.mitrakov.self.rush.model.Field.TRASH_CELL;

/**
 * Created by mitrakov on 09.03.2018
 */
class Player {
    private Cells.CellObjectThing thing;

    final ActorEx actor;
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
