package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Pear extends CellObjectFood {
    public Pear(int xy, int number) {
        super(0x06, xy);
        this.number = number;
    }
}
