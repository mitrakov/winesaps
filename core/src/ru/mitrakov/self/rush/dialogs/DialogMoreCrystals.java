package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.IBillingProvider;
import ru.mitrakov.self.rush.PsObject;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogMoreCrystals extends DialogFeat {
    private final Model model;
    private final PsObject psObject;
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

    public DialogMoreCrystals(Model model, Skin skin, String style, final Dialog promoDialog, final Stage stage,
                              final PsObject psObject) {
        super("", skin, style);
        assert model != null && promoDialog != null && stage != null;
        this.model = model;
        this.psObject = psObject;

        lblOverview = new Label("", skin, "default");
        lblWay1 = new Label("", skin, "default");
        lblText1 = new Label("", skin, "small");
        lblWay2 = new Label("", skin, "default");
        lblText2 = new LinkedLabel("", "", "", skin, "small", "link", new Runnable() {
            @Override
            public void run() {
                promoDialog.show(stage);
            }
        });
        lblText2extra = new Label("", skin, "small");
        lblWay3 = new Label("", skin, "default");
        lblText3 = new Label("", skin, "small");
        lblWay4 = new Label("", skin, "default");
        lblText4 = new LinkedLabel("", "", "", skin, "small", "link", new Runnable() {
            @Override
            public void run() {
                if (psObject != null) {
                    IBillingProvider provider = psObject.getBillingProvider();
                    if (provider != null) {
                        // ....
                    }
                }
            }
        });

        init(getContentTable());
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
        initSkuGems();
        return super.show(stage);
    }

    private void init(Table table) {
        assert table != null;

        table.pad(16);
        table.add(lblOverview);
        table.row();
        table.add(lblWay1).left();
        table.row();
        table.add(lblText1).left().padLeft(40);
        table.row();
        table.add(lblWay2).left();
        table.row();
        table.add(lblText2).left().padLeft(40);
        table.row();
        table.add(lblText2extra).left().padLeft(40);
        table.row();
        table.add(lblWay3).left();
        table.row();
        table.add(lblText3).left().padLeft(40);
        table.row();
        table.add(lblWay4).left();
        table.row();
        table.add(lblText4).left().padLeft(40);
    }

    private void initSkuGems() {
        if (psObject != null) {
            IBillingProvider provider = psObject.getBillingProvider();
            if (provider != null) {
                for (IBillingProvider.Sku sku: provider.getProducts()) {
                    model.requestSkuGems(sku.id);
                }
            }
        }
    }
}
