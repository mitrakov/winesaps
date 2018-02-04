package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Cell;
import ru.mitrakov.self.rush.model.Cells;
import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 08.03.2018
 */
public class ActorEx extends Cells.Actor1 {
    private Model.Character character;
    private boolean directionRight = true;


    private Cells.CellObjectThing thing;

    public ActorEx(Cell cell, int number) {
        super(cell, number);
    }

    public Model.Character getCharacter() {
        return character;
    }

    public void setCharacter(Model.Character character) {
        this.character = character;
    }

    public boolean isDirectedToRight() {
        return directionRight;
    }

    public Cells.CellObjectThing getThing() {
        return thing;
    }

    public void setThing(Cells.CellObjectThing thing) {
        this.thing = thing;
    }

    public void addStep() {

    }

    public void setDirectionRight(boolean directedRight) {
        this.directionRight = directedRight;
    }

    public boolean hasSwagga(Model.Ability ability) {
        return true;
    }

    public void setEffect(Model.Effect effect, int steps, Runnable callback) {

    }
}
