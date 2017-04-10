package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogAbout extends DialogFeat {
    public DialogAbout(Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.about.header"), skin, windowStyleName);

        Table table = getContentTable();
        assert table != null;

        table.pad(30);
        table.add(new Label(i18n.format("dialog.about.overview"), skin, "default"));
        table.row().space(20);
        table.add(new Label(i18n.format("dialog.about.created"), skin, "default"));
        table.row();
        table.add(new Label(i18n.format("dialog.about.support"), skin, "default")).left();
        table.row();
        table.add(new Label(i18n.format("dialog.about.web"), skin, "default")).left();
        table.row();

        button("Close");
    }
}
