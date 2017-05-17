package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import ru.mitrakov.self.rush.AudioManager;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.ui.DialogFeat;
import ru.mitrakov.self.rush.ui.TextButtonFeat;

/**
 * Created by mitrakov on 05.03.2017
 */
public class DialogPromocode extends DialogFeat {
    private final Model model;
    private final Label labelIntro;
    private final TextField fieldPromocode;
    private final TextButton btnCopy;
    private final Label labelCopied;

    public DialogPromocode(final Model model, Skin skin, String windowStyleName, AudioManager audioManager) {
        super("", skin, windowStyleName);
        assert model != null && audioManager != null;
        this.model = model;

        Table table = getContentTable();
        assert table != null;
        table.pad(20);
        table.add(labelIntro = new Label("", skin, "default"));
        table.row().spaceTop(10);
        table.add(fieldPromocode = new TextField("", skin, "default") {{
            setAlignment(Align.center);
            setOnscreenKeyboard(new OnscreenKeyboard() {
                @Override
                public void show(boolean visible) {
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
            });
            addListener(new ChangeListener() { // make the text field readonly (P.S. who knows another way?)
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    event.cancel();
                }
            });
        }}).width(250);
        table.row().spaceTop(10);
        table.add(labelCopied = new Label("", skin, "default") {{
            setVisible(false);
        }});
        table.row();
        table.add(btnCopy = new TextButtonFeat("", skin, "default", audioManager) {{
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    btnCopy.setVisible(false);
                    Gdx.app.getClipboard().setContents(model.promocode);
                    labelCopied.addAction(
                            sequence(fadeIn(.1f), Actions.show(), fadeOut(2, Interpolation.fade), Actions.hide()));
                }
            });
        }});

        button("OK"); // text will be replaced in onLocaleChanged()
    }

    @Override
    public Dialog show(Stage stage) {
        fieldPromocode.setText(model.promocode);
        btnCopy.setVisible(true);
        return super.show(stage);
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;

        labelIntro.setText(bundle.format("dialog.promocode.text"));
        btnCopy.setText(bundle.format("copy"));
        labelCopied.setText(bundle.format("dialog.promocode.copied"));
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
