package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Simple "Battle Finished" dialog (usually used in Training)
 * @see DialogFinishedEx
 * @author Mitrakov
 */
public class DialogFinished extends DialogFeat {
    /** Current message (e.g. "You won the round!") */
    final Label lblMessage;

    /**
     * Creates new "Battle Finished" dialog (usually used in Training)
     * @see DialogFinishedEx
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param atlas atlas that contains all the textures needed
     */
    public DialogFinished(Skin skin, String windowStyleName, TextureAtlas atlas) {
        super("", skin, windowStyleName, true);
        assert atlas != null;

        lblMessage = new Label("", skin, "default");

        Table table = getContentTable();
        assert table != null;
        table.pad(10).clear();
        table.add(new Image(new TextureRegionDrawable(atlas.findRegion("battleWin"))));
        table.row();
        table.add(lblMessage);

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 1) {
                Actor actor = buttons.first();
                if (actor instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) actor).setText(bundle.format("ok"));
            }
        }
    }

    /**
     * Sets the dialog's header and body message
     * @param header title
     * @param msg body message
     * @return this
     */
    public DialogFinished setText(String header, String msg) {
        if (getTitleLabel() != null)
            getTitleLabel().setText(header);
        lblMessage.setText(msg);
        return this;
    }
}
