package ru.mitrakov.self.rush.ui;

import static java.lang.Math.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
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
    static private class MyClickListener extends ClickListener {
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

    static private final class AnimInfo {
        float x, y;
        float t;
        boolean dirRight;
        int delay;
        final ObjectMap<Model.Character, Animation<TextureRegion>> animations;

        AnimInfo(ObjectMap<Model.Character, Animation<TextureRegion>> animations, boolean dirRight) {
            this.animations = animations;
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
    private final TextureAtlas atlasRabbit = new TextureAtlas(Gdx.files.internal("pack/rabbit.pack"));
    private final TextureAtlas atlasHedgehog = new TextureAtlas(Gdx.files.internal("pack/hedgehog.pack"));
    private final TextureAtlas atlasSquirrel = new TextureAtlas(Gdx.files.internal("pack/squirrel.pack"));
    private final TextureAtlas atlasCat = new TextureAtlas(Gdx.files.internal("pack/cat.pack"));
    private final Texture background = new Texture(Gdx.files.internal("back/back0.jpg"));
    private final ObjectMap<Class, TextureRegion> texturesDown = new ObjectMap<Class, TextureRegion>(3);
    private final ObjectMap<Class, TextureRegion> texturesUp = new ObjectMap<Class, TextureRegion>(20);
    private final ObjectMap<Class, AnimInfo> texturesAnim = new ObjectMap<Class, AnimInfo>(3);
    private final ObjectSet<Class> setStatic = new ObjectSet<Class>() {{
        addAll(Block.class, LadderTop.class, LadderBottom.class, RopeLine.class, Water.class, Stair.class, Mine.class,
                Umbrella.class, Waterfall.class, WaterfallSafe.class);
    }};
    private final ObjectSet<Class> setFood = new ObjectSet<Class>() {{
        addAll(Apple.class, Pear.class, Meat.class, Carrot.class, Nut.class, Mushroom.class);
    }};
    private final ObjectSet<Class> setUmbrella = new ObjectSet<Class>(1) {{
        add(OpenedUmbrella.class);
    }};

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

        ObjectSet<Class> upClasses = new ObjectSet<Class>() {{
            addAll(setStatic);
            addAll(setFood);
            addAll(setUmbrella);
        }};

        for (Class clazz : new Class[]{Block.class, Dias.class, Water.class}) {
            TextureRegion texture = atlasDown.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesDown.put(clazz, texture);
        }
        for (Class clazz : upClasses) {
            TextureRegion texture = atlasUp.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesUp.put(clazz, texture);
        }
        for (Class clazz : new Class[]{Actor1.class, Actor2.class, Wolf.class}) {
            ObjectMap<Model.Character, Animation<TextureRegion>> animations =
                    new ObjectMap<Model.Character, Animation<TextureRegion>>(4);

            Array<TextureAtlas.AtlasRegion> framesRabbit = atlasRabbit.findRegions(clazz.getSimpleName());
            Array<TextureAtlas.AtlasRegion> framesHedgehog = atlasHedgehog.findRegions(clazz.getSimpleName());
            Array<TextureAtlas.AtlasRegion> framesSquirrel = atlasSquirrel.findRegions(clazz.getSimpleName());
            Array<TextureAtlas.AtlasRegion> framesCat = atlasCat.findRegions(clazz.getSimpleName());

            Animation<TextureRegion> an1 = new Animation<TextureRegion>(.09f, framesRabbit, Animation.PlayMode.LOOP);
            Animation<TextureRegion> an2 = new Animation<TextureRegion>(.09f, framesHedgehog, Animation.PlayMode.LOOP);
            Animation<TextureRegion> an3 = new Animation<TextureRegion>(.09f, framesSquirrel, Animation.PlayMode.LOOP);
            Animation<TextureRegion> an4 = new Animation<TextureRegion>(.09f, framesCat, Animation.PlayMode.LOOP);

            animations.put(Model.Character.Rabbit, an1);
            animations.put(Model.Character.Hedgehog, an2);
            animations.put(Model.Character.Squirrel, an3);
            animations.put(Model.Character.Cat, an4);

            texturesAnim.put(clazz, new AnimInfo(animations, clazz != Actor2.class));
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float dt = Gdx.graphics.getDeltaTime();
        float dx = SPEED_X * dt, dy = SPEED_Y * dt;
        controller.checkInput(listener.getPressedButton(), listener.x, listener.y);

        batch.draw(background, 0, 0);

        Field field = model.field; // model.field may suddenly become NULL at any moment, so a local var being used
        if (field != null) {
            // draw 1-st layer (bottom (block/water/dias))
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                    if (cell.bottom != null) {
                        if (texturesDown.containsKey(cell.bottom.getClass())) {
                            TextureRegion texture = texturesDown.get(cell.bottom.getClass()); // here texture != null
                            float x = convertXFromModelToScreen(i);
                            float y = convertYFromModelToScreen(j);
                            batch.draw(texture, x, y);
                        }
                    }
                }
            }
            // draw 2-nd layer (static objects)
            drawObjects(field, batch, setStatic);
            // draw 3-rd layer (food)
            drawObjects(field, batch, setFood);
            // draw 4-th layer (animated characters)
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                    float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                    for (CellObject obj : cell.objects) {
                        if (texturesAnim.containsKey(obj.getClass())) {
                            AnimInfo anim = texturesAnim.get(obj.getClass()); // anim != null (assert omitted)
                            Model.Character key = obj.getClass() == Actor1.class ? model.character1 : model.character2;
                            Animation<TextureRegion> animation = anim.animations.get(key);
                            if (animation != null) {
                                TextureRegion texture = animation.getKeyFrame(anim.t); // assert omitted

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
                                    texture.flip(true, false); // flip is not intensive operation (affects UV-mapping)
                                    batch.draw(texture, x, y);
                                    texture.flip(true, false);
                                }
                            }
                        }
                    }
                }
            }
            // draw 5-th layer (opened umbrella)
            drawObjects(field, batch, setUmbrella);
            // draw 6-th layer here...
        }
    }

    public void setMovesAllowed(boolean value) {
        controller.setMovesAllowed(value);
    }

    public void dispose() {
        atlasDown.dispose(); // disposing an atlas also disposes all its internal textures
        atlasUp.dispose();
        atlasRabbit.dispose();
        atlasHedgehog.dispose();
        atlasSquirrel.dispose();
        atlasCat.dispose();
        background.dispose();
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
            if (obj instanceof LadderBottom || obj instanceof LadderTop)
                return true;
        }
        return false;
    }

    private void drawObjects(Field field, Batch batch, ObjectSet<Class> filter) {
        assert field != null;
        for (int j = 0; j < Field.HEIGHT; j++) {
            for (int i = 0; i < Field.WIDTH; i++) {
                Cell cell = field.cells[j * Field.WIDTH + i]; // cell != NULL (assert omitted)
                float bottomWidth = getBottomWidth(cell), bottomHeight = getBottomHeight(cell);
                for (CellObject obj : cell.objects) {
                    if (filter.contains(obj.getClass())) {
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
    }
}
