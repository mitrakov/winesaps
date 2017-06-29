package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import ru.mitrakov.self.rush.Localizable;

/**
 * Created by mitrakov on 27.03.2017
 */
public abstract class DialogFeat extends Dialog implements Localizable {
    public static void hideAll(Stage stage) {
        assert stage != null;
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Window)
                actor.remove(); // It is SAFE! See SnapshotArray<T>
        }
    }

    private Runnable onResultAction;

    public DialogFeat(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);

        // set up button styles
        getButtonTable().defaults().minWidth(120).height(46).padLeft(10).padRight(10);

        // add 'close-on-esc' feature
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                Array<Actor> children = getButtonTable().getChildren();
                assert children != null;
                if (keycode == Input.Keys.ESCAPE || (keycode == Input.Keys.ENTER && children.size == 1)) {
                    // programmatically 'push' the last button
                    if (children.size > 0) {
                        assert children.peek() != null;
                        children.peek().fire(new ChangeListener.ChangeEvent());
                    }
                }
                return super.keyDown(event, keycode);
            }
        });
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

    public DialogFeat setOnResultAction(Runnable f) {
        onResultAction = f;
        return this;
    }
}
