package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.dialogs.DialogFood;

import static ru.mitrakov.self.rush.model.Model.characterValues;

/**
 * ScreenCharacter shows 4 characters to choose after successful registration
 * @author Mitrakov
 */
public class ScreenCharacter extends LocalizableScreen {
    /** Array of 4 checkboxes (each checkbox for each character) */
    private final Array<TextButton> checkboxes = new Array<TextButton>(4);
    /** "Next" button */
    private final TextButton btnNext;
    /** Pop up dialog: "You can eat this, you cannot eat that" */
    private final DialogFood dialog;

    /**
     * Creates a new instance of ScreenCharacter
     * @param game instance of Winesaps (NON-NULL)
     * @param model model (NON-NULL)
     * @param psObject Platform Specific Object (NON-NULL)
     * @param assetManager asset manager (NON-NULL)
     * @param audioManager audio manager (NON-NULL)
     */
    public ScreenCharacter(Winesaps game, final Model model, PsObject psObject, AssetManager assetManager,
                           AudioManager audioManager) {
        super(game, model, psObject, assetManager, audioManager);
        Skin skin = assetManager.get("skin/uiskin.json");

        btnNext = createButton(skin);
        dialog = createFoodDialog(skin);
        buildTable(skin);
    }

    @Override
    public void show() {
        super.show();
        if (!model.newbie)
            game.setNextScreen();
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        super.onLocaleChanged(bundle);
        assert bundle != null;

        for (TextButton btn : checkboxes) {
            Object obj = btn.getUserObject();
            if (obj instanceof Model.Character) {  // stackoverflow.com/questions/2950319
                btn.setText(bundle.format("character." + obj));
            }
        }
        dialog.onLocaleChanged(bundle);
        btnNext.setText(bundle.format("next"));
    }

    @Override
    public void handleEvent(EventBus.Event event) {
    }

    @Override
    public void handleEventBackground(EventBus.Event event) {
    }

    /**
     * @param skin skin
     * @return array of 4 images with characters: Rabbit, Squirrel, Hedgehog and Cat
     */
    private Array<Actor> loadImages(Skin skin) {
        Array<Actor> result = new Array<Actor>(characterValues.length);

        TextureAtlas atlasCharacter = assetManager.get("pack/char.pack");
        for (Model.Character character : characterValues) {
            if (character != Model.Character.None) {
                // create checkboxes
                final TextButton btn = new CheckBox("", skin, "default");
                btn.setUserObject(character);
                checkboxes.add(btn);

                // create pictures
                TextureRegion region = atlasCharacter.findRegion(character.name());
                if (region != null) {
                    result.add(new ImageButtonFeat(new TextureRegionDrawable(region), audioManager, new Runnable() {
                        @Override
                        public void run() {
                            btn.setChecked(!btn.isChecked());
                        }
                    }));
                }
            }
        }

        if (checkboxes.size > 0) {
            checkboxes.first().setChecked(true);
            new ButtonGroup<Button>((Button[]) (checkboxes.toArray(Button.class)));
        }

        return result;
    }

    /**
     * @param skin skin
     * @return "Next" button
     */
    private TextButton createButton(Skin skin) {
        return new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                for (TextButton btn : checkboxes) {           // in Java 8 may be replaced with lambda
                    if (btn.isChecked()) {
                        Object obj = btn.getUserObject();
                        if (obj instanceof Model.Character) { // stackoverflow.com/questions/2950319
                            model.changeCharacter((Model.Character) obj);
                            dialog.setCharacter((Model.Character) obj).show(stage);
                        }
                    }
                }
            }
        });
    }

    /**
     * @param skin skin
     * @return food dialog ("these food items are OK and those ones are danger")
     */
    private DialogFood createFoodDialog(Skin skin) {
        DialogFood dialog = new DialogFood("", skin, "default", assetManager.<TextureAtlas>get("pack/menu.pack"));
        dialog.setOnResultAction(new Runnable() {
            @Override
            public void run() {
                game.setNextScreen();
            }
        });
        return dialog;
    }

    /**
     * Arranges the content table
     * @param skin skin
     */
    private void buildTable(Skin skin) {
        Table tableMain = new Table(skin);
        tableMain.pad(20).setBackground("panel-maroon");
        Array<Actor> images = loadImages(skin);

        table.add(tableMain).expand();
        table.row().pad(5);
        table.add(btnNext).colspan(images.size).width(200).height(50).right();
        table.setBackground(new Image(assetManager.<Texture>get("back/main.jpg")).getDrawable());

        for (Actor img : images) {
            tableMain.add(img).bottom().space(20);
        }
        tableMain.row();
        for (TextButton btn : checkboxes) {
            tableMain.add(btn).space(20);
        }
    }
}
