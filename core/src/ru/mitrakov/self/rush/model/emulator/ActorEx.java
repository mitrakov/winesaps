package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.*;

/**
 * Created by mitrakov on 08.03.2018
 */
class ActorEx extends Cells.Actor1 {
    private Model.Character character;
    private boolean directionRight = true;

    ActorEx(Cell cell, int number) {
        super(cell, number);
    }

    public Model.Character getCharacter() {
        return character;
    }

    public void setCharacter(Model.Character character) {
        this.character = character;
    }

    boolean isDirectedToRight() {
        return directionRight;
    }

    void addStep() {
        // TODO
    }

    void setDirectionRight(boolean directedRight) {
        this.directionRight = directedRight;
    }

    boolean hasSwagga(Model.Ability ability) {
        return false; // TODO
    }

    void setEffect(Model.Effect effect, int steps, Runnable callback) {
        // TODO
    }
}
