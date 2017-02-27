package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Umbrella extends CellObject {
    public Umbrella(int xy, int number) {
        super(0x16, xy);
        this.number = number;
    }
}
