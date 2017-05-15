package ru.mitrakov.self.rush.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.Network;
import ru.mitrakov.self.rush.model.object.CellObject;
import static ru.mitrakov.self.rush.utils.SimpleLogger.*;

/**
 * Created by mitrakov on 27.02.2017
 */
class InputController {
    enum MoveResult {None, Left, Right, Up, Down}

    private final Model model;
    private boolean movesAllowed = true;

    InputController(Model model) {
        assert model != null;
        this.model = model;
    }

    void checkInput() {
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
        else if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            Network.TMP_NO_CONNECTION = !Network.TMP_NO_CONNECTION;
            log("TMP_NO_CONNECTION = " + Network.TMP_NO_CONNECTION);
        }
    }

    MoveResult checkMovement(int mouseButton, float x, float y) {
        CellObject actor = model.curActor; // copy to local to avoid Null-Exceptions
        if (actor != null) {
            // MOVEMENT HANDLING (restricted by TOUCH_DELAY intervals)
            if (movesAllowed) {
                if (mouseButton >= 0) {
                    // getting coordinates
                    int touchX = Gui.convertXFromScreenToModel(x);
                    int touchY = Gui.convertYFromScreenToModel(y);

                    // check
                    if (touchY < actor.getY()) return moveUp();
                    else if (touchY > actor.getY()) return moveDown();
                    else if (touchX > actor.getX()) return moveRight();
                    else if (touchX < actor.getX()) return moveLeft();
                } else if (Gdx.input.isKeyPressed(Input.Keys.W)) return moveUp();
                else if (Gdx.input.isKeyPressed(Input.Keys.S)) return moveDown();
                else if (Gdx.input.isKeyPressed(Input.Keys.A)) return moveLeft();
                else if (Gdx.input.isKeyPressed(Input.Keys.D)) return moveRight();
                else if (Gdx.input.isKeyPressed(Input.Keys.UP)) return moveUp();
                else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) return moveDown();
                else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) return moveLeft();
                else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) return moveRight();
            }
        }
        return MoveResult.None;
    }

    void setMovesAllowed(boolean movesAllowed) {
        this.movesAllowed = movesAllowed;
    }

    private MoveResult moveLeft() {
        model.moveLeft();
        return MoveResult.Left;
    }

    private MoveResult moveRight() {
        model.moveRight();
        return MoveResult.Right;
    }

    private MoveResult moveUp() {
        model.moveUp();
        return MoveResult.Up;
    }

    private MoveResult moveDown() {
        model.moveDown();
        return MoveResult.Down;
    }
}
