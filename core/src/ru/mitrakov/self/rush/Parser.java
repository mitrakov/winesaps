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
    private final PsObject psObject;

    Parser(Model model, PsObject psObject) {
        assert model != null;
        this.model = model;
        this.psObject = psObject; // may be NULL
    }

    @Override
    public void handle(int[] data) {
        // @mitrakov: on Android copyOfRange requires minSdkVersion=9
        assert data != null;
        System.out.println(Arrays.toString(data));

        if (data.length > 0) {
            int code = data[0];
            Cmd[] commands = Cmd.values();
            if (0 <= code && code < commands.length) {
                Cmd cmd = commands[code];
                switch (cmd) {
                    case SIGN_IN:
                    case SIGN_UP:
                        signIn(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case SIGN_OUT:
                        signOut(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case USER_INFO:
                    case BUY_PRODUCT:
                        userInfo(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case ATTACK: // response on Attack
                        attack(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case CALL:
                        call(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case STOPCALL:
                        stopCall(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case FRIEND_LIST:
                        friendList(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case ADD_FRIEND:
                        addFriend(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case REMOVE_FRIEND:
                        removeFriend(Arrays.copyOfRange(data, 1, data.length));
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
                    case FINISHED:
                        finished(Arrays.copyOfRange(data, 1, data.length));
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
                    case CHECK_PROMOCODE:
                        checkPromocode(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    case PROMOCODE_DONE:
                        promocodeDone(Arrays.copyOfRange(data, 1, data.length));
                        break;
                    default:
                }
            } else throw new IllegalArgumentException("Incorrect command code");
        } else throw new IllegalArgumentException("Empty data");
    }

    private void signIn(int[] data) {
        if (data.length == 1) {
            int ok = data[0];
            if (ok == 0)
                model.setAuthorized(true);
            else throw new IllegalArgumentException("Incorrect login/password");
        } else throw new IllegalArgumentException("Incorrect sign-in format");
    }

    private void signOut(int[] data) {
        if (data.length == 1) {
            int ok = data[0];
            if (ok == 0)
                model.setAuthorized(false);
            else throw new IllegalArgumentException("Incorrect sign out response");
        } else throw new IllegalArgumentException("Incorrect sign-out format");
    }

    private void userInfo(int[] data) {
        if (data.length > 0) {
            int ok = data[0];
            if (ok == 0)
                model.setUserInfo(Arrays.copyOfRange(data, 1, data.length));
            else throw new IllegalArgumentException("Incorrect user info response");
        } else throw new IllegalArgumentException("Incorrect user info format");
    }

    private void attack(int[] data) {
        if (data.length > 1) {
            int ok = data[0];
            if (ok == 0) {
                StringBuilder victim = new StringBuilder();
                for (int i = 1; i < data.length; i++) {
                    victim.append((char) data[i]);
                }
                model.setVictim(victim.toString());
            } else throw new IllegalArgumentException("Incorrect attack response");
        } else throw new IllegalArgumentException("Incorrect attack format");
    }

    private void call(int[] data) {
        if (data.length > 3) {
            int sidH = data[0];
            int sidL = data[1];
            int sid = sidH * 256 + sidL;
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < data.length; i++) {
                name.append((char) data[i]);
            }
            model.attacked(sid, name.toString());
            if (psObject != null)
                psObject.activate();
        } else throw new IllegalArgumentException("Incorrect call format");
    }

    private void stopCall(int[] data) {
        if (data.length > 1) {
            boolean rejected = data[0] == 0;
            boolean missed = data[0] == 1;
            boolean expired = data[0] == 2;
            StringBuilder name = new StringBuilder(); // oh... Java 1.8 has "mkstring" :(
            for (int i = 1; i < data.length; i++) {
                name.append((char) data[i]);
            }
            if (rejected)
                model.stopCallRejected(name.toString());
            else if (missed)
                model.stopCallMissed(name.toString());
            else if (expired)
                model.stopCallExpired(name.toString());
            else throw new IllegalArgumentException("Incorrect stopCall response");
        } else throw new IllegalArgumentException("Incorrect stopCall format");
    }

    private void friendList(int[] data) {
        if (data.length > 0) {
            int ok = data[0];
            if (ok == 0)
                model.setFriendList(Arrays.copyOfRange(data, 1, data.length));
            else throw new IllegalArgumentException("Incorrect friend list response");
        } else throw new IllegalArgumentException("Incorrect friend list format");
    }

    private void addFriend(int[] data) {
        if (data.length > 0) {
            int ok = data[0];
            if (ok == 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < data.length; i++) {
                    builder.append((char) data[i]);
                }
                model.friendAdded(builder.toString());
            } else throw new IllegalArgumentException("Incorrect add friend response");
        } else throw new IllegalArgumentException("Incorrect add friend format");
    }

    private void removeFriend(int[] data) {
        if (data.length > 0) {
            int ok = data[0];
            if (ok == 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < data.length; i++) {
                    builder.append((char) data[i]);
                }
                model.friendRemoved(builder.toString());
            } else throw new IllegalArgumentException("Incorrect remove friend response");
        } else throw new IllegalArgumentException("Incorrect remove friend format");
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

    private void finished(int[] data) {
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
            } else throw new IllegalArgumentException("Incorrect finished type");
        } else throw new IllegalArgumentException("Incorrect finished format");
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

    private void checkPromocode(int[] data) {
        if (data.length == 1) {
            int res = data[0];
            if (res == 0 || res == 1) {
                model.setPromocodeValid(res == 1);
            } else throw new IllegalArgumentException("Incorrect checkPromocode response");
        } else throw new IllegalArgumentException("Incorrect checkPromocode format");
    }

    private void promocodeDone(int[] data) {
        if (data.length > 2) {
            boolean inviter = data[0] == 1;
            int crystals = data[1];
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < data.length; i++) {
                name.append((char) data[i]);
            }
            model.setPromocodeDone(name.toString(), inviter, crystals);
        } else throw new IllegalArgumentException("Incorrect 'promocode done' format");
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
