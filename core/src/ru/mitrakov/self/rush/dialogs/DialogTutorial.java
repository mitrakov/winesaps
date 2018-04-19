package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.mitrakov.self.rush.ui.LabelFeat;

/**
 * Tutorial dialog (in fact just a window)
 * @author Mitrakov
 */
public class DialogTutorial extends Window {
    /** Single tip with a description text, picture and action text */
    static private final class Item {
        /** Picture */
        Drawable image;
        /** Description text (e.g. "In order to do something, you should do something") */
        String text1;
        /** Action text (e.g. "Now go left and do something") */
        String text2;

        /**
         * Creates a new tip
         * @param image picture
         * @param text1 description text
         * @param text2 action text
         */
        Item(TextureRegion image, String text1, String text2) {
            this.image = image != null ? new TextureRegionDrawable(image) : null;
            this.text1 = text1;
            this.text2 = text2;
        }
    }

    /** Picture */
    private final Image imgMessage;
    /** Description text label (e.g. "In order to do something, you should do something") */
    private final Label lblMessage1;
    /** Action text label (e.g. "Now go left and do something") */
    private final Label lblMessage2;
    /** Queue that contains all the tips to show */
    private final Queue<Item> items = new Queue<Item>(5);

    /**
     * Creates a new Tutorial dialog (in fact it's a window, not a dialog, because it won't block the other controls)
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     */
    public DialogTutorial(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        imgMessage = new Image();
        lblMessage1 = new LabelFeat("", skin, "small", true);
        lblMessage2 = new LabelFeat("", skin, "default", true);

        setMovable(false);
        buildTable(skin);
    }

    /**
     * Shows the dialog
     * <br><b>Note:</b> Since in fact this is just a window, it won't block the other controls on the stage
     * @see #next()
     * @param stage stage to show the dialog on
     * @return this
     */
    public DialogTutorial show(Stage stage) {
        assert stage != null;
        setWidth(stage.getWidth() / 2);
        setHeight(stage.getHeight() * 2 / 3);
        setPosition(stage.getWidth() - getWidth() - 5, stage.getHeight() - getHeight() - 5);
        stage.addActor(this);
        return this;
    }

    /**
     * Adds new tip to the collection of tips
     * @param image picture describing the situation
     * @param text1 description text (e.g. "In order to do something, you should do something")
     * @param text2 action text (e.g. "Now go left and do something")
     * @return this
     */
    public DialogTutorial addMessage(TextureRegion image, String text1, String text2) {
        assert text1 != null && text2 != null; // image may be NULL
        items.addLast(new Item(image, text1, text2));
        return this;
    }

    /**
     * Clears the tips queue
     * @return this
     */
    public DialogTutorial clearMessages() {
        items.clear();
        return this;
    }

    /**
     * Shows the next tip
     * @see #addMessage(TextureRegion, String, String)
     */
    public void next() {
        if (items.size > 0) {
            Item item = items.removeFirst(); // item is always != NULL
            imgMessage.setDrawable(item.image);
            lblMessage1.setText(item.text1);
            lblMessage2.setText(item.text2);
        }
    }

    /**
     * Builds internal content table
     * @param skin LibGdx skin
     */
    private void buildTable(Skin skin) {
        assert skin != null;

        pad(5);
        add(imgMessage).expand();
        row();
        add(lblMessage1).expand();
        row();
        add(new Image(skin, "splitpane")).width(250).height(2);
        row();
        add(lblMessage2).expand();
    }
}
