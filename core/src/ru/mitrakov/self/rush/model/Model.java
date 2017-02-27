package ru.mitrakov.self.rush.model;

import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 23.02.2017
 */

@SuppressWarnings("WeakerAccess")
public class Model {
    public static final byte SIGN_IN = 0x02;
    public static final byte ATTACK = 0x06;
    public static final byte INVITE = 0x07;
    public static final byte FULL_STATE = 0x10;
    public static final byte STATE_CHANGED = 0x11;
    public static final byte SCORE_CHANGED = 0x12;
    public static final byte FINISHED = 0x18;
    public static final byte THING_TAKEN = 0x1A;
    public static final byte FACILITY_LIST = 0x1C;

    public interface ISender {
        void send(int cmd, byte[] data);
    }

    private static final int AGGRESSOR_ID = 1;
    private static final int DEFENDER_ID = 2;

    // @mitrakov: getters are supposed to have a little overhead, so we make the fields "public" for efficiency
    public int score1 = 0;
    public int score2 = 0;
    public Field field;
    public CellObject curActor;

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

    public void setNewField(int[] fieldData) {
        field = new Field(fieldData);
    }

    public void appendObject(int number, int id, int xy) {
        assert field != null;
        field.appendObject(number, id, xy);
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

    public void invite(String victim) {
        if (sender != null) {
            sender.send(ATTACK, "\0".concat(victim).getBytes());
        }
    }

    public void finishedRound(boolean me) {

    }

    public void finishedGame(boolean me, int score1, int score2) {

    }

    public void setThing(int thingId) {

    }

    public void setFacilities(int[] facilities) {

    }
}
