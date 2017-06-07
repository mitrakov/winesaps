package ru.mitrakov.self.rush.model;

/**
 * Created by mitrakov on 29.05.2017
 */
@SuppressWarnings("WeakerAccess")
public class Cells {
    public static abstract class CellObject {
        protected int number = 0;
        protected int xy;
        private int id;

        public CellObject(int id, int xy) {
            this.id = id;
            this.xy = xy;
        }

        public int getNumber() {
            return number;
        }

        public int getId() {
            return id;
        }

        public int getXy() {
            return xy;
        }

        public void setXy(int xy) {
            this.xy = xy;
        }

        public int getX() {
            return xy % Field.WIDTH;
        }

        public int getY() {
            return xy / Field.WIDTH;
        }
    }

    public static abstract class CellObjectAnimated extends CellObject {
        public CellObjectAnimated(int id, int xy) {
            super(id, xy);
        }
    }

    public static abstract class CellObjectFood extends CellObject {
        public CellObjectFood(int id, int xy) {
            super(id, xy);
        }
    }

    public static abstract class CellObjectRaisable extends CellObject {
        public CellObjectRaisable(int id, int xy) {
            super(id, xy);
        }
    }

    public static class Block extends CellObject {
        public Block(int xy) {
            super(0x01, xy);
        }
    }

    public static class Dais extends CellObject {
        public Dais(int xy) {
            super(0x02, xy);
        }
    }

    public static class Water extends CellObject {
        public Water(int xy) {
            super(0x03, xy);
        }
    }

    public static class Actor1 extends CellObjectAnimated {
        public Actor1(int xy, int number) {
            super(0x04, xy);
            this.number = number;
        }
    }

    public static class Actor2 extends CellObjectAnimated {
        public Actor2(int xy, int number) {
            super(0x05, xy);
            this.number = number;
        }
    }

    public static class Wolf extends CellObjectAnimated {
        public Wolf(int xy, int number) {
            super(0x06, xy);
            this.number = number;
        }
    }

    public static class Entry1 extends CellObject {
        public Entry1(int xy) {
            super(0x07, xy);
        }
    }

    public static class Entry2 extends CellObject {
        public Entry2(int xy) {
            super(0x08, xy);
        }
    }

    public static class LadderTop extends CellObject {
        public LadderTop(int xy) {
            super(0x09, xy);
        }
    }

    public static class LadderBottom extends CellObject {
        public LadderBottom(int xy) {
            super(0x0A, xy);
        }
    }

    public static class Stair extends CellObjectRaisable {
        public Stair(int xy) {
            super(0x0B, xy);
        }
    }

    public static class RopeLine extends CellObject {
        public RopeLine(int xy) {
            super(0x0C, xy);
        }
    }

    public static class Waterfall extends CellObject {
        public Waterfall(int xy) {
            super(0x0D, xy);
        }
        public Waterfall(int id, int xy) {
            super(id, xy);
        }
    }

    public static class WaterfallSafe extends Waterfall {
        public WaterfallSafe(int xy) {
            super(0x0E, xy);
        }
    }

    public static class BeamChunk extends CellObject {
        public BeamChunk(int xy, int number) {
            super(0x0F, xy);
            this.number = number;
        }
    }

    public static class Apple extends CellObjectFood {
        public Apple(int xy, int number) {
            super(0x10, xy);
            this.number = number;
        }
    }

    public static class Pear extends CellObjectFood {
        public Pear(int xy, int number) {
            super(0x11, xy);
            this.number = number;
        }
    }

    public static class Meat extends CellObjectFood {
        public Meat(int xy, int number) {
            super(0x12, xy);
            this.number = number;
        }
    }

    public static class Carrot extends CellObjectFood {
        public Carrot(int xy, int number) {
            super(0x13, xy);
            this.number = number;
        }
    }

    public static class Mushroom extends CellObjectFood {
        public Mushroom(int xy, int number) {
            super(0x14, xy);
            this.number = number;
        }
    }

    public static class Nut extends CellObjectFood {
        public Nut(int xy, int number) {
            super(0x15, xy);
            this.number = number;
        }
    }

    public static class UmbrellaThing extends CellObject {
        public UmbrellaThing(int xy, int number) {
            super(0x20, xy);
            this.number = number;
        }
    }

    public static class MineThing extends CellObject {
        public MineThing(int xy, int number) {
            super(0x21, xy);
            this.number = number;
        }
    }

    public static class BeamThing extends CellObject {
        public BeamThing(int xy, int number) {
            super(0x22, xy);
            this.number = number;
        }
    }

    public static class AntidoteThing extends CellObject {
        public AntidoteThing(int xy, int number) {
            super(0x23, xy);
            this.number = number;
        }
    }

    public static class DazzleGrenadeThing extends CellObject {
        public DazzleGrenadeThing(int xy, int number) {
            super(0x24, xy);
            this.number = number;
        }
    }

    public static class TeleportThing extends CellObject {
        public TeleportThing(int xy, int number) {
            super(0x25, xy);
            this.number = number;
        }
    }

    public static class DetectorThing extends CellObject {
        public DetectorThing(int xy, int number) {
            super(0x26, xy);
            this.number = number;
        }
    }

    public static class BoxThing extends CellObject {
        public BoxThing(int xy, int number) {
            super(0x27, xy);
            this.number = number;
        }
    }

    public static class Umbrella extends CellObject {
        public Umbrella(int xy, int number) {
            super(0x28, xy);
            this.number = number;
        }
    }

    public static class Mine extends CellObject {
        public Mine(int xy, int number) {
            super(0x29, xy);
            this.number = number;
        }
    }

    public static class Beam extends CellObject {
        public Beam(int xy, int number) {
            super(0x2A, xy);
            this.number = number;
        }
    }

    public static class Antidote extends CellObject {
        public Antidote(int xy, int number) {
            super(0x2B, xy);
            this.number = number;
        }
    }

    public static class DazzleGrenade extends CellObject {
        public DazzleGrenade(int xy, int number) {
            super(0x2C, xy);
            this.number = number;
        }
    }

    public static class Teleport extends CellObject {
        public Teleport(int xy, int number) {
            super(0x2D, xy);
            this.number = number;
        }
    }

    public static class Detector extends CellObject {
        public Detector(int xy, int number) {
            super(0x2E, xy);
            this.number = number;
        }
    }

    public static class Box extends CellObjectRaisable {
        public Box(int xy, int number) {
            super(0x2F, xy);
            this.number = number;
        }
    }

    public static class DecorationStatic extends CellObject {
        public DecorationStatic(int xy) {
            super(0x30, xy);
        }
    }

    public static class DecorationDynamic extends CellObject {
        public DecorationDynamic(int xy) {
            super(0x31, xy);
        }
    }

    public static class DecorationWarning extends CellObject {
        public DecorationWarning(int xy) {
            super(0x32, xy);
        }
    }
}
