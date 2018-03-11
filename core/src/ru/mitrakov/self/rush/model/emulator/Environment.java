package ru.mitrakov.self.rush.model.emulator;

import java.util.*;

import ru.mitrakov.self.rush.model.*;

import static ru.mitrakov.self.rush.model.Field.WIDTH;
import static ru.mitrakov.self.rush.model.Model.Ability.VoodooMask;
import static ru.mitrakov.self.rush.model.Model.Effect.Afraid;

/**
 * Created by mitrakov on 11.03.2018
 */
@SuppressWarnings("FieldCanBeLocal")
class Environment {
    private static int TICK_DELAY = 250;
    private static int VOODOO_DISTANCE = 3;

    private final Random rand = new Random(System.nanoTime());
    private final Timer stop;
    private FieldEx field;

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

    void addField(FieldEx field) {
        assert field != null;
        // TODO add lock
        this.field = field;
    }

    FieldEx getField() {
        return field;
    }

    void removeField() {
        field = null;
    }

    void close() {
        stop.cancel();
    }

    private void stepWolf(FieldEx field, WolfEx wolf, BattleManager battleManager) {
        System.out.println(1);
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
