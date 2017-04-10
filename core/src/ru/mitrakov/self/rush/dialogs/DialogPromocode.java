package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogPromocode extends DialogFeat {
    private final Model model;
    private final TextField field;

    public DialogPromocode(final Model model, Skin skin, String windowStyleName, I18NBundle i18n) {
        super(i18n.format("dialog.promocode.header"), skin, windowStyleName);
        assert model != null;
        this.model = model;

        Table table = getContentTable();
        assert table != null;
        table.pad(20);
        table.add(new Label(i18n.format("dialog.promocode.text"), skin, "default"));
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

        button(i18n.format("ok"));
    }

    @Override
    public Dialog show(Stage stage) {
        field.setText(model.promocode);
        return super.show(stage);
    }
}
