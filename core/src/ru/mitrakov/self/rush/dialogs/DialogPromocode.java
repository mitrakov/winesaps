package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.AudioManager;
import ru.mitrakov.self.rush.model.Model;

/**
 * "Promocode" dialog
 * @author Mitrakov
 */
public class DialogPromocode extends DialogFeat {
    /** Reference to the model */
    private final Model model;
    /** "Your promo code" label */
    private final Label labelIntro;
    /** Text area with a promo code */
    private final TextField fieldPromocode;
    /** "Copy to clipboard" button */
    private final TextButton btnCopy;
    /** "Copied to clipboard" label */
    private final Label labelCopied;

    /**
     * Creates new "Promocode" dialog
     * @param model {@link Model}
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param audioManager {@link AudioManager}
     */
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
        }}).minWidth(250);
        table.row().spaceTop(10);
        table.add(labelCopied = new Label("", skin, "default") {{
            setVisible(false);
        }});
        table.row();
        table.add(btnCopy = new TextButtonFeat("", skin, "default", audioManager, new Runnable() {
            @Override
            public void run() {
                btnCopy.setVisible(false);
                Gdx.app.getClipboard().setContents(model.promocode);
                labelCopied.addAction(
                        sequence(fadeIn(.1f), Actions.show(), fadeOut(2, Interpolation.fade), Actions.hide()));
            }
        }));

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
