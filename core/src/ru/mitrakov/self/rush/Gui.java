package ru.mitrakov.self.rush;

import java.util.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.*;

/**
 * Created by mitrakov on 23.02.2017
 */

class Gui {
    private static final int CELL_SIZ_W = 15;
    private static final int CELL_SIZ_H = 85;
    private static final int OFFSET_X = 20;
    private static final int OFFSET_Y = -30;

    private final Model model;
    private final BitmapFont font = new BitmapFont();
    private final Map<Class, TextureRegion> texturesUp = new HashMap<Class, TextureRegion>(20);
    private final Map<Class, TextureRegion> texturesDown = new HashMap<Class, TextureRegion>(3);
    private final TextureAtlas atlasDown = new TextureAtlas(Gdx.files.internal("down.pack"));
    private final TextureAtlas atlasUp = new TextureAtlas(Gdx.files.internal("up.pack"));

    Gui(Model model) {
        assert model != null;
        this.model = model;
    }

    void init() {
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
            TextureAtlas.AtlasRegion texture = atlasUp.findRegion(clazz.getSimpleName());
            if (texture != null)
                texturesUp.put(clazz, texture);
        }
        // don't dispose TextureAtlas here; TextureAtlas must be disposed only in "disposed" method
    }

    void render(SpriteBatch batch) {
        assert batch != null;

        // draw a field
        if (model.field != null) {
            for (int j = 0; j < Field.HEIGHT; j++) {
                for (int i = 0; i < Field.WIDTH; i++) {
                    Cell cell = model.field.cells[j * Field.WIDTH + i];
                    assert cell != null;
                    // @mitrakov: "Map::getOrDefault" requires too high Level API (24), so we use usual "Map::get"
                    float bottomHeight = 0;
                    if (cell.bottom != null) {
                        if (texturesDown.containsKey(cell.bottom.getClass())) {
                            TextureRegion texture = texturesDown.get(cell.bottom.getClass());
                            float x = i * CELL_SIZ_W - texture.getRegionWidth() / 2;
                            float y = (Field.HEIGHT - j) * CELL_SIZ_H;
                            batch.draw(texture, x + OFFSET_X, y + OFFSET_Y);
                            bottomHeight = texture.getRegionHeight();
                        }
                    }
                    for (CellObject obj : cell.objects) {
                        if (texturesUp.containsKey(obj.getClass())) {
                            TextureRegion texture = texturesUp.get(obj.getClass());
                            float x = i * CELL_SIZ_W - texture.getRegionWidth() / 2;
                            float y = (Field.HEIGHT - j) * CELL_SIZ_H + bottomHeight;
                            batch.draw(texture, x + OFFSET_X, y + OFFSET_Y);
                        }
                    }
                }
            }
        }

        font.draw(batch, String.format(Locale.getDefault(), "Score: %d-%d", model.score1, model.score2), 20, 20);
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
