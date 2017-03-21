package ru.mitrakov.self.rush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 27.02.2017
 */

class InputController {

    public static final int TOUCH_DELAY = 250;

    private final Model model;
    private long time = 0;

    InputController(Model model) {
        assert model != null;
        this.model = model;
    }

    void checkInput(int mouseButton, float x, float y) {
        CellObject actor = model.curActor; // copy to local to avoid Null-Exceptions
        if (actor != null) {
            // MOVEMENT HANDLING (restricted by TOUCH_DELAY intervals)
            if (TimeUtils.timeSinceMillis(time) > TOUCH_DELAY) {
                if (mouseButton >= 0) {
                    // getting coordinates
                    int touchX = Gui.convertXFromScreenToModel(x);
                    int touchY = Gui.convertYFromScreenToModel(y);

                    // check
                    if (touchY < actor.getY()) moveUp();
                    else if (touchY > actor.getY()) moveDown();
                    else if (touchX > actor.getX()) moveRight();
                    else if (touchX < actor.getX()) moveLeft();
                } else if (Gdx.input.isKeyPressed(Input.Keys.W)) moveUp();
                else if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDown();
                else if (Gdx.input.isKeyPressed(Input.Keys.A)) moveLeft();
                else if (Gdx.input.isKeyPressed(Input.Keys.D)) moveRight();
                else if (Gdx.input.isKeyPressed(Input.Keys.UP)) moveUp();
                else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveDown();
                else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveLeft();
                else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveRight();
            }
            // NON-MOVEMENT HANDLING (may be handled at any time)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) model.useThing();
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) model.useAbility(0);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) model.useAbility(1);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) model.useAbility(2);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) model.useAbility(3);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) model.useAbility(4);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) model.useAbility(5);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) model.useAbility(6);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) model.useAbility(7);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) model.useAbility(8);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) model.useAbility(9);
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
