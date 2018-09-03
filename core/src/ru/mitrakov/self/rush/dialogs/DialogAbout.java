package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.Winesaps;

/**
 * "About" dialog
 * @author Mitrakov
 */
public class DialogAbout extends DialogFeat {
    /** Big "Winesaps" label */
    private final Label lblOverview;
    /** "Created by" label */
    private final Label lblCreated;
    /** "Mitrakov Artem" label */
    private final Label lblCreatedTxt;
    /** "Version" label */
    private final Label lblVersion;
    /** "Support" label */
    private final Label lblSupport;
    /** "support@winesaps.com" label */
    private final Label lblSupportTxt;
    /** "Website" label */
    private final Label lblWebSite;
    /** "https://winesaps.com" label */
    private final LinkedLabel lblWebSiteTxt;
    /** "Platforms" label */
    private final Label lblPlatforms;
    /** "Windows, Linux, Android" label */
    private final Label lblPlatformsTxt;

    /**
     * Creates new "About" dialog
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogAbout(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName, true);

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
