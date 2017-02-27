package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 27.02.2017
 */

class Controller {

    private final Model model;
    private final Camera camera;
    private Vector3 touchPos = new Vector3(); // ....
    private long time = TimeUtils.millis();

    Controller(Model model, Camera camera) {
        assert model != null && camera != null;
        this.model = model;
        this.camera = camera;
    }

    void checkInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            model.signIn();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            model.invite("Bobby");
        }

        CellObject actor = model.curActor;
        if (actor != null) {
            if (TimeUtils.timeSinceMillis(time) > 250) {
                if (Gdx.input.isTouched()) {
                    // getting actor's coordinates
                    int myX = actor.getXy() % Field.WIDTH;
                    int myY = actor.getXy() / Field.WIDTH;

                    // getting touch coordinates
                    touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                    camera.unproject(touchPos);
                    int touchX = Gui.convertXFromScreenToModel(touchPos.x);
                    int touchY = Gui.convertYFromScreenToModel(touchPos.y);

                    // check
                    if (touchY > myY) moveDown();
                    else if (touchY < myY) moveUp();
                    else if (touchX > myX) moveRight();
                    else if (touchX < myX) moveLeft();
                } else if (Gdx.input.isKeyPressed(Input.Keys.D)) moveRight();
                else if (Gdx.input.isKeyPressed(Input.Keys.A)) moveLeft();
                else if (Gdx.input.isKeyPressed(Input.Keys.W)) moveUp();
                else if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDown();
                else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveRight();
                else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveLeft();
                else if (Gdx.input.isKeyPressed(Input.Keys.UP)) moveUp();
                else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveDown();
            }
        }
    }

    private void moveLeft() {
        model.moveLeft();
        time = TimeUtils.millis();
    }
    private void moveRight() {
        model.moveRight();
        time = TimeUtils.millis();
    }
    private void moveUp() {
        model.moveUp();
        time = TimeUtils.millis();
    }
    private void moveDown() {
        model.moveDown();
        time = TimeUtils.millis();
    }
}
