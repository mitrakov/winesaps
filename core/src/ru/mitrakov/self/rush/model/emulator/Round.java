package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Field.*;

/**
 * Created by mitrakov on 09.03.2018
 */
public class Round {

    final FieldEx field;
    final Player player1;
    final Player player2;
    final int number;
    final int timeSec;
    final String levelname;

    public Round(Model.Character character1, Model.Character character2, int number, String levelName, int timeSec,
                 BattleManager battleManager) {
        assert character1 != null && character2 != null && levelName != null && battleManager != null;
        assert number >= 0 && timeSec > 0;
        this.number = number;
        this.timeSec = timeSec;
        this.levelname = levelName;

        Model.IFileReader reader = battleManager.getFileReader();
        assert reader != null;

        String path = String.format("levels/%s.level", levelName);
        byte[] level = reader.readAsByteArray(path);
        IIntArray array = new GcResistantIntArray(WIDTH * HEIGHT);
        IIntArray raw = new GcResistantIntArray(level.length);
        array.fromByteArray(level, WIDTH * HEIGHT);
        raw.fromByteArray(level, level.length);
        field = new FieldEx(array, raw, battleManager);

        int actor1Id = new Cells.Actor1(TRASH_CELL, 0).getId();
        int actor2Id = new Cells.Actor2(TRASH_CELL, 0).getId();
        Cells.CellObject obj1 = field.getObjectById(actor1Id);
        Cells.CellObject obj2 = field.getObjectById(actor2Id);
        ActorEx actor1 = new ActorEx(obj1.getCell(), obj1.getNumber());
        ActorEx actor2 = new ActorEx(obj2.getCell(), obj2.getNumber());
        actor1.setCharacter(character1);
        actor2.setCharacter(character2);

        field.replaceFavouriteFood(actor1, actor2);
        player1 = new Player(actor1);
        player2 = new Player(actor2);
    }

    void move(Model.MoveDirection direction) {
        // get components
        ActorEx actor = player1.actor;
        assert actor != null;
        Cell cell = actor.getCell();
        assert cell != null;

        // calculate delta
        int delta = 0;
        switch (direction) {
            case LeftDown:
                delta = field.isMoveDownPossible(cell) ? WIDTH : -1;
                break;
            case Left:
                delta = -1;
                break;
            case LeftUp:
                delta = field.isMoveUpPossible(cell) ? -WIDTH : -1;
                break;
            case RightDown:
                delta = field.isMoveDownPossible(cell) ? WIDTH : 1;
                break;
            case Right:
                delta = 1;
                break;
            case RightUp:
                delta = field.isMoveUpPossible(cell) ? -WIDTH : 1;
                break;
            default:
        }

        // set actor's direction (left/right)
        if (delta == 1)
            actor.setDirectionRight(true);
        if (delta == -1)
            actor.setDirectionRight(false);

        // go!
        boolean ok = field.move(actor, actor.getXy() + delta);

        // if movement ok => inc actor's internal step counter to get effects working
        if (ok)
            actor.addStep();
    }

    void useThing() {
        Cells.CellObjectThing thing = player1.setThing(null);
        if (thing != null)
            field.useThing(player1.actor, thing);
    }

    void restore() {
        ActorEx actor = player1.actor;
        assert actor != null;
        Cells.Entry1 entry = field.getEntryByActor();
        if (entry != null) {
            assert actor.getCell() != null && entry.getCell() != null;
            field.relocate(actor.getCell(), entry.getCell(), actor, true);
        } else throw new IllegalStateException("Entry not found");
    }

    void checkRoundFinished() {
        // TODO
    }

    void setThingToPlayer(Cells.CellObjectThing thing) {
        assert field != null && player1 != null;

        Cells.CellObjectThing oldThing = player1.setThing(thing);
        if (oldThing != null)
            field.dropThing(player1.actor, oldThing);
    }

    boolean wound(boolean me) {
        return me ? (--player1.lives > 0) : (--player2.lives > 0);
    }

    Player getPlayerByActor(ActorEx actor) {
        assert actor != null;
        if (actor == player1.actor) return player1;
        if (actor == player2.actor) return player2;
        return null;
    }
}
