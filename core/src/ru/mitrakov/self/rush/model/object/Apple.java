package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Apple extends CellObjectFood {
    public Apple(int xy, int number) {
        super(0x05, xy);
        this.number = number;
    }
}
