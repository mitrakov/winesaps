package ru.mitrakov.self.rush.model.emulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Model.Effect.*;
import static ru.mitrakov.self.rush.model.Model.Ability.*;
import static ru.mitrakov.self.rush.model.Model.HurtCause.*;

/**
 * Created by mitrakov on 08.03.2018
 */
class FieldEx extends Field {
    private static final int ANTIDOTE_EFFECT = 10;
    private static final int DETECTION_LENGTH = 8;

    private final BattleManager battleManager;
    private final ReentrantLock lock = new ReentrantLock();
    private final ReentrantLock cellLock = new ReentrantLock();
    private final List<WolfEx> wolfList = new CopyOnWriteArrayList<WolfEx>();
    private final List<Cells.CellObjectFavouriteFood> favouriteFoodList =
            new CopyOnWriteArrayList<Cells.CellObjectFavouriteFood>();

    /*final*/ ActorEx actor1, actor2;
    final IIntArray raw;

    FieldEx(IIntArray fieldData, IIntArray raw, BattleManager emulator) {
        super(fieldData);
        assert emulator != null;
        this.raw = raw;
        this.battleManager = emulator;

        createSubTypesInternal();
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void createSubTypesInternal() {
        wolfList.clear();
        for (int i = 0; i < cells.length; i++) {
            Cell cell = cells[i];
            cellLock.lock();
            Cells.Actor1 actor1 = cell.getFirst(Cells.Actor1.class);
            if (actor1 != null) {
                this.actor1 = new ActorEx(actor1.getCell(), actor1.getNumber());
                cell.objects.add(this.actor1);
                objects.put(this.actor1.getNumber(), this.actor1);
                actor1.getCell().objects.remove(actor1); // don't forget to remove original Actor to avoid bugs
            }
            Cells.Actor2 actor2 = cell.getFirst(Cells.Actor2.class);
            if (actor2 != null) {
                this.actor2 = new ActorEx(actor2.getCell(), actor2.getNumber());
                cell.objects.add(this.actor2);
                objects.put(this.actor2.getNumber(), this.actor2);
                actor2.getCell().objects.remove(actor2); // don't forget to remove original Actor to avoid bugs
            }
            Cells.Wolf wolf = cell.getFirst(Cells.Wolf.class);
            if (wolf != null) {
                WolfEx wolfEx = new WolfEx(wolf.getCell(), wolf.getNumber());
                wolfList.add(wolfEx);
                cell.objects.add(wolfEx);
                objects.put(wolfEx.getNumber(), wolfEx);
                wolf.getCell().objects.remove(wolf); // don't forget to remove original Wolf to avoid bugs
            }
            cellLock.unlock();
        }
    }

    int getNextNum() {
        return nextNumber.next();
    }

    private void objChanged(Cells.CellObject obj, int xy, boolean reset) {
        battleManager.objChanged(obj, xy, reset);
    }

    private void objAppended(Cells.CellObject obj) {
        battleManager.objAppended(obj);
    }

    boolean isMoveUpPossible(Cell cell) {
        assert cell != null;
        return cell.objectExists(Cells.LadderBottom.class) || cell.objectExists(Cells.RopeLine.class);
    }

    boolean isMoveDownPossible(Cell cell) {
        assert cell != null;
        return cell.objectExists(Cells.LadderTop.class);
    }

    public boolean move(Cells.CellObject actor, int idxTo) {
        lock.lock();
        boolean result = moveSync(actor, idxTo);
        lock.unlock();
        return result;
    }

    private boolean moveSync(Cells.CellObject obj, int idxTo) {
        assert obj != null && obj.getCell() != null;

        if (0 <= idxTo && idxTo < WIDTH * HEIGHT) {
            Cell oldCell = obj.getCell();
            Cell newCell = getCell(idxTo);
            int h = idxTo - oldCell.xy; // increment of index
            boolean leftRight = h * h == 1;
            // face an obstacle
            if (newCell.objectExists(Cells.Block.class)) return false;
            // climb a rope
            if (h == -WIDTH && oldCell.objectExists(Cells.RopeLine.class)) {
                relocate(oldCell, newCell, obj, false);
                return true;
            }
            // scale a dias
            if (leftRight && !(oldCell.bottom instanceof Cells.Dais) && (newCell.bottom instanceof Cells.Dais) &&
                    !oldCell.objectExists(Cells.CellObjectRaisable.class)) {
                if (obj == actor1 && !actor1.hasSwagga(ClimbingShoes) ||
                        obj == actor2 && !actor2.hasSwagga(ClimbingShoes))
                    return false;
            }
            // sink through the floor
            if (oldCell.bottom != null) {
                if (h == WIDTH && !oldCell.objectExists(Cells.LadderTop.class)) return false;
                if (h == -WIDTH && !oldCell.objectExists(Cells.LadderBottom.class)) return false;
            }
            // left-right edges
            if ((oldCell.xy + 1) % WIDTH == 0 && (h > 0 && h < WIDTH)) return false; // if right edge
            if (oldCell.xy % WIDTH == 0 && (h < 0 && h > -WIDTH)) return false; // if left edge

            // relocating
            relocate(oldCell, newCell, obj, false);
            // check if there is a firm ground underfoot
            if (newCell.bottom != null || newCell.objectExists(Cells.BeamChunk.class)) return true;
            // else nothing underfoot: fall down!
            return moveSync(obj, idxTo + WIDTH);
        }
        return false; // in fact client CAN send incorrect XY (example: Move(LeftDown) at X=0; Y=0); since 2.0.0
    }

    private Cell getCell(int xy) {
        assert 0 <= xy && xy < WIDTH * HEIGHT;
        return cells[xy];
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    List<WolfEx> getWolves() {
        return wolfList;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private List<Cells.CellObjectFavouriteFood> getFavouriteFoodList() {
        favouriteFoodList.clear();
        for (int i=0; i<cells.length; i++) {
            Cell cell = cells[i];
            cellLock.lock();
            Cells.CellObjectFavouriteFood food = cell.getFirst(Cells.CellObjectFavouriteFood.class);
            if (food != null)
                favouriteFoodList.add(food);
            cellLock.unlock();
        }
        return favouriteFoodList;
    }

    Cells.Entry1 getEntryByActor() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < cells.length; i++) { // GC!
            Cell cell = cells[i];
            Cells.Entry1 entry = cell.getFirst(Cells.Entry1.class);
            if (entry != null) return entry;
        }
        return null;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    int getFoodCount() {
        int result = 0;
        for (int i = 0; i < cells.length; i++) {
            cellLock.lock();
            Cell cell = cells[i];
            if (cell.objectExists(Cells.CellObjectFood.class))
                result++;
            cellLock.unlock();
        }
        return result;
    }

    void dropThing(ActorEx actor, Cells.CellObjectThing thing) {
        assert actor != null && actor.getCell() != null && thing != null;

        Cell cell = actor.getCell();
        cell.objects.add(thing);
        objChanged(thing, cell.xy, false);
    }

    void useThing(ActorEx actor, Cells.CellObjectThing thing) {
        assert actor != null && thing != null;

        Cell myCell = actor.getCell();
        assert myCell != null;

        // get an appropriate cell (if an obstacle ahead, actor uses its own cell)
        Cell cell = getCellByDirection(myCell, actor.isDirectedToRight());
        if (cell == null || cell.objectExists(Cells.Block.class) || (cell.bottom instanceof Cells.Water)
                || ((cell.bottom instanceof Cells.Dais) && !(myCell.bottom instanceof Cells.Dais))) {
            cell = myCell;
        }

        // // ================
        if (thing instanceof Cells.UmbrellaThing) {
            Cell next = getCellByDirection(cell, actor.isDirectedToRight());
            if (next != null && next.objectExists(Cells.Waterfall.class)) {
                cell = next;
            }
        }
        // ================

        // emplace object
        int number = nextNumber.next();
        Cells.CellObject emplaced = emplace(thing, number, myCell);
        objAppended(emplaced);
        move(emplaced, cell.xy);

        // for mines we must provide a safe single step over the buried mine
        if (emplaced instanceof Cells.Mine)
            actor.setEffect(Attention, 2, null);
    }

    void relocate(Cell oldCell, Cell newCell, Cells.CellObject obj, boolean reset) {
        assert oldCell != null && newCell != null;

        cellLock.lock(); // this lock is needed, because relocate() may be called outside move() context
        obj.setCell(newCell);
        cellLock.unlock();

        objChanged(obj, newCell.xy, reset);
        checkCell(newCell);
    }

    private void checkCell(Cell cell) {
        assert cell != null;

        if (cell.objectExists(Cells.CellObjectActor.class)) {
            final ActorEx actor = cell.objectExists(Cells.Actor1.class)
                    ? actor1 : cell.objectExists(Cells.Actor2.class) ? actor2 : null;
            assert actor != null;

            // ==== 1. Checks that DO NOT return (e.g. items can be collected simultaneously) ===
            Cells.CellObjectFood food = cell.getFirst(Cells.CellObjectFood.class);
            Cells.CellObjectThing thing = cell.getFirst(Cells.CellObjectThing.class);
            Cells.Beam beam = cell.getFirst(Cells.Beam.class);
            Cells.Antidote antidote = cell.getFirst(Cells.Antidote.class);
            Cells.Flashbang bang = cell.getFirst(Cells.Flashbang.class);
            Cells.Detector detector = cell.getFirst(Cells.Detector.class);
            Cells.Teleport teleport = cell.getFirst(Cells.Teleport.class);
            Cells.Mine mine = cell.getFirst(Cells.Mine.class);

            if (food != null && !isPoison(actor, food)) {
                cell.objects.remove(food);
                objChanged(food, 0xFF, true);
                battleManager.foodEaten();
            }
            if (thing != null) {
                cell.objects.remove(thing);
                objChanged(thing, 0xFF, true);
                battleManager.thingTaken(thing);
            }
            if (beam != null) {
                cell.objects.remove(beam);
                objChanged(beam, 0xFF, true);
                createBeamChunks(cell, actor.isDirectedToRight());
            }
            if (antidote != null) {
                actor.setEffect(Antidote, ANTIDOTE_EFFECT, new Runnable() {
                    @Override
                    public void run() {
                        battleManager.effectChanged(Antidote, false, actor.getNumber());
                    }
                });
                cell.objects.remove(antidote);
                objChanged(antidote, 0xFF, true);
                battleManager.effectChanged(Antidote, true, actor.getNumber());
            }
            if (bang != null) {
                cell.objects.remove(bang);
                objChanged(bang, 0xFF, true);
                battleManager.setEffectOnEnemy(Dazzle);
            }
            if (detector != null) {
                cell.objects.remove(detector);
                objChanged(detector, 0xFF, true);
                detectMines(cell, actor.isDirectedToRight(), DETECTION_LENGTH);
            }
            // ==== 2. Checks that RETURN (if 2 things may hurt an actor, it loses only 1 live) ===
            // teleport case (please note, that teleport RETURNS to save from mines, water, etc.)
            if (teleport != null) {
                cell.objects.remove(teleport);
                objChanged(teleport, 0xFF, true);
                int newXy = getMirrorXy(cell.xy);
                if (newXy / WIDTH == HEIGHT - 1)
                    newXy -= WIDTH; // avoid teleportation to underground (not all levels support underground)
                Cell newCell = getCell(newXy);
                relocate(actor.getCell(), newCell, actor, true);
                return;
            }
            if (food != null && isPoison(actor, food)) {
                cell.objects.remove(food);
                objChanged(food, 0xFF, true);
                if (actor.hasEffect(Antidote)) {
                    battleManager.foodEaten();
                    // no return here (we should check mines, waterfalls and so on)
                } else battleManager.hurt(true, Poisoned);
            }
            if (mine != null && !cell.objectExists(Cells.BeamChunk.class) && !actor.hasSwagga(SapperShoes)) {
                if (!actor.hasEffect(Attention)) {
                    // thanks to this condition the actor can ONCE step onto the mine immediately upon it burried;but in
                    // theory the condition allows the enemy to avoid explosion (if it's stepCount = mine.stepCount+1)
                    // so let's consider it as a feature
                    cell.objects.remove(mine);
                    objChanged(mine, 0xFF, true);
                    battleManager.hurt(true, Exploded);
                }
            }
            if (cell.objectExists(Cells.Wolf.class)) {
                battleManager.eatenByWolf(actor);
                return;
            }
            if (cell.objectExists(Cells.Waterfall.class) && !cell.objectExists(Cells.Umbrella.class) &&
                    !actor.hasSwagga(SouthWester)) {
                battleManager.hurt(true, Soaked);
                return;
            }
            if ((cell.bottom instanceof Cells.Water) && !cell.objectExists(Cells.BeamChunk.class) && !actor.hasSwagga(Snorkel)) {
                battleManager.hurt(true, Sunk);
                //noinspection UnnecessaryReturnStatement
                return;
            }
        }
    }

    Cell getCellByDirection(Cell curCell, boolean toRight) {
        assert curCell != null;
        int xy = curCell.xy;

        if (toRight) {
            if ((xy + 1) % WIDTH == 0) return null;
            return getCell(xy + 1);
        } else {
            if (xy % WIDTH == 0) return null;
            return getCell(xy - 1);
        }
    }

    private void createBeamChunks(Cell cell0, boolean toRight) {
        if (cell0 != null) {
            Cell cell1 = getCellByDirection(cell0, toRight);
            if (cell1 != null) {
                Cell cell2 = getCellByDirection(cell1, toRight);
                if (cell2 != null) {
                    Cell cell3 = getCellByDirection(cell2, toRight);
                    if (cell3 != null) {
                        Cell cell4 = getCellByDirection(cell3, toRight);
                        if (cell4 != null) {
                            Cells.CellObject edgeA = cell0.bottom;
                            Cells.CellObject edgeB = cell4.bottom;
                            if (edgeA != null && edgeB != null && edgeA.getId() == edgeB.getId()) {
                                Cells.CellObject chunk1 = Cell.newObject(0x0F, cell1, nextNumber);
                                Cells.CellObject chunk2 = Cell.newObject(0x0F, cell2, nextNumber);
                                Cells.CellObject chunk3 = Cell.newObject(0x0F, cell3, nextNumber);
                                cell1.objects.add(chunk1);
                                cell2.objects.add(chunk2);
                                cell3.objects.add(chunk3);
                                objAppended(chunk1);
                                objAppended(chunk2);
                                objAppended(chunk3);
                            }
                        }
                    }
                }
            }
        }
    }

    private void detectMines(Cell cell, boolean toRight, int n) {
        if (cell != null && n >= 0) {
            Cells.Mine mine = cell.getFirst(Cells.Mine.class);
            if (mine != null) {
                cell.objects.remove(mine);
                objChanged(mine, 0xFF, true);
            }
            Cell nextCell = getCellByDirection(cell, toRight);
            detectMines(nextCell, toRight, n - 1);
        }
    }

    void replaceFavouriteFood(ActorEx actor1, ActorEx actor2) {
        List<Cells.CellObjectFavouriteFood> foodActorLst = getFavouriteFoodList();
        for (int i = 0; i < foodActorLst.size(); i++) {
            Cells.CellObjectFavouriteFood favouriteFood = foodActorLst.get(i);

            // first determine who loves this "virtual" food
            ActorEx actor = favouriteFood instanceof Cells.FoodActor1 ? actor1 : actor2;
            assert actor != null;

            // now replace "virtual" food with the actor's favorite one
            Cell cell = favouriteFood.getCell();
            assert cell != null;
            Cells.CellObjectFood food = createFavouriteFood(actor, favouriteFood.getNumber(), cell);
            cell.objects.remove(favouriteFood);
            cell.objects.add(food);

            // also fix raw field data for sending to clients
            raw.set(cell.xy, (raw.get(cell.xy) & 0xC0) | food.getId());
        }
    }

    private Cells.CellObjectFood createFavouriteFood(ActorEx actor, int num, Cell cell) {
        assert cell != null;
        switch (actor.getCharacter()) {
            case Rabbit:
                return new Cells.Carrot(cell, num);
            case Hedgehog:
                return new Cells.Mushroom(cell, num);
            case Squirrel:
                return new Cells.Nut(cell, num);
            case Cat:
                return new Cells.Meat(cell, num);
            default:
                return new Cells.Apple(cell, num);
        }
    }

    private boolean isPoison(ActorEx actor, Cells.CellObjectFood food) {
        switch (actor.getCharacter()) {
            case Rabbit:
                return isPoisonForRabbit(food);
            case Hedgehog:
                return isPoisonForHedgehog(food);
            case Squirrel:
                return isPoisonForSquirrel(food);
            case Cat:
                return isPoisonForCat(food);
        }
        return true;
    }

    private boolean isPoisonForRabbit(Cells.CellObjectFood food) {
        // can eat apples, pears and CARROTS
        return food instanceof Cells.Mushroom || food instanceof Cells.Nut || food instanceof Cells.Meat;
    }

    private boolean isPoisonForHedgehog(Cells.CellObjectFood food) {
        // can eat apples, pears and MUSHROOMS
        return food instanceof Cells.Carrot || food instanceof Cells.Nut || food instanceof Cells.Meat;
    }

    private boolean isPoisonForSquirrel(Cells.CellObjectFood food) {
        // can eat apples, pears and NUTS
        return food instanceof Cells.Carrot || food instanceof Cells.Mushroom || food instanceof Cells.Meat;
    }

    private boolean isPoisonForCat(Cells.CellObjectFood food) {
        // can eat apples, pears and MEAT
        return food instanceof Cells.Carrot || food instanceof Cells.Mushroom || food instanceof Cells.Nut;
    }

    private int getMirrorXy(int xy) {
        int x0 = xy % WIDTH;
        int y0 = xy / WIDTH;
        int x = x0 + 2 * (WIDTH / 2 - x0);
        int y = y0 + 2 * (HEIGHT / 2 - y0);
        return y * WIDTH + x;
    }

    private Cells.CellObject emplace(Cells.CellObjectThing thing, int number, Cell cell) {
        if (thing instanceof Cells.UmbrellaThing) {
            //umbrella.setCell(cell);
            //umbrella.setNumber(number);
            return new Cells.Umbrella(cell, number);
        }
        if (thing instanceof Cells.MineThing) {
            //mine.setCell(cell);
            //mine.setNumber(number);
            return new Cells.Mine(cell, number);
        }
        if (thing instanceof Cells.BeamThing) {
            //beam.setCell(cell);
            //beam.setNumber(number);
            return new Cells.Beam(cell, number);
        }
        if (thing instanceof Cells.AntidoteThing) {
            //antidote.setCell(cell);
            //antidote.setNumber(number);
            return new Cells.Antidote(cell, number);
        }
        if (thing instanceof Cells.FlashbangThing) {
            //flashbang.setCell(cell);
            //flashbang.setNumber(number);
            return new Cells.Flashbang(cell, number);
        }
        if (thing instanceof Cells.TeleportThing) {
            //teleport.setCell(cell);
            //teleport.setNumber(number);
            return new Cells.Teleport(cell, number);
        }
        if (thing instanceof Cells.DetectorThing) {
            //detector.setCell(cell);
            //detector.setNumber(number);
            return new Cells.Detector(cell, number);
        }
        if (thing instanceof Cells.BoxThing) {
            //box.setCell(cell);
            //box.setNumber(number);
            return new Cells.Box(cell, number);
        }
        return null;
    }
}
