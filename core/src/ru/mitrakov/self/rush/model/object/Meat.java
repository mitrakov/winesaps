package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Meat extends CellObjectFood {
    public Meat(int xy, int number) {
        super(0x07, xy);
        this.number = number;
    }
}
