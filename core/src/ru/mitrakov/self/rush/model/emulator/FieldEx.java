package ru.mitrakov.self.rush.model.emulator;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Model.Effect.*;
import static ru.mitrakov.self.rush.model.Model.Ability.*;
import static ru.mitrakov.self.rush.model.Model.Character.*;
import static ru.mitrakov.self.rush.model.Model.HurtCause.*;

/**
 * Analog of Server Field class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
class FieldEx extends Field {
    /** Duration of Antidote effect (in steps) */
    private static final int ANTIDOTE_EFFECT = 10;
    /** Mine detector effective distance (in cells) */
    private static final int DETECTION_LENGTH = 8;
    /** Default round time */
    private static final int ROUND_TIME = 90;

    /** Reference to the Battle manager */
    private final BattleManager battleManager;
    /** Lock (analog of Mutex/RWMutex in Go) */
    private final ReentrantLock lock = new ReentrantLock();
    /** Lock (analog of Mutex/RWMutex in Go) */
    private final ReentrantLock cellLock = new ReentrantLock();
    /** List of wolves on the field (needed to avoid using "new" operations to decrease Garbage Collector pressure) */
    private final List<WolfEx> wolfList = new CopyOnWriteArrayList<WolfEx>();
    /** List of all favourite fruit (needed to avoid using "new" operations to decrease Garbage Collector pressure) */
    private final List<Cells.CellObjectFavouriteFood> favouriteFoodList =
            new CopyOnWriteArrayList<Cells.CellObjectFavouriteFood>();

    /** Field Actor */
    /*final*/ ActorEx actor1, actor2;
    /** Raw binary field data */
    final IIntArray raw;
    /** Round time, in seconds */
    int timeSec = ROUND_TIME;

    /**
     * Creates new battlefield
     * @param fieldData binary field data (just 255 bytes)
     * @param raw raw binary field data, may be more than 255 bytes
     * @param battleManager {@link BattleManager}
     */
    FieldEx(IIntArray fieldData, IIntArray raw, BattleManager battleManager) {
        super(fieldData);
        assert battleManager != null;
        this.raw = raw;
        this.battleManager = battleManager;

        // parse additional sections
        for (int j = fieldData.length(); j + 1 < raw.length(); j += 2) {
            int sectionCode = raw.get(j);
            int sectionLen = raw.get(j + 1);
            switch (sectionCode) {
                case 1: // parse additional level objects
                    int startK = j + 2;
                    for (int k = startK; k + 2 < startK + sectionLen && k + 2 < raw.length(); k += 3) {
                        int number = raw.get(k);
                        int id = raw.get(k + 1);
                        int xy = raw.get(k + 2);
                        appendObject(number, id, xy);
                    }
                    break;
                case 2: // no need in style pack in Server Emulator
                    break;
                case 3: // parse round time
                    if (j + 2 < raw.length())
                        timeSec = raw.get(j + 2);
                    break;
                default: // don't throw exceptions, just skip
            }
            j += sectionLen;
        }

        createSubTypesInternal();
    }

    /**
     * Internal method to properly initialize some objects on the battlefield (the problem is that the most of objects
     * are "passive" and may be directly taken from existing data types of {@link Cells} class; but some objects are
     * "smart", e.g. actors and wolves, hence they stored additional internal data; in order not to touch {@link Cells}
     * class we decided to create the subclasses and set them up accordingly before the battle start; so this method
     * is for those purposes)
     */
    private synchronized void createSubTypesInternal() {
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
                wolf.getCell().objects.remove(wolf);     // don't forget to remove original Wolf to avoid bugs
            }
            cellLock.unlock();
        }
    }

    /**
     * @return next generated number for new objects on the battlefield
     */
    int getNextNum() {
        return nextNumber.next();
    }

    /**
     * Invoked when an object coordinates on the battlefield are changed
     * @param obj object (NON-NULL)
     * @param xy new position
     * @param reset TRUE if an object resets its position (e.g. teleportation), and FALSE - for smooth moving
     */
    private void objChanged(Cells.CellObject obj, int xy, boolean reset) {
        battleManager.objChanged(obj, xy, reset);
    }

    /**
     * Invoked when a new object have been added to the battlefield (e.g. an actor established an umbrella or a beam)
     * @param obj object (NON-NULL)
     */
    private void objAppended(Cells.CellObject obj) {
        battleManager.objAppended(obj);
    }

    /**
     * Checks whether moving up is possible (e.g. there is a ladder or a rope)
     * @see #isMoveDownPossible(Cell)
     * @param cell cell
     * @return TRUE, if moving up is possible, and FALSE otherwise
     */
    boolean isMoveUpPossible(Cell cell) {
        assert cell != null;
        return cell.objectExists(Cells.LadderBottom.class) || cell.objectExists(Cells.RopeLine.class);
    }

    /**
     * Checks whether moving down is possible (e.g. there is a ladder)
     * @see #isMoveUpPossible(Cell)
     * @param cell cell
     * @return TRUE, if moving down is possible, and FALSE otherwise
     */
    boolean isMoveDownPossible(Cell cell) {
        assert cell != null;
        return cell.objectExists(Cells.LadderTop.class);
    }

    /**
     * Performs single move to the given location
     * @param obj object to move
     * @param idxTo destination coordinates
     * @return TRUE, if moving is success, and FALSE - otherwise (e.g. there is a wall or the edge of the battlefield)
     */
    public boolean move(Cells.CellObject obj, int idxTo) {
        // the implementation is given "as is" because in Go there are no 'synchronized' blocks, nor reentrant mutexes
        lock.lock();
        boolean result = moveSync(obj, idxTo);
        lock.unlock();
        return result;
    }

    /**
     * Performs single move to the given location
     * <br><b>Note:</b> Internal only! External code must use {@link #move(Cells.CellObject, int)} method for movements
     * @param obj object to move
     * @param idxTo destination coordinates
     * @return TRUE, if moving is success, and FALSE - otherwise (e.g. there is a wall or the edge of the battlefield)
     */
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

    /**
     * Returns cell by its coordinates
     * @param xy position
     * @return cell by XY coordinates
     */
    private Cell getCell(int xy) {
        assert 0 <= xy && xy < WIDTH * HEIGHT;
        return cells[xy];
    }

    /**
     * Returns a list of wolves (note that the same list is returned to avoid "new" operations and decrease GC pressure)
     * @return list of wolves on the battlefield
     */
    List<WolfEx> getWolves() {
        return wolfList;
    }

    /**
     * Retrieves a list of all favourite food items on the battlefield (please note that method is NOT thread-safe)
     * @return a list of all favourite food items
     */
    private List<Cells.CellObjectFavouriteFood> getFavouriteFoodList() {
        favouriteFoodList.clear();
        for (int i = 0; i < cells.length; i++) {
            Cell cell = cells[i];
            cellLock.lock();
            Cells.CellObjectFavouriteFood food = cell.getFirst(Cells.CellObjectFavouriteFood.class);
            if (food != null)
                favouriteFoodList.add(food);
            cellLock.unlock();
        }
        return favouriteFoodList;
    }

    /**
     * Looks up entry object by the given actor
     * <br>Please note that this method only looks up Entry1, just as on the Server both actors are considered
     * @return entry object, or NULL if entry is not found
     */
    Cells.Entry1 getEntryByActor() {
        for (int i = 0; i < cells.length; i++) { // GC!
            Cell cell = cells[i];
            Cells.Entry1 entry = cell.getFirst(Cells.Entry1.class);
            if (entry != null) return entry;
        }
        return null;
    }

    /**
     * @return remaining food count on the battlefield
     * @see #getFoodCountForActor(ActorEx)
     */
    @SuppressWarnings("unused")
    private int getFoodCount() {
        int result = 0;
        for (int i = 0; i < WIDTH*HEIGHT; i++) {
            cellLock.lock();
            Cell cell = cells[i];
            if (cell.objectExists(Cells.CellObjectFood.class))
                result++;
            cellLock.unlock();
        }
        return result;
    }

    /**
     * Returns remaining food count for the given actor
     * <br> Please note that there is no such a method on the Server ({@link #getFoodCount()} is used instead)
     * @param actor actor
     * @return food count for the given actor
     */
    int getFoodCountForActor(ActorEx actor) {
        int result = 0;
        for (int i = 0; i < WIDTH*HEIGHT; i++) {
            cellLock.lock();
            Cell cell = cells[i];
            Cells.CellObjectFood food = cell.getFirst(Cells.CellObjectFood.class);
            if (food != null && !isPoison(actor, food))
                result++;
            cellLock.unlock();
        }
        return result;
    }

    /**
     * Drops a thing (it may happen because an actor has only 1 slot, so if he takes a thing - the old thing is dropped)
     * @param actor actor
     * @param thing thing to be thrown off from the actor
     */
    void dropThing(ActorEx actor, Cells.CellObjectThing thing) {
        assert actor != null && actor.getCell() != null && thing != null;

        Cell cell = actor.getCell();
        cell.objects.add(thing);
        objChanged(thing, cell.xy, false);
    }

    /**
     * Method to use a thing
     * @param actor actor
     * @param thing thing to be used
     */
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

        // ================
        if (thing instanceof Cells.UmbrellaThing) {
            Cell next = getCellByDirection(cell, actor.isDirectedToRight());
            if (next != null && next.objectExists(Cells.Waterfall.class)) {
                cell = next;
            }
        }
        // ================

        // emplace object
        int number = getNextNum();
        Cells.CellObject emplaced = emplace(thing, number, myCell);
        objAppended(emplaced);
        move(emplaced, cell.xy);

        // for mines we must provide a safe single step over the buried mine
        if (emplaced instanceof Cells.Mine)
            actor.setEffect(Attention, 2, null);
    }

    /**
     * Relocates the object from one point to another. This is internal method, please use
     * {@link #move(Cells.CellObject, int)} from external code
     * @param oldCell old cell
     * @param newCell new cell
     * @param obj object to be relocated
     * @param reset TRUE if an object resets its position (e.g. teleportation), and FALSE - for smooth moving
     */
    void relocate(Cell oldCell, Cell newCell, Cells.CellObject obj, boolean reset) {
        assert oldCell != null && newCell != null;

        cellLock.lock(); // this lock is needed, because relocate() may be called outside move() context
        obj.setCell(newCell);
        cellLock.unlock();

        objChanged(obj, newCell.xy, reset);
        checkCell(newCell);
    }

    /**
     * Checks cell for any game actions. All the game logic is concentrated here.
     * Should be called each time an object takes the cell
     * @param cell cell to check
     */
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
                battleManager.setEffectOnEnemy(actor == actor1, Dazzle);
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

    /**
     * Retrieves neighbour cell to <b>curCell</b> according to given direction
     * @param curCell current cell
     * @param toRight TRUE to get the cell on the right, and FALSE - on the left
     * @return neighbour cell, or NULL, if the current cell is located on the edge
     */
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

    /**
     * Creates a bridge (by establishing 3 beam chunks)
     * @param cell0 start cell
     * @param toRight TRUE to build a bridge to the right, and FALSE - to the left
     */
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
                                Cells.CellObject chunk1 = Cell.newObject(0x0F, cell1, nextNumber, 0);
                                Cells.CellObject chunk2 = Cell.newObject(0x0F, cell2, nextNumber, 0);
                                Cells.CellObject chunk3 = Cell.newObject(0x0F, cell3, nextNumber, 0);
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

    /**
     * [Recursively] looks up the [buried] mines to the given direction, and removes them, if any
     * @param cell start point
     * @param toRight TRUE to look up to the right, and FALSE - to the left
     * @param n recursive parameter
     */
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

    /**
     * Replaces all the virtual "Favourite Food" items with correponding analogs (carrot, nut, meat or mushroom)
     * @param actor1 actor1
     * @param actor2 actor2
     */
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

    /**
     * Creates real favourite food (carrot, nut, meat or mushroom) regarding to the actor's character
     * @param actor actor
     * @param num object number on the battlefield
     * @param cell cell to store the new food item
     * @return new food item created
     * @see #replaceFavouriteFood(ActorEx, ActorEx)
     */
    private Cells.CellObjectFood createFavouriteFood(ActorEx actor, int num, Cell cell) {
        assert cell != null;

        // DO NOT use switch(direction)!!! It causes call Character.values() that produces work for GC!
        if (actor.getCharacter() == Rabbit)
            return new Cells.Carrot(cell, num);
        if (actor.getCharacter() == Hedgehog)
            return new Cells.Mushroom(cell, num);
        if (actor.getCharacter() == Squirrel)
            return new Cells.Nut(cell, num);
        if (actor.getCharacter() == Cat)
            return new Cells.Meat(cell, num);
        return new Cells.Apple(cell, num);
    }

    /**
     * Checks whether the given food is poison for the given actor
     * <br><b>Note:</b> it's a "pure" method, i.e. it DOES NOT take to account antidotes and so on
     * @param actor actor
     * @param food food
     * @return TRUE, if the given food is poison for the given actor
     */
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

    /**
     * @param food food item
     * @return TRUE, if the given food is poison for rabbits
     * @see #isPoison(ActorEx, Cells.CellObjectFood)
     */
    private boolean isPoisonForRabbit(Cells.CellObjectFood food) {
        // can eat apples, pears and CARROTS
        return food instanceof Cells.Mushroom || food instanceof Cells.Nut || food instanceof Cells.Meat;
    }

    /**
     * @param food food item
     * @return TRUE, if the given food is poison for hedgehogs
     * @see #isPoison(ActorEx, Cells.CellObjectFood)
     */
    private boolean isPoisonForHedgehog(Cells.CellObjectFood food) {
        // can eat apples, pears and MUSHROOMS
        return food instanceof Cells.Carrot || food instanceof Cells.Nut || food instanceof Cells.Meat;
    }

    /**
     * @param food food item
     * @return TRUE, if the given food is poison for squirrels
     * @see #isPoison(ActorEx, Cells.CellObjectFood)
     */
    private boolean isPoisonForSquirrel(Cells.CellObjectFood food) {
        // can eat apples, pears and NUTS
        return food instanceof Cells.Carrot || food instanceof Cells.Mushroom || food instanceof Cells.Meat;
    }

    /**
     * @param food food item
     * @return TRUE, if the given food is poison for cats
     * @see #isPoison(ActorEx, Cells.CellObjectFood)
     */
    private boolean isPoisonForCat(Cells.CellObjectFood food) {
        // can eat apples, pears and MEAT
        return food instanceof Cells.Carrot || food instanceof Cells.Mushroom || food instanceof Cells.Nut;
    }

    /**
     * @param xy base XY
     * @return the "reflected" XY coordinate regarding to the given base <b>xy</b> for this battlefield
     */
    private int getMirrorXy(int xy) {
        int x0 = xy % WIDTH;
        int y0 = xy / WIDTH;
        int x = x0 + 2 * (WIDTH / 2 - x0);
        int y = y0 + 2 * (HEIGHT / 2 - y0);
        return y * WIDTH + x;
    }

    /**
     * Factory method to create new objects based on their things (i.e.
     * {@link ru.mitrakov.self.rush.model.Cells.UmbrellaThing UmbrellaThing} will produce
     * {@link ru.mitrakov.self.rush.model.Cells.Umbrella Umbrella} object)
     * <br><b>Note:</b> this method is helper, and it doesn't exist on the Server (on Server logic of producing objects
     * is encapsulated inside the objects themselves; here we just reuse {@link Cells} class, and in order not to
     * rewrite that code we have to have such factory methods)
     * @param thing thing to be emplaced with a corresponding object
     * @param number sequential number for a new object on the battlefield
     * @param cell cell to store new object
     * @return new object emplaced
     */
    private Cells.CellObject emplace(Cells.CellObjectThing thing, int number, Cell cell) {
        // DANGER CODE: "new" may cause troubles with Garbage Collector; we should investigate its impact
        if (thing instanceof Cells.UmbrellaThing)
            return new Cells.Umbrella(cell, number);
        if (thing instanceof Cells.MineThing)
            return new Cells.Mine(cell, number);
        if (thing instanceof Cells.BeamThing)
            return new Cells.Beam(cell, number);
        if (thing instanceof Cells.AntidoteThing)
            return new Cells.Antidote(cell, number);
        if (thing instanceof Cells.FlashbangThing)
            return new Cells.Flashbang(cell, number);
        if (thing instanceof Cells.TeleportThing)
            return new Cells.Teleport(cell, number);
        if (thing instanceof Cells.DetectorThing)
            return new Cells.Detector(cell, number);
        if (thing instanceof Cells.BoxThing)
            return new Cells.Box(cell, number);
        return null;
    }
}
