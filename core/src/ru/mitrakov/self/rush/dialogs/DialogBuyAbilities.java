package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.AudioManager;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogBuyAbilities extends DialogFeat {
    private final Model model;
    private final Label lblPicture;
    private final Label lblCrystals;
    private final Label lblCurAbility;
    private final List<Product> productsList;
    private final TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));

    public DialogBuyAbilities(final Model model, Skin skin, String style, AudioManager audioManager, I18NBundle i18n) {
        super(i18n.format("dialog.abilities.header"), skin, style);
        assert model != null; // audioManager may be NULL
        this.model = model;

        productsList = new List<Product>(skin, "default");
        lblCrystals = new Label("", skin, "default");
        lblPicture = new Label("", skin, "default");
        lblCurAbility = new Label("", skin, "default");

        button(i18n.format("buy"), true);
        button(i18n.format("close"));

        init(getContentTable(), loadTextures(audioManager), skin, i18n);
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildContent(Model.Ability.Snorkel);
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        if (object != null && productsList.getSelected() != null) // "Buy" is pressed
            model.buyProduct(productsList.getSelected());
    }

    public void dispose() {
        atlasAbility.dispose(); // disposing an atlas also disposes all its internal textures
    }

    private Array<Actor> loadTextures(AudioManager audioManager) {
        Array<Actor> res = new Array<Actor>();

        for (final Model.Ability ability : Model.Ability.values()) {
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                ImageButton imageButton = new ImageButtonFeat(new TextureRegionDrawable(region), audioManager) {{
                    addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            rebuildContent(ability);
                        }
                    });
                }};
                res.add(imageButton);
            }
        }
        return res;
    }

    private void init(Table table, final Array<Actor> abilities, Skin skin, I18NBundle i18n) {
        assert table != null && skin != null && i18n != null;

        table.add(new Label(i18n.format("dialog.abilities.total"), skin, "default"));
        table.add(lblCrystals);
        table.row();
        table.add(new Table() {{
            for (Actor actor : abilities) {
                add(actor).space(5);
            }
        }}).colspan(2);
        table.row();
        table.add(lblPicture).colspan(2);
        table.row();
        table.add(lblCurAbility).colspan(2);
        table.row();
        table.add(productsList).height(100).colspan(2);
    }

    private synchronized void rebuildContent(Model.Ability ability) {
        lblCrystals.setText(String.valueOf(model.crystals));
        lblPicture.setText("picture " + ability);
        lblCurAbility.setText(ability.name());

        Array<Product> items = new Array<Product>(model.getProductsByAbility(ability).toArray(new Product[0]));
        productsList.setItems(items);
    }
}
