package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 27.02.2017
 */

class Controller {

    private final Model model;
    private final Camera camera;
    private Vector3 touchPos = new Vector3(); // ....

    Controller(Model model, Camera camera) {
        assert model != null && camera != null;
        this.model = model;
        this.camera = camera;
    }

    void check() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            model.signIn();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            model.invite("Bobby");
        }

        CellObject actor = model.curActor;
        if (actor != null) {
            if (Gdx.input.isTouched()) {
                int myX = actor.getXy() % Field.WIDTH;
                int myY = actor.getXy() / Field.WIDTH;
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);
                int touchX = Gui.convertXFromScreenToModel(touchPos.x);
                int touchY = Gui.convertYFromScreenToModel(touchPos.y);
                System.out.println("x = " + touchX + "; y = " + touchY);
            }
        }
    }
}
