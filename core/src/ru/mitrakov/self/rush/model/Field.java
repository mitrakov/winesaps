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
    static final int TRASH_CELL = WIDTH * HEIGHT;

    interface NextNumber {
        int next();
    }

    final static NextNumber ZeroNumber = new NextNumber() {
        @Override
        public int next() {
            return 0;
        }
    };

    public final Cell cells[] = new Cell[WIDTH * HEIGHT + 1]; // .... public // + 1 fake cell

    private final Map<Integer, CellObject> objects = new HashMap<Integer, CellObject>(8);
    private int objectNumber = 0;

    Field(IIntArray fieldData) {
        assert fieldData != null;
        if (fieldData.length() != WIDTH * HEIGHT) throw new IllegalArgumentException("Incorrect field length");

        NextNumber nextNumber = new NextNumber() {
            @Override
            public int next() {
                return ++objectNumber;
            }
        };
        for (int i = 0; i < fieldData.length(); i++) {
            cells[i] = Cell.newCell(fieldData.get(i), i, nextNumber);
            assert cells[i] != null;
            for (CellObject object : cells[i].objects) {
                objects.put(object.getNumber(), object);
            }
        }
        // create fake cell for "removed" objects
        cells[TRASH_CELL] = Cell.newCell(0, TRASH_CELL, nextNumber);
    }

    void appendObject(final int number, int id, int xy) {
        if (xy < 0 || xy > TRASH_CELL) // 0xFF is a LEGAL coordinate! it means a fake cell
            throw new IllegalArgumentException("Incorrect xy");

        CellObject object = Cell.newObject(id, xy, new Field.NextNumber() {
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

    void setXy(int number, int newXy) {
        if (newXy < 0 || newXy > TRASH_CELL) // 0xFF is a LEGAL coordinate! it means a fake cell
            throw new IllegalArgumentException("Incorrect xy");

        CellObject object = objects.get(number);
        if (object != null) {
            int oldXy = object.getXy();
            object.setXy(newXy);
            cells[oldXy].objects.remove(object);
            cells[newXy].objects.add(object);
        }
    }
    void setEffect(int number, Model.Effect effect) {
        CellObject object = objects.get(number);
        if (object != null)
            object.setEffect(effect);
    }

    CellObject getObjectById(int id) {
        // in Java 8 may be replaced with lambda
        for (Cell cell : cells) {
            for (CellObject object : cell.objects) {
                if (object.getId() == id)
                    return object;
            }
        }
        return null;
    }

    CellObject getObjectByNumber(int number) {
        return objects.get(number);
    }
}
