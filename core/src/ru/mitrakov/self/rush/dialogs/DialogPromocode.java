package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.model.Model;

/**
 * Created by mitrakov on 05.03.2017
 */

public class DialogPromocode extends Dialog {
    private final Model model;
    private final TextField field;

    public DialogPromocode(final Model model, Skin skin, String windowStyleName) {
        super("Promocode", skin, windowStyleName);
        assert model != null;
        this.model = model;

        Table table = getContentTable();
        assert table != null;
        table.pad(20);
        table.add(new Label("Your promo code is:", skin, "default"));
        table.row().space(10);
        table.add(field = new TextField("", skin, "default") {{
            setAlignment(Align.center);
            addListener(new ChangeListener() { // make the text field readonly (P.S. who knows another way?)
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    event.cancel();
                }
            });
        }});

        button("OK");
    }

    @Override
    public Dialog show(Stage stage) {
        field.setText(model.promocode);
        return super.show(stage);
    }
}
