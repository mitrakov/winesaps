package ru.mitrakov.self.rush;

import com.badlogic.gdx.utils.TimeUtils;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 27.02.2017
 */

class InputController {

    private static final int TOUCH_DELAY = 250;

    private final Model model;
    private long time = TimeUtils.millis();

    InputController(Model model) {
        assert model != null;
        this.model = model;
    }

    void checkInput(float x, float y) {
        CellObject actor = model.curActor;
        if (actor != null) {
            if (TimeUtils.timeSinceMillis(time) > TOUCH_DELAY) {
                // getting coordinates
                int myX = actor.getXy() % Field.WIDTH;
                int myY = actor.getXy() / Field.WIDTH;
                int touchX = Gui.convertXFromScreenToModel(x);
                int touchY = Gui.convertYFromScreenToModel(y);

                // check
                if (touchY < myY) moveUp();
                else if (touchY > myY) moveDown();
                else if (touchX > myX) moveRight();
                else if (touchX < myX) moveLeft();
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
