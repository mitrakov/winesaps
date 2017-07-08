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
 * Created by mitrakov on 05.03.2017
 */
public class DialogMoreCrystals extends DialogFeat {
    private final Model model;
    private final Label lblOverview;
    private final Label lblWay1;
    private final Label lblText1;
    private final Label lblWay2;
    private final LinkedLabel lblText2;
    private final Label lblText2extra;
    private final Label lblWay3;
    private final Label lblText3;
    private final Label lblWay4;
    private final LinkedLabel lblText4;

    public DialogMoreCrystals(Model model, Skin skin, String style, AssetManager assetManager, AudioManager audioMgr,
                              final Dialog promoDialog, final Dialog purchaseDialog, final Stage stage) {
        super("", skin, style);
        assert model != null && assetManager != null && promoDialog != null && stage != null;
        this.model = model;

        lblOverview = new Label("", skin, "default");
        lblWay1 = new Label("", skin, "default") {{ setAlignment(Align.center, Align.center);}};
        lblText1 = new Label("", skin, "small") {{ setAlignment(Align.center, Align.center);}};
        lblWay2 = new Label("", skin, "default") {{ setAlignment(Align.center, Align.center);}};
        lblText2 = new LinkedLabel("", "", "", skin, "small", "link", new Runnable() {
            @Override
            public void run() {
                promoDialog.show(stage);
            }
        });
        lblText2extra = new Label("", skin, "small") {{ setAlignment(Align.center, Align.center);}};
        lblWay3 = new Label("", skin, "default") {{ setAlignment(Align.center, Align.center);}};
        lblText3 = new Label("", skin, "small") {{ setAlignment(Align.center, Align.center);}};
        lblWay4 = new Label("", skin, "default") {{ setAlignment(Align.center, Align.center);}};
        lblText4 = new LinkedLabel("", "", "", skin, "small", "link", new Runnable() {
            @Override
            public void run() {
                purchaseDialog.show(stage);
            }
        });

        init(getContentTable(), assetManager, audioMgr, skin, promoDialog, purchaseDialog, stage);
        button("Close"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblOverview.setText(bundle.format("dialog.crystals.overview"));
        lblWay1.setText(bundle.format("dialog.crystals.way1"));
        lblText1.setText(bundle.format("dialog.crystals.text1", 1));
        lblWay2.setText(bundle.format("dialog.crystals.way2"));
        lblText2.setText(bundle.format("dialog.crystals.text2.start"), bundle.format("dialog.crystals.text2.link"),
                bundle.format("dialog.crystals.text2.end"));
        lblText2extra.setText(bundle.format("dialog.crystals.text2.extra"));
        lblWay3.setText(bundle.format("dialog.crystals.way3"));
        lblText3.setText(bundle.format("dialog.crystals.text3"));
        lblWay4.setText(bundle.format("dialog.crystals.way4"));
        lblText4.setText(bundle.format("dialog.crystals.text4.start"), bundle.format("dialog.crystals.text4.link"), "");

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.crystals.header"));
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

    private void init(Table table, AssetManager assetManager, AudioManager audioManager, Skin skin,
                      final Dialog promoDialog, final Dialog purchaseDialog, final Stage stage) {
        assert table != null && assetManager != null && audioManager != null;
        TextureAtlas atlas = assetManager.get("pack/menu.pack");
        //table.setDebug(true);

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
        table2.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("more2")), audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    promoDialog.show(stage);
                }
            });
        }}).expand();
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
        table4.add(new ImageButtonFeat(new TextureRegionDrawable(atlas.findRegion("more4")), audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    purchaseDialog.show(stage);
                }
            });
        }}).expand();
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
