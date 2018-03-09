package ru.mitrakov.self.rush.model;

/**
 * Created by mitrakov on 29.05.2017
 */
@SuppressWarnings("WeakerAccess")
public class Cells {
    public static abstract class CellObject {
        protected int number = 0;
        protected Cell cell; // reverse reference; since 2.0.0

        private int id;
        private Model.Effect effect = Model.Effect.None;

        public CellObject(int id, Cell cell) {
            assert cell != null;
            this.id = id;
            this.cell = cell;
        }

        public int getNumber() {
            return number;
        }

        public int getId() {
            return id;
        }

        public int getXy() {
            return cell.xy;
        }

        public int getX() {
            return cell.xy % Field.WIDTH;
        }

        public int getY() {
            return cell.xy / Field.WIDTH;
        }

        public Model.Effect getEffect() {
            return effect;
        }

        public void setEffect(Model.Effect effect) {
            this.effect = effect;
        }

        public Cell getCell() {
            return cell;
        }

        public void setCell(Cell newCell) {
            Cell oldCell = this.cell;
            oldCell.objects.remove(this);
            newCell.objects.add(this);
            this.cell = newCell;
        }
    }

    public static abstract class CellObjectAnimated extends CellObject {
        public CellObjectAnimated(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static abstract class CellObjectActor extends CellObjectAnimated {
        public CellObjectActor(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static abstract class CellObjectFood extends CellObject {
        public CellObjectFood(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static abstract class CellObjectFavouriteFood extends CellObjectFood {
        public CellObjectFavouriteFood(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static abstract class CellObjectRaisable extends CellObject {
        public CellObjectRaisable(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static abstract class CellObjectThing extends CellObject {
        public CellObjectThing(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static class Block extends CellObject {
        public Block(Cell cell) {
            super(0x01, cell);
        }
    }

    public static class Dais extends CellObject {
        public Dais(Cell cell) {
            super(0x02, cell);
        }
    }

    public static class Water extends CellObject {
        public Water(Cell cell) {
            super(0x03, cell);
        }
    }

    public static class Actor1 extends CellObjectActor {
        public Actor1(Cell cell, int number) {
            super(0x04, cell);
            this.number = number;
        }
    }

    public static class Actor2 extends CellObjectActor {
        public Actor2(Cell cell, int number) {
            super(0x05, cell);
            this.number = number;
        }
    }

    public static class Wolf extends CellObjectAnimated {
        public Wolf(Cell cell, int number) {
            super(0x06, cell);
            this.number = number;
        }
    }

    public static class Entry1 extends CellObject {
        public Entry1(Cell cell) {
            super(0x07, cell);
        }
    }

    public static class Entry2 extends CellObject {
        public Entry2(Cell cell) {
            super(0x08, cell);
        }
    }

    public static class LadderTop extends CellObject {
        public LadderTop(Cell cell) {
            super(0x09, cell);
        }
    }

    public static class LadderBottom extends CellObject {
        public LadderBottom(Cell cell) {
            super(0x0A, cell);
        }
    }

    public static class Stair extends CellObjectRaisable {
        public Stair(Cell cell) {
            super(0x0B, cell);
        }
    }

    public static class RopeLine extends CellObject {
        public RopeLine(Cell cell) {
            super(0x0C, cell);
        }
    }

    public static class Waterfall extends CellObject {
        public Waterfall(Cell cell) {
            super(0x0D, cell);
        }
        public Waterfall(int id, Cell cell) {
            super(id, cell);
        }
    }

    public static class WaterfallSafe extends Waterfall {
        public WaterfallSafe(Cell cell) {
            super(0x0E, cell);
        }
    }

    public static class BeamChunk extends CellObject {
        public BeamChunk(Cell cell, int number) {
            super(0x0F, cell);
            this.number = number;
        }
    }

    public static class Apple extends CellObjectFood {
        public Apple(Cell cell, int number) {
            super(0x10, cell);
            this.number = number;
        }
    }

    public static class Pear extends CellObjectFood {
        public Pear(Cell cell, int number) {
            super(0x11, cell);
            this.number = number;
        }
    }

    public static class Meat extends CellObjectFood {
        public Meat(Cell cell, int number) {
            super(0x12, cell);
            this.number = number;
        }
    }

    public static class Carrot extends CellObjectFood {
        public Carrot(Cell cell, int number) {
            super(0x13, cell);
            this.number = number;
        }
    }

    public static class Mushroom extends CellObjectFood {
        public Mushroom(Cell cell, int number) {
            super(0x14, cell);
            this.number = number;
        }
    }

    public static class Nut extends CellObjectFood {
        public Nut(Cell cell, int number) {
            super(0x15, cell);
            this.number = number;
        }
    }

    public static class FoodActor1 extends CellObjectFavouriteFood {
        public FoodActor1(Cell cell, int number) {
            super(0x16, cell);
            this.number = number;
        }
    }

    public static class FoodActor2 extends CellObjectFavouriteFood {
        public FoodActor2(Cell cell, int number) {
            super(0x17, cell);
            this.number = number;
        }
    }

    public static class UmbrellaThing extends CellObjectThing {
        public UmbrellaThing(Cell cell, int number) {
            super(0x20, cell);
            this.number = number;
        }
    }

    public static class MineThing extends CellObjectThing {
        public MineThing(Cell cell, int number) {
            super(0x21, cell);
            this.number = number;
        }
    }

    public static class BeamThing extends CellObjectThing {
        public BeamThing(Cell cell, int number) {
            super(0x22, cell);
            this.number = number;
        }
    }

    public static class AntidoteThing extends CellObjectThing {
        public AntidoteThing(Cell cell, int number) {
            super(0x23, cell);
            this.number = number;
        }
    }

    public static class FlashbangThing extends CellObjectThing {
        public FlashbangThing(Cell cell, int number) {
            super(0x24, cell);
            this.number = number;
        }
    }

    public static class TeleportThing extends CellObjectThing {
        public TeleportThing(Cell cell, int number) {
            super(0x25, cell);
            this.number = number;
        }
    }

    public static class DetectorThing extends CellObjectThing {
        public DetectorThing(Cell cell, int number) {
            super(0x26, cell);
            this.number = number;
        }
    }

    public static class BoxThing extends CellObjectThing {
        public BoxThing(Cell cell, int number) {
            super(0x27, cell);
            this.number = number;
        }
    }

    public static class Umbrella extends CellObject {
        public Umbrella(Cell cell, int number) {
            super(0x28, cell);
            this.number = number;
        }
    }

    public static class Mine extends CellObject {
        public Mine(Cell cell, int number) {
            super(0x29, cell);
            this.number = number;
        }
    }

    public static class Beam extends CellObject {
        public Beam(Cell cell, int number) {
            super(0x2A, cell);
            this.number = number;
        }
    }

    public static class Antidote extends CellObject {
        public Antidote(Cell cell, int number) {
            super(0x2B, cell);
            this.number = number;
        }
    }

    public static class Flashbang extends CellObject {
        public Flashbang(Cell cell, int number) {
            super(0x2C, cell);
            this.number = number;
        }
    }

    public static class Teleport extends CellObject {
        public Teleport(Cell cell, int number) {
            super(0x2D, cell);
            this.number = number;
        }
    }

    public static class Detector extends CellObject {
        public Detector(Cell cell, int number) {
            super(0x2E, cell);
            this.number = number;
        }
    }

    public static class Box extends CellObjectRaisable {
        public Box(Cell cell, int number) {
            super(0x2F, cell);
            this.number = number;
        }
    }

    public static class DecorationStatic extends CellObject {
        public DecorationStatic(Cell cell) {
            super(0x30, cell);
        }
    }

    public static class DecorationDynamic extends CellObject {
        public DecorationDynamic(Cell cell) {
            super(0x31, cell);
        }
    }

    public static class DecorationWarning extends CellObject {
        public DecorationWarning(Cell cell) {
            super(0x32, cell);
        }
    }

    public static class DecorationDanger extends CellObject {
        public DecorationDanger(Cell cell) {
            super(0x33, cell);
        }
    }
}
