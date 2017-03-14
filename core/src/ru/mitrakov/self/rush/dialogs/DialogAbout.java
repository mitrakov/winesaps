package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogAbout extends Dialog {
    public DialogAbout(Skin skin, String windowStyleName) {
        super("About the game", skin, windowStyleName);

        Table table = getContentTable();
        assert table != null;

        table.pad(30);
        table.add(new Label("Apple Rush (v0.1.0)", skin, "default"));
        table.row().space(20);
        table.add(new Label("Created by: Mitrakov Artem", skin, "default"));
        table.row();
        table.add(new Label("Support: tom-trix@ya.ru", skin, "default")).left();
        table.row();
        table.add(new Label("Web: applerush.com", skin, "default")).left();
        table.row();

        button("Close");
    }
}
