package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.*;

/**
 * Analog of Server Wolf class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
class WolfEx extends Cells.Wolf {
    /** Current direction, expressed as an integer (1 -> right, -1 -> left) */
    int curDir = 1;
    /** Helper flag to prevent a wolf from moving up and down 1000 times; TRUE if a wolf just used a ladder this step */
    boolean justUsedLadder;

    /**
     * Creates a new wolf
     * @param cell cell to store the wolf
     * @param number sequence number on the battlefield
     */
    WolfEx(Cell cell, int number) {
        super(cell, number);
    }
}
