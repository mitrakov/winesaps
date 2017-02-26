package ru.mitrakov.self.rush.model;

import ru.mitrakov.self.rush.model.object.CellObject;

/**
 * Created by mitrakov on 23.02.2017
 */

public class Model {


    // @mitrakov: getters are supposed to have a little overhead, so we make the fields "public" for efficiency
    public int score1 = 0;
    public int score2 = 0;
    public Field field;


    public Model() {

    }

    public void setNewField(int[] fieldData) {
        field = new Field(fieldData);
    }

    public void appendObject(int number, int id, int xy) {
        assert field != null;
        field.appendObject(number, id, xy);
    }

    public void setXy(int number, int xy) {
        assert field != null;
        field.setXy(number, xy);
    }

    public void setScore(int score1, int score2) {
        this.score1 = score1;
        this.score2 = score2;
    }

    public void invite(String aggressor) {

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
