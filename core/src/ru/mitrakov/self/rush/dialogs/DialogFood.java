package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.mitrakov.self.rush.model.Cells.*;
import ru.mitrakov.self.rush.ui.DialogFeat;
import ru.mitrakov.self.rush.model.Model.Character;

/**
 * "Food" dialog (shows which fruit are acceptable, and which are poison)
 * @author Mitrakov
 */
public class DialogFood extends DialogFeat {
    /** "Your character can collect" label */
    private final Label lblEdible;
    /** "These products are dangerous" label */
    private final Label lblDangerous;
    /** Apple image */
    private final Actor apple;
    /** Pear image */
    private final Actor pear;
    /** Green checkmark icon */
    private final Actor signOk;
    /** Danger icon */
    private final Actor signDanger;

    /** Map: Character (e.g. rabbit, squirrel, cat) -> Image */
    private final ObjectMap<Character, Image> food = new ObjectMap<Character, Image>(4);

    /**
     * Creates a new "Food" dialog (that shows which fruit are acceptable, and which are poison)
     * @param title dialog title
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param atlas atlas that contains all the textures needed
     */
    public DialogFood(String title, Skin skin, String windowStyleName, TextureAtlas atlas) {
        super(title, skin, windowStyleName, true);
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

    /**
     * Rebuilds the content of the dialog according to what a user've chosen as a character
     * @param character character (Squirrel, Cat, etc.)
     * @return this
     */
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
