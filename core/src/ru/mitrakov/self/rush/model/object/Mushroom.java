package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Mushroom extends CellObject {
    public Mushroom(int xy, int number) {
        super(0x09, xy);
        this.number = number;
    }
}
