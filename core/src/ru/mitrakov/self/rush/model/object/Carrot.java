package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Carrot extends CellObjectFood {
    public Carrot(int xy, int number) {
        super(0x08, xy);
        this.number = number;
    }
}
