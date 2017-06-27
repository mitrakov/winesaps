package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Input.Keys;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.Network;
import ru.mitrakov.self.rush.model.Cells.CellObject;

import static com.badlogic.gdx.Gdx.input;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * Created by mitrakov on 27.02.2017
 */
class InputController {

    private final Model model;
    private boolean curDirRight = true;
    private boolean movesAllowed = true;

    InputController(Model model) {
        assert model != null;
        this.model = model;
    }

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
        else if (input.isKeyJustPressed(Keys.P)) {
            Network.TMP_NO_CONNECTION = !Network.TMP_NO_CONNECTION;
            log("TMP_NO_CONNECTION = ", Network.TMP_NO_CONNECTION);
        }
    }

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

    void setMovesAllowed(boolean movesAllowed) {
        this.movesAllowed = movesAllowed;
    }

    private boolean moveDown() {
        model.move(curDirRight ? Model.MoveDirection.RightDown : Model.MoveDirection.LeftDown);
        return true;
    }

    private boolean moveUp() {
        model.move(curDirRight ? Model.MoveDirection.RightUp : Model.MoveDirection.LeftUp);
        return true;
    }

    private boolean moveLeft() {
        curDirRight = false;
        model.move(Model.MoveDirection.Left);
        return true;
    }

    private boolean moveRight() {
        curDirRight = true;
        model.move(Model.MoveDirection.Right);
        return true;
    }

    private boolean moveLeftDown() {
        curDirRight = false;
        model.move(Model.MoveDirection.LeftDown);
        return true;
    }

    private boolean moveLeftUp() {
        curDirRight = false;
        model.move(Model.MoveDirection.LeftUp);
        return true;
    }

    private boolean moveRightDown() {
        curDirRight = true;
        model.move(Model.MoveDirection.RightDown);
        return true;
    }

    private boolean moveRightUp() {
        curDirRight = true;
        model.move(Model.MoveDirection.RightUp);
        return true;
    }
}
