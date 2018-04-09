package ru.mitrakov.self.rush.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.Cells.*;

/**
 * Cell is a single element of a battle field. An actor can move only from one cell to another
 * @author mitrakov
 */
public class Cell {
    // @mitrakov: fields are public, because they are frequently accessed in render() method

    /** Coordinate (0-255) */
    public final int xy;
    /** Bottom object (block, water, etc., may be NULL) */
    public CellObject bottom;
    /** List of objects on the cell. Note that CopyOnWriteList has been chosen DELIBERATELY to decrease GC pressure */
    public List<CellObject> objects = new CopyOnWriteArrayList<CellObject>();

    /**
     * Private constructor (use <b>newCell()</b> factory method)
     * @param xy index in a battle field
     * @see {@link ru.mitrakov.self.rush.model.Cell#newCell(int, int, Field.NextNumber, int) newCell}
     */
    private Cell(int xy) {
        this.xy = xy;
    }

    /**
     * Creates a new instance of Cell
     * @param value binary value (2 bits are bottom, 6 bits - object, like ladders, rope lines, etc.)
     * @param xy index in a battle field
     * @param numberFunc function, that returns current order number on a battle field (starting with 1), may be NULL
     * @param number if `numberFunc` is NULL, then this value will be used (otherwise ignored)
     * @return new Cell
     */
    static Cell newCell(int value, int xy, Field.NextNumber numberFunc, int number) {
        Cell res = new Cell(xy);
        switch (value >> 6) {
            case 1:
                res.bottom = new Block(res);
                break;
            case 2:
                res.bottom = new Dais(res);
                break;
            case 3:
                res.bottom = new Water(res);
                break;
            default:
        }
        CellObject object = newObject(value & 0x3F, res, numberFunc, number);
        if (object != null)
            res.objects.add(object);
        return res;
    }

    /**
     * Creates a new instance of CellObject
     * @param value binary 6-bits value
     * @param cell cell
     * @param number function, that returns current order number on a battle field (starting with 1)
     * @return new CellObject
     */
    public static CellObject newObject(int value, Cell cell, Field.NextNumber numberFunc, int number) {
        switch (value) {
            case 0x01:
                return new Block(cell);
            case 0x02:
                return new Dais(cell);
            case 0x03:
                return new Water(cell);
            case 0x04:
                return new Actor1(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x05:
                return new Actor2(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x06:
                return new Wolf(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x07:
                return new Entry1(cell);
            case 0x08:
                return new Entry2(cell);
            case 0x09:
                return new LadderTop(cell);
            case 0x0A:
                return new LadderBottom(cell);
            case 0x0B:
                return new Stair(cell);
            case 0x0C:
                return new RopeLine(cell);
            case 0x0D:
                return new Waterfall(cell);
            case 0x0E:
                return new WaterfallSafe(cell);
            case 0x0F:
                return new BeamChunk(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x10:
                return new Apple(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x11:
                return new Pear(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x12:
                return new Meat(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x13:
                return new Carrot(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x14:
                return new Mushroom(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x15:
                return new Nut(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x16:
                return new FoodActor1(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x17:
                return new FoodActor2(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x20:
                return new UmbrellaThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x21:
                return new MineThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x22:
                return new BeamThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x23:
                return new AntidoteThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x24:
                return new FlashbangThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x25:
                return new TeleportThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x26:
                return new DetectorThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x27:
                return new BoxThing(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x28:
                return new Umbrella(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x29:
                return new Mine(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x2A:
                return new Beam(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x2B:
                return new Antidote(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x2C:
                return new Flashbang(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x2D:
                return new Teleport(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x2E:
                return new Detector(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x2F:
                return new Box(cell, numberFunc != null ? numberFunc.next() : number);
            case 0x30:
                return new DecorationStatic(cell);
            case 0x31:
                return new DecorationDynamic(cell);
            case 0x32:
                return new DecorationWarning(cell);
            case 0x33:
                return new DecorationDanger(cell);
            default:
                return null;
        }
    }

    /**
     * @param objClass java class of CellObject to search
     * @param <T> CellObject type
     * @return first object in the cell that corresponds to a given java class
     */
    @SuppressWarnings("unchecked")
    public <T extends CellObject> T getFirst(Class<T> objClass) {
        for (int i = 0; i < objects.size(); i++) {  // .... GC!
            CellObject obj = getObject(i);
            if (objClass.isInstance(obj))
                return (T) obj;
        }
        return null;
    }

    /**
     * @param objClass java class of CellObject to check
     * @return true, if a CellObject of a given java class exists in the cell
     */
    public boolean objectExists(Class<? extends CellObject> objClass) {
        return getFirst(objClass) != null;
    }

    /**
     * @return count of CellObjects in the cell
     */
    public int getObjectsCount() {
        return objects.size();
    }

    /**
     * Gets CellObject by its index
     * This method is asymptotically O(1), safe and highly recommended to use inside for(...) loop to reduce GC,
     * produced by for-each constructions
     * @param idx index
     * @return CellObject by its index
     */
    public CellObject getObject(int idx) {
        try {
            return objects.get(idx);
        } catch (ArrayIndexOutOfBoundsException ignored) { // it may happen HARDLY EVER during concurrent
            return null;                                   // index_based_traversal/removing
        }
    }
}
