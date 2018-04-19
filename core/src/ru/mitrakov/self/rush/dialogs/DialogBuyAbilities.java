package ru.mitrakov.self.rush.dialogs;

import java.util.Collection;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.AudioManager;

import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * "Inventory purchase" dialog
 * @author Mitrakov
 */
public class DialogBuyAbilities extends DialogFeat {
    /** Reference to the model */
    private final Model model;
    /** LibGdx skin */
    private final Skin skin;
    /** Texture atlas with menu icons */
    private final TextureAtlas atlasMenu;

    /** "Gems total" label */
    private final Label lblCrystals;
    /** Label with current ability, chosen by a user (e.g. "Snorkel") */
    private final Label lblCurAbility;
    /** Total gems value label */
    private final Label lblTotalCrystals;
    /** Image with current ability, chosen by a user */
    private final Image imgGoods;
    /** List of pictures of all the products available */
    private final Table productsList = new Table();

    /** Map: [Ability -> Drawable] */
    private final ObjectMap<Model.Ability, Drawable> abilityIcons = new ObjectMap<Model.Ability, Drawable>(10);

    /** Internationalization bundle */
    private I18NBundle i18n;
    /** User's current gems count */
    private int crystals = 0;
    /** Currently selected product (default: no products selected) */
    private Product selectedItem;

    /**
     * Creates a new "Buy skills and swaggas" dialog
     * @param model Model
     * @param assetManager LibGdx Asset Manager
     * @param skin LibGdx skin
     * @param style style name (usually just "default")
     * @param audioManager Audio Manager
     * @param i18n internationalization bundle
     */
    public DialogBuyAbilities(final Model model, AssetManager assetManager, Skin skin, String style,
                              AudioManager audioManager, I18NBundle i18n) {
        super("", skin, style);
        assert model != null && audioManager != null && i18n != null;
        this.model = model;
        this.skin = skin;
        this.i18n = i18n;

        lblTotalCrystals = new Label("", skin, "default");
        lblCrystals = new Label("", skin, "default");
        lblCurAbility = new Label("", skin, "default");
        imgGoods = new Image();
        atlasMenu = assetManager.get("pack/menu.pack");

        button("Buy", true); // text will be replaced in onLocaleChanged()
        button("Close");     // text will be replaced in onLocaleChanged()

        init(getContentTable(), loadTextures(assetManager, audioManager), skin);
    }

    @Override
    public Dialog show(Stage stage) {
        rebuildContent(Model.Ability.Snorkel);
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        if (object != null && selectedItem != null) // "Buy" is pressed
            model.buyProduct(selectedItem);
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

    /**
     * Sets user's current gems count
     * @param crystals gems count
     */
    public void setCrystals(int crystals) {
        this.crystals = crystals;
    }

    /**
     * Loads textures according to all possible abilities and return a collection of buttons
     * @param assetManager LibGdx Asset Manager
     * @param audioManager Audio Manager
     * @return collection of buttons for each ability
     */
    private Array<Actor> loadTextures(AssetManager assetManager, AudioManager audioManager) {
        Array<Actor> res = new Array<Actor>();

        TextureAtlas atlasAbility = assetManager.get("pack/ability.pack");
        TextureAtlas atlasGoods = assetManager.get("pack/goods.pack");

        for (final Model.Ability ability : abilityValues) {
            // == buttons ==
            TextureRegion region = atlasAbility.findRegion(ability.name());
            if (region != null) {
                res.add(new ImageButtonFeat(new TextureRegionDrawable(region), audioManager, new Runnable() {
                    @Override
                    public void run() {
                        rebuildContent(ability);
                    }
                }));
            }
            // == images ==
            region = atlasGoods.findRegion(ability.name());
            if (region != null)
                abilityIcons.put(ability, new TextureRegionDrawable(region));
        }
        return res;
    }

    /**
     * Initializes the dialog
     * @param table target table
     * @param abilities array of controls that represent specific abilities
     * @param skin LibGdx skin
     */
    private void init(Table table, final Array<Actor> abilities, Skin skin) {
        assert table != null && skin != null;

        // create gem table (label + image)
        Table tableGems = new Table();
        tableGems.add(lblCrystals).spaceRight(5);
        tableGems.add(new Image(atlasMenu.findRegion("gem"))); // TextureRegion may be NULL

        // build content table
        table.pad(16);
        table.add(lblTotalCrystals);
        table.add(tableGems).left();
        table.row();
        table.add(new Table() {{
            for (Actor actor : abilities) {
                add(actor).space(5);
            }
        }}).colspan(2);
        table.row();
        table.add(imgGoods).colspan(2).spaceTop(16);
        table.row();
        table.add(lblCurAbility).colspan(2);
        table.row();
        table.add(productsList).height(90).colspan(2).spaceTop(16);
    }

    /**
     * Rebuilds the table content according to which ability user has just chosen
     * @param ability ability chosen by a user
     */
    private void rebuildContent(Model.Ability ability) {
        assert i18n != null;

        // update labels and images
        lblCrystals.setText(String.valueOf(crystals));
        imgGoods.setDrawable(abilityIcons.get(ability));
        lblCurAbility.setText(i18n.format(String.format("ability.%s", ability.name().toLowerCase())));

        // reset selectedItem to avoid unexpected purchases
        selectedItem = null;

        // updating products for a chosen ability
        productsList.clear();
        Collection<Product> products = model.getProductsByAbility(ability);
        for (final Product product : products) {
            Table t = new Table(skin) {{
                addListener(new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        selectedItem = product;
                        for (Actor actor : productsList.getChildren()) {
                            if (actor instanceof Table)
                                ((Table) actor).setBackground((Drawable) null);
                        }
                        setBackground("peach");
                        return super.touchDown(event, x, y, pointer, button);
                    }
                });
            }};
            t.add(new Label(i18n.format("product", product.days), skin, "small")).minWidth(80).left();
            t.add(new Label(String.valueOf(product.crystals), skin, "small")).spaceLeft(16);
            t.add(new Image(atlasMenu.findRegion("gem_small")));
            productsList.add(t);
            productsList.row();
        }

        // programmatically select the first product
        Array<Actor> rows = productsList.getChildren();
        if (rows.size > 0) {
            for (EventListener listener : rows.first().getListeners()) {
                if (listener instanceof ClickListener)
                    ((ClickListener) listener).touchDown(null, 0, 0, 0, 0);
            }
        }
    }
}
