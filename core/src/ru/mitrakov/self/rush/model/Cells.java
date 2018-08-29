package ru.mitrakov.self.rush.model;

/**
 * Static class for creating Cells of different types
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public class Cells {
    /**
     * Base class for all Cell Objects (actors, things, stairs, umbrellas, mines, etc.)
     */
    public static abstract class CellObject {
        /** Sequence number (must be unique, but theoretically might have "holes") */
        protected int number = 0;
        /**
         * Reverse reference (before 2.0.0 we just use "xy")
         * @since 2.0.0
         */
        protected Cell cell;

        /** Object ID */
        private int id;
        /** Current effect applied to the object */
        private Model.Effect effect = Model.Effect.None;

        /**
         * Creates new Cell Object
         * @param id object ID (see Server API documentation for more details)
         * @param cell cell to store the new object created
         */
        public CellObject(int id, Cell cell) {
            assert cell != null;
            this.id = id;
            this.cell = cell;
        }

        /**
         * @return sequence number of the object
         */
        public int getNumber() {
            return number;
        }

        /**
         * @return ID of the object
         */
        public int getId() {
            return id;
        }

        /**
         * @return coordinates of the object (0-255)
         * @see #getX()
         * @see #getY()
         */
        public int getXy() {
            return cell.xy;
        }

        /**
         * @return X-coordinate of the object (0 to {@link Field#WIDTH WIDTH} - 1)
         * @see #getXy()
         * @see #getY()
         */
        public int getX() {
            return cell.xy % Field.WIDTH;
        }

        /**
         * @return Y-coordinate of the object (0 to {@link Field#HEIGHT HEIGHT} - 1)
         * @see #getXy()
         * @see #getX()
         */
        public int getY() {
            return cell.xy / Field.WIDTH;
        }

        /**
         * @return current effect that have been applied to the object (may be NULL)
         */
        public Model.Effect getEffect() {
            return effect;
        }

        /**
         * Assigns the given effect to the object
         * @param effect effect (may be NULL)
         */
        public void setEffect(Model.Effect effect) {
            this.effect = effect;
        }

        /**
         * @return cell where the object is located
         * @since 2.0.0
         */
        public Cell getCell() {
            return cell;
        }

        /**
         * Sets the cell to the object so that the object will be located in the new <b>newCell</b>
         * @param newCell new cell to store the object
         * @since 2.0.0
         */
        public void setCell(Cell newCell) {
            Cell oldCell = this.cell;
            oldCell.objects.remove(this);
            newCell.objects.add(this);
            this.cell = newCell;
        }
    }

    /** Animated Object (e.g. Actors and Wolves) */
    public static abstract class CellObjectAnimated extends CellObject {
        /**
         * Creates a new Animated object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public CellObjectAnimated(int id, Cell cell) {
            super(id, cell);
        }
    }

    /** Actor Object */
    public static abstract class CellObjectActor extends CellObjectAnimated {
        /**
         * Creates a new Actor object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public CellObjectActor(int id, Cell cell) {
            super(id, cell);
        }
    }

    /** Food Object (base class for all the food) */
    public static abstract class CellObjectFood extends CellObject {
        /**
         * Creates a new Food object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public CellObjectFood(int id, Cell cell) {
            super(id, cell);
        }
    }

    /** Favourite Food Object (to be replaced with real food according to a user's character) */
    public static abstract class CellObjectFavouriteFood extends CellObjectFood {
        /**
         * Creates a new Favourite Food object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public CellObjectFavouriteFood(int id, Cell cell) {
            super(id, cell);
        }
    }

    /** Raisable Object (e.g. stairs, etc.) */
    public static abstract class CellObjectRaisable extends CellObject {
        /**
         * Creates a new  object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public CellObjectRaisable(int id, Cell cell) {
            super(id, cell);
        }
    }

    /** Thing Object (base class for all possible things) */
    public static abstract class CellObjectThing extends CellObject {
        /**
         * Creates a new Thing object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public CellObjectThing(int id, Cell cell) {
            super(id, cell);
        }
    }

    /** Block Object */
    public static class Block extends CellObject {
        /**
         * Creates a new Block object
         * @param cell cell to store the new object created
         */
        public Block(Cell cell) {
            super(0x01, cell);
        }
    }

    /** Dais Object */
    public static class Dais extends CellObject {
        /**
         * Creates a new Dais object
         * @param cell cell to store the new object created
         */
        public Dais(Cell cell) {
            super(0x02, cell);
        }
    }

    /** Water Object */
    public static class Water extends CellObject {
        /**
         * Creates a new Water object
         * @param cell cell to store the new object created
         */
        public Water(Cell cell) {
            super(0x03, cell);
        }
    }

    /** Actor1 Object */
    public static class Actor1 extends CellObjectActor {
        /**
         * Creates a new Actor1 object
         * @param cell cell to store the new object created
         */
        public Actor1(Cell cell, int number) {
            super(0x04, cell);
            this.number = number;
        }
    }

    /** Actor2 Object */
    public static class Actor2 extends CellObjectActor {
        /**
         * Creates a new Actor2 object
         * @param cell cell to store the new object created
         */
        public Actor2(Cell cell, int number) {
            super(0x05, cell);
            this.number = number;
        }
    }

    /** Wolf Object */
    public static class Wolf extends CellObjectAnimated {
        /**
         * Creates a new Wolf object
         * @param cell cell to store the new object created
         */
        public Wolf(Cell cell, int number) {
            super(0x06, cell);
            this.number = number;
        }
    }

    /** Entry1 Object (where Actor1 is respawn) */
    public static class Entry1 extends CellObject {
        /**
         * Creates a new Entry1 object
         * @param cell cell to store the new object created
         */
        public Entry1(Cell cell) {
            super(0x07, cell);
        }
    }

    /** Entry2 Object (where Actor2 is respawn) */
    public static class Entry2 extends CellObject {
        /**
         * Creates a new Entry2 object
         * @param cell cell to store the new object created
         */
        public Entry2(Cell cell) {
            super(0x08, cell);
        }
    }

    /** LadderTop Object (in order to move down) */
    public static class LadderTop extends CellObject {
        /**
         * Creates a new LadderTop object
         * @param cell cell to store the new object created
         */
        public LadderTop(Cell cell) {
            super(0x09, cell);
        }
    }

    /** LadderBottom Object (in order to move up) */
    public static class LadderBottom extends CellObject {
        /**
         * Creates a newLadderBottom  object
         * @param cell cell to store the new object created
         */
        public LadderBottom(Cell cell) {
            super(0x0A, cell);
        }
    }

    /** Stair Object */
    public static class Stair extends CellObjectRaisable {
        /**
         * Creates a new Stair object
         * @param cell cell to store the new object created
         */
        public Stair(Cell cell) {
            super(0x0B, cell);
        }
    }

    /** RopeLine Object */
    public static class RopeLine extends CellObject {
        /**
         * Creates a new RopeLine object
         * @param cell cell to store the new object created
         */
        public RopeLine(Cell cell) {
            super(0x0C, cell);
        }
    }

    /** Waterfall Object */
    public static class Waterfall extends CellObject {
        /**
         * Creates a new Waterfall object (with default ID)
         * @param cell cell to store the new object created
         */
        public Waterfall(Cell cell) {
            super(0x0D, cell);
        }

        /**
         * Creates a new Waterfall object
         * @param id object ID
         * @param cell cell to store the new object created
         */
        public Waterfall(int id, Cell cell) {
            super(id, cell);
        }
    }

    /**
     * Safe Waterfall Object
     * @deprecated (since 2.0.0) because now Training level is under control of SinglePlayer Emulator, and the user's
     * actor will never die
     */
    public static class WaterfallSafe extends Waterfall {
        /**
         * Creates a new Safe Waterfall object
         * @deprecated since 2.0.0
         * @param cell cell to store the new object created
         */
        public WaterfallSafe(Cell cell) {
            super(0x0E, cell);
        }
    }

    /** Beam Chunk Object (single part of a bridge) */
    public static class BeamChunk extends CellObject {
        /**
         * Creates a new Beam Chunk object
         * @param cell cell to store the new object created
         */
        public BeamChunk(Cell cell, int number) {
            super(0x0F, cell);
            this.number = number;
        }
    }

    /** Apple Object */
    public static class Apple extends CellObjectFood {
        /**
         * Creates a new Apple object
         * @param cell cell to store the new object created
         */
        public Apple(Cell cell, int number) {
            super(0x10, cell);
            this.number = number;
        }
    }

    /** Pear Object */
    public static class Pear extends CellObjectFood {
        /**
         * Creates a new Pear object
         * @param cell cell to store the new object created
         */
        public Pear(Cell cell, int number) {
            super(0x11, cell);
            this.number = number;
        }
    }

    /** Meat Object */
    public static class Meat extends CellObjectFood {
        /**
         * Creates a new Meat object
         * @param cell cell to store the new object created
         */
        public Meat(Cell cell, int number) {
            super(0x12, cell);
            this.number = number;
        }
    }

    /** Carrot Object */
    public static class Carrot extends CellObjectFood {
        /**
         * Creates a new Carrot object
         * @param cell cell to store the new object created
         */
        public Carrot(Cell cell, int number) {
            super(0x13, cell);
            this.number = number;
        }
    }

    /** Mushroom Object */
    public static class Mushroom extends CellObjectFood {
        /**
         * Creates a new Mushroom object
         * @param cell cell to store the new object created
         */
        public Mushroom(Cell cell, int number) {
            super(0x14, cell);
            this.number = number;
        }
    }

    /** Nut Object */
    public static class Nut extends CellObjectFood {
        /**
         * Creates a new Nut object
         * @param cell cell to store the new object created
         */
        public Nut(Cell cell, int number) {
            super(0x15, cell);
            this.number = number;
        }
    }

    /** FoodActor1 Object (reason is to be replaced with what an Actor1 likes, e.g. Carrot for Rabbits) */
    public static class FoodActor1 extends CellObjectFavouriteFood {
        /**
         * Creates a new FoodActor1 object
         * @param cell cell to store the new object created
         */
        public FoodActor1(Cell cell, int number) {
            super(0x16, cell);
            this.number = number;
        }
    }

    /** FoodActor2 Object (reason is to be replaced with what an Actor2 likes, e.g. Carrot for Rabbits) */
    public static class FoodActor2 extends CellObjectFavouriteFood {
        /**
         * Creates a new FoodActor2 object
         * @param cell cell to store the new object created
         */
        public FoodActor2(Cell cell, int number) {
            super(0x17, cell);
            this.number = number;
        }
    }

    /** Umbrella Thing Object */
    public static class UmbrellaThing extends CellObjectThing {
        /**
         * Creates a new Umbrella Thing object
         * @param cell cell to store the new object created
         */
        public UmbrellaThing(Cell cell, int number) {
            super(0x20, cell);
            this.number = number;
        }
    }

    /** Mine Thing Object */
    public static class MineThing extends CellObjectThing {
        /**
         * Creates a new Mine Thing object
         * @param cell cell to store the new object created
         */
        public MineThing(Cell cell, int number) {
            super(0x21, cell);
            this.number = number;
        }
    }

    /** Beam Thing Object */
    public static class BeamThing extends CellObjectThing {
        /**
         * Creates a new Beam Thing object
         * @param cell cell to store the new object created
         */
        public BeamThing(Cell cell, int number) {
            super(0x22, cell);
            this.number = number;
        }
    }

    /** Antidote Thing Object */
    public static class AntidoteThing extends CellObjectThing {
        /**
         * Creates a new Antidote Thing object
         * @param cell cell to store the new object created
         */
        public AntidoteThing(Cell cell, int number) {
            super(0x23, cell);
            this.number = number;
        }
    }

    /** Flashbang Thing Object */
    public static class FlashbangThing extends CellObjectThing {
        /**
         * Creates a new Flashbang Thing object
         * @param cell cell to store the new object created
         */
        public FlashbangThing(Cell cell, int number) {
            super(0x24, cell);
            this.number = number;
        }
    }

    /** Teleport Thing Object */
    public static class TeleportThing extends CellObjectThing {
        /**
         * Creates a new Teleport Thing object
         * @param cell cell to store the new object created
         */
        public TeleportThing(Cell cell, int number) {
            super(0x25, cell);
            this.number = number;
        }
    }

    /** Mines Detector Thing Object */
    public static class DetectorThing extends CellObjectThing {
        /**
         * Creates a new Mines Detector Thing object
         * @param cell cell to store the new object created
         */
        public DetectorThing(Cell cell, int number) {
            super(0x26, cell);
            this.number = number;
        }
    }

    /** Box Thing Object */
    public static class BoxThing extends CellObjectThing {
        /**
         * Creates a new Box Thing object
         * @param cell cell to store the new object created
         */
        public BoxThing(Cell cell, int number) {
            super(0x27, cell);
            this.number = number;
        }
    }

    /** Umbrella Object */
    public static class Umbrella extends CellObject {
        /**
         * Creates a new Umbrella object
         * @param cell cell to store the new object created
         */
        public Umbrella(Cell cell, int number) {
            super(0x28, cell);
            this.number = number;
        }
    }

    /** Mine Object */
    public static class Mine extends CellObject {
        /**
         * Creates a new Mine object
         * @param cell cell to store the new object created
         */
        public Mine(Cell cell, int number) {
            super(0x29, cell);
            this.number = number;
        }
    }

    /** Beam Object */
    public static class Beam extends CellObject {
        /**
         * Creates a new Beam object
         * @param cell cell to store the new object created
         */
        public Beam(Cell cell, int number) {
            super(0x2A, cell);
            this.number = number;
        }
    }

    /** Antidote Object */
    public static class Antidote extends CellObject {
        /**
         * Creates a new Antidote object
         * @param cell cell to store the new object created
         */
        public Antidote(Cell cell, int number) {
            super(0x2B, cell);
            this.number = number;
        }
    }

    /** Flashbang Object */
    public static class Flashbang extends CellObject {
        /**
         * Creates a new Flashbang object
         * @param cell cell to store the new object created
         */
        public Flashbang(Cell cell, int number) {
            super(0x2C, cell);
            this.number = number;
        }
    }

    /** Teleport Object */
    public static class Teleport extends CellObject {
        /**
         * Creates a new Teleport object
         * @param cell cell to store the new object created
         */
        public Teleport(Cell cell, int number) {
            super(0x2D, cell);
            this.number = number;
        }
    }

    /** Mines Detector Object */
    public static class Detector extends CellObject {
        /**
         * Creates a new Mines Detector object
         * @param cell cell to store the new object created
         */
        public Detector(Cell cell, int number) {
            super(0x2E, cell);
            this.number = number;
        }
    }

    /** Box Object */
    public static class Box extends CellObjectRaisable {
        /**
         * Creates a new Box object
         * @param cell cell to store the new object created
         */
        public Box(Cell cell, int number) {
            super(0x2F, cell);
            this.number = number;
        }
    }

    /** Static Decoration Object (for appearance purposes only) */
    public static class DecorationStatic extends CellObject {
        /**
         * Creates a new Static Decoration object
         * @param cell cell to store the new object created
         */
        public DecorationStatic(Cell cell) {
            super(0x30, cell);
        }
    }

    /** Dynamic Decoration Object (for appearance purposes only) */
    public static class DecorationDynamic extends CellObject {
        /**
         * Creates a new Dynamic Decoration object
         * @param cell cell to store the new object created
         */
        public DecorationDynamic(Cell cell) {
            super(0x31, cell);
        }
    }

    /** Warning Decoration Object (for appearance purposes only) */
    public static class DecorationWarning extends CellObject {
        /**
         * Creates a new Warning Decoration object
         * @param cell cell to store the new object created
         */
        public DecorationWarning(Cell cell) {
            super(0x32, cell);
        }
    }

    /** Danger Decoration Object (for appearance purposes only) */
    public static class DecorationDanger extends CellObject {
        /**
         * Creates a new Danger Decoration object
         * @param cell cell to store the new object created
         */
        public DecorationDanger(Cell cell) {
            super(0x33, cell);
        }
    }
}
