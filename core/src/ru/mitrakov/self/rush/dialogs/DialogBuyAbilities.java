package ru.mitrakov.self.rush.dialogs;

import java.util.Collection;

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
 * Class is NOT thread-safe
 */
public class DialogBuyAbilities extends DialogFeat {

    static private class Item {
        Product product;
        String text;

        Item(Product p, String s) {
            this.product = p;
            this.text = s;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final Model model;
    private final Label lblCrystals;
    private final Label lblCurAbility;
    private final Label lblTotalCrystals;
    private final Image imgGoods;
    private final List<Item> productsList;
    private final TextureAtlas atlasAbility = new TextureAtlas(Gdx.files.internal("pack/ability.pack"));
    private final TextureAtlas atlasGoods = new TextureAtlas(Gdx.files.internal("pack/goods.pack"));

    private final ObjectMap<Model.Ability, Drawable> abilityIcons = new ObjectMap<Model.Ability, Drawable>(10);

    private I18NBundle i18n;

    public DialogBuyAbilities(final Model model, Skin skin, String style, AudioManager audioManager, I18NBundle i18n) {
        super("", skin, style);
        assert model != null && audioManager != null && i18n != null;
        this.model = model;
        this.i18n = i18n;

        productsList = new List<Item>(skin, "default");
        lblTotalCrystals = new Label("", skin, "default");
        lblCrystals = new Label("", skin, "default");
        lblCurAbility = new Label("", skin, "default");
        imgGoods = new Image();

        button("Buy", true); // text will be replaced in onLocaleChanged()
        button("Close");     // text will be replaced in onLocaleChanged()

        init(getContentTable(), loadTextures(audioManager), skin);
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildContent(Model.Ability.Snorkel);
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        if (object != null && productsList.getSelected() != null) // "Buy" is pressed
            model.buyProduct(productsList.getSelected().product);
    }

    public void dispose() {
        atlasAbility.dispose(); // disposing an atlas also disposes all its internal textures
        atlasGoods.dispose();
    }

    private Array<Actor> loadTextures(AudioManager audioManager) {
        Array<Actor> res = new Array<Actor>();

        for (final Model.Ability ability : Model.Ability.values()) {
            // == buttons ==
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
            // == images ==
            region = atlasGoods.findRegion(ability.name());
            if (region != null)
                abilityIcons.put(ability, new TextureRegionDrawable(region));
        }
        return res;
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        lblTotalCrystals.setText(bundle.format("dialog.abilities.total"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.abilities.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 2) {
                Actor buy = buttons.first();
                if (buy instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) buy).setText(bundle.format("buy"));
                Actor close = buttons.get(1);
                if (close instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) close).setText(bundle.format("close"));
            }
        }
    }

    private void init(Table table, final Array<Actor> abilities, Skin skin) {
        assert table != null && skin != null;

        table.padTop(20);
        table.add(lblTotalCrystals);
        table.add(lblCrystals);
        table.row();
        table.add(new Table() {{
            for (Actor actor : abilities) {
                add(actor).space(5);
            }
        }}).colspan(2);
        table.row();
        table.add(imgGoods).colspan(2);
        table.row();
        table.add(lblCurAbility).colspan(2);
        table.row();
        table.add(productsList).height(100).colspan(2);
    }

    private void rebuildContent(Model.Ability ability) {
        assert i18n != null;

        lblCrystals.setText(String.valueOf(model.crystals));
        imgGoods.setDrawable(abilityIcons.get(ability));
        lblCurAbility.setText(ability.name());

        // updating products for a chosen ability
        Collection<Product> products = model.getProductsByAbility(ability);
        Array<Item> items = new Array<Item>(products.size());
        for (Product product : products) {
            items.add(new Item(product, i18n.format("product", product.days, product.crystals)));
        }
        productsList.setItems(items); // in Java 8 may be replaced with lambda
    }
}
