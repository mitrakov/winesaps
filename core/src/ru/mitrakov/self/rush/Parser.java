package ru.mitrakov.self.rush;

import java.util.Arrays;
import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.Network;
import static ru.mitrakov.self.rush.model.Model.*;

/**
 * Created by mitrakov on 23.02.2017
 */

class Parser implements Network.IHandler {
    private final Model model;

    Parser(Model model) {
        assert model != null;
        this.model = model;
    }

    @Override
    public void handle(int[] data) {
        // @mitrakov: on Android copyOfRange requires minSdkVersion=9
        System.out.println(Arrays.toString(data));
        if (data.length > 0) {
            int code = data[0];
            switch (code) {
                case SIGN_IN:
                    signIn(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case USER_INFO:
                case BUY_PRODUCT:
                    userInfo(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case RANGE_OF_PRODUCTS:
                    rangeOfProducts(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case RATING:
                    rating(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case FULL_STATE:
                    fullState(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case STATE_CHANGED:
                    stateChanged(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case SCORE_CHANGED:
                    scoreChanged(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case THING_TAKEN:
                    thingTaken(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case ABILITY_LIST:
                    abilitiesList(Arrays.copyOfRange(data, 1, data.length));
                    break;
                case OBJECT_APPENDED:
                    objectAppended(Arrays.copyOfRange(data, 1, data.length));
                    break;
                default:
            }
        } else throw new IllegalArgumentException("Empty data");
    }

    private void signIn(int[] data) {
        if (data.length == 1) {
            int ok = data[0];
            if (ok == 0)
                model.setAuthorized();
            else throw new IllegalArgumentException("Incorrect login/password");
        } else throw new IllegalArgumentException("Incorrect sign-in format");
    }

    private void userInfo(int[] data) {
        if (data.length > 0) {
            int ok = data[0];
            if (ok == 0)
                model.setUserInfo(Arrays.copyOfRange(data, 1, data.length));
            else throw new IllegalArgumentException("Incorrect user info response");
        } else throw new IllegalArgumentException("Incorrect user info format");
    }

    private void rangeOfProducts(int[] data) {
        if (data.length % 3 == 0) {
            model.setRangeOfProducts(data);
        } else throw new IllegalArgumentException("Incorrect range-of-products format");
    }

    private void rating(int[] data) {
        if (data.length > 1) {
            int error = data[0];
            int type = data[1];
            RatingType[] types = RatingType.values();
            if (error == 0 && (0 <= type && type < types.length)) {
                model.setRating(types[type], Arrays.copyOfRange(data, 2, data.length));
            }
        } else throw new IllegalArgumentException("Incorrect rating format");
    }

    private void fullState(int[] state) {
        int n = Field.HEIGHT * Field.WIDTH;
        if (state.length >= n) {
            int field[] = Arrays.copyOfRange(state, 0, n);
            int tail[] = Arrays.copyOfRange(state, n, state.length);

            model.setNewField(field);
            if (tail.length % 3 == 0) {
                for (int i = 0; i < tail.length; i += 3) {
                    int number = tail[i];
                    int id = tail[i + 1];
                    int xy = tail[i + 2];
                    model.appendObject(number, id, xy);
                }
            } else throw new IllegalArgumentException("Incorrect tail data format");
        } else throw new IllegalArgumentException("Incorrect field size");
    }

    private void stateChanged(int[] pairs) {
        if (pairs.length % 2 == 0) {
            for (int i = 0; i < pairs.length; i += 2) {
                int number = pairs[i];
                int xy = pairs[i + 1];
                model.setXy(number, xy);
            }
        } else throw new IllegalArgumentException("Incorrect state changed format");
    }

    private void scoreChanged(int[] score) {
        if (score.length == 2) {
            int score1 = score[0];
            int score2 = score[1];
            model.setScore(score1, score2);
        } else throw new IllegalArgumentException("Incorrect score format");
    }

    private void thingTaken(int[] data) {
        if (data.length == 1) {
            int thingId = data[0];
            model.setThing(thingId);
        } else throw new IllegalArgumentException("Incorrect thing format");
    }

    private void objectAppended(int[] data) {
        if (data.length == 3) {
            int id = data[0];
            int objNum = data[1];
            int xy = data[2];
            model.appendObject(objNum, id, xy);
        } else throw new IllegalArgumentException("Incorrect object format");
    }

    private void abilitiesList(int[] data) {
        if (data.length > 0) {
            int count = data[0];
            int abilities[] = Arrays.copyOfRange(data, 1, data.length);
            if (abilities.length == count)
                model.setAbilities(abilities);
            else throw new IllegalArgumentException("Incorrect abilities size");
        } else throw new IllegalArgumentException("Incorrect abilities format");
    }
}
