package ru.mitrakov.self.rush.ui;

import static java.lang.Math.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Gui extends Actor {
    private class MyClickListener extends ClickListener {
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

    private final class AnimInfo {
        float x, y;
        float t;
        boolean dirRight;
        int delay;
        final Animation<TextureRegion> animation;

        AnimInfo(Animation<TextureRegion> animation, boolean dirRight) {
            this.animation = animation;
            this.dirRight = dirRight;
        }
    }

    static final int TOUCH_DELAY = 250;
    private static final int CELL_SIZ_W = 14;
    private static final int CELL_SIZ_H = 85;
    private static final int OFFSET_X = (RushClient.WIDTH - Field.WIDTH * CELL_SIZ_W) / 2; // (800 - 51*14) / 2
    private static final int OFFSET_Y = 33; // inferred by expertise
    private static final float SPEED_X = 1000f * CELL_SIZ_W / TOUCH_DELAY;
    private static final float SPEED_Y = 1000f * CELL_SIZ_H / TOUCH_DELAY;

    private final Model model;
    private final InputController controller;
    private final MyClickListener listener = new MyClickListener();
    private final TextureAtlas atlasDown = new TextureAtlas(Gdx.files.internal("pack/down.pack"));
    private final TextureAtlas atlasUp = new TextureAtlas(Gdx.files.internal("pack/up.pack"));
    private final TextureAtlas atlasAnim = new TextureAtlas(Gdx.files.internal("pack/anim.pack"));
    private final ObjectMap<Class, TextureRegion> texturesDown = new ObjectMap<Class, TextureRegion>(3);
    private final ObjectMap<Class, TextureRegion> texturesUp = new ObjectMap<Class, TextureRegion>(20);
    private final ObjectMap<Class, AnimInfo> texturesAnim = new ObjectMap<Class, AnimInfo>(3);

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
        setWidth(RushClient.WIDTH);
        setHeight(Field.HEIGHT * CELL_SIZ_H);

        Class[] downClasses = new Class[]{Block.class, Dias.class, Water.class};
        Class[] upClasses = new Class[]{Entry1.class, Entry2.class, Apple.class, Pear.class,
                Block.class, LadderTop.class, LadderBottom.class, RopeLine.class, Water.class, Stair.class,
                Mine.class, Umbrella.class, OpenedUmbrella.class, Waterfall.class, WaterfallSafe.class};
        Class[] animClasses = new Class[]{Actor1.class, Actor2.class, Wolf.class};
        for (Class clazz : downClasses) {
            TextureRegion texture = atlasDown.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesDown.put(clazz, texture);
        }
        for (Class clazz : upClasses) {
            TextureRegion texture = atlasUp.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesUp.put(clazz, texture);
        }
        for (Class clazz : animClasses) {
            Array<TextureAtlas.AtlasRegion> frames = atlasAnim.findRegions(clazz.getSimpleName());
            Animation<TextureRegion> animation = new Animation<TextureRegion>(.09f, frames, Animation.PlayMode.LOOP);
            texturesAnim.put(clazz, new AnimInfo(animation, clazz != Actor2.class));
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float dt = Gdx.graphics.getDeltaTime();
        float dx = SPEED_X * dt, dy = SPEED_Y * dt;
        controller.checkInput(listener.getPressedButton(), listener.x, listener.y);

        Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
        if (field != null) {
            // draw 1-st layer (bottom, static objects except food)
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                    float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                    // draw bottom (block/water/dias)
                    if (cell.bottom != null) {
                        if (texturesDown.containsKey(cell.bottom.getClass())) {
                            TextureRegion texture = texturesDown.get(cell.bottom.getClass()); // here texture != null
                            float x = convertXFromModelToScreen(i);
                            float y = convertYFromModelToScreen(j);
                            batch.draw(texture, x, y);
                        }
                    }
                    // draw static objects (except food) above the bottom
                    for (CellObject obj : cell.objects) {
                        if (!(obj instanceof CellObjectFood)) {
                            if (texturesUp.containsKey(obj.getClass())) {
                                TextureRegion texture = texturesUp.get(obj.getClass()); // here texture != null
                                float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                                float y = convertYFromModelToScreen(j) + bottomHeight;
                                batch.draw(texture, x, y);
                            }
                        }
                    }
                }
            }
            // draw 2-nd layer (food)
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                    float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                    for (CellObject obj : cell.objects) {
                        if (obj instanceof CellObjectFood) {
                            if (texturesUp.containsKey(obj.getClass())) {
                                TextureRegion texture = texturesUp.get(obj.getClass()); // here texture != null
                                float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                                float y = convertYFromModelToScreen(j) + bottomHeight;
                                batch.draw(texture, x, y);
                            }
                        }
                    }
                }
            }
            // draw 3-rd layer (animated characters)
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                    float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                    for (CellObject obj : cell.objects) {
                        if (texturesAnim.containsKey(obj.getClass())) {
                            AnimInfo anim = texturesAnim.get(obj.getClass()); // anim != null (assert omitted)
                            TextureRegion texture = anim.animation.getKeyFrame(anim.t); // assert omitted

                            // get non-animated server-side coordinates
                            float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                            float y = convertYFromModelToScreen(j) + bottomHeight;

                            // correct x-coordinate, direction and time adjusted for animation
                            float deltaX = x - anim.x;
                            boolean deltaX_equals_0 = abs(deltaX) < dx / 2;
                            boolean not_initialized = abs(deltaX) > 2 * CELL_SIZ_W;
                            if (deltaX_equals_0 || not_initialized) {
                                anim.x = x;
                                if (anim.delay++ == 10) // time should be stopped within at least 10 loop cycles
                                    anim.t = 0;
                            } else {
                                x = anim.x;
                                anim.x += signum(deltaX) * dx;
                                anim.t += dt;
                                anim.delay = 0;
                                if (abs(deltaX) > CELL_SIZ_W / 2) // if delta is too small it may cause inaccuracy
                                    anim.dirRight = deltaX > 0;
                            }

                            // correct y-coordinate
                            float deltaY = y - anim.y;
                            boolean deltaY_equals_0 = abs(deltaY) < dy / 2;
                            if (deltaY_equals_0 || not_initialized || ladderExists(cell))
                                anim.y = y;
                            else {
                                y = anim.y;
                                anim.y += signum(deltaY) * dy * (deltaY > 0 ? 1 : 2); // fall down is twice faster
                            }

                            // if direction == right then draw pure texture, else draw flipped texture
                            if (anim.dirRight)
                                batch.draw(texture, x, y);
                            else {
                                texture.flip(true, false); // flip is not intensive operation (just affects UV-mapping)
                                batch.draw(texture, x, y);
                                texture.flip(true, false);
                            }
                        }
                    }
                }
            }
            // draw 4-th layer here...
        }
    }

    public void setMovesAllowed(boolean value) {
        controller.setMovesAllowed(value);
    }

    public void dispose() {
        atlasDown.dispose(); // disposing an atlas also disposes all its internal textures
        atlasUp.dispose();
        atlasAnim.dispose();
    }

    private float getBottomWidth(Cell cell) {
        // cell != null (assert omitted because it's called inside 'render()')
        float bottomWidth = CELL_SIZ_W;
        if (cell.bottom != null) {
            if (texturesDown.containsKey(cell.bottom.getClass())) {
                bottomWidth = texturesDown.get(cell.bottom.getClass()).getRegionWidth(); // texture != null
            }
        }
        return bottomWidth;
    }

    private float getBottomHeight(Cell cell) {
        // cell != null (assert omitted because it's called inside 'render()')
        float bottomHeight = 0;
        if (cell.bottom != null) {
            if (texturesDown.containsKey(cell.bottom.getClass())) {
                bottomHeight = texturesDown.get(cell.bottom.getClass()).getRegionHeight(); // texture != null
            }
        }
        return bottomHeight;
    }

    private boolean ladderExists(Cell cell) {
        // cell != null (assert omitted because it's called inside 'render()')
        for (CellObject obj : cell.objects) { // in Java 8 may be replaced with lambda
            if (obj instanceof LadderBottom || obj instanceof  LadderTop)
                return true;
        }
        return false;
    }
}
