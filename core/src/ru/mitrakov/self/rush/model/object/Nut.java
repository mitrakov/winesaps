package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Nut extends CellObjectFood {
    public Nut(int xy, int number) {
        super(0x0A, xy);
        this.number = number;
    }
}
