package ru.mitrakov.self.rush.screens;

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

class Gui extends Actor {
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

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            System.out.println("Code = " + keycode);
            return super.keyDown(event, keycode);
        }
    }

    private final class AnimInfo {
        float x;
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
    private static final float SPEED = 1000f * CELL_SIZ_W / TOUCH_DELAY;

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

    Gui(Model model) {
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
        float dx = SPEED * dt;
        controller.checkInput(listener.getPressedButton(), listener.x, listener.y);

        Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
        if (field != null) {
            // draw a field
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell must NOT be NULL (assert omitted)
                    // draw bottom (block/water/dias)
                    float bottomWidth = CELL_SIZ_W;
                    float bottomHeight = 0;
                    if (cell.bottom != null) {
                        if (texturesDown.containsKey(cell.bottom.getClass())) {
                            TextureRegion texture = texturesDown.get(cell.bottom.getClass()); // here texture != null
                            float x = convertXFromModelToScreen(i);
                            float y = convertYFromModelToScreen(j);
                            batch.draw(texture, x, y);
                            bottomWidth = texture.getRegionWidth();
                            bottomHeight = texture.getRegionHeight();
                        }
                    }
                    // draw objects above the bottom
                    for (CellObject obj : cell.objects) {
                        // draw static objects
                        if (texturesUp.containsKey(obj.getClass())) {
                            TextureRegion texture = texturesUp.get(obj.getClass()); // here texture != null
                            float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                            float y = convertYFromModelToScreen(j) + bottomHeight;
                            batch.draw(texture, x, y);
                        }
                        // draw animated characters
                        if (texturesAnim.containsKey(obj.getClass())) {
                            AnimInfo anim = texturesAnim.get(obj.getClass()); // info != null (assert omitted)
                            TextureRegion texture = anim.animation.getKeyFrame(anim.t); // assert omitted

                            // get coordinates BY SERVER
                            float x = convertXFromModelToScreen(i) - (texture.getRegionWidth() - bottomWidth) / 2;
                            float y = convertYFromModelToScreen(j) + bottomHeight;

                            // correct x-coordinate, direction and time adjusted for animation
                            float delta = x - anim.x;
                            boolean delta_equals_0 = abs(delta) < dx / 2;
                            boolean not_initialized = abs(delta) > 2 * CELL_SIZ_W;
                            if (delta_equals_0 || not_initialized) {
                                anim.x = x;
                                if (anim.delay++ == 10) // time should be stopped within at least 10 loop cycles
                                    anim.t = 0;
                            } else {
                                x = anim.x;
                                anim.x += signum(delta) * dx;
                                anim.t += dt;
                                anim.delay = 0;
                                if (abs(delta) > CELL_SIZ_W / 2) // if delta is too small it may cause inaccuracy
                                    anim.dirRight = delta > 0;
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
        }
    }

    @Override
    public boolean remove() {
        for (TextureRegion texture : texturesDown.values()) {
            texture.getTexture().dispose();
        }
        for (TextureRegion texture : texturesUp.values()) {
            texture.getTexture().dispose();
        }
        for (AnimInfo anim : texturesAnim.values()) {
            for (TextureRegion texture : anim.animation.getKeyFrames())
                texture.getTexture().dispose();
        }
        atlasDown.dispose();
        atlasUp.dispose();
        atlasAnim.dispose();
        return super.remove();
    }

    void setMovesAllowed(boolean value) {
        controller.setMovesAllowed(value);
    }
}
