package ru.mitrakov.self.rush.model.emulator;

import java.util.*;

import ru.mitrakov.self.rush.model.*;

import static ru.mitrakov.self.rush.model.Field.WIDTH;
import static ru.mitrakov.self.rush.model.Model.Effect.Afraid;
import static ru.mitrakov.self.rush.model.Model.Ability.VoodooMask;

/**
 * Analog of Server Environment class (reconstructed from Server v.1.3.6)
 * <br>This class is intended to have a single instance for all the battles
 * @author Mitrakov
 */
@SuppressWarnings("FieldCanBeLocal")
class Environment {
    /** Duration between each wolf step, in ms (e.g. 250 means that a wolf performs 4 steps per second) */
    private static int TICK_DELAY = 250;
    /** Distance (in cells) that a wolf looks up for an actor wearing VooDoo Mask */
    private static int VOODOO_DISTANCE = 3;

    /** Random */
    private final Random rand = new Random(System.nanoTime());
    /** Main Timer (it is named "stop" because in Go there is a channel "stop" to interrupt the main timer) */
    private final Timer stop;
    /** Battlefield (note that on the Server there is a map of different fields) */
    private volatile FieldEx field;

    /**
     * Creates new Environment
     * <br>This class is intended to have a single instance for all the battles
     * @param battleManager {@link BattleManager}
     */
    Environment(final BattleManager battleManager) {
        assert battleManager != null;

        this.stop = new Timer(true);
        this.stop.schedule(new TimerTask() {
            @Override
            public void run() {
                FieldEx field = getField();
                if (field != null) {
                    List<WolfEx> wolves = field.getWolves();
                    for (int i = 0; i < wolves.size(); i++) {
                        WolfEx wolf = wolves.get(i);
                        Field possiblyAlreadyNewField = getField();
                        if (field == possiblyAlreadyNewField) {
                            stepWolf(field, wolf, battleManager);
                        }
                    }
                }
            }
        }, TICK_DELAY, TICK_DELAY);
    }

    /**
     * Sets new field for the environment
     * (note that on the Server there are a lot of fields, that's why it's named "addField" instead of "setField")
     * @param field battlefield
     */
    void addField(FieldEx field) {
        assert field != null;
        this.field = field;
    }

    /**
     * Returns current field (note that on the Server it returns a field by Sid)
     * @return current field (might be NULL)
     */
    FieldEx getField() {
        return field;
    }

    /**
     * Removes field (note that here we clear a reference, but on the Server it really removes the field from the Map)
     */
    void removeField() {
        field = null;
    }

    /**
     * Shuts the environment down and releases all the acquired resources
     */
    void close() {
        stop.cancel();
    }

    /**
     * Performs single step for a wolf
     * @param field battlefield
     * @param wolf wolf to perform actions on
     * @param battleManager {@link BattleManager}
     */
    private void stepWolf(FieldEx field, WolfEx wolf, BattleManager battleManager) {
        assert wolf != null && field != null;
        Cell cell = wolf.getCell();
        assert cell != null;

        if (wolfAfraid(field, cell, wolf.curDir > 0, VOODOO_DISTANCE)) {
            wolf.curDir *= -1;
            battleManager.effectChanged(Afraid, true, wolf.getNumber());
        }
        if (cell.objectExists(Cells.LadderTop.class) && rand.nextBoolean() && !wolf.justUsedLadder) {
            field.move(wolf, cell.xy + WIDTH);
            wolf.justUsedLadder = true;
        } else if (cell.objectExists(Cells.LadderBottom.class) && rand.nextBoolean() && !wolf.justUsedLadder) {
            field.move(wolf, cell.xy - WIDTH);
            wolf.justUsedLadder = true;
        } else if (cell.objectExists(Cells.RopeLine.class) && rand.nextBoolean()) {
            field.move(wolf, cell.xy - WIDTH);
        } else {
            boolean success = field.move(wolf, cell.xy + wolf.curDir);
            if (!success) {
                wolf.curDir *= -1;
            }
            wolf.justUsedLadder = false;
        }
    }

    /**
     * [Recursively] checks whether a wolf is terrified of an actor wearing VooDoo mask
     * @param field battlefield
     * @param cell current cell
     * @param toRight wolf's direction (TRUE to check to the right, FALSE to check to the left)
     * @param n recursive parameter
     * @return TRUE if wolf is afraid of an actor wearing VooDoo mask, FALSE - otherwise
     */
    private boolean wolfAfraid(FieldEx field, Cell cell, boolean toRight, int n) {
        Cell nextCell = field.getCellByDirection(cell, toRight);
        if (nextCell != null) {
            if (nextCell.objectExists(Cells.Actor1.class)) {
                return field.actor1.hasSwagga(VoodooMask);
            } else if (n > 0) {
                return wolfAfraid(field, nextCell, toRight, n - 1);
            }
        }
        return false;
    }
}
