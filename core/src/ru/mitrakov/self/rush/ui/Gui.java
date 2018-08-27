package ru.mitrakov.self.rush.ui;

import static java.lang.Math.*;
import static ru.mitrakov.self.rush.model.Model.HurtCause.Exploded;
import static ru.mitrakov.self.rush.model.Model.STYLES_COUNT;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.Cells.*;

/**
 * Gui is the main class to render battle field on the screen
 * <br><b>Remember 2 golden rules:</b>
 * <ul>
 *     <li> render() method MUST been calculated less than 16 ms (to provide 60 FPS)
 *     <li> render() method MUST NOT create new objects to decrease excessive Garbage Collection
 * </ul>
 * Also remember that it's just an actor, so that different screens should create their own instances
 * @author mitrakov
 */
public class Gui extends Actor {
    /**
     * MyClickListener is a simple wrapper over ClickListener that also provides touched XY-coordinates
     * @author mitrakov
     */
    static private final class MyClickListener extends ClickListener {
        /** X mouse position */
        float x = 0;
        /** Y mouse position */
        float y = 0;

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

    /**
     * Simple wrapper over {@link Animation} that also stores time <b>t</b> and XY-coordinates
     */
    static private class AnimInfo {
        /** X-coordinate of animation */
        float x;
        /** Y-coordinate of animation */
        float y;
        /** Time, in seconds */
        float t = BIG_VALUE;
        /** Animation */
        final Animation<TextureRegion> animation;

        /**
         * Creates a new Animation Info instance
         * @param animation LibGdx animation
         */
        private AnimInfo(Animation<TextureRegion> animation) {
            this.animation = animation;
        }
    }

    /** Cell width, in World Units (in our case in pixels) */
    private static final int CELL_SIZ_W = 14;
    /** Cell height, in World Units (in our case in pixels) */
    private static final int CELL_SIZ_H = 85;
    /** Offset by X to draw the battle field */
    private static final int OFFSET_X = (Winesaps.WIDTH - Field.WIDTH * CELL_SIZ_W) / 2; // (800 - 51*14) / 2
    /** Offset by Y to draw the battle field */
    private static final int OFFSET_Y = 33; // inferred by expertise
    /** Moves per second for Actor, defined by Server (e.g. 5 moves/second means that single step duration is 200 ms) */
    private static final int MOVES_PER_SEC = 5;
    /** Moves per second for Wolf, defined by Server (e.g. 4 moves/second means that single step duration is 250 ms) */
    private static final int MOVES_PER_SEC_WOLF = 4;
    /** Animation speed for Actor by X-axis */
    private static final int SPEED_X = CELL_SIZ_W * MOVES_PER_SEC;
    /** Animation speed for Wolf by X-axis */
    private static final int SPEED_X_WOLF = CELL_SIZ_W * MOVES_PER_SEC_WOLF;
    /** Animation speed for Actor by Y-axis */
    private static final int SPEED_Y = CELL_SIZ_H * MOVES_PER_SEC;
    /** Frames per single move */
    private static final int FRAMES_PER_MOVE = 60 / MOVES_PER_SEC; // FPS / MOVES_PER_SEC
    /** Some big value for animation time that means "Animation is off" */
    private static final int BIG_VALUE = 99;

    /** Reference to the model */
    private final Model model;
    /** Input Controller */
    private final InputController controller;
    /** Click listener for this Component */
    private final MyClickListener listener = new MyClickListener();

    /** Array of backgrounds (one background picture per style pack) */
    private final Array<Texture> backgrounds = new Array<Texture>(STYLES_COUNT);
    /** Map [Class -> Textures_for_Bottom_for_each_Stylepack], e.g. "classOf(Dais)" -> "Textures-of-Dais" */
    private final ObjectMap<Class, IntMap<TextureRegion>> texturesDown = new ObjectMap<Class, IntMap<TextureRegion>>(3);
    /** Map [Class -> Textures_for_Static_Objects_for_each_Stylepack], e.g. "classOf(Rope)" -> "Textures-of-Rope" */
    private final ObjectMap<Class, IntMap<TextureRegion>> texturesStat = new ObjectMap<Class, IntMap<TextureRegion>>(9);
    /** Map [Class -> Texture_for_Collectible_Objects], e.g. "classOf(Carrot)" -> "Texture-of-Carrot" */
    private final ObjectMap<Class, TextureRegion> texturesCollectible = new ObjectMap<Class, TextureRegion>(20);
    /** Map [Class -> Texture_for_Overlay_Objects], e.g. "classOf(Umbrella)" -> "Texture-of-Umbrella" */
    private final ObjectMap<Class, TextureRegion> texturesOverlay = new ObjectMap<Class, TextureRegion>(20);
    /** Map [Class -> AnimationData_for_each_Character], e.g. "classOf(Actor1)" -> "AnimationData" */
    private final ObjectMap<Class, AnimationData<Model.Character>> texturesAnim =
            new ObjectMap<Class, AnimationData<Model.Character>>(2);
    /** The same as {@link #texturesDown}, but without grass on top of the texture (to avoid grass-on-grass effect) */
    private final IntMap<TextureRegion> texturesDownSolid = new IntMap<TextureRegion>(STYLES_COUNT);
    /** The same as {@link #texturesStat}, but for Water (since 2.0.0 we have to draw water in a separate layer) */
    private final IntMap<TextureRegion> texturesWaterUp = new IntMap<TextureRegion>(STYLES_COUNT);
    /** The same as {@link #texturesDown}, but for Water (since 2.0.0 we have to draw water in a separate layer) */
    private final IntMap<TextureRegion> texturesWaterDown = new IntMap<TextureRegion>(STYLES_COUNT);
    /** The same as {@link #texturesAnim}, but for Wolves (if wolves > 1, then we can't map AnimationData to a class) */
    private final IntMap<AnimationData<Model.Character>> texturesAnimWolf =
            new IntMap<AnimationData<Model.Character>>(100);
    /** The same as {@link #texturesStat}, but for {@link LadderBottom LadderBottoms}, because they are animated */
    private final IntMap<Animation<TextureRegion>> animLadders = new IntMap<Animation<TextureRegion>>(STYLES_COUNT);
    /** The same as {@link #texturesStat}, but for {@link DecorationDynamic}, because they are animated */
    private final IntMap<Animation<TextureRegion>> decorations = new IntMap<Animation<TextureRegion>>(STYLES_COUNT);
    /** Animation time for all Ladders. "animTime[i]" stores time position of animation for the Ladder at "cells[i]" */
    private final FloatArray animTime = new FloatArray(Field.HEIGHT * Field.WIDTH);
    /** Animation for {@link Waterfall Waterfall} */
    private final Animation<TextureRegion> animWaterfall;
    /** Animation for {@link Waterfall Waterfall}, but when an actor established an {@link Umbrella Umbrella} in it */
    private final Animation<TextureRegion> animWaterfallSmall;
    /** Animation for {@link Antidote Antidote} */
    private final Animation<TextureRegion> animAntidote;
    /** Animation for {@link Teleport Teleport} */
    private final Animation<TextureRegion> animTeleport;
    /** Animation for {@link Flashbang Flashbang} */
    private final Animation<TextureRegion> animFlashbang;
    /** Animation for {@link Detector Mines Detector} */
    private final Animation<TextureRegion> animDetector;
    /** Animation for Aura (around an actor that appears during 3 seconds) */
    private final AnimInfo animAura;
    /** Animation for Flare (when the enemy uses {@link Flashbang Flashbang} against us) */
    private final AnimInfo animFlare;
    /** Animation for Explosion (when an actor suddenly trips a {@link Mine Mine}) */
    private final AnimInfo animExplosion;
    /** Animation for Smoke (that appears on actor's death) */
    private final AnimInfo animSmoke;
    /** "Antidote Activated" balloon (when an actor eats {@link Antidote Antidote}) */
    private final TextureRegion textureAntidote;
    /** "?" balloon (when we use {@link Flashbang Flashbang} against the enemy to show the fact he's dazzled) */
    private final TextureRegion textureDazzle;

    /** Global frames counter */
    private long frameNumber = 0;
    /** Number of frame (counted by {@link #frameNumber}) when the {@link InputController} checked last move (this is
     * needed to forbid InputController handle events during {@link #FRAMES_PER_MOVE} frames) */
    private long lastMoveFrame = -FRAMES_PER_MOVE;
    /** Global time counter, in seconds */
    private float time = 0;
    /** Countdown "timer" for enemy Dazzle effect (it is set up at 3.0 sec and runs out each frame until becomes 0) */
    private float enemyDazzleEffectTime = 0;

    /**
     * Static method to convert Server-based X-coordinate to GUI-based screen X-coordinate
     * @param x Server-based X-coordinate (0 to {@link Field#WIDTH})
     * @return GUI-based X-coordinate
     * @see #convertXFromScreenToModel(float)
     */
    private static float convertXFromModelToScreen(int x) {
        return x * CELL_SIZ_W + OFFSET_X;
    }

    /**
     * Static method to convert Server-based Y-coordinate to GUI-based screen Y-coordinate
     * @param y Server-based Y-coordinate (0 to {@link Field#HEIGHT})
     * @return GUI-based Y-coordinate
     * @see #convertYFromScreenToModel(float)
     */
    private static float convertYFromModelToScreen(int y) {
        return (Field.HEIGHT - y) * CELL_SIZ_H - OFFSET_Y;
    }

    /**
     * Static method to convert GUI-based screen X-coordinate to Server-based X-coordinate
     * @param x GUI-based X-coordinate
     * @return Server-based X-coordinate (0 to {@link Field#WIDTH})
     * @see #convertXFromModelToScreen(int)
     */
    static int convertXFromScreenToModel(float x) {
        return (int) ((x - OFFSET_X) / CELL_SIZ_W);
    }

    /**
     * Static method to convert GUI-based screen Y-coordinate to Server-based Y-coordinate
     * @param y GUI-based Y-coordinate
     * @return Server-based Y-coordinate (0 to {@link Field#HEIGHT})
     * @see #convertYFromModelToScreen(int)
     */
    static int convertYFromScreenToModel(float y) {
        return (int) (Field.HEIGHT - y / CELL_SIZ_H);
    }

    /**
     * Creates a new instance of Gui
     * @param model model (NON-NULL)
     * @param assetManager asset manager (NON-NULL)
     */
    public Gui(Model model, AssetManager assetManager) {
        assert model != null && assetManager != null;
        this.model = model;
        controller = new InputController(model);
        addListener(listener);

        // in theory width must be "Field.WIDTH * CELL_SIZ_W", but we use full width for convenience on touch screens
        setWidth(Winesaps.WIDTH);
        setHeight(Field.HEIGHT * CELL_SIZ_H);

        // down textures (block, dias), each with 4 styles (since 2.0.0: water was excluded)
        TextureAtlas atlasDown = assetManager.get("pack/down.pack");
        for (Class clazz : new Class[]{Block.class, Dais.class/*, Water.class*/}) {
            IntMap<TextureRegion> m = new IntMap<TextureRegion>(STYLES_COUNT); // .... GC!
            for (int i = 0; i < STYLES_COUNT; i++) {
                String key = clazz.getSimpleName() + i;
                TextureRegion texture = atlasDown.findRegion(key);
                if (texture != null)
                    m.put(i, texture);
            }
            texturesDown.put(clazz, m);
        }
        // down solid textures (4 styles)
        for (int i = 0; i < STYLES_COUNT; i++) {
            TextureRegion texture = atlasDown.findRegion(String.format(Locale.getDefault(), "Block%dsolid", i));
            if (texture != null)
                texturesDownSolid.put(i, texture);
        }
        // down water textures (4 styles, since 2.0.0)
        for (int i = 0; i < STYLES_COUNT; i++) {
            String key = Water.class.getSimpleName() + i;
            TextureRegion texture = atlasDown.findRegion(key);
            if (texture != null)
                texturesWaterDown.put(i, texture);
        }
        // static up textures, each with 4 styles (since 2.0.0: water was excluded)
        TextureAtlas atlasUp = assetManager.get("pack/up.pack");
        for (Class clazz : new Class[]{Block.class, LadderTop.class, RopeLine.class, /*Water.class,*/
                DecorationStatic.class, DecorationWarning.class, DecorationDanger.class, Stair.class}) {
            IntMap<TextureRegion> m = new IntMap<TextureRegion>(STYLES_COUNT); // .... GC!
            for (int i = 0; i < STYLES_COUNT; i++) {
                String key = clazz.getSimpleName() + i;
                TextureRegion texture = atlasUp.findRegion(key);
                if (texture != null)
                    m.put(i, texture);
            }
            texturesStat.put(clazz, m);
        }
        // up water textures (4 styles, since 2.0.0)
        for (int i = 0; i < STYLES_COUNT; i++) {
            String key = Water.class.getSimpleName() + i;
            TextureRegion texture = atlasUp.findRegion(key);
            if (texture != null)
                texturesWaterUp.put(i, texture);
        }
        // collectible textures
        for (Class clazz : new Class[]{
                Apple.class, Pear.class, Meat.class, Carrot.class, Nut.class, Mushroom.class,
                UmbrellaThing.class, MineThing.class, BeamThing.class, AntidoteThing.class, FlashbangThing.class,
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
            AnimationData<Model.Character> data = new AnimationData<Model.Character>(SPEED_X);
            for (Model.Character character : Model.characterValues) {
                if (character != Model.Character.None) {
                    String filename = String.format("pack/%s.pack", character.name().toLowerCase());
                    data.add(character, assetManager.<TextureAtlas>get(filename), .07f);
                }
            }
            texturesAnim.put(clazz, data);
        }
        // wolf
        for (int i = 0; i < 100; i++) {
            AnimationData<Model.Character> data = new AnimationData<Model.Character>(SPEED_X_WOLF);
            data.add(Model.Character.None, assetManager.<TextureAtlas>get("pack/wolf.pack"), .09f);
            texturesAnimWolf.put(i, data);
        }
        // animated objects (waterfalls, antidotes, teleports)
        TextureAtlas atlasAnimated = assetManager.get("pack/animated.pack");
        TextureAtlas atlasEffects = assetManager.get("pack/effects.pack");
        TextureAtlas atlasFlare = assetManager.get("pack/flare.pack");
        TextureAtlas atlasAura = assetManager.get("pack/aura.pack");

        Array<TextureAtlas.AtlasRegion> framesWaterfall = atlasAnimated.findRegions("Waterfall");
        Array<TextureAtlas.AtlasRegion> framesWaterfallSmall = atlasAnimated.findRegions("WaterfallSmall");
        Array<TextureAtlas.AtlasRegion> framesAntidote = atlasAnimated.findRegions("Antidote");
        Array<TextureAtlas.AtlasRegion> framesTeleport = atlasAnimated.findRegions("Teleport");
        Array<TextureAtlas.AtlasRegion> framesFlashbang = atlasAnimated.findRegions("Flashbang");
        Array<TextureAtlas.AtlasRegion> framesDetector = atlasAnimated.findRegions("Detector");
        Array<TextureAtlas.AtlasRegion> framesExplosion = atlasEffects.findRegions("Explosion");
        Array<TextureAtlas.AtlasRegion> framesSmoke = atlasEffects.findRegions("Smoke");
        Array<TextureAtlas.AtlasRegion> framesFlare = atlasFlare.findRegions("Flare");
        Array<TextureAtlas.AtlasRegion> framesAura = atlasAura.findRegions("Aura");

        animWaterfall = new Animation<TextureRegion>(.09f, framesWaterfall, Animation.PlayMode.LOOP);
        animWaterfallSmall = new Animation<TextureRegion>(.09f, framesWaterfallSmall, Animation.PlayMode.LOOP);
        animAntidote = new Animation<TextureRegion>(.15f, framesAntidote, Animation.PlayMode.LOOP_PINGPONG);
        animTeleport = new Animation<TextureRegion>(.09f, framesTeleport, Animation.PlayMode.LOOP);
        animFlashbang = new Animation<TextureRegion>(.09f, framesFlashbang, Animation.PlayMode.LOOP);
        animDetector = new Animation<TextureRegion>(.15f, framesDetector, Animation.PlayMode.LOOP);

        animAura = new AnimInfo(new Animation<TextureRegion>(.09f, framesAura));
        animExplosion = new AnimInfo(new Animation<TextureRegion>(.06f, framesExplosion));
        animSmoke = new AnimInfo(new Animation<TextureRegion>(.06f, framesSmoke));
        animFlare = new AnimInfo(new Animation<TextureRegion>(.09f, framesFlare));
        //
        TextureAtlas atlasLadder = assetManager.get("pack/ladder.pack");
        TextureAtlas atlasDecors = assetManager.get("pack/decor.pack");
        for (int i = 0; i < STYLES_COUNT; i++) {
            // ladderBottom animations
            Array<TextureAtlas.AtlasRegion> frames = atlasLadder.findRegions(LadderBottom.class.getSimpleName() + i);
            animLadders.put(i, new Animation<TextureRegion>(.05f, frames));
            // dynamic decorations
            Array<TextureAtlas.AtlasRegion> frms = atlasDecors.findRegions(DecorationDynamic.class.getSimpleName() + i);
            decorations.put(i, new Animation<TextureRegion>(.07f, frms, Animation.PlayMode.LOOP));
            // backgrounds
            backgrounds.add(assetManager.<Texture>get(String.format(Locale.getDefault(), "back/battle%d.jpg", i)));
        }

        // fill animation time
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                animTime.add(BIG_VALUE);
            }
        }

        // simple textures
        textureAntidote = atlasEffects.findRegion("Antidote");
        textureDazzle = atlasEffects.findRegion("Dazzle");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        controller.checkInput();
        if (++frameNumber - lastMoveFrame >= FRAMES_PER_MOVE) {
            if (controller.checkMovement(listener.getPressedButton(), listener.x, listener.y))
                lastMoveFrame = frameNumber;
        }

        batch.draw(backgrounds.get(model.stylePack), 0, 0);

        float dt = Gdx.graphics.getDeltaTime();
        time += dt;

        Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
        if (field != null) {
            // draw 1-st layer (restrictive walls, with one of 4 styles)
            drawEdgeWalls(field, batch);
            // draw 2-st layer (bottom (block/water/dias) with one of 4 styles)
            drawBottom(field, batch);
            // draw 3-nd layer (static objects)
            drawObjects(field, batch);
            // draw 4-rd layer (dynamic decorations)
            drawDynamicDecorations(field, batch);
            // draw 5-rd layer (LadderBottom objects)
            drawLadderBottom(field, batch);
            // draw 6-th layer (waterfalls)
            drawWaterfalls(field, batch);
            // draw 7-th layer (collectible objects)
            drawObjects(field, batch, texturesCollectible);
            // draw 8-th layer (antidotes, teleports)
            drawAnim(field, batch, Antidote.class, animAntidote);
            drawAnim(field, batch, Teleport.class, animTeleport);
            drawAnim(field, batch, Flashbang.class, animFlashbang);
            drawAnim(field, batch, Detector.class, animDetector);
            // draw 9-th layer (animated characters)
            drawAnimatedObjects(field, batch, dt);
            // draw 10-th layer (water, and also redraw walls near the water ONCE AGAIN, see note#11)
            drawWater(field, batch);
            drawWallsNearWater(field, batch);
            // draw 10-th layer (all overlaying objects like Umbrella)
            drawObjects(field, batch, texturesOverlay);
            // draw 11-th layer (smokes, explosions, aura)
            drawSingleAnim(animExplosion, batch, dt);
            drawSingleAnim(animSmoke, batch, dt);
            drawSingleAnim(animAura, batch, dt);
            // draw last layer (flare)
            drawFlare(batch, dt);
        }
    }

    /**
     * Handles model's events from the Event Bus
     * @param event event (may be NULL)
     */
    public void handleEvent(EventBus.Event event) {
        if (event instanceof EventBus.MoveResponseEvent || event instanceof EventBus.ConnectedChangeEvent)
            controller.setNextMoveAllowed(true);
        if (event instanceof EventBus.PlayerWoundedEvent) {
            EventBus.PlayerWoundedEvent ev = (EventBus.PlayerWoundedEvent) event;
            Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
            if (field != null && ev.cause != Exploded) {
                float bottomWidth = getBottomWidth(field.cells[ev.xy]);
                TextureRegion r = animSmoke.animation.getKeyFrame(0);
                animSmoke.x = convertXFromModelToScreen(ev.xy % Field.WIDTH) - (r.getRegionWidth() - bottomWidth) / 2;
                animSmoke.y = convertYFromModelToScreen(ev.xy / Field.WIDTH);
                animSmoke.t = 0; // start animation
            }
        }
        if (event instanceof EventBus.ObjectRemovedEvent) {
            EventBus.ObjectRemovedEvent ev = (EventBus.ObjectRemovedEvent) event;
            Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
            if (ev.obj instanceof Mine && field != null) {
                int xy = ev.oldXy;
                float botWidth = getBottomWidth(field.cells[xy]);
                TextureRegion r = animExplosion.animation.getKeyFrame(0);
                animExplosion.x = convertXFromModelToScreen(xy % Field.WIDTH) - (r.getRegionWidth() - botWidth) / 2;
                animExplosion.y = convertYFromModelToScreen(xy / Field.WIDTH);
                animExplosion.t = 0; // start animation
            }
        }
        if (event instanceof EventBus.NewFieldEvent) {
            EventBus.NewFieldEvent ev = (EventBus.NewFieldEvent) event;
            // play "aura" animation around the actor
            float bottomHeight = getBottomHeight(ev.field.cells[ev.actor.getXy()]); // ev.field, ev.actor != null
            TextureRegion r = animExplosion.animation.getKeyFrame(0);
            animAura.x = convertXFromModelToScreen(ev.actor.getX()) - (.5f * r.getRegionWidth());
            animAura.y = convertYFromModelToScreen(ev.actor.getY()) - CELL_SIZ_H / 2 + bottomHeight;
            animAura.t = 0; // start animation
            // also allow nextMove (in case of re-connections)
            controller.setNextMoveAllowed(true);
        }
        if (event instanceof EventBus.ActorResetEvent) {
            EventBus.ActorResetEvent ev = (EventBus.ActorResetEvent) event;
            CellObject actor = ev.obj;
            if (actor != null) {
                AnimationData anim = texturesAnim.get(actor.getClass());
                if (anim != null)
                    anim.reset = true;
            }
        }
    }

    // =======================
    // === PUBLIC  METHODS ===
    // =======================
    // put all public methods here
    // =======================

    /**
     * Turns movements input listening on/off
     * @param value true to allow movements
     */
    public void setMovesAllowed(boolean value) {
        controller.setMovesAllowed(value);
    }

    // ====================================
    // === PRIVATE DRAW HELPERS METHODS ===
    // ====================================
    // put all helpers methods here
    // ====================================

    /**
     * @param cell cell (NON-NULL)
     * @return bottom's texture width (default is CELL_SIZ_W)
     */
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

    /**
     * @param cell cell (NON-NULL)
     * @return bottom's texture height (default is CELL_SIZ_W)
     */
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

    /**
     * @param field battle field (NON-NULL)
     * @param cell cell to check (NON-NULL)
     * @return true, if the cell below has a rope line
     */
    private boolean isRopeBelow(Field field, Cell cell) {
        // field != NULL, cell != NULL
        Cell cellBelow = cell.xy + Field.WIDTH < Field.WIDTH * Field.HEIGHT ? field.cells[cell.xy + Field.WIDTH] : null;
        return cellBelow != null && cellBelow.objectExists(RopeLine.class);
    }

    /**
     * @param field battle field (NON-NULL)
     * @param cell cell to check (NON-NULL)
     * @return true, if the cell has a LadderBottom object
     */
    private boolean ladderBottomExists(Field field, Cell cell) {
        // field != NULL, cell != NULL
        Cell cellBelow = cell.xy + Field.WIDTH < Field.WIDTH * Field.HEIGHT ? field.cells[cell.xy + Field.WIDTH] : null;
        return cellBelow != null && cell.objectExists(LadderTop.class) && cellBelow.objectExists(LadderBottom.class);
    }

    /**
     * @param field battle field (NON-NULL)
     * @param cell cell to check (NON-NULL)
     * @return true, if the cell has a LadderTop object
     */
    private boolean ladderTopExists(Field field, Cell cell) {
        // field != NULL, cell != NULL
        Cell cellAbove = cell.xy - Field.WIDTH >= 0 ? field.cells[cell.xy - Field.WIDTH] : null;
        return cellAbove != null && cellAbove.objectExists(LadderTop.class) && cell.objectExists(LadderBottom.class);
    }

    /**
     * @param field battle field (NON-NULL)
     * @param cell cell to check (NON-NULL)
     * @return true, if the animated object (Actor1, Actor2 or Wolf) uses a ladder (moves up or down)
     */
    private boolean animatedUsesLadder(Field field, Cell cell) {
        // field != null (assert omitted)
        CellObject animated = cell.getFirst(CellObjectAnimated.class);
        if (animated == null) { // maybe animatedObject is above?
            Cell cellAbove = cell.xy - Field.WIDTH >= 0 ? field.cells[cell.xy - Field.WIDTH] : null;
            if (cellAbove != null)
                animated = cellAbove.getFirst(CellObjectAnimated.class);
        }
        if (animated != null) {
            AnimationData anim = animated instanceof CellObjectActor
                    ? texturesAnim.get(animated.getClass())
                    : texturesAnimWolf.get(animated.getNumber());
            if (anim != null)
                return anim.getAnimation() == AnimationData.AnimationType.Ladder;
        }
        return false;
    }

    /**
     * Checks whether the given row contains any object
     * @param field battle field (NON-NULL)
     * @param y Y-coordinate
     * @return TRUE, if and only if the given row contains any object
     * @see #anythingExistsOnRowBottom(Field, int, boolean)
     */
    private boolean anythingExistsOnRow(Field field, int y, boolean leftSide) {
        int start = leftSide ? 0 : Field.WIDTH / 2, end = leftSide ? Field.WIDTH / 2 : Field.WIDTH;
        for (int i = start; i < end; i++) {
            Cell cell = field.cells[y * Field.WIDTH + i]; // cell != NULL (assert omitted)
            if (cell.getObjectsCount() > 0)
                return true;
        }
        return false;
    }

    /**
     * Checks whether the given row contains at least 1 non-empty bottom
     * @param field battle field (NON-NULL)
     * @param y Y-coordinate
     * @return TRUE, if and only if the given row contains at least one non-empty bottom
     * @see #anythingExistsOnRow(Field, int, boolean)
     */
    private boolean anythingExistsOnRowBottom(Field field, int y, boolean leftSide) {
        int start = leftSide ? 0 : Field.WIDTH / 2, end = leftSide ? Field.WIDTH / 2 : Field.WIDTH;
        for (int i = start; i < end; i++) {
            Cell cell = field.cells[y * Field.WIDTH + i]; // cell != NULL (assert omitted)
            if (cell.bottom != null)
                return true;
        }
        return false;
    }

    // ============================
    // === PRIVATE DRAW METHODS ===
    // ============================
    // put all the draw methods here
    // ============================

    /**
     * Draws static objects according to a given map (NOT CONSIDER Style Packs)
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     * @param map map that matches object classes to their textures
     */
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
                            if (obj instanceof BeamChunk)
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

    /**
     * Draws cells bottom (block, dais)
     * <br><b>Note:</b> since 2.0.0 we draw bottom water textures separately
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     */
    private void drawBottom(Field field, Batch batch) {
        // field != null && batch != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                TextureRegion texture = null;
                if (cell.bottom instanceof Block && (cell.objectExists(Block.class) || cell.objectExists(Stair.class)))
                    texture = texturesDownSolid.get(model.stylePack); // to avoid "grass-on-grass" artifacts
                else {
                    Class key = null;
                    if (cell.bottom != null)
                        key = cell.bottom.getClass();
                    else { // case: long RopeLine
                        Cell below = (j + 1 < Field.HEIGHT) ? field.cells[(j + 1) * Field.WIDTH + i] : null;
                        if (below != null && cell.objectExists(RopeLine.class) && below.objectExists(RopeLine.class))
                            key = Block.class;
                    }
                    if (key != null && texturesDown.containsKey(key))
                        texture = texturesDown.get(key).get(model.stylePack);
                }
                if (texture != null) {
                    float x = convertXFromModelToScreen(i);
                    float y = convertYFromModelToScreen(j);
                    batch.draw(texture, x, y);
                }
            }
        }
    }

    /**
     * Draws restrictive virtual walls on the left and the right side of the battlefield (they DON'T exists on Server!)
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     * @since 2.0.0
     */
    private void drawEdgeWalls(Field field, Batch batch) {
        // field != null && batch != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            boolean needDrawUpLeft = anythingExistsOnRow(field, j, true);
            boolean needDrawUpRight = anythingExistsOnRow(field, j, false);
            boolean needDrawDownLeft = anythingExistsOnRowBottom(field, j, true);
            boolean needDrawDownRight = anythingExistsOnRowBottom(field, j, false);
            for (int i = -2; i < Field.WIDTH + 2; i++) {
                if (i < 0 || i >= Field.WIDTH) {
                    boolean needDrawUp = i < 0 ? needDrawUpLeft : needDrawUpRight;
                    boolean needDrawDown = i < 0 ? needDrawDownLeft : needDrawDownRight;
                    IntMap<TextureRegion> m = texturesStat.get(Block.class);
                    if (m != null) {
                        TextureRegion textureDown = texturesDownSolid.get(model.stylePack);
                        TextureRegion textureUp = m.get(model.stylePack);
                        if (textureDown != null && textureUp != null) {
                            float bottomWidth = textureDown.getRegionWidth();
                            float bottomHeight = textureDown.getRegionHeight();
                            float x1 = convertXFromModelToScreen(i);
                            float y1 = convertYFromModelToScreen(j);
                            float x2 = convertXFromModelToScreen(i) - (textureUp.getRegionWidth() - bottomWidth) / 2;
                            float y2 = convertYFromModelToScreen(j) + bottomHeight;
                            if (needDrawDown)
                                batch.draw(textureDown, x1, y1);
                            if (needDrawUp)
                                batch.draw(textureUp, x2, y2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws static objects according to texturesStat map and considering the current Style Pack
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     */
    private void drawObjects(Field field, Batch batch) {
        // field != null && batch != null (assert omitted)
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

    /**
     * Draws water
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     * @since 2.0.0
     */
    private void drawWater(Field field, Batch batch) {
        // field != null && batch != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                if (cell.bottom instanceof Water) {
                    TextureRegion texture = texturesWaterDown.get(model.stylePack);
                    if (texture != null) {
                        float x = convertXFromModelToScreen(i);
                        float y = convertYFromModelToScreen(j);
                        batch.draw(texture, x, y);
                    }
                }
                if (cell.objectExists(Water.class)) {
                    TextureRegion texture = texturesWaterUp.get(model.stylePack);
                    if (texture != null) {
                        float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                        float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                        float y = convertYFromModelToScreen(j) + bottomHeight;
                        batch.draw(texture, x, y);
                    }
                }
            }
        }
    }

    /**
     * Draws walls up textures located next to the water up textures
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     * @since 2.0.0
     */
    private void drawWallsNearWater(Field field, Batch batch) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 1; i < Field.WIDTH - 1; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                Cell cellR = field.cells[j * Field.WIDTH + i + 1]; // cell != NULL (assert omitted)
                Cell cellL = field.cells[j * Field.WIDTH + i - 1]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                boolean nearWater = cellL.objectExists(Water.class) || cellR.objectExists(Water.class);
                if (cell.objectExists(Block.class) && nearWater) {
                    IntMap<TextureRegion> m = texturesStat.get(Block.class);
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

    /**
     * Draws animated waterfalls
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     */
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

    /**
     * Draws animated decorations according to the current Style Pack
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     */
    private void drawDynamicDecorations(Field field, Batch batch) {
        // field != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                CellObject obj = cell.getFirst(DecorationDynamic.class);
                if (obj != null) {
                    Animation<TextureRegion> animation = decorations.get(model.stylePack);
                    if (animation != null) {
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
    }

    /**
     * Draw the given animation considering current "time" variable
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     * @param clazz CellObject class
     * @param anim animation
     */
    private void drawAnim(Field field, Batch batch, Class<? extends CellObject> clazz, Animation<TextureRegion> anim) {
        // field != null && batch != null (assert omitted)
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                if (cell.objectExists(clazz)) {
                    float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
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

    /**
     * Draws animated LadderBottom objects
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     */
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
                    if (!animation.isAnimationFinished(t)) {      // play already started animation
                        t += Gdx.graphics.getDeltaTime();
                        animTime.set(idx, t);
                        texture = animation.getKeyFrame(t);
                    } else if (animatedUsesLadder(field, cell)) { // start animation here
                        animTime.set(idx, 0);
                        texture = animation.getKeyFrame(0);
                    } else texture = animation.getKeyFrame(0);    // draw static texture
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

    /**
     * Draws animated objects (Actor1, Actor2, Wolf)
     * @param field battle field (NON-NULL)
     * @param batch OpenGL sprite batch (NON-NULL)
     * @param dt current delta time
     */
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
                } else if (cell.bottom instanceof Water && !cell.objectExists(BeamChunk.class)) {
                    float coeff = cell.objectExists(Wolf.class) ? 1.8f : 1; // because wolves are taller
                    bottomHeight *= -coeff; // animated objects go deep in water up to the head (since 2.0.0)
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
                            boolean out_of_sync = abs(deltaX) > 2 * CELL_SIZ_W || anim.reset;
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

                            // "reset" is not actual anymore
                            if (anim.reset) anim.reset = false;

                            // if direction == right then draw pure texture, else draw flipped texture
                            TextureRegion texture = anim.getFrame(key);
                            if (texture != null) {
                                if (anim.dirRight)
                                    batch.draw(texture, x, y);
                                else {
                                    texture.flip(true, false); // flip is not intensive operation (affects UV-mapping)
                                    batch.draw(texture, x, y);
                                    texture.flip(true, false);
                                }
                            }

                            // draw antidote balloon
                            if (obj.getEffect() == Model.Effect.Antidote) {
                                float yy = y + CELL_SIZ_H / 2;
                                if (anim.dirRight)
                                    batch.draw(textureAntidote, x - (.5f * textureAntidote.getRegionWidth()), yy);
                                else {
                                    textureAntidote.flip(true, false);
                                    batch.draw(textureAntidote, x + 10, yy);
                                    textureAntidote.flip(true, false);
                                }
                            }
                            // draw dazzle balloon
                            if (obj == model.enemyActor) {
                                if (obj.getEffect() == Model.Effect.Dazzle) {
                                    obj.setEffect(Model.Effect.None);
                                    enemyDazzleEffectTime = 3.0f;
                                }
                                if (enemyDazzleEffectTime > 0) {
                                    enemyDazzleEffectTime -= dt;
                                    float xx = x - (.5f * textureDazzle.getRegionWidth());
                                    float yy = y + CELL_SIZ_H / 2;
                                    batch.draw(textureDazzle, xx, yy);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws single animation from the beginning up to the end
     * @param anim animation
     * @param batch OpenGL sprite batch
     * @param dt current delta time
     */
    private void drawSingleAnim(AnimInfo anim, Batch batch, float dt) {
        // anim != null && batch != null (assert omitted)
        Animation<TextureRegion> animation = anim.animation; // animation != NULL (assert omitted)
        if (!animation.isAnimationFinished(anim.t)) {
            TextureRegion texture = animation.getKeyFrame(anim.t);
            if (texture != null)
                batch.draw(texture, anim.x, anim.y);
            anim.t += dt;
        }
    }

    /**
     * Draws full screen flare effect (ensure it is drawn after all other layers)
     * @param batch OpenGL sprite batch
     * @param dt current delta time
     */
    private void drawFlare(Batch batch, float dt) {
        Animation<TextureRegion> animation = animFlare.animation; // animation != NULL (assert omitted)
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

// note#6 (@mitrakov, 2017-06-29): NOT ACTUAL ANYMORE (2017-07-22)

// note#11 (@mitrakov, 2018-05-07): since 2.0.0, I decided instead of having swimming animation, just let a character
// sink a bit into water, and draw water textures above the character; as a result we could see only the head of the
// character. But there is a problem: when a character comes too close to a wall, located next to water, we can see its
// arms, legs and so on! It happens because the walls are already drawn. The obvious solution to draw walls after the
// animated objects DOESN'T work, because the vicious cycle appears:
// 1) we want static objects above the walls
// 2) we want animated objects above the static objects
// 3) we want walls above the animated objects
// Hence the most easiest way is just to determine walls next to water and redraw them once again after drawing the
// animated characters
