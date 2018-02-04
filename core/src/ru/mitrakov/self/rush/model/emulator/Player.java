package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Cells;
import ru.mitrakov.self.rush.model.Model;

import static ru.mitrakov.self.rush.model.Field.TRASH_CELL;

/**
 * Created by mitrakov on 09.03.2018.
 */
public class Player {
    final ActorEx actor;
    int score = 0;
    int lives = 2;

    public Player(ActorEx actor) {
        this.actor = actor;
    }
}
