package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.Winesaps;
import ru.mitrakov.self.rush.ui.DialogFeat;
import ru.mitrakov.self.rush.ui.LinkedLabel;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogAbout extends DialogFeat {

    private final Label lblOverview;
    private final Label lblCreated;
    private final Label lblCreatedTxt;
    private final Label lblVersion;
    private final Label lblSupport;
    private final Label lblSupportTxt;
    private final Label lblWebSite;
    private final LinkedLabel lblWebSiteTxt;
    private final Label lblPlatforms;
    private final Label lblPlatformsTxt;

    public DialogAbout(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        Table table = getContentTable();
        assert table != null;

        table.pad(20);
        table.add(lblOverview = new Label("", skin, "title")).colspan(2);
        table.row();
        table.add(new Image(skin, "splitpane")).width(250).height(2).spaceBottom(20).colspan(2);
        table.row();
        table.add(lblCreated = new Label("", skin, "default")).left();
        table.add(lblCreatedTxt = new Label("", skin, "default")).left().spaceLeft(20);
        table.row();
        table.add(lblVersion = new Label("", skin, "default")).left();
        table.add(new Label(Winesaps.VERSION_STR, skin, "default")).left().spaceLeft(20);
        table.row();
        table.add(lblSupport = new Label("", skin, "default")).left();
        table.add(lblSupportTxt = new Label("", skin, "default")).left().spaceLeft(20);
        table.row();
        table.add(lblWebSite = new Label("", skin, "default")).left();
        table.add(lblWebSiteTxt = new LinkedLabel("", "", "", skin, "default", "link", new Runnable() {
            @Override
            public void run() {
                Gdx.net.openURI(Winesaps.URL);
            }
        })).left().spaceLeft(20);
        table.row();
        table.add(lblPlatforms = new Label("", skin, "default")).left();
        table.add(lblPlatformsTxt = new Label("", skin, "default")).left().spaceLeft(20);

        button("Close"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        lblOverview.setText(bundle.format("dialog.about.overview"));
        lblCreated.setText(bundle.format("dialog.about.created"));
        lblCreatedTxt.setText(bundle.format("dialog.about.created.txt"));
        lblVersion.setText(bundle.format("dialog.about.version"));
        lblSupport.setText(bundle.format("dialog.about.support"));
        lblSupportTxt.setText(bundle.format("dialog.about.support.txt"));
        lblWebSite.setText(bundle.format("dialog.about.web"));
        lblWebSiteTxt.setText("", bundle.format("dialog.about.web.txt"), "");
        lblPlatforms.setText(bundle.format("dialog.about.platforms"));
        lblPlatformsTxt.setText(bundle.format("dialog.about.platforms.txt"));

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.about.header"));
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
}
