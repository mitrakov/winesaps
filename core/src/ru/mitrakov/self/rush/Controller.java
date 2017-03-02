package ru.mitrakov.self.rush;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.TimeUtils;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 27.02.2017
 */

class Controller {

    private static final int TOUCH_DELAY = 250;

    private final Model model;
    private final Camera camera;
    private final Vector3 touchPos = new Vector3(); // ....
    private long time = TimeUtils.millis();

    Controller(Model model, Camera camera) {
        assert model != null && camera != null;
        this.model = model;
        this.camera = camera;
    }

    void checkInput(Gui gui) {
        // gui must NOT be NULL (assert omitted)

        // tmp code
        if (model.field == null && Gdx.input.justTouched()) {
            model.invite("Bobby");
            return;
        }

        CellObject actor = model.curActor;
        if (actor != null) {
            // getting the touch position in world coordinates
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            // check
            checkThingButton(gui);
            checkAbilityButtons(gui);
            checkMove(actor);
        }
    }

    private void checkThingButton(Gui gui) {
        // gui, gui.buttonThing must NOT be NULL (assert omitted)
//        if (Gdx.input.justTouched()) {
//            if (gui.buttonThing.contains(touchPos.x, touchPos.y))
//                model.useThing();
//        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) model.useThing();
    }

    private void checkAbilityButtons(Gui gui) {
        // gui, gui.buttonAbilities must NOT be NULL (assert omitted)
        if (Gdx.input.justTouched()) {
//            for (int i = 0; i < gui.buttonAbilities.size; i++) {
//                Rectangle rectangle = gui.buttonAbilities.get(i); // must NOT be NULL (assert omitted)
//                if (rectangle.contains(touchPos.x, touchPos.y)) {
//                    model.useAbilityByIndex(i);
//                }
//            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            model.useAbilityByIndex(0);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            model.useAbilityByIndex(1);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
            model.useAbilityByIndex(2);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4))
            model.useAbilityByIndex(3);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5))
            model.useAbilityByIndex(4);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6))
            model.useAbilityByIndex(5);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7))
            model.useAbilityByIndex(6);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8))
            model.useAbilityByIndex(7);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9))
            model.useAbilityByIndex(8);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0))
            model.useAbilityByIndex(9);
    }

    private void checkMove(CellObject actor) {
        if (TimeUtils.timeSinceMillis(time) > TOUCH_DELAY) {
            if (Gdx.input.isTouched()) {
//                if (touchPos.y > Gui.TOOLBAR_WIDTH) { // touched above the toolbar
//                    // getting actor's coordinates
//                    int myX = actor.getXy() % Field.WIDTH;
//                    int myY = actor.getXy() / Field.WIDTH;
//
//                    // getting touch coordinates (touchPos MUST be already unprojected!)
//                    int touchX = Gui.convertXFromScreenToModel(touchPos.x);
//                    int touchY = Gui.convertYFromScreenToModel(touchPos.y);
//
//                    // check
//                    if (touchY < myY) moveUp();
//                    else if (touchY > myY) moveDown();
//                    else if (touchX > myX) moveRight();
//                    else if (touchX < myX) moveLeft();
//                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.W)) moveUp();
            else if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDown();
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) moveRight();
            else if (Gdx.input.isKeyPressed(Input.Keys.A)) moveLeft();
            else if (Gdx.input.isKeyPressed(Input.Keys.UP)) moveUp();
            else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveDown();
            else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveRight();
            else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveLeft();
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
