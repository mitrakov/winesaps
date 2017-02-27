package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Actor1 extends CellObject {
    public Actor1(int xy, int number) {
        super(0x01, xy);
        this.number = number;
    }
}
