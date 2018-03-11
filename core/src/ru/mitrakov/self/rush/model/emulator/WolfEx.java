package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Cell;
import ru.mitrakov.self.rush.model.Cells;

/**
 * Created by mitrakov on 11.03.2018
 */
class WolfEx extends Cells.Wolf {
    int curDir = 1;
    boolean justUsedLadder;

    WolfEx(Cell cell, int number) {
        super(cell, number);
    }
}
