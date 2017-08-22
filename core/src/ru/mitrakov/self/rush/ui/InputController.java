package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Input.Keys;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.model.Cells.CellObject;

import static com.badlogic.gdx.Gdx.input;

/**
 * InputController is responsible for mouse, touch screen or keyboard input
 * This class is a [logical] part of Gui class (just extracted to reduce the source file size)
 * @author mitrakov
 */
class InputController {

    private final Model model;
    private boolean curDirRight = true;
    private boolean movesAllowed = true;

    /**
     * Creates a new instance of InputController
     * @param model - model (NON-NULL)
     */
    InputController(Model model) {
        assert model != null;
        this.model = model;
    }

    /**
     * Checks SPACE or 1234567890 input (only keyboard)
     */
    void checkInput() {
        if (input.isKeyJustPressed(Keys.SPACE)) model.useThing();
        else if (input.isKeyJustPressed(Keys.NUM_1)) model.useAbility(0);
        else if (input.isKeyJustPressed(Keys.NUM_2)) model.useAbility(1);
        else if (input.isKeyJustPressed(Keys.NUM_3)) model.useAbility(2);
        else if (input.isKeyJustPressed(Keys.NUM_4)) model.useAbility(3);
        else if (input.isKeyJustPressed(Keys.NUM_5)) model.useAbility(4);
        else if (input.isKeyJustPressed(Keys.NUM_6)) model.useAbility(5);
        else if (input.isKeyJustPressed(Keys.NUM_7)) model.useAbility(6);
        else if (input.isKeyJustPressed(Keys.NUM_8)) model.useAbility(7);
        else if (input.isKeyJustPressed(Keys.NUM_9)) model.useAbility(8);
        else if (input.isKeyJustPressed(Keys.NUM_0)) model.useAbility(9);
        /* @note uncomment only for debug! else if (input.isKeyJustPressed(Keys.P)) {
            Network.TMP_NO_CONNECTION = !Network.TMP_NO_CONNECTION;
            log("TMP_NO_CONNECTION = ", Network.TMP_NO_CONNECTION);
        }*/
    }

    /**
     * Checks movement (mouse, touch screen, keyboard)
     * @param mouseButton - current pressed mouse button
     * @param x - x mouse position
     * @param y - y mouse position
     * @return true, if a movement registered
     */
    boolean checkMovement(int mouseButton, float x, float y) {
        CellObject actor = model.curActor; // copy to local to avoid Null-Exceptions
        if (actor != null) {
            // MOVEMENT HANDLING (restricted by TOUCH_DELAY intervals)
            if (movesAllowed) {
                if (mouseButton >= 0) {
                    // get server oriented coordinates
                    int touchX = Gui.convertXFromScreenToModel(x);
                    int touchY = Gui.convertYFromScreenToModel(y);

                    // check
                    if (touchX < actor.getX() && touchY < actor.getY()) return moveLeftUp();
                    else if (touchX < actor.getX() && touchY > actor.getY()) return moveLeftDown();
                    else if (touchX > actor.getX() && touchY < actor.getY()) return moveRightUp();
                    else if (touchX > actor.getX() && touchY > actor.getY()) return moveRightDown();
                    else if (touchY < actor.getY()) return moveUp();
                    else if (touchY > actor.getY()) return moveDown();
                    else if (touchX < actor.getX()) return moveLeft();
                    else if (touchX > actor.getX()) return moveRight();
                } else if (input.isKeyPressed(Keys.W) && input.isKeyPressed(Keys.A)) return moveLeftUp();
                else if (input.isKeyPressed(Keys.S) && input.isKeyPressed(Keys.A)) return moveLeftDown();
                else if (input.isKeyPressed(Keys.W) && input.isKeyPressed(Keys.D)) return moveRightUp();
                else if (input.isKeyPressed(Keys.S) && input.isKeyPressed(Keys.D)) return moveRightDown();
                else if (input.isKeyPressed(Keys.W)) return moveUp();
                else if (input.isKeyPressed(Keys.S)) return moveDown();
                else if (input.isKeyPressed(Keys.A)) return moveLeft();
                else if (input.isKeyPressed(Keys.D)) return moveRight();
                else if (input.isKeyPressed(Keys.UP) && input.isKeyPressed(Keys.LEFT)) return moveLeftUp();
                else if (input.isKeyPressed(Keys.DOWN) && input.isKeyPressed(Keys.LEFT)) return moveLeftDown();
                else if (input.isKeyPressed(Keys.UP) && input.isKeyPressed(Keys.RIGHT)) return moveRightUp();
                else if (input.isKeyPressed(Keys.DOWN) && input.isKeyPressed(Keys.RIGHT)) return moveRightDown();
                else if (input.isKeyPressed(Keys.UP)) return moveUp();
                else if (input.isKeyPressed(Keys.DOWN)) return moveDown();
                else if (input.isKeyPressed(Keys.LEFT)) return moveLeft();
                else if (input.isKeyPressed(Keys.RIGHT)) return moveRight();
            }
        }
        return false;
    }

    /**
     * Allows or denies movement input listening (doesn't affect UseThing or UseAbility input listening)
     * @param movesAllowed - true to turn on input listening
     */
    void setMovesAllowed(boolean movesAllowed) {
        this.movesAllowed = movesAllowed;
    }

    /**
     * Moves down
     * @return true
     */
    private boolean moveDown() {
        model.move(curDirRight ? Model.MoveDirection.RightDown : Model.MoveDirection.LeftDown);
        return true;
    }

    /**
     * Moves up
     * @return true
     */
    private boolean moveUp() {
        model.move(curDirRight ? Model.MoveDirection.RightUp : Model.MoveDirection.LeftUp);
        return true;
    }

    /**
     * Moves left
     * @return true
     */
    private boolean moveLeft() {
        curDirRight = false;
        model.move(Model.MoveDirection.Left);
        return true;
    }

    /**
     * Moves right
     * @return true
     */
    private boolean moveRight() {
        curDirRight = true;
        model.move(Model.MoveDirection.Right);
        return true;
    }

    /**
     * Moves down (if impossible, then left)
     * @return true
     */
    private boolean moveLeftDown() {
        curDirRight = false;
        model.move(Model.MoveDirection.LeftDown);
        return true;
    }

    /**
     * Moves up (if impossible, then left)
     * @return true
     */
    private boolean moveLeftUp() {
        curDirRight = false;
        model.move(Model.MoveDirection.LeftUp);
        return true;
    }

    /**
     * Moves down (if impossible, then right)
     * @return true
     */
    private boolean moveRightDown() {
        curDirRight = true;
        model.move(Model.MoveDirection.RightDown);
        return true;
    }

    /**
     * Moves up (if impossible, then right)
     * @return true
     */
    private boolean moveRightUp() {
        curDirRight = true;
        model.move(Model.MoveDirection.RightUp);
        return true;
    }
}
