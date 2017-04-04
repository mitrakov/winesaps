package ru.mitrakov.self.rush;

import java.util.*;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.IHandler;

import static ru.mitrakov.self.rush.model.Model.*;
import static ru.mitrakov.self.rush.net.Utils.copyOfRange;

/**
 * Created by mitrakov on 23.02.2017
 */

class Parser implements IHandler {
    private static final int ERR_USER_NOT_FOUND = 105;
    private static final int ERR_INCORRECT_TOKEN = 106;

    private final Model model;
    private final PsObject psObject;

    Parser(Model model, PsObject psObject) {
        assert model != null;
        this.model = model;
        this.psObject = psObject; // may be NULL
    }

    @Override
    public void onReceived(int[] data) {
        // @mitrakov: on Android copyOfRange requires minSdkVersion=9
        assert data != null;

        if (data.length > 0) {
            int code = data[0];
            Cmd[] commands = Cmd.values();
            if (0 <= code && code < commands.length) {
                Cmd cmd = commands[code];
                switch (cmd) {
                    case SIGN_IN:
                    case SIGN_UP:
                        signIn(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case SIGN_OUT:
                        signOut(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case USER_INFO:
                    case BUY_PRODUCT:
                        userInfo(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case ATTACK: // response on Attack
                        attack(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case CALL:
                        call(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case STOPCALL:
                        stopCall(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case FRIEND_LIST:
                        friendList(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case ADD_FRIEND:
                        addFriend(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case REMOVE_FRIEND:
                        removeFriend(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case RANGE_OF_PRODUCTS:
                        rangeOfProducts(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case ROUND_INFO:
                        roundInfo(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case RATING:
                        rating(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case FULL_STATE:
                        fullState(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case STATE_CHANGED:
                        stateChanged(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case SCORE_CHANGED:
                        scoreChanged(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case FINISHED:
                        finished(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case THING_TAKEN:
                        thingTaken(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case ABILITY_LIST:
                        abilitiesList(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case OBJECT_APPENDED:
                        objectAppended(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case CHECK_PROMOCODE:
                        checkPromocode(cmd, copyOfRange(data, 1, data.length));
                        break;
                    case PROMOCODE_DONE:
                        promocodeDone(cmd, copyOfRange(data, 1, data.length));
                        break;
                    default:
                }
            } else throw new IllegalArgumentException("Incorrect command code");
        } else throw new IllegalArgumentException("Empty data");
    }

    @Override
    public void onChanged(boolean connected) {
        model.setConnected(connected);
    }

    private void signIn(Cmd cmd, int[] data) {
        if (data.length == 1) {
            int error = data[0];
            if (error == 0)
                model.setAuthorized(true);
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect sign-in format");
    }

    private void signOut(Cmd cmd, int[] data) {
        if (data.length == 1) {
            int error = data[0];
            if (error == 0)
                model.setAuthorized(false);
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect sign-out format");
    }

    private void userInfo(Cmd cmd, int[] data) {
        if (data.length > 0) {
            int error = data[0];
            if (error == 0)
                model.setUserInfo(copyOfRange(data, 1, data.length));
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect user info format");
    }

    private void attack(Cmd cmd, int[] data) {
        if (data.length > 0) {
            int error = data[0];
            if (error == 0) {
                StringBuilder victim = new StringBuilder(); // in Java 8 may be replaced with a StringJoiner
                for (int i = 1; i < data.length; i++) {
                    victim.append((char) data[i]);
                }
                model.setVictim(victim.toString());
            } else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect attack format");
    }

    private void call(Cmd cmd, int[] data) {
        if (data.length > 3) {
            int sidH = data[0];
            int sidL = data[1];
            int sid = sidH * 256 + sidL;
            StringBuilder name = new StringBuilder(); // in Java 8 may be replaced with a StringJoiner
            for (int i = 2; i < data.length; i++) {
                name.append((char) data[i]);
            }
            model.attacked(sid, name.toString());
            if (psObject != null)
                psObject.activate();
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect call format");
    }

    private void stopCall(Cmd cmd, int[] data) {
        if (data.length > 0) {
            boolean rejected = data[0] == 0;
            boolean missed = data[0] == 1;
            boolean expired = data[0] == 2;
            StringBuilder name = new StringBuilder(); // in Java 8 may be replaced with a StringJoiner
            for (int i = 1; i < data.length; i++) {
                name.append((char) data[i]);
            }
            if (rejected)
                model.stopCallRejected(name.toString());
            else if (missed)
                model.stopCallMissed(name.toString());
            else if (expired)
                model.stopCallExpired(name.toString());
            else inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect stopCall format");
    }

    private void friendList(Cmd cmd, int[] data) {
        if (data.length > 0) {
            int error = data[0];
            if (error == 0)
                model.setFriendList(copyOfRange(data, 1, data.length));
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect friend list format");
    }

    private void addFriend(Cmd cmd, int[] data) {
        if (data.length > 0) {
            int error = data[0];
            if (error == 0) {
                StringBuilder name = new StringBuilder(); // in Java 8 may be replaced with a StringJoiner
                for (int i = 1; i < data.length; i++) {
                    name.append((char) data[i]);
                }
                model.friendAdded(name.toString());
            } else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect add friend format");
    }

    private void removeFriend(Cmd cmd, int[] data) {
        if (data.length > 0) {
            int error = data[0];
            if (error == 0) {
                StringBuilder name = new StringBuilder(); // in Java 8 may be replaced with a StringJoiner
                for (int i = 1; i < data.length; i++) {
                    name.append((char) data[i]);
                }
                model.friendRemoved(name.toString());
            } else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect remove friend format");
    }

    private void rangeOfProducts(Cmd cmd, int[] data) {
        if (data.length % 3 == 0) {
            model.setRangeOfProducts(data);
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect range-of-products format");
    }

    private void roundInfo(Cmd cmd, int[] data) {
        if (data.length > 2) {
            int number = data[0];
            int timeSec = data[1];
            boolean aggressor = data[2] != 0;
            model.setRoundInfo(number, timeSec, aggressor);
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect round info format");
    }

    private void rating(Cmd cmd, int[] data) {
        if (data.length > 1) {
            int error = data[0];
            int type = data[1];
            RatingType[] types = RatingType.values();
            if (error == 0 && (0 <= type && type < types.length)) {
                model.setRating(types[type], copyOfRange(data, 2, data.length));
            } else inspectError(cmd, error);
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect rating format");
    }

    private void fullState(Cmd cmd, int[] state) {
        int n = Field.HEIGHT * Field.WIDTH;
        if (state.length >= n) {
            int field[] = copyOfRange(state, 0, n);
            int tail[] = copyOfRange(state, n, state.length);

            model.setNewField(field);
            if (tail.length % 3 == 0) {
                for (int i = 0; i < tail.length; i += 3) {
                    int number = tail[i];
                    int id = tail[i + 1];
                    int xy = tail[i + 2];
                    model.appendObject(number, id, xy);
                }
            } else throw new IllegalArgumentException("Incorrect tail data format");
        } else if (state.length == 1) {
            inspectError(cmd, state[0]);
        } else throw new IllegalArgumentException("Incorrect field size");
    }

    private void stateChanged(Cmd cmd, int[] pairs) {
        if (pairs.length % 2 == 0) {
            for (int i = 0; i < pairs.length; i += 2) {
                int number = pairs[i];
                int xy = pairs[i + 1];
                model.setXy(number, xy);
            }
        } else if (pairs.length == 1) {
            inspectError(cmd, pairs[0]);
        } else throw new IllegalArgumentException("Incorrect state changed format");
    }

    private void scoreChanged(Cmd cmd, int[] score) {
        if (score.length == 2) {
            int score1 = score[0];
            int score2 = score[1];
            model.setScore(score1, score2);
        } else if (score.length == 1) {
            inspectError(cmd, score[0]);
        } else throw new IllegalArgumentException("Incorrect score format");
    }

    private void finished(Cmd cmd, int[] data) {
        if (data.length > 1) {
            boolean roundFinished = data[0] == 0; // 0 = finished round, 1 = finished game
            boolean gameFinished = data[0] == 1;
            boolean winner = data[1] > 0;
            if (gameFinished)
                model.gameFinished(winner);
            else if (roundFinished) {
                if (data.length == 4) {
                    int score1 = data[2];
                    int score2 = data[3];
                    model.roundFinished(winner, score1, score2);
                } else throw new IllegalArgumentException("Incorrect finished round format");
            } else inspectError(cmd, data[0]);
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect finished format");
    }

    private void thingTaken(Cmd cmd, int[] data) {
        if (data.length == 1) {
            int thingId = data[0];
            model.setThing(thingId);
        } else throw new IllegalArgumentException("Incorrect thing format: " + cmd);
    }

    private void objectAppended(Cmd cmd, int[] data) {
        if (data.length == 3) {
            int id = data[0];
            int objNum = data[1];
            int xy = data[2];
            model.appendObject(objNum, id, xy);
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect object format");
    }

    private void checkPromocode(Cmd cmd, int[] data) {
        if (data.length == 1) {
            int res = data[0];
            if (res == 0 || res == 1) {
                model.setPromocodeValid(res == 1);
            } else inspectError(cmd, res);
        } else throw new IllegalArgumentException("Incorrect checkPromocode format");
    }

    private void promocodeDone(Cmd cmd, int[] data) {
        if (data.length > 1) {
            boolean inviter = data[0] == 1;
            int crystals = data[1];
            StringBuilder name = new StringBuilder(); // in Java 8 may be replaced with a StringJoiner
            for (int i = 2; i < data.length; i++) {
                name.append((char) data[i]);
            }
            model.setPromocodeDone(name.toString(), inviter, crystals);
        } else if (data.length == 1) {
            inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect 'promocode done' format");
    }

    private void abilitiesList(Cmd cmd, int[] data) {
        if (data.length > 0) {
            int count = data[0];
            int abilities[] = copyOfRange(data, 1, data.length);
            if (abilities.length == count)
                model.setAbilities(abilities);
            else inspectError(cmd, data[0]);
        } else throw new IllegalArgumentException("Incorrect abilities format");
    }

    private void inspectError(Cmd cmd, int code) {
        if (code == ERR_USER_NOT_FOUND || code == ERR_INCORRECT_TOKEN) {
            model.signIn();
        } else {
            String s = String.format(Locale.getDefault(), "Unhandled error (cmd = %s, code = %d)", cmd, code);
            throw new IllegalArgumentException(s);
        }
    }
}
