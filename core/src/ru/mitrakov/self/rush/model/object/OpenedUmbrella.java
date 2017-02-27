package ru.mitrakov.self.rush.model.object;

/**
 * Created by mitrakov on 23.02.2017
 */

public class OpenedUmbrella extends CellObject {
    public OpenedUmbrella(int xy, int number) {
        super(0x17, xy);
        this.number = number;
    }
}
