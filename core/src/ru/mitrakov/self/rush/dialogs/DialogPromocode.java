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
    private final Label label;

    public DialogPromocode(final Model model, Skin skin, String windowStyleName) {
        super("", skin, windowStyleName);
        assert model != null;
        this.model = model;

        Table table = getContentTable();
        assert table != null;
        table.pad(20);
        table.add(label = new Label("", skin, "default"));
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

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public Dialog show(Stage stage) {
        field.setText(model.promocode);
        return super.show(stage);
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        label.setText(bundle.format("dialog.promocode.text"));
        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.promocode.header"));
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
}
