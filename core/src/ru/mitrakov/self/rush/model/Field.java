package ru.mitrakov.self.rush.model;

import java.util.*;

import ru.mitrakov.self.rush.model.Cells.CellObject;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Field {
    public static final int WIDTH = 51;
    public static final int HEIGHT = 5;

    public interface NextNumber {
        int next();
    }

    final static NextNumber ZeroNumber = new NextNumber() {
        @Override
        public int next() {
            return 0;
        }
    };

    public static final int TRASH_XY = WIDTH * HEIGHT;
    public static final Cell TRASH_CELL = Cell.newCell(0, TRASH_XY, ZeroNumber);


    public final Cell cells[] = new Cell[WIDTH * HEIGHT + 1]; // .... public // + 1 fake cell

    private final Map<Integer, CellObject> objects = new HashMap<Integer, CellObject>(8);
    private int objectNumber = 0;

    protected final NextNumber nextNumber = new NextNumber() {
        @Override
        public int next() {
            return ++objectNumber;
        }
    };

    public Field(IIntArray fieldData) {
        assert fieldData != null;
        if (fieldData.length() != WIDTH * HEIGHT) throw new IllegalArgumentException("Incorrect field length");

        for (int i = 0; i < fieldData.length(); i++) {
            cells[i] = Cell.newCell(fieldData.get(i), i, nextNumber);
            assert cells[i] != null;
            for (CellObject object : cells[i].objects) {
                objects.put(object.getNumber(), object);
            }
        }
        // create fake cell for "removed" objects
        cells[TRASH_XY] = TRASH_CELL;
    }

    public void appendObject(final int number, int id, int xy) {
        if (xy < 0 || xy > TRASH_XY) // 0xFF is a LEGAL coordinate! it means a fake cell
            throw new IllegalArgumentException("Incorrect xy");

        CellObject object = Cell.newObject(id, cells[xy], new Field.NextNumber() {
            @Override
            public int next() {
                return number;
            }
        });
        if (object != null) {
            cells[xy].objects.add(object);
            objects.put(object.getNumber(), object);
        }
    }

    public void setXy(int number, int id, int newXy) {
        if (newXy < 0 || newXy > TRASH_XY) // 0xFF is a LEGAL coordinate! it means a fake cell
            throw new IllegalArgumentException("Incorrect xy");

        CellObject object = objects.get(number);
        if (object != null) {
            if (object.getId() == id) { // Server API recommends to check it to avoid out-of-sync occasions
                object.setCell(cells[newXy]);
            } else throw new IllegalStateException(String.format(Locale.getDefault(),
                    "SetXY (%d) error! Object num %d has different id (%d <> %d)", newXy, number, object.getId(), id));
        }
    }

    public void setEffect(int number, Model.Effect effect) {
        CellObject object = objects.get(number);
        if (object != null)
            object.setEffect(effect);
    }

    public CellObject getObjectById(int id) {
        // in Java 8 may be replaced with lambda
        for (Cell cell : cells) {
            for (CellObject object : cell.objects) {
                if (object.getId() == id)
                    return object;
            }
        }
        return null;
    }

    public CellObject getObjectByNumber(int number) {
        return objects.get(number);
    }
}
