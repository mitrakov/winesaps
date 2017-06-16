package ru.mitrakov.self.rush.ui;

import static java.lang.Math.*;
import static ru.mitrakov.self.rush.model.Model.STYLES_COUNT;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.Cells.*;
import ru.mitrakov.self.rush.ui.InputController.MoveResult;

/**
 * Created by mitrakov on 23.02.2017
 */
public class Gui extends Actor {
    static private final class MyClickListener extends ClickListener {
        float x, y = 0;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            this.x = x;
            this.y = y;
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            this.x = x;
            this.y = y;
            super.touchDragged(event, x, y, pointer);
        }
    }

    static private abstract class AnimInfo {
        float x, y;
        float t;

        abstract Animation<TextureRegion> getAnimation(Object key);
    }

//    static private abstract class AnimInfoMovable extends AnimInfo {
//        float speedX;
//        boolean dirRight;
//        int delay;
//    }

    static private final class AnimInfoSimple extends AnimInfo {
        final Animation<TextureRegion> animation;

        private AnimInfoSimple(Animation<TextureRegion> animation) {
            this.animation = animation;
            t = BIG_VALUE;
        }

        @Override
        Animation<TextureRegion> getAnimation(Object key) {
            return animation;
        }
    }

//    static private final class AnimInfoChar extends AnimInfoMovable {
//        final ObjectMap<Model.Character, AnimationData> animations;
//
//        AnimInfoChar(ObjectMap<Model.Character, AnimationData> animations, float speedX, boolean dirRight) {
//            this.animations = animations;
//            this.speedX = speedX;
//            this.dirRight = dirRight;
//        }
//
//        @Override
//        Animation<TextureRegion> getAnimation(Object key) {
//            if (key instanceof Model.Character) {
//                AnimationData data = animations.get((Model.Character) key);
//                return data.animations.get(AnimationData.AnimationType.Run);
//            }
//            return null;
//        }
//    }
//
//    static private final class AnimInfoWolf extends AnimInfoMovable {
//        final Animation<TextureRegion> animation;
//
//        AnimInfoWolf(Animation<TextureRegion> animation, float speedX) {
//            this.animation = animation;
//            this.speedX = speedX;
//        }
//
//        @Override
//        Animation<TextureRegion> getAnimation(Object key) {
//            return animation;
//        }
//    }

    private static final int CELL_SIZ_W = 14;
    private static final int CELL_SIZ_H = 85;
    private static final int OFFSET_X = (Winesaps.WIDTH - Field.WIDTH * CELL_SIZ_W) / 2; // (800 - 51*14) / 2
    private static final int OFFSET_Y = 33; // inferred by expertise
    private static final int MOVES_PER_SEC = 5;
    private static final int MOVES_PER_SEC_WOLF = 4;
    private static final int SPEED_X = CELL_SIZ_W * MOVES_PER_SEC;
    private static final int SPEED_X_WOLF = CELL_SIZ_W * MOVES_PER_SEC_WOLF;
    private static final int SPEED_Y = CELL_SIZ_H * MOVES_PER_SEC;
    private static final int FRAMES_PER_MOVE = 60 / MOVES_PER_SEC; // FPS / MOVES_PER_SEC
    private static final int BIG_VALUE = 99;

    private final Model model;
    private final InputController controller;
    private final MyClickListener listener = new MyClickListener();

    private final TextureAtlas atlasDown = new TextureAtlas(Gdx.files.internal("pack/down.pack"));
    private final TextureAtlas atlasUp = new TextureAtlas(Gdx.files.internal("pack/up.pack"));
    private final TextureAtlas atlasAnimated = new TextureAtlas(Gdx.files.internal("pack/animated.pack"));
    private final TextureAtlas atlasExplosion = new TextureAtlas(Gdx.files.internal("pack/explosion.pack"));
    private final TextureAtlas atlasLadder = new TextureAtlas(Gdx.files.internal("pack/ladder.pack"));
    private final TextureAtlas atlasWolf = new TextureAtlas(Gdx.files.internal("pack/wolf.pack"));
    private final TextureAtlas atlasFlare = new TextureAtlas(Gdx.files.internal("pack/flare.pack"));

    private final Array<Texture> backgrounds = new Array<Texture>(STYLES_COUNT);
    private final Array<TextureAtlas> charAtlases = new Array<TextureAtlas>(Model.Character.values().length);
    private final ObjectMap<Class, IntMap<TextureRegion>> texturesDown = new ObjectMap<Class, IntMap<TextureRegion>>(3);
    private final ObjectMap<Class, IntMap<TextureRegion>> texturesStat = new ObjectMap<Class, IntMap<TextureRegion>>(9);
    private final ObjectMap<Class, TextureRegion> texturesCollectible = new ObjectMap<Class, TextureRegion>(20);
    private final ObjectMap<Class, TextureRegion> texturesOverlay = new ObjectMap<Class, TextureRegion>(20);
    private final ObjectMap<Class, AnimationData<Model.Character>> texturesAnim =
            new ObjectMap<Class, AnimationData<Model.Character>>(2);
    private final IntMap<AnimationData<Model.Character>> texturesAnimWolf =
            new IntMap<AnimationData<Model.Character>>(100);
    private final IntMap<Animation<TextureRegion>> animLadders = new IntMap<Animation<TextureRegion>>(STYLES_COUNT);
    private final FloatArray animTime = new FloatArray(Field.HEIGHT * Field.WIDTH);
    private final IntSet mineIndices = new IntSet();
    private final ObjectSet<Class> heightOffsets = new ObjectSet<Class>(1);
    private final Animation<TextureRegion> animWaterfall;
    private final Animation<TextureRegion> animWaterfallSmall;
    private final Animation<TextureRegion> animAntidote;
    private final Animation<TextureRegion> animTeleport;
    private final Animation<TextureRegion> animDazzleGrenade;
    private final Animation<TextureRegion> animDetector;
    private final AnimInfo animFlare;
    private final AnimInfo animExplosion;

    private long frameNumber = 0, lastMoveFrame = -FRAMES_PER_MOVE;
    private float time = 0;

    private static float convertXFromModelToScreen(int x) {
        return x * CELL_SIZ_W + OFFSET_X;
    }

    private static float convertYFromModelToScreen(int y) {
        return (Field.HEIGHT - y) * CELL_SIZ_H - OFFSET_Y;
    }

    static int convertXFromScreenToModel(float x) {
        return (int) ((x - OFFSET_X) / CELL_SIZ_W);
    }

    static int convertYFromScreenToModel(float y) {
        return (int) (Field.HEIGHT - y / CELL_SIZ_H);
    }

    public Gui(Model model) {
        assert model != null;
        this.model = model;
        controller = new InputController(model);
        addListener(listener);

        // in theory width must be "Field.WIDTH * CELL_SIZ_W", but we use full width for convenience on touch screens
        setWidth(Winesaps.WIDTH);
        setHeight(Field.HEIGHT * CELL_SIZ_H);

        // down textures (block, dias, water), each with 4 styles
        for (Class clazz : new Class[]{Block.class, Dais.class, Water.class}) {
            IntMap<TextureRegion> m = new IntMap<TextureRegion>(STYLES_COUNT); // .... GC!
            for (int i = 0; i < STYLES_COUNT; i++) {
                String key = clazz.getSimpleName() + i;
                TextureRegion texture = atlasDown.findRegion(key);
                if (texture != null)
                    m.put(i, texture);
            }
            texturesDown.put(clazz, m);
        }
        // static up textures, each with 4 styles
        for (Class clazz : new Class[]{Block.class, Stair.class, LadderTop.class, RopeLine.class, Water.class,
                DecorationStatic.class, DecorationWarning.class}) {
            IntMap<TextureRegion> m = new IntMap<TextureRegion>(STYLES_COUNT); // .... GC!
            for (int i = 0; i < STYLES_COUNT; i++) {
                String key = clazz.getSimpleName() + i;
                TextureRegion texture = atlasUp.findRegion(key);
                if (texture != null)
                    m.put(i, texture);
            }
            texturesStat.put(clazz, m);
        }
        // collectible textures
        for (Class clazz : new Class[]{
                Apple.class, Pear.class, Meat.class, Carrot.class, Nut.class, Mushroom.class,
                UmbrellaThing.class, MineThing.class, BeamThing.class, AntidoteThing.class, DazzleGrenadeThing.class,
                TeleportThing.class, DetectorThing.class, BoxThing.class}) {
            TextureRegion texture = atlasUp.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesCollectible.put(clazz, texture);
        }
        // overlay
        for (Class clazz : new Class[]{Umbrella.class, Box.class, Beam.class, BeamChunk.class}) {
            TextureRegion texture = atlasUp.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesOverlay.put(clazz, texture);
        }
        // animations
        for (Class clazz : new Class[]{Actor1.class, Actor2.class}) {
            //ObjectMap<Model.Character, AnimationData> animations = new ObjectMap<Model.Character, AnimationData>(4);
            AnimationData<Model.Character> data = new AnimationData<Model.Character>(SPEED_X);
            for (Model.Character character : Model.Character.values()) {
                if (character != Model.Character.None) {
                    String filename = String.format("pack/%s.pack", character.name().toLowerCase());
                    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(filename));
                    data.add(character, atlas, .07f);
                    //animations.put(character, new AnimationData(atlas, .07f));
                    charAtlases.add(atlas);
                }
            }
            texturesAnim.put(clazz, data);
        }
        // wolf
        //Array<TextureAtlas.AtlasRegion> framesWolf = atlasWolf.findRegions("wolf");
        for (int i = 0; i < 100; i++) {
            AnimationData<Model.Character> data = new AnimationData<Model.Character>(SPEED_X_WOLF);
            data.add(Model.Character.None, atlasWolf, .09f);
            //Animation<TextureRegion> anim = new Animation<TextureRegion>(.09f, framesWolf, Animation.PlayMode.LOOP);
            texturesAnimWolf.put(i, data);
        }
        // animated objects (waterfalls, antidotes, teleports)
        Array<TextureAtlas.AtlasRegion> framesWaterfall = atlasAnimated.findRegions("Waterfall");
        Array<TextureAtlas.AtlasRegion> framesWaterfallSmall = atlasAnimated.findRegions("WaterfallSmall");
        Array<TextureAtlas.AtlasRegion> framesAntidote = atlasAnimated.findRegions("Antidote");
        Array<TextureAtlas.AtlasRegion> framesTeleport = atlasAnimated.findRegions("Teleport");
        Array<TextureAtlas.AtlasRegion> framesDazzleGrenade = atlasAnimated.findRegions("DazzleGrenade");
        Array<TextureAtlas.AtlasRegion> framesDetector = atlasAnimated.findRegions("Detector");
        Array<TextureAtlas.AtlasRegion> framesExplosion = atlasExplosion.findRegions("Explosion");
        Array<TextureAtlas.AtlasRegion> framesFlare = atlasFlare.findRegions("Flare");

        animWaterfall = new Animation<TextureRegion>(.09f, framesWaterfall, Animation.PlayMode.LOOP);
        animWaterfallSmall = new Animation<TextureRegion>(.09f, framesWaterfallSmall, Animation.PlayMode.LOOP);
        animAntidote = new Animation<TextureRegion>(.15f, framesAntidote, Animation.PlayMode.LOOP_PINGPONG);
        animTeleport = new Animation<TextureRegion>(.09f, framesTeleport, Animation.PlayMode.LOOP);
        animDazzleGrenade = new Animation<TextureRegion>(.09f, framesDazzleGrenade, Animation.PlayMode.LOOP);
        animDetector = new Animation<TextureRegion>(.15f, framesDetector, Animation.PlayMode.LOOP);

        animExplosion = new AnimInfoSimple(new Animation<TextureRegion>(.06f, framesExplosion));
        animFlare = new AnimInfoSimple(new Animation<TextureRegion>(.09f, framesFlare));
        //
        for (int i = 0; i < STYLES_COUNT; i++) {
            // ladderBottom animations
            Array<TextureAtlas.AtlasRegion> frames = atlasLadder.findRegions(LadderBottom.class.getSimpleName() + i);
            animLadders.put(i, new Animation<TextureRegion>(.05f, frames));
            // backgrounds
            backgrounds.add(new Texture(Gdx.files.internal(String.format(Locale.getDefault(), "back/back%d.jpg", i))));
        }

        // fill animation time
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                animTime.add(BIG_VALUE);
            }
        }
        // fill offsets
        heightOffsets.add(BeamChunk.class);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        controller.checkInput();
        if (++frameNumber - lastMoveFrame >= FRAMES_PER_MOVE) {
            if (controller.checkMovement(listener.getPressedButton(), listener.x, listener.y) != MoveResult.None)
                lastMoveFrame = frameNumber;
        }

        batch.draw(backgrounds.get(model.stylePack), 0, 0);

        float dt = Gdx.graphics.getDeltaTime();
        time += dt;

        Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
        if (field != null) {
            // draw 1-st layer (bottom (block/water/dias) with one of 4 styles)
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                    Class key = null;
                    if (cell.bottom != null)
                        key = cell.bottom.getClass();
                    else { // case: long RopeLine
                        Cell below = (j + 1 < Field.HEIGHT) ? field.cells[(j + 1) * Field.WIDTH + i] : null;
                        if (below != null && cell.objectExists(RopeLine.class) && below.objectExists(RopeLine.class))
                            key = Block.class;
                    }
                    if (key != null && texturesDown.containsKey(key)) {
                        TextureRegion texture = texturesDown.get(key).get(model.stylePack);
                        if (texture != null) {
                            float x = convertXFromModelToScreen(i);
                            float y = convertYFromModelToScreen(j);
                            batch.draw(texture, x, y);
                        }
                    }
                }
            }
            // draw 2-nd layer (static objects)
            drawObjects(field, batch);
            // draw 3-rd layer (LadderBottom objects)
            drawLadderBottom(field, batch);
            // draw 4-th layer (waterfalls)
            drawWaterfalls(field, batch);
            // draw 5-th layer (collectible objects)
            drawObjects(field, batch, texturesCollectible);
            // draw 6-th layer (antidotes, teleports)
            drawAnim(field, batch, Antidote.class, animAntidote);
            drawAnim(field, batch, Teleport.class, animTeleport);
            drawAnim(field, batch, DazzleGrenade.class, animDazzleGrenade);
            drawAnim(field, batch, Detector.class, animDetector);
            // draw 7-th layer (animated characters)
            drawAnimatedObjects(field, batch, dt);
            // draw 8-th layer (all overlaying objects like Umbrella)
            drawObjects(field, batch, texturesOverlay);
            //
            drawExplosions(field, batch, dt);
            //
            drawFlare(batch, dt);
        }
    }

    public void setMovesAllowed(boolean value) {
        controller.setMovesAllowed(value);
    }

    public void dispose() {
        atlasDown.dispose(); // disposing an atlas also disposes all its internal textures
        atlasUp.dispose();
        atlasAnimated.dispose();
        atlasExplosion.dispose();
        atlasFlare.dispose();
        atlasLadder.dispose();
        atlasWolf.dispose();
        for (Texture texture : backgrounds)
            texture.dispose();
        for (TextureAtlas atlas : charAtlases)
            atlas.dispose();
    }

    private float getBottomWidth(Cell cell) {
        // cell != null (assert omitted because it's called inside 'render()')
        float bottomWidth = CELL_SIZ_W;
        if (cell.bottom != null) {
            Class key = cell.bottom.getClass();
            if (texturesDown.containsKey(key)) {
                TextureRegion region = texturesDown.get(key).get(model.stylePack);
                if (region != null)
                    bottomWidth = region.getRegionWidth();
            }
        }
        return bottomWidth;
    }

    private float getBottomHeight(Cell cell) {
        // cell != null (assert omitted because it's called inside 'render()')
        float bottomHeight = CELL_SIZ_W;
        if (cell.bottom != null) {
            Class key = cell.bottom.getClass();
            if (texturesDown.containsKey(key)) {
                TextureRegion region = texturesDown.get(key).get(model.stylePack);
                if (region != null)
                    bottomHeight = region.getRegionHeight();
            }
        }

        return bottomHeight;
    }

    private boolean isRopeBelow(Field field, Cell cell) {
        // field != NULL, cell != NULL
        Cell cellBelow = cell.xy + Field.WIDTH < Field.WIDTH * Field.HEIGHT ? field.cells[cell.xy + Field.WIDTH] : null;
        return cellBelow != null && cellBelow.objectExists(RopeLine.class);
    }

    private boolean ladderBottomExists(Field field, Cell cell) {
        // field != NULL, cell != NULL
        Cell cellBelow = cell.xy + Field.WIDTH < Field.WIDTH * Field.HEIGHT ? field.cells[cell.xy + Field.WIDTH] : null;
        if (cellBelow != null) {
            if (cell.objectExists(LadderTop.class) && cellBelow.objectExists(LadderBottom.class))
                return true;
        }
        return false;
    }

    private boolean ladderTopExists(Field field, Cell cell) {
        // field != NULL, cell != NULL
        Cell cellAbove = cell.xy - Field.WIDTH >= 0 ? field.cells[cell.xy - Field.WIDTH] : null;
        if (cellAbove != null) {
            if (cellAbove.objectExists(LadderTop.class) && cell.objectExists(LadderBottom.class))
                return true;
        }
        return false;
    }

    private boolean animatedUsesLadder(Field field, Cell cell) {
        // field != null (assert omitted)
        CellObject actor = cell.getFirst(CellObjectAnimated.class);
        if (actor == null) {
            Cell cellAbove = cell.xy - Field.WIDTH >= 0 ? field.cells[cell.xy - Field.WIDTH] : null;
            if (cellAbove != null)
                actor = cellAbove.getFirst(CellObjectAnimated.class);
        }
        if (actor != null) {
            AnimationData anim = texturesAnim.get(actor.getClass()); // FIXME wolf
            if (anim != null)
                return anim.getAnimation() == AnimationData.AnimationType.Ladder;
        }
        return false;
    }

    private void drawObjects(Field field, Batch batch, ObjectMap<Class, TextureRegion> map) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                for (int k = 0; k < cell.getObjectsCount(); k++) {  // .... GC!
                    CellObject obj = cell.getObject(k);
                    if (obj != null) {
                        TextureRegion texture = map.get(obj.getClass());
                        if (texture != null) {
                            if (heightOffsets.contains(obj.getClass()))
                                bottomHeight -= texture.getRegionHeight();
                            float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                            float y = convertYFromModelToScreen(j) + bottomHeight;
                            batch.draw(texture, x, y);
                        }
                    }
                }
            }
        }
    }

    private void drawObjects(Field field, Batch batch) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                for (int k = 0; k < cell.getObjectsCount(); k++) {  // .... GC!
                    CellObject obj = cell.getObject(k);
                    if (obj != null) {
                        IntMap<TextureRegion> m = texturesStat.get(obj.getClass());
                        if (m != null) {
                            TextureRegion texture = m.get(model.stylePack);
                            if (texture != null) {
                                float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                                float y = convertYFromModelToScreen(j) + bottomHeight;
                                batch.draw(texture, x, y);
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawWaterfalls(Field field, Batch batch) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                boolean hasUmbrella = cell.objectExists(Umbrella.class);
                CellObject obj = cell.getFirst(Waterfall.class);
                if (obj != null) {
                    Animation<TextureRegion> animation = hasUmbrella ? animWaterfallSmall : animWaterfall;
                    TextureRegion texture = animation.getKeyFrame(time);
                    if (texture != null) {
                        float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                        float y = convertYFromModelToScreen(j) + bottomHeight;
                        batch.draw(texture, x, y);
                    }
                }
            }
        }
    }

    private void drawAnim(Field field, Batch batch, Class<? extends CellObject> clazz, Animation<TextureRegion> anim) {
        // field != null && batch != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                CellObject obj = cell.getFirst(clazz);
                if (obj != null) {
                    TextureRegion texture = anim.getKeyFrame(time);
                    if (texture != null) {
                        float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                        float y = convertYFromModelToScreen(j) + bottomHeight;
                        batch.draw(texture, x, y);
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void drawLadderBottom(Field field, Batch batch) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                int idx = j * Field.WIDTH + i;
                Cell cell = field.cells[idx]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                float t = animTime.get(idx);
                CellObject obj = cell.getFirst(LadderBottom.class);
                if (obj != null) {
                    TextureRegion texture;
                    // get current animation
                    Animation<TextureRegion> animation = animLadders.get(model.stylePack); // assert omitted
                    // find a texture region depending on ladder animation
                    if (!animation.isAnimationFinished(t)) {     // play already started animation
                        t += Gdx.graphics.getDeltaTime();
                        animTime.set(idx, t);
                        texture = animation.getKeyFrame(t);
                    } else if (animatedUsesLadder(field, cell)) {   // start animation here
                        animTime.set(idx, 0);
                        texture = animation.getKeyFrame(0);
                    } else texture = animation.getKeyFrame(0);   // draw static texture
                    // draw texture region
                    if (texture != null) {
                        float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                        float y = convertYFromModelToScreen(j) + bottomHeight;
                        batch.draw(texture, x, y);
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void drawAnimatedObjects(Field field, Batch batch, float dt) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                if (cell.objectExists(CellObjectRaisable.class)) {
                    if (texturesOverlay.containsKey(Box.class))
                        bottomHeight += texturesOverlay.get(Box.class).getRegionHeight();
                }
                for (int k = 0; k < cell.getObjectsCount(); k++) { //  // .... GC!
                    CellObject obj = cell.getObject(k);
                    if (obj instanceof CellObjectAnimated) { // stackoverflow.com/questions/2950319
                        AnimationData<Model.Character> anim = obj instanceof CellObjectActor
                                ? texturesAnim.get(obj.getClass())
                                : texturesAnimWolf.get(obj.getNumber());
                        if (anim != null) {
                            // add dt to our animation to get it up-to-date
                            anim.addDt(dt);

                            // get character ("None" for wolves)
                            Model.Character key = obj.getClass() == Actor1.class ? model.character1 :
                                    obj.getClass() == Actor2.class ? model.character2 : Model.Character.None;

                            // get client-side dx, dy
                            final float dx = anim.speedX * dt, dy = SPEED_Y * dt;

                            // get server-side coordinates
                            float x = convertXFromModelToScreen(i) - (anim.getWidth(key) - bottomWidth) / 2;
                            float y = convertYFromModelToScreen(j) + bottomHeight;

                            // correct x-coordinate and direction adjusted for animation
                            float deltaX = x - anim.x;
                            boolean deltaX_equals_0 = abs(deltaX) < dx / 2;
                            boolean out_of_sync = abs(deltaX) > 2 * CELL_SIZ_W;
                            if (deltaX_equals_0 || out_of_sync) {
                                anim.x = x;
                                anim.setAnimation(AnimationData.AnimationType.Run, false);
                            } else {
                                x = anim.x;
                                anim.setAnimation(AnimationData.AnimationType.Run, true);
                                anim.x += signum(deltaX) * dx;
                                if (abs(deltaX) > CELL_SIZ_W / 2) // if delta is too small it may cause inaccuracy
                                    anim.dirRight = deltaX > 0;
                            }

                            // correct y-coordinate
                            float deltaY = y - anim.y;
                            boolean deltaY_equals_0 = abs(deltaY) < dy / 2;
                            boolean ladder = deltaY > 0 && ladderBottomExists(field, cell)
                                    || deltaY < 0 && ladderTopExists(field, cell);
                            boolean rope = deltaY > 0 && isRopeBelow(field, cell);
                            if (deltaY_equals_0 || out_of_sync) {
                                anim.y = y;
                                anim.setAnimation(AnimationData.AnimationType.Climb, false);
                                anim.setAnimation(AnimationData.AnimationType.Ladder, false);
                            } else if (ladder) {
                                if (abs(y - anim.y) > CELL_SIZ_H / 2)    // modification of "y = anim.y"
                                    y -= signum(deltaY) * CELL_SIZ_H;
                                anim.setAnimation(AnimationData.AnimationType.Ladder, true);
                                anim.y += signum(deltaY) * dy * .5f;
                            } else if (rope) {
                                y = anim.y;
                                anim.setAnimation(AnimationData.AnimationType.Climb, true);
                                anim.y += signum(deltaY) * dy * .5f;
                            } else {
                                y = anim.y;
                                anim.setAnimation(AnimationData.AnimationType.Climb, false);
                                anim.setAnimation(AnimationData.AnimationType.Ladder, false);
                                anim.y += signum(deltaY) * dy;
                            }

                            // if direction == right then draw pure texture, else draw flipped texture
                            TextureRegion texture = anim.getFrame(key);
                            if (anim.dirRight)
                                batch.draw(texture, x, y);
                            else {
                                texture.flip(true, false); // flip is not intensive operation (only affects UV-mapping)
                                batch.draw(texture, x, y);
                                texture.flip(true, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawExplosions(Field field, Batch batch, float dt) {
        // field != null (assert omitted)
        // ....
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                int idx = j * Field.WIDTH + i;
                Cell cell = field.cells[idx]; // cell != NULL (assert omitted)
                if (cell.objectExists(Mine.class))
                    mineIndices.add(idx);
            }
        }
        // ....
        IntSet.IntSetIterator iter = mineIndices.iterator();
        while (iter.hasNext) {
            int idx = iter.next();
            Cell cell = field.cells[idx]; // cell != NULL (assert omitted)
            if (!cell.objectExists(Mine.class)) {
                iter.remove();
                float bottomWidth = getBottomWidth(cell);
                TextureRegion r = animExplosion.getAnimation(null).getKeyFrame(0); // r != NULL (assert omitted)
                animExplosion.x = convertXFromModelToScreen(idx % Field.WIDTH) - (r.getRegionWidth() - bottomWidth) / 2;
                animExplosion.y = convertYFromModelToScreen(idx / Field.WIDTH);
                animExplosion.t = 0;
            }
        }
        // ....
        Animation<TextureRegion> animation = animExplosion.getAnimation(null); // animation != NULL
        if (!animation.isAnimationFinished(animExplosion.t)) {
            TextureRegion texture = animation.getKeyFrame(animExplosion.t);
            if (texture != null)
                batch.draw(texture, animExplosion.x, animExplosion.y);
            animExplosion.t += dt;
        }
    }

    private void drawFlare(Batch batch, float dt) {
        Animation<TextureRegion> animation = animFlare.getAnimation(null); // animation != NULL (assert omitted)
        CellObject actor = model.curActor;
        if (actor != null) {
            // ....
            boolean animationNotStarted = animation.isAnimationFinished(animFlare.t);
            boolean effectExists = actor.getEffect() == Model.Effect.Dazzle;
            if (animationNotStarted && effectExists) {
                animFlare.t = 0;
                actor.setEffect(Model.Effect.None); // server doesn't remove this effect because it's momentary
            }
            // ....
            if (!animation.isAnimationFinished(animFlare.t)) {
                TextureRegion texture = animation.getKeyFrame(animFlare.t);
                batch.draw(texture, 0, 0, Winesaps.WIDTH, Winesaps.HEIGHT);
                animFlare.t += dt;
            }
        }
    }
}
