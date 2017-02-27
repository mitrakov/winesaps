package ru.mitrakov.self.rush;

import java.util.*;

import javax.lang.model.type.NullType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 23.02.2017
 */

class Gui {
    private static final int CELL_SIZ_W = 15;
    private static final int CELL_SIZ_H = 85;
    private static final int OFFSET_X = 17;      // (Screen.width - Field.WIDTH*CELL_SIZ_W)/2 = (800 - 51*15)/2
    private static final int OFFSET_Y = -30;
    private static final int BUTTON_MARGIN = 2;
    static final int TOOLBAR_WIDTH = 55;         // Screen.height - Field.HEIGHT*CELL_SIZ_H = 480 - 5*85

    private final Model model;
    private final BitmapFont font = new BitmapFont();
    private final Map<Class, TextureRegion> texturesUp = new HashMap<Class, TextureRegion>(20);
    private final Map<Class, TextureRegion> texturesDown = new HashMap<Class, TextureRegion>(3);
    private final Map<Class, TextureRegion> texturesThing = new HashMap<Class, TextureRegion>(8);
    private final TextureAtlas atlasDown = new TextureAtlas(Gdx.files.internal("down.pack"));
    private final TextureAtlas atlasUp = new TextureAtlas(Gdx.files.internal("up.pack"));
    private final TextureAtlas atlasThing = new TextureAtlas(Gdx.files.internal("thing.pack"));

    final Rectangle buttonThing = new Rectangle();

    Gui(Model model) {
        assert model != null;
        this.model = model;
    }

    private static float convertXFromModelToScreen(int x) {
        return x * CELL_SIZ_W + OFFSET_X;
    }

    private static float convertYFromModelToScreen(int y) {
        return (Field.HEIGHT - y) * CELL_SIZ_H + OFFSET_Y;
    }

    static int convertXFromScreenToModel(float x) {
        int res = (int) ((x - OFFSET_X) / CELL_SIZ_W);
        if (res < 0) res = 0;
        if (res >= Field.WIDTH) res = Field.WIDTH - 1;
        return res;
    }

    static int convertYFromScreenToModel(float y) {
        int res = (int) (Field.HEIGHT - ((y - OFFSET_Y - CELL_SIZ_H) / CELL_SIZ_H));
        if (res < 0) res = 0;
        if (res >= Field.HEIGHT) res = Field.HEIGHT - 1;
        return res;
    }

    void init() {
        Class downClasses[] = new Class[]{Block.class, Dias.class, Water.class};
        Class upClasses[] = new Class[]{Actor1.class, Actor2.class, Entry1.class, Entry2.class, Apple.class, Pear.class,
                Block.class, LadderTop.class, LadderBottom.class, RopeLine.class, Water.class, Wolf.class, Stair.class,
                Mine.class, Umbrella.class, OpenedUmbrella.class, Waterfall.class};
        Class thingClasses[] = new Class[]{NullType.class, Mine.class, Umbrella.class};
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
        for (Class clazz : thingClasses) {
            TextureRegion texture = atlasThing.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesThing.put(clazz, texture);
        }
        // don't dispose TextureAtlas here; TextureAtlas must be disposed only in "disposed" method
    }

    void render(SpriteBatch batch) {
        assert batch != null;

        if (model.field != null) {
            // draw a field
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = model.field.cells[j * Field.WIDTH + i];
                    assert cell != null;
                    // @mitrakov: "Map::getOrDefault" requires too high Level API (24), so we use usual "Map::get"
                    // draw bottom (block/water/dias)
                    float bottomWidth = CELL_SIZ_W;
                    float bottomHeight = 0;
                    if (cell.bottom != null) {
                        if (texturesDown.containsKey(cell.bottom.getClass())) {
                            TextureRegion texture = texturesDown.get(cell.bottom.getClass());
                            float x = convertXFromModelToScreen(i);
                            float y = convertYFromModelToScreen(j);
                            batch.draw(texture, x, y);
                            bottomWidth = texture.getRegionWidth();
                            bottomHeight = texture.getRegionHeight();
                        }
                    }
                    // draw objects above the bottom
                    for (CellObject obj : cell.objects) {
                        if (texturesUp.containsKey(obj.getClass())) {
                            TextureRegion texture = texturesUp.get(obj.getClass());
                            float x = convertXFromModelToScreen(i) - .5f * (texture.getRegionWidth() - bottomWidth);
                            float y = convertYFromModelToScreen(j) + bottomHeight;
                            batch.draw(texture, x, y);
                        }
                    }
                }
            }

            // draw a thing
            Class clazz = model.curThing != null ? model.curThing.getClass() : NullType.class;
            TextureRegion texture = texturesThing.get(clazz);
            batch.draw(texture, BUTTON_MARGIN, BUTTON_MARGIN);
            buttonThing.set(BUTTON_MARGIN, BUTTON_MARGIN, texture.getRegionWidth(), texture.getRegionHeight());

            // draw a score
            font.draw(batch, String.format(Locale.getDefault(), "Score: %d-%d", model.score1, model.score2), 620, 20);
        }
    }

    void dispose() {
        for (TextureRegion texture : texturesDown.values()) {
            texture.getTexture().dispose();
        }
        for (TextureRegion texture : texturesUp.values()) {
            texture.getTexture().dispose();
        }
        atlasDown.dispose();
        atlasUp.dispose();
    }
}
