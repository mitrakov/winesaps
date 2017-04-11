package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogAbout extends DialogFeat {

    private final Label lblOverview;
    private final Label lblCreated;
    private final Label lblSupport;
    private final Label lblWebSite;

    public DialogAbout(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        Table table = getContentTable();
        assert table != null;

        table.pad(30);
        table.add(lblOverview = new Label("", skin, "default"));
        table.row().space(20);
        table.add(lblCreated = new Label("", skin, "default"));
        table.row();
        table.add(lblSupport = new Label("", skin, "default")).left();
        table.row();
        table.add(lblWebSite = new Label("", skin, "default")).left();
        table.row();

        button("Close"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblOverview.setText(bundle.format("dialog.about.overview"));
        lblCreated.setText(bundle.format("dialog.about.created"));
        lblSupport.setText(bundle.format("dialog.about.support"));
        lblWebSite.setText(bundle.format("dialog.about.web"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.about.header"));
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
}
