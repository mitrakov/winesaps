package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.AudioManager;

/**
 * "How to get more gems" dialog
 * @author Mitrakov
 */
public class DialogMoreGems extends DialogFeat {
    /** Reference to the model */
    private final Model model;
    /** Big "How you can get more gems" label */
    private final Label lblOverview;
    /** Way1 header label */
    private final Label lblWay1;
    /** Way1 description label */
    private final Label lblText1;
    /** Way2 header label */
    private final Label lblWay2;
    /** Way2 description label */
    private final LinkedLabel lblText2;
    /** Extra way2 description label (because text is too long) */
    private final Label lblText2extra;
    /** Way3 header label */
    private final Label lblWay3;
    /** Way3 description label */
    private final Label lblText3;
    /** Way4 header label */
    private final Label lblWay4;
    /** Way4 description label */
    private final LinkedLabel lblText4;

    /**
     * Creates new "How to get more gems" dialog
     * @param model {@link Model}
     * @param skin LibGdx skin
     * @param style style name (usually just "default")
     * @param assetManager LibGdx Assets Manager
     * @param audioMgr {@link AudioManager}
     * @param promoDialog dialog to show when "Show promo code" is clicked
     * @param purchaseDialog dialog to show when "Buy gems here" is clicked
     */
    public DialogMoreGems(Model model, Skin skin, String style, AssetManager assetManager, AudioManager audioMgr,
                          final Dialog promoDialog, final Dialog purchaseDialog) {
        super("", skin, style);
        assert model != null && assetManager != null && promoDialog != null;
        this.model = model;

        lblOverview = new Label("", skin, "default");
        lblWay1 = new LabelFeat("", skin, "default", true);
        lblText1 = new LabelFeat("", skin, "small", true);
        lblWay2 = new LabelFeat("", skin, "default", true);
        lblText2 = new LinkedLabel("", "", "", skin, "small", "link", new Runnable() {
            @Override
            public void run() {
                promoDialog.show(getStage());
            }
        });
        lblText2extra = new LabelFeat("", skin, "small", true);
        lblWay3 = new LabelFeat("", skin, "default", true);
        lblText3 = new LabelFeat("", skin, "small", true);
        lblWay4 = new LabelFeat("", skin, "default", true);
        lblText4 = new LinkedLabel("", "", "", skin, "small", "link", new Runnable() {
            @Override
            public void run() {
                purchaseDialog.show(getStage());
            }
        });

        init(getContentTable(), assetManager, audioMgr, skin, promoDialog, purchaseDialog);
        button("Close"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblOverview.setText(bundle.format("dialog.gems.overview"));
        lblWay1.setText(bundle.format("dialog.gems.way1"));
        lblText1.setText(bundle.format("dialog.gems.text1", 1));
        lblWay2.setText(bundle.format("dialog.gems.way2"));
        lblText2.setText(bundle.format("dialog.gems.text2.start"), bundle.format("dialog.gems.text2.link"),
                bundle.format("dialog.gems.text2.end"));
        lblText2extra.setText(bundle.format("dialog.gems.text2.extra"));
        lblWay3.setText(bundle.format("dialog.gems.way3"));
        lblText3.setText(bundle.format("dialog.gems.text3"));
        lblWay4.setText(bundle.format("dialog.gems.way4"));
        lblText4.setText(bundle.format("dialog.gems.text4.start"), bundle.format("dialog.gems.text4.link"), "");

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.gems.header"));
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

    @Override
    public Dialog show(Stage stage) {
        model.requestSkuGems();
        return super.show(stage);
    }

    /**
     * Initializes components
     * @param table content table
     * @param assetManager LibGdx Assets Manager
     * @param audioManager {@link AudioManager}
     * @param skin LibGdx skin
     * @param promoDialog promo code dialog
     * @param purchaseDialog purchase dialog
     */
    private void init(Table table, AssetManager assetManager, AudioManager audioManager, Skin skin,
                      final Dialog promoDialog, final Dialog purchaseDialog) {
        assert table != null && assetManager != null && audioManager != null;
        TextureAtlas atlas = assetManager.get("pack/menu.pack");
        TextureRegionDrawable drawablePromo = new TextureRegionDrawable(atlas.findRegion("more2"));
        TextureRegionDrawable drawablePurchase = new TextureRegionDrawable(atlas.findRegion("more4"));

        Table table1 = new Table(skin);
        Table table2 = new Table(skin);
        Table table3 = new Table(skin);
        Table table4 = new Table(skin);

        table1.padLeft(8).padRight(8).setBackground("panel-maroon");
        table2.padLeft(8).padRight(8).setBackground("panel-maroon");
        table3.padLeft(8).padRight(8).setBackground("panel-maroon");
        table4.padLeft(8).padRight(8).setBackground("panel-maroon");

        table1.add(lblWay1);
        table1.row();
        table1.add(new Image(atlas.findRegion("more1"))).expand();
        table1.row();
        table1.add(lblText1);

        table2.add(lblWay2);
        table2.row();

        table2.add(new ImageButtonFeat(drawablePromo, audioManager, new Runnable() {
            @Override
            public void run() {
                promoDialog.show(getStage());
            }
        })).expand();
        table2.row();
        table2.add(lblText2);
        table2.row();
        table2.add(lblText2extra);

        table3.add(lblWay3);
        table3.row();
        table3.add(new Image(atlas.findRegion("more3"))).expand();
        table3.row();
        table3.add(lblText3);

        table4.add(lblWay4);
        table4.row();
        table4.add(new ImageButtonFeat(drawablePurchase, audioManager, new Runnable() {
            @Override
            public void run() {
                purchaseDialog.show(getStage());
            }
        })).expand();
        table4.row();
        table4.add(lblText4);

        table.pad(4);
        table.add(lblOverview).colspan(2);
        table.row();
        table.add(table1).space(4).fill();
        table.add(table2).space(4).fill();
        table.row();
        table.add(table3).space(4).fill();
        table.add(table4).space(4).fill();
    }
}
