package ru.mitrakov.self.rush.screens;

import java.util.*;

import static java.lang.Math.*;

import com.badlogic.gdx.Gdx;
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

    private static final int CELL_SIZ_W = 14;
    private static final int CELL_SIZ_H = 85;
    private static final int OFFSET_X = (RushClient.WIDTH - Field.WIDTH * CELL_SIZ_W) / 2; // (800 - 51*14) / 2
    private static final int OFFSET_Y = 33; // inferred by expertise
    private static final float SPEED = 1000f * CELL_SIZ_W / InputController.TOUCH_DELAY;

    private final Model model;
    private final InputController controller;
    private final MyClickListener listener = new MyClickListener();
    private final TextureAtlas atlasDown = new TextureAtlas(Gdx.files.internal("pack/down.pack"));
    private final TextureAtlas atlasUp = new TextureAtlas(Gdx.files.internal("pack/up.pack"));
    private final Map<Class, TextureRegion> texturesDown = new HashMap<Class, TextureRegion>(3);
    private final Map<Class, TextureRegion> texturesUp = new HashMap<Class, TextureRegion>(20);

//    Animation<TextureRegion> animation;
//    float stateTime = 0;
    float actorX = -1;

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

        Class downClasses[] = new Class[]{Block.class, Dias.class, Water.class};
        Class upClasses[] = new Class[]{Actor1.class, Actor2.class, Entry1.class, Entry2.class, Apple.class, Pear.class,
                Block.class, LadderTop.class, LadderBottom.class, RopeLine.class, Water.class, Wolf.class, Stair.class,
                Mine.class, Umbrella.class, OpenedUmbrella.class, Waterfall.class};
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

//        Texture sheet = new Texture(Gdx.files.internal("sprite-animation4.png"));
//        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / 6, sheet.getHeight() / 5);
//        List<TextureRegion> lst = new ArrayList<TextureRegion>(30);
//        for (TextureRegion[] array : tmp) {
//            lst.addAll(Arrays.asList(array));
//        }
//        animation = new Animation<TextureRegion>(0.033f, lst.toArray(new TextureRegion[0]));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
//        stateTime += Gdx.graphics.getDeltaTime();
        float dx = SPEED * Gdx.graphics.getDeltaTime();
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
                            TextureRegion texture = texturesDown.get(cell.bottom.getClass());
                            if (texture != null) {
                                float x = convertXFromModelToScreen(i);
                                float y = convertYFromModelToScreen(j);
                                batch.draw(texture, x, y);
                                bottomWidth = texture.getRegionWidth();
                                bottomHeight = texture.getRegionHeight();
                            }
                        }
                    }
                    // draw objects above the bottom
                    for (CellObject obj : cell.objects) {
                        if (texturesUp.containsKey(obj.getClass())) {
                            TextureRegion texture = texturesUp.get(obj.getClass());
                            if (texture != null) {
                                float x = convertXFromModelToScreen(i) - .5f * (texture.getRegionWidth() - bottomWidth);
                                float y = convertYFromModelToScreen(j) + bottomHeight;
                                if (obj.getClass() == Actor1.class) {
                                    float d = x - actorX;
                                    if (abs(d) < dx / 2 || actorX < 0) { // if actorX==x || actorX not initialized
                                        actorX = x;
                                    } else {
                                        x = actorX;
                                        actorX += signum(d) * dx;
                                    }
                                }
                                batch.draw(texture, x, y);
                            }
                        }
//                        TextureRegion texture = texturesUp.get(obj.getClass());
//                        if (obj.getClass() == Actor1.class)
//                            texture = animation.getKeyFrame(stateTime, true);
//                        if (texture != null) {
//                            float x = convertXFromModelToScreen(i) - .5f * (texture.getRegionWidth() - bottomWidth);
//                            float y = convertYFromModelToScreen(j) + bottomHeight;
//                            batch.draw(texture, x, y);
//                        }
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
        atlasDown.dispose();
        atlasUp.dispose();
        return super.remove();
    }
}
