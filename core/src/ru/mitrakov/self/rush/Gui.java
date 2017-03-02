package ru.mitrakov.self.rush;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 23.02.2017
 */

class Gui extends Actor {
    private static final int CELL_SIZ_W = 15;
    private static final int CELL_SIZ_H = 85;

    private final Model model;
    private final InputController controller;
    private final MyClickListener listener = new MyClickListener();
    private final TextureAtlas atlasDown = new TextureAtlas(Gdx.files.internal("down.pack"));
    private final TextureAtlas atlasUp = new TextureAtlas(Gdx.files.internal("up.pack"));
    private final Map<Class, TextureRegion> texturesDown = new HashMap<Class, TextureRegion>(3);
    private final Map<Class, TextureRegion> texturesUp = new HashMap<Class, TextureRegion>(20);

    private static float convertXFromModelToScreen(int x) {
        return (x + 1) * CELL_SIZ_W + 3;  // +3 to smooth an inaccuracy
    }

    private static float convertYFromModelToScreen(int y) {
        return (Field.HEIGHT - y) * CELL_SIZ_H - .5f * CELL_SIZ_H - 1; // -1 to smooth an inaccuracy
    }

    static int convertXFromScreenToModel(float x) {
        return (int) (x / CELL_SIZ_W);
    }

    static int convertYFromScreenToModel(float y) {
        return (int) (Field.HEIGHT - y / CELL_SIZ_H);
    }

    Gui(Model model) {
        assert model != null;
        this.model = model;
        controller = new InputController(model);
        addListener(listener);

        setWidth(Field.WIDTH * CELL_SIZ_W);
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
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if (listener.getPressedButton() >= 0)
            controller.checkInput(listener.x, listener.y);

        if (model.field != null) {
            // draw a field
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = model.field.cells[j * Field.WIDTH + i]; // cell must NOT be NULL (assert omitted)
                    // @mitrakov: "Map::getOrDefault" requires too high Level API (24), so we use usual "Map::get"
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
                                batch.draw(texture, x, y);
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
        atlasDown.dispose();
        atlasUp.dispose();
        return super.remove();
    }
}

class MyClickListener extends ClickListener {
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
