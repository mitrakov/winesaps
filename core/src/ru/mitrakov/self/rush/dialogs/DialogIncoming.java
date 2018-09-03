package ru.mitrakov.self.rush.dialogs;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.ui.*;
import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.AudioManager;

/**
 * "Incoming call" dialog
 * @author Mitrakov
 */
public class DialogIncoming extends DialogFeat {
    /** Buttons result */
    private enum Result {Accept, Reject, Ignore}

    /** Reference to the model */
    private final Model model;
    /** Reference to Audio Manager */
    private final AudioManager audioManager;
    /** "{0} challenges you. Accept?" label */
    private final Label lblQuestion;

    /** Enemy name */
    private String enemy = "";
    /** Enemy Session ID (Server wants us to put this ID into response) */
    private int enemySid = 0;
    /** LibGdx internationalization bundle */
    private I18NBundle i18n;

    /**
     * Creates a new "Incoming call" dialog with 3 options: Accept, Reject, Ignore
     * @param model {@link Model}
     * @param skin LibGdx skin
     * @param windowStyleName style name (usually just "default")
     * @param audioManager {@link AudioManager}
     * @param i18n LibGdx internationalization bundle
     */
    public DialogIncoming(Model model, Skin skin, String windowStyleName, AudioManager audioManager, I18NBundle i18n) {
        super("", skin, windowStyleName, true);
        assert model != null && audioManager != null && i18n != null;
        this.model = model;
        this.audioManager = audioManager;
        this.i18n = i18n;

        lblQuestion = new LabelFeat("", skin, "default", true);

        button("Accept", Result.Accept); // text will be replaced in onLocaleChanged()
        button("Reject", Result.Reject); // text will be replaced in onLocaleChanged()
        button("Ignore", Result.Ignore); // text will be replaced in onLocaleChanged()

        Table table = getContentTable();

        table.pad(10).add(lblQuestion);
    }

    @Override
    public Dialog show(Stage stage) {
        audioManager.sound("call");
        lblQuestion.setText(i18n.format("dialog.incoming.text", enemy)); // i18n != NULL (assert omitted)
        pack();
        return super.show(stage);
    }

    @Override
    protected void result(Object object) {
        assert object instanceof Result; // stackoverflow.com/questions/2950319
        switch ((Result) object) {
            case Accept:
                model.accept(enemySid);
                break;
            case Reject:
                model.reject(enemySid);
                break;
            default:
        }
    }

    @Override
    public void onLocaleChanged(I18NBundle bundle) {
        assert bundle != null;
        this.i18n = bundle;

        if (getTitleLabel() != null)
            getTitleLabel().setText(bundle.format("dialog.incoming.header"));
        if (getButtonTable() != null) {
            Array<Actor> buttons = getButtonTable().getChildren();
            assert buttons != null;
            if (buttons.size == 3) {
                Actor accept = buttons.first();
                if (accept instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) accept).setText(bundle.format("dialog.incoming.accept"));
                Actor reject = buttons.get(1);
                if (reject instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) reject).setText(bundle.format("dialog.incoming.reject"));
                Actor ignore = buttons.get(2);
                if (ignore instanceof TextButton) // stackoverflow.com/questions/2950319
                    ((TextButton) ignore).setText(bundle.format("dialog.incoming.ignore"));
            }
        }
    }

    /**
     * Sets arguments for this dialog
     * @param enemy enemy name
     * @param enemySid enemy Session ID (please see Server API protocol for more details)
     * @return this
     */
    public Dialog setArguments(String enemy, int enemySid) {
        this.enemy = enemy;
        this.enemySid = enemySid;
        return this;
    }
}
