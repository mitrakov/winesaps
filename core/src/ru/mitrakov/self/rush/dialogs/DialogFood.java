package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.mitrakov.self.rush.model.Cells.*;
import ru.mitrakov.self.rush.ui.DialogFeat;
import ru.mitrakov.self.rush.model.Model.Character;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogFood extends DialogFeat {
    private final Label lblEdible;
    private final Label lblDangerous;
    private final Actor apple;
    private final Actor pear;
    private final Actor signOk;
    private final Actor signDanger;
    private final ObjectMap<Character, Image> food = new ObjectMap<Character, Image>(4);

    public DialogFood(String title, Skin skin, String windowStyleName, TextureAtlas atlas) {
        super(title, skin, windowStyleName);
        assert atlas != null;

        // build components
        lblEdible = new Label("", skin, "default");
        lblDangerous = new Label("", skin, "default");
        apple = new Image(atlas.findRegion(Apple.class.getSimpleName().toLowerCase()));
        pear = new Image(atlas.findRegion(Pear.class.getSimpleName().toLowerCase()));
        signOk = new Image(atlas.findRegion("valid"));
        signDanger = new Image(atlas.findRegion("invalid"));

        // fill up map Character -> FavouriteFood
        food.put(Character.Rabbit, new Image(atlas.findRegion(Carrot.class.getSimpleName().toLowerCase())));
        food.put(Character.Hedgehog, new Image(atlas.findRegion(Mushroom.class.getSimpleName().toLowerCase())));
        food.put(Character.Squirrel, new Image(atlas.findRegion(Nut.class.getSimpleName().toLowerCase())));
        food.put(Character.Cat, new Image(atlas.findRegion(Meat.class.getSimpleName().toLowerCase())));

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblEdible.setText(bundle.format("dialog.food.edible"));
        lblDangerous.setText(bundle.format("dialog.food.dangerous"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.info"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    public Dialog setCharacter(Character character) {
        Table table = getContentTable();
        assert table != null;

        Table tableEdible = new Table();
        tableEdible.add(apple).space(20);
        tableEdible.add(pear).space(20);
        tableEdible.add(food.get(character)).space(20); // add(NULL) is not dangerous

        Table tableDangerous = new Table();
        for (ObjectMap.Entry<Character, Image> e : food.entries()) {
            if (e.key != character)
                tableDangerous.add(e.value).space(20);
        }

        table.pad(20).clear();
        table.add(lblEdible).colspan(3);
        table.row();
        table.add().width(signOk.getWidth());
        table.add(tableEdible).expandX();
        table.add(signOk).width(signOk.getWidth());
        table.row();
        table.add(lblDangerous).colspan(3).spaceTop(25);
        table.row();
        table.add().width(signDanger.getWidth());
        table.add(tableDangerous).expandX();
        table.add(signDanger).width(signDanger.getWidth());
        return this;
    }
}
