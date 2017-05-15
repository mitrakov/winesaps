package ru.mitrakov.self.rush.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Cell {
    // ....
    public CellObject bottom; // may be NULL
    public List<CellObject> objects = new CopyOnWriteArrayList<CellObject>(); // .... GC bla-bla-bla

    private Cell() {
    }

    static Cell newCell(int value, int xy, Field.NextNumber number) {
        Cell res = new Cell();
        switch (value >> 6) {
            case 1:
                res.bottom = new Block(xy);
                break;
            case 2:
                res.bottom = new Water(xy);
                break;
            case 3:
                res.bottom = new Dias(xy);
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
                return new Actor1(xy, number.next());
            case 0x02:
                return new Actor2(xy, number.next());
            case 0x03:
                return new Entry1(xy);
            case 0x04:
                return new Entry2(xy);
            case 0x05:
                return new Apple(xy, number.next());
            case 0x06:
                return new Pear(xy, number.next());
            case 0x07:
                return new Meat(xy, number.next());
            case 0x08:
                return new Carrot(xy, number.next());
            case 0x09:
                return new Mushroom(xy, number.next());
            case 0x0A:
                return new Nut(xy, number.next());
            case 0x0B:
                return new Block(xy);
            case 0x0C:
                return new LadderTop(xy);
            case 0x0D:
                return new LadderBottom(xy);
            case 0x0E:
                return new RopeBolt(xy);
            case 0x0F:
                return new RopeLine(xy);
            case 0x10:
                return new Water(xy);
            case 0x11:
                return new Wolf(xy, number.next());
            case 0x12:
                return new Stair(xy);
            case 0x13:
                return new Dias(xy);
            case 0x14:
                return new Mine(xy, number.next());
            case 0x15:
                return new BurriedMine(xy, number.next());
            case 0x16:
                return new Umbrella(xy, number.next());
            case 0x17:
                return new OpenedUmbrella(xy, number.next());
            case 0x18:
                return new Waterfall(xy);
            case 0x19:
                return new WaterfallSafe(xy);
            /*case 0x1A: only for server
              case 0x1B: only for server */
            case 0x1C:
                return new DecorationStatic(xy);
            case 0x1D:
                return new DecorationDynamic(xy);
            case 0x1E:
                return new DecorationWarning(xy);
            default:
                return null;
        }
    }
}
