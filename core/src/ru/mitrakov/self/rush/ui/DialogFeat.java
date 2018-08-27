package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import ru.mitrakov.self.rush.Localizable;

/**
 * Gdx Dialog with extra features added
 * @see com.badlogic.gdx.scenes.scene2d.ui.Dialog
 * @author mitrakov
 */
public abstract class DialogFeat extends Dialog implements Localizable {
    /**
     * Static method to close all dialogs and bring a user back to the screen
     * @param stage LibGdx stage
     */
    public static void hideAll(Stage stage) {
        assert stage != null;
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog)
                actor.remove(); // It is SAFE to remove inside iterator! See SnapshotArray<T>
        }
    }

    /** Function to run when a user closes the dialog (may be NULL) */
    private Runnable onResultAction;

    /**
     * Creates a new instance of DialogFeat
     * @param title title
     * @param skin LibGdx skin
     * @param windowStyleName style name (default is "default")
     */
    public DialogFeat(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);
        getButtonTable().defaults().minWidth(120).height(46).padLeft(10).padRight(10);
        key(Input.Keys.ESCAPE, null);
    }

    @Override
    protected void result(Object object) {
        super.result(object);
        if (onResultAction != null)
            onResultAction.run();
    }

    @Override
    public void hide() {
        hide(null); // null = close immediately (without fadeOut)
    }

    /**
     * Sets the additional action when a user closes the dialog
     * @param f function to call
     * @return "this"
     */
    public DialogFeat setOnResultAction(Runnable f) {
        onResultAction = f;
        return this;
    }
}
