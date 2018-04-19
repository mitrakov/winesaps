package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Cell;
import ru.mitrakov.self.rush.model.Cells;

/**
 * Analog of Server Wolf class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class WolfEx extends Cells.Wolf {
    int curDir = 1;
    boolean justUsedLadder;

    WolfEx(Cell cell, int number) {
        super(cell, number);
    }
}
