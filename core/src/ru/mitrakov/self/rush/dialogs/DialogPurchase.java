package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import java.util.List;
import java.util.Collections;

import ru.mitrakov.self.rush.*;
import ru.mitrakov.self.rush.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogPurchase extends DialogFeat {
    private final Label lblError;

    public DialogPurchase(Skin skin, String windowStyleName, I18NBundle i18n) {
        super("", skin, windowStyleName);
        Table table = getContentTable();
        assert i18n != null && table != null;

        table.pad(20).add(lblError = new Label(i18n.format("dialog.crystals.error"), skin));
        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        Table table = getContentTable();
        assert bundle != null && table != null;

        lblError.setText(bundle.format("dialog.crystals.error"));

        // gems count info ("10 gems", "35 gems", etc.)
        for (Actor actor : table.getChildren()) {
            if (actor instanceof Label && actor.getUserObject() instanceof IBillingProvider.Sku) {
                Label lbl = ((Label) actor);
                IBillingProvider.Sku sku = (IBillingProvider.Sku) actor.getUserObject();
                lbl.setText(bundle.format("dialog.crystals.count", sku.value));
            }
        }

        // close button
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("close"));
            }
        }
    }

    public void updateSkuButtons(Skin skin, final IBillingProvider provider, final String username, I18NBundle i18n,
                                 TextureAtlas atlas, AudioManager audioManager) {
        assert skin != null && provider != null && i18n != null && atlas != null && audioManager != null;

        Table table = getContentTable();
        assert table != null;
        table.clear();

        List<IBillingProvider.Sku> products = provider.getProducts();
        Collections.sort(products);

        for (final IBillingProvider.Sku sku : products) {
            TextureRegion texture = atlas.findRegion(sku.id);
            if (texture != null) {
                table.add(new ImageButtonFeat(new TextureRegionDrawable(texture), audioManager) {{
                    addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            provider.purchaseProduct(sku, username);
                        }
                    });
                }}).spaceLeft(20);
            }
        }

        table.row();
        for (IBillingProvider.Sku sku : products) {
            Label lbl = new Label(i18n.format("dialog.crystals.count", sku.value), skin);
            lbl.setUserObject(sku);
            table.add(lbl).spaceLeft(20);
        }

        table.row();
        for (IBillingProvider.Sku sku : products) {
            table.add(new Label(sku.price, skin)).spaceLeft(20);
        }

        pack();
    }
}
