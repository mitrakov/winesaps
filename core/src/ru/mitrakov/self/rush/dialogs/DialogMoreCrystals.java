package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ru.mitrakov.self.rush.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogMoreCrystals extends DialogFeat {

    public DialogMoreCrystals(Skin skin, String windowStyleName, Dialog promoDialog, Stage stage, I18NBundle i18n) {
        super(i18n.format("dialog.crystals.header"), skin, windowStyleName);
        assert promoDialog != null && stage != null;

        init(getContentTable(), skin, promoDialog, stage, i18n);

        button(i18n.format("close"));
    }

    private void init(Table table, Skin skin, final Dialog promoDialog, final Stage stage, I18NBundle i18n) {
        assert table != null && skin != null && i18n != null;

        table.pad(30);
        table.add(new Label(i18n.format("dialog.crystals.overview"), skin, "default"));
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.way1"), skin, "default")).left();
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.text1"), skin, "default")).left().padLeft(40);
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.way2"), skin, "default")).left();
        table.row();
        table.add(new LinkedLabel(i18n.format("dialog.crystals.text2.start"), i18n.format("dialog.crystals.text2.link"),
                i18n.format("dialog.crystals.text2.end"), skin, "default", new Runnable() {
            @Override
            public void run() {
                promoDialog.show(stage);
            }
        })).left().padLeft(40);
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.text2.extra"), skin, "default")).left().padLeft(40);
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.way3"), skin, "default")).left();
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.text3"), skin, "default")).left().padLeft(40);
        table.row();
        table.add(new Label(i18n.format("dialog.crystals.way4"), skin, "default")).left();
        table.row();
        table.add(new LinkedLabel(i18n.format("dialog.crystals.text4.start"), i18n.format("dialog.crystals.text4.link"),
                "", skin, "default", new Runnable() {
            @Override
            public void run() {
                System.out.println("Hey-Hey!");
            }
        })).left().padLeft(40);
    }
}
