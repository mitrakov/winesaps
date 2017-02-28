package ru.mitrakov.self.rush.model;

import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 23.02.2017
 */

@SuppressWarnings("WeakerAccess")
public class Model {
    public static final byte SIGN_UP = 0x01;
    public static final byte SIGN_IN = 0x02;
    public static final byte SIGN_OUT = 0x03;
    public static final byte CHANGE_PASSWORD = 0x04;
    public static final byte USER_INFO = 0x05;
    public static final byte ATTACK = 0x06;
    public static final byte INVITE = 0x07;
    public static final byte ACCEPT = 0x08;
    public static final byte REJECT = 0x09;
    public static final byte GIVE_UP = 0x0A;
    public static final byte READY = 0x0B;
    public static final byte CHAT_TO_ENEMY = 0x0C;
    public static final byte BUY_PRODUCT = 0x0D;
    public static final byte RESERVED1 = 0x0E;
    public static final byte RESERVED2 = 0x0F;
    public static final byte FULL_STATE = 0x10;
    public static final byte STATE_CHANGED = 0x11;
    public static final byte SCORE_CHANGED = 0x12;
    public static final byte MOVE_LEFT = 0x13;
    public static final byte MOVE_RIGHT = 0x14;
    public static final byte MOVE_UP = 0x15;
    public static final byte MOVE_DOWN = 0x16;
    public static final byte USE_THING = 0x17;
    public static final byte FINISHED = 0x18;
    public static final byte WOUND = 0x19;
    public static final byte THING_TAKEN = 0x1A;
    public static final byte USE_FACILITY = 0x1B;
    public static final byte FACILITY_LIST = 0x1C;
    public static final byte OBJECT_APPENDED = 0x1D;

    public interface ISender {
        void send(int cmd);

        void send(int cmd, byte arg);

        void send(int cmd, byte[] data);
    }

    private static final int AGGRESSOR_ID = 1;
    private static final int DEFENDER_ID = 2;

    // @mitrakov: getters are supposed to have a little overhead, so we make the fields "public" for efficiency
    public int score1 = 0;
    public int score2 = 0;
    public Field field;
    public CellObject curActor;
    public CellObject curThing;

    private ISender sender;
    private boolean aggressor = true;


    public Model() {
    }

    public void setSender(ISender sender) {
        this.sender = sender;
    }

    public void signIn() {
        if (sender != null) {
            sender.send(SIGN_IN, "\1Tommy\0Tommy".getBytes());
        }
    }

    public void invite(String victim) {
        if (sender != null) {
            sender.send(ATTACK, "\0".concat(victim).getBytes());
        }
    }

    public void moveLeft() {
        if (sender != null) {
            sender.send(MOVE_LEFT);
        }
    }

    public void moveRight() {
        if (sender != null) {
            sender.send(MOVE_RIGHT);
        }
    }

    public void moveUp() {
        if (sender != null) {
            sender.send(MOVE_UP);
        }
    }

    public void moveDown() {
        if (sender != null) {
            sender.send(MOVE_DOWN);
        }
    }

    public void useThing() {
        if (sender != null && curThing != null) {
            sender.send(USE_THING, (byte) curThing.getId());
        }
    }

    public void setNewField(int[] fieldData) {
        field = new Field(fieldData);
    }

    public void appendObject(int number, int id, int xy) {
        assert field != null;
        field.appendObject(number, id, xy);
        if (id == AGGRESSOR_ID || id == DEFENDER_ID)
            curActor = aggressor ? field.getObject(AGGRESSOR_ID) : field.getObject(DEFENDER_ID);
    }

    public void setXy(int number, int xy) {
        assert field != null;
        field.setXy(number, xy);
    }

    public void setScore(int score1, int score2) {
        this.score1 = score1;
        this.score2 = score2;
    }

    public void finishedRound(boolean me) {

    }

    public void finishedGame(boolean me, int score1, int score2) {

    }

    public void setThing(int thingId) {
        curThing = Cell.newObject(thingId, 0xFF, new Field.NextNumber() {
            @Override
            public int next() {
                return 0;
            }
        });
    }

    public void setFacilities(int[] facilities) {

    }
}
