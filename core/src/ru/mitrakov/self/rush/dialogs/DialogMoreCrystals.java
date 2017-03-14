package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.LinkedLabel;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogMoreCrystals extends Dialog {

    public DialogMoreCrystals(Skin skin, String windowStyleName) {
        super("Getting more crystals", skin, windowStyleName);
        button("Close");
        init(getContentTable(), skin);
    }

    private void init(Table table, Skin skin) {
        assert table != null && skin != null;

        table.pad(30);
        table.add(new Label("There are several ways to get more crystals:", skin, "default"));
        table.row();
        table.add(new Label("1. Win the battle", skin, "default")).left();
        table.row();
        table.add(new Label("You earn 1 crystal for each battle you won", skin, "default")).left().padLeft(40);
        table.row();
        table.add(new Label("2. Invite your friends to the game", skin, "default")).left();
        table.row();
        String msg = "Invite your friends with a promo code and \nafter his/her win you'll both get extra crystals";
        table.add(new Label(msg, skin, "default")).left().padLeft(40);
        table.row();
        table.add(new Label("3. Achieve a paid place in a Week Rating", skin, "default")).left();
        table.row();
        table.add(new Label("Top players in a Week Rating get additional crystals every Monday", skin, "default"))
                .left().padLeft(40);
        table.row();
        table.add(new Label("4. Buy crystals", skin, "default")).left();
        table.row();
        table.add(new LinkedLabel("You can buy crystals ", " here", "", skin, "default", new Runnable() {
            @Override
            public void run() {
                System.out.println("Hey-Hey!");
            }
        })).left().padLeft(40);
    }
}
