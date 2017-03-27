package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogTraining extends DialogFeat {
    private final class Item {
        Drawable image;
        String text1;
        String text2;

        Item(TextureRegion image, String text1, String text2) {
            this.image = image != null ? new TextureRegionDrawable(image) : null;
            this.text1 = text1;
            this.text2 = text2;
        }
    }

    private final Image imgMessage;
    private final Label lblMessage1;
    private final Label lblMessage2;
    private final Queue<Item> items = new Queue<Item>(5);

    public DialogTraining(Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);

        imgMessage = new Image();
        lblMessage1 = new Label("", skin, "default");
        lblMessage1.setAlignment(Align.center, Align.center);
        lblMessage2 = new Label("", skin, "default");
        lblMessage2.setAlignment(Align.center, Align.center);

        setMovable(false);
        buildTable();
    }

    public DialogTraining show(Stage stage) {
        assert stage != null;
        setWidth(stage.getWidth() / 2);
        setHeight(stage.getHeight() / 2 + 20);
        setPosition(stage.getWidth() - getWidth() - 5, stage.getHeight() - getHeight() - 5);
        stage.addActor(this);
        return this;
    }

    public DialogTraining addMessage(TextureRegion image, String text1, String text2) {
        assert text1 != null && text2 != null; // image may be NULL
        items.addLast(new Item(image, text1, text2));
        return this;
    }

    public void next() {
        if (items.size > 0) {
            Item item = items.removeFirst(); // item is always != NULL
            imgMessage.setDrawable(item.image);
            lblMessage1.setText(item.text1);
            lblMessage2.setText(item.text2);
        }
    }

    private void buildTable() {
        pad(5);
        add(imgMessage);
        row().space(5);
        add(lblMessage1);
        row().spaceTop(10);
        add(lblMessage2);
    }
}
