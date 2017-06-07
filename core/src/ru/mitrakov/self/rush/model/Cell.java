package ru.mitrakov.self.rush.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.Cells.*;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Cell {
    // ....
    public CellObject bottom; // may be NULL
    List<CellObject> objects = new CopyOnWriteArrayList<CellObject>(); // .... GC bla-bla-bla

    private Cell() {
    }

    static Cell newCell(int value, int xy, Field.NextNumber number) {
        Cell res = new Cell();
        switch (value >> 6) {
            case 1:
                res.bottom = new Block(xy);
                break;
            case 2:
                res.bottom = new Dais(xy);
                break;
            case 3:
                res.bottom = new Water(xy);
                break;
            default:
        }
        CellObject object = newObject(value & 0x3F, xy, number);
        if (object != null)
            res.objects.add(object);
        return res;
    }

    static CellObject newObject(int value, int xy, Field.NextNumber number) {
        switch (value) {
            case 0x01:
                return new Block(xy);
            case 0x02:
                return new Dais(xy);
            case 0x03:
                return new Water(xy);
            case 0x04:
                return new Actor1(xy, number.next());
            case 0x05:
                return new Actor2(xy, number.next());
            case 0x06:
                return new Wolf(xy, number.next());
            case 0x07:
                return new Entry1(xy);
            case 0x08:
                return new Entry2(xy);
            case 0x09:
                return new LadderTop(xy);
            case 0x0A:
                return new LadderBottom(xy);
            case 0x0B:
                return new Stair(xy);
            case 0x0C:
                return new RopeLine(xy);
            case 0x0D:
                return new Waterfall(xy);
            case 0x0E:
                return new WaterfallSafe(xy);
            case 0x0F:
                return new BeamChunk(xy, number.next());
            case 0x10:
                return new Apple(xy, number.next());
            case 0x11:
                return new Pear(xy, number.next());
            case 0x12:
                return new Meat(xy, number.next());
            case 0x13:
                return new Carrot(xy, number.next());
            case 0x14:
                return new Mushroom(xy, number.next());
            case 0x15:
                return new Nut(xy, number.next());
          /*case 0x16:
                return new FoodActor1(xy, number.next()); only for server
            case 0x17:
                return new FoodActor2(xy, number.next()); only for server */
            case 0x20:
                return new UmbrellaThing(xy, number.next());
            case 0x21:
                return new MineThing(xy, number.next());
            case 0x22:
                return new BeamThing(xy, number.next());
            case 0x23:
                return new AntidoteThing(xy, number.next());
            case 0x24:
                return new DazzleGrenadeThing(xy, number.next());
            case 0x25:
                return new TeleportThing(xy, number.next());
            case 0x26:
                return new DetectorThing(xy, number.next());
            case 0x27:
                return new BoxThing(xy, number.next());
            case 0x28:
                return new Umbrella(xy, number.next());
            case 0x29:
                return new Mine(xy, number.next());
            case 0x2A:
                return new Beam(xy, number.next());
            case 0x2B:
                return new Antidote(xy, number.next());
            case 0x2C:
                return new DazzleGrenade(xy, number.next());
            case 0x2D:
                return new Teleport(xy, number.next());
            case 0x2E:
                return new Detector(xy, number.next());
            case 0x2F:
                return new Box(xy, number.next());
            case 0x30:
                return new DecorationStatic(xy);
            case 0x31:
                return new DecorationDynamic(xy);
            case 0x32:
                return new DecorationWarning(xy);
            default:
                return null;
        }
    }

    public CellObject getFirst(Class<? extends CellObject> objClass) {
        for (int i = 0; i < objects.size(); i++) {  // .... GC!
            CellObject obj = getObject(i);
            if (objClass.isInstance(obj))
                return obj;
        }
        return null;
    }

    public boolean objectExists(Class<? extends CellObject> objClass) {
        return getFirst(objClass) != null;
    }

    public int getObjectsCount() {
        return objects.size();
    }

    public CellObject getObject(int idx) {
        try {
            return objects.get(idx);
        } catch (ArrayIndexOutOfBoundsException ignored) { // it may happen HARDLY EVER during concurrent
            return null;                                   // index_based_traversal/removing
        }
    }
}
