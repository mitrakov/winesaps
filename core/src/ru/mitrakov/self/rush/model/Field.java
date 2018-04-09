package ru.mitrakov.self.rush.model;

import java.util.*;

import ru.mitrakov.self.rush.model.Cells.CellObject;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Class that represents a battle field (array of 255 cells)
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public class Field {
    /** Default field width */
    public static final int WIDTH = 51;
    /** Default field height */
    public static final int HEIGHT = 5;
    /** Trash cell coordinate */
    public static final int TRASH_XY = WIDTH * HEIGHT;
    /** Trash cell (all the removed objects go there) */
    public static final Cell TRASH_CELL = Cell.newCell(0, TRASH_XY, null, 0);

    /** Interface that is able to generate a next sequence number */
    public interface NextNumber {
        int next();
    }

    /** Array of cells. We make it 'public' to smooth little overhead of having getters. +1 is used for the TrashCell */
    public final Cell cells[] = new Cell[WIDTH * HEIGHT + 1];
    /** Map: ObjectNumber -> CellObject */
    protected final Map<Integer, CellObject> objects = new HashMap<Integer, CellObject>(8);
    /** Current object number, for internal use only */
    protected int objectNumber = 0;
    /** Function that generates a next sequence number. In Java 8 may be replaced with lambda expression */
    protected final NextNumber nextNumber = new NextNumber() {
        @Override
        public int next() {
            return ++objectNumber;
        }
    };

    /**
     * Created a new Field based on the given binary data
     * @param fieldData binary data (see Server Documentation for more details)
     */
    public Field(IIntArray fieldData) {
        assert fieldData != null;
        if (fieldData.length() != WIDTH * HEIGHT) throw new IllegalArgumentException("Incorrect field length");

        for (int i = 0; i < fieldData.length(); i++) {
            cells[i] = Cell.newCell(fieldData.get(i), i, nextNumber, 0);
            assert cells[i] != null;
            for (CellObject object : cells[i].objects) {
                objects.put(object.getNumber(), object);
            }
        }
        // create fake cell for "removed" objects
        TRASH_CELL.bottom = null;
        TRASH_CELL.objects.clear();
        cells[TRASH_XY] = TRASH_CELL;
    }

    /**
     * Adds a new object to the battle field
     * @param number object number
     * @param id object ID
     * @param xy coordinate (may be 0xFF that means that an object is to be removed to a trash cell)
     */
    void appendObject(final int number, int id, int xy) {
        if (xy < 0 || xy > TRASH_XY)
            throw new IllegalArgumentException("Incorrect xy");

        CellObject object = Cell.newObject(id, cells[xy], null, number);
        if (object != null) {
            cells[xy].objects.add(object);
            objects.put(object.getNumber(), object);
        }
    }

    /**
     * Relocates an object with a given number to new position
     * @param number number of an object to relocate
     * @param id object ID (excessive parameter, Server API recommends to check it to avoid out-of-sync occasions)
     * @param newXy new coordinate (may be 0xFF that means that an object is to be removed to a trash cell)
     */
    void setXy(int number, int id, int newXy) {
        if (newXy < 0 || newXy > TRASH_XY)
            throw new IllegalArgumentException("Incorrect xy");

        CellObject object = objects.get(number);
        if (object != null) {
            if (object.getId() == id) {
                object.setCell(cells[newXy]);
            } else throw new IllegalStateException(String.format(Locale.getDefault(),
                    "SetXY (%d) error! Object num %d has different id (%d <> %d)", newXy, number, object.getId(), id));
        }
    }

    /**
     * Sets an effect to the object with a given number
     * @param number number of an object to set the effect on
     * @param effect effect
     */
    void setEffect(int number, Model.Effect effect) {
        CellObject object = objects.get(number);
        if (object != null)
            object.setEffect(effect);
    }

    /**
     * See also: {@link #getObjectByNumber(int) getObjectByNumber}
     * @param id object ID (please don't mix up with an object number).
     * @return the first found object with a given <b>id</b>
     */
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

    /**
     * See also {@link #getObjectById(int) getObjectById}
     * @param number object number.
     * @return object with a given <b>number</b>
     */
    CellObject getObjectByNumber(int number) {
        return objects.get(number);
    }
}
