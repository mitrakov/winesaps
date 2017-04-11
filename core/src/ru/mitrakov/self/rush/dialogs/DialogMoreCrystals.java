package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogMoreCrystals extends DialogFeat {

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

    public DialogMoreCrystals(Skin skin, String style, final Dialog promoDialog, final Stage stage) {
        super("", skin, style);
        assert promoDialog != null && stage != null;

        lblOverview = new Label("", skin, "default");
        lblWay1 = new Label("", skin, "default");
        lblText1 = new Label("", skin, "default");
        lblWay2 = new Label("", skin, "default");
        lblText2 = new LinkedLabel("", "", "", skin, "default", new Runnable() {
            @Override
            public void run() {
                promoDialog.show(stage);
            }
        });
        lblText2extra = new Label("", skin, "default");
        lblWay3 = new Label("", skin, "default");
        lblText3 = new Label("", skin, "default");
        lblWay4 = new Label("", skin, "default");
        lblText4 = new LinkedLabel("", "", "", skin, "default", new Runnable() {
            @Override
            public void run() {
                System.out.println("Hey-Hey!");
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
        lblText1.setText(bundle.format("dialog.crystals.text1"));
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
                if (actor != null && actor instanceof TextButton)
                    ((TextButton) actor).setText(bundle.format("close"));
            }
        }
    }

    private void init(Table table) {
        assert table != null;

        table.pad(30);
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
}
