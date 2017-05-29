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

    public static abstract class CellObjectFood extends CellObject {
        public CellObjectFood(int id, int xy) {
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

    public static class Actor1 extends CellObject {
        public Actor1(int xy, int number) {
            super(0x04, xy);
            this.number = number;
        }
    }

    public static class Actor2 extends CellObject {
        public Actor2(int xy, int number) {
            super(0x05, xy);
            this.number = number;
        }
    }

    public static class Wolf extends CellObject {
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

    public static class RopeBolt extends CellObject {
        public RopeBolt(int xy) {
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
    }

    public static class WaterfallSafe extends CellObject {
        public WaterfallSafe(int xy) {
            super(0x0E, xy);
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

    public static class Box extends CellObject {
        public Box(int xy) {
            super(0x2F, xy);
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
