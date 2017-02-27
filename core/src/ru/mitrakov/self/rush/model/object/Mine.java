package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Mine extends CellObject {
    public Mine(int xy, int number) {
        super(0x14, xy);
        this.number = number;
    }
}
