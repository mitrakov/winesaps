package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public abstract class CellObject {
    protected int number = 0;
    protected int xy;
    protected int id;

    public CellObject(int id, int xy) {
        this.id = id;
        this.xy = xy;
    }

    public int getNumber() {
        return number;
    }

    public int getId() {
        return id;
    }

    public int getXy() {
        return xy;
    }

    public void setXy(int xy) {
        this.xy = xy;
    }
}
