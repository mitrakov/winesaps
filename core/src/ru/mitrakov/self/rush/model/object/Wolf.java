package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Wolf extends CellObject {
    public Wolf(int xy, int number) {
        super(0x11, xy);
        this.number = number;
    }
}
