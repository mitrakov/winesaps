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
    /**
     * Max possible delay to wait for MOVE-ACK and forbid sending MOVE commands to the Server
     * @since 1.1.6
     */
    private final static long NEXT_MOVE_MAX_DELAY_NS = 10 * 1000000000L; // 10 sec

    /** Reference to the model */
    private final Model model;
    /** Current direction of an actor (TRUE for right, FALSE for left) */
    private boolean curDirRight = true;
    /** Moves allowed flag (TRUE means allowed, FALSE means forbidden) */
    private boolean movesAllowed = true;
    /** Special flag to restrict sending next MOVE cmd until we receive Ack on previous one */
    private boolean nextMoveAllowed = true;
    /** Timestamp of when the {@link #nextMoveAllowed} flag has been changed */
    private transient long nextMoveTimestamp = System.nanoTime();

    /**
     * Creates a new instance of InputController
     * @param model model (NON-NULL)
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
     * @param mouseButton current pressed mouse button
     * @param x x mouse position
     * @param y y mouse position
     * @return true, if a movement registered
     */
    boolean checkMovement(int mouseButton, float x, float y) {
        CellObject actor = model.curActor; // copy to local to avoid Null-Exceptions
        if (actor != null) {
            // MOVEMENT HANDLING (restricted by TOUCH_DELAY intervals)
            if (movesAllowed && isNextMoveAllowed()) {
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
     * @param movesAllowed true to turn on input listening
     */
    void setMovesAllowed(boolean movesAllowed) {
        this.movesAllowed = movesAllowed;
    }

    /**
     * Allows next move (in case of poor connection we forbid sending next MOVE cmd until we receive Ack on previous)
     */
    void setNextMoveAllowed(boolean value) {
        nextMoveAllowed = value;
        nextMoveTimestamp = System.nanoTime();
    }

    private boolean isNextMoveAllowed() {
        // if 10 sec elapsed: allow nextMoveAllowed in order to avoid possible hang ups (since 1.1.6)
        if (System.nanoTime() - nextMoveTimestamp > NEXT_MOVE_MAX_DELAY_NS)
            setNextMoveAllowed(true);
        return nextMoveAllowed;
    }

    /**
     * Moves down
     * @return true
     */
    private boolean moveDown() {
        if (model.move(curDirRight ? Model.MoveDirection.RightDown : Model.MoveDirection.LeftDown))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves up
     * @return true
     */
    private boolean moveUp() {
        if (model.move(curDirRight ? Model.MoveDirection.RightUp : Model.MoveDirection.LeftUp))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves left
     * @return true
     */
    private boolean moveLeft() {
        curDirRight = false;
        if (model.move(Model.MoveDirection.Left))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves right
     * @return true
     */
    private boolean moveRight() {
        curDirRight = true;
        if (model.move(Model.MoveDirection.Right))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves down (if impossible, then left)
     * @return true
     */
    private boolean moveLeftDown() {
        curDirRight = false;
        if (model.move(Model.MoveDirection.LeftDown))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves up (if impossible, then left)
     * @return true
     */
    private boolean moveLeftUp() {
        curDirRight = false;
        if (model.move(Model.MoveDirection.LeftUp))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves down (if impossible, then right)
     * @return true
     */
    private boolean moveRightDown() {
        curDirRight = true;
        if (model.move(Model.MoveDirection.RightDown))
            setNextMoveAllowed(false);
        return true;
    }

    /**
     * Moves up (if impossible, then right)
     * @return true
     */
    private boolean moveRightUp() {
        curDirRight = true;
        if (model.move(Model.MoveDirection.RightUp))
            setNextMoveAllowed(false);
        return true;
    }
}
