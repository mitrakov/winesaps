package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.utils.TimeUtils;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 27.02.2017
 */

class InputController {

    private static final int TOUCH_DELAY = 250;

    private final Model model;
    private long time = 0;

    InputController(Model model) {
        assert model != null;
        this.model = model;
    }

    void checkInput(float x, float y) {
        CellObject actor = model.curActor; // copy to local to avoid Null-Exceptions
        if (actor != null) {
            if (TimeUtils.timeSinceMillis(time) > TOUCH_DELAY) {
                // getting coordinates
                int touchX = Gui.convertXFromScreenToModel(x);
                int touchY = Gui.convertYFromScreenToModel(y);

                // check
                if (touchY < actor.getY()) moveUp();
                else if (touchY > actor.getY()) moveDown();
                else if (touchX > actor.getX()) moveRight();
                else if (touchX < actor.getX()) moveLeft();
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
