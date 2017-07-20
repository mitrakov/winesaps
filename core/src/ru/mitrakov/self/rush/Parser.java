package ru.mitrakov.self.rush;

import java.util.*;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.IHandler;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.log;
import static ru.mitrakov.self.rush.model.Model.*;

/**
 * Created by mitrakov on 23.02.2017
 */
class Parser implements IHandler {
    private static final int ERR_SIGNIN_INCORRECT_PASSWORD = 31;
    private static final int ERR_ATTACK_YOURSELF = 50;
    private static final int ERR_AGGRESSOR_BUSY = 51;
    private static final int ERR_DEFENDER_BUSY = 52;
    private static final int ERR_BATTLE_NOT_FOUND = 55;
    private static final int ERR_BATTLE_NOT_FOUND2 = 77;
    private static final int ERR_SIGN_UP = 201;
    private static final int ERR_SIGNIN_INCORRECT_LOGIN = 204;
    private static final int ERR_NO_CRYSTALS = 215;
    private static final int ERR_ADD_FRIEND = 223;
    private static final int ERR_NOT_HANDLED = 240;
    private static final int ERR_USER_NOT_FOUND = 245;
    private static final int ERR_INCORRECT_TOKEN = 246;
    private static final int ERR_ENEMY_NOT_FOUND = 247;
    private static final int ERR_WAIT_FOR_ENEMY = 248;
    private static final int ERR_INCORRECT_NAME = 249;
    private static final int ERR_INCORRECT_EMAIL = 251;
    private static final int ERR_DUPLICATE_NAME = 252;

    private final Model model;
    private final PsObject psObject;
    private final Cmd[] commands = Cmd.values();
    private final IIntArray accessorial = new GcResistantIntArray(Field.WIDTH * Field.HEIGHT);
    private final IIntArray field = new GcResistantIntArray(Field.WIDTH * Field.HEIGHT);

    Parser(Model model, PsObject psObject) {
        assert model != null && psObject != null;
        this.model = model;
        this.psObject = psObject;
    }

    @Override
    public synchronized void onReceived(IIntArray data) {
        // @mitrakov: on Android copyOfRange requires minSdkVersion=9
        assert data != null;
        while (data.length() > 2) {
            int len = data.get(0)*256 + data.get(1);
            processMsg(accessorial.copyFrom(data.remove(0, 2), len));
            data.remove(0, len);
        }
    }

    @Override
    public void onChanged(boolean connected) {
        model.setConnected(connected);
    }

    private void processMsg(IIntArray data) {
        assert data != null;
        log("Precessing:", data);
        if (data.length() > 0) {
            int code = data.get(0);
            if (0 <= code && code < commands.length) {
                Cmd cmd = commands[code];
                switch (cmd) {
                    case SIGN_IN:
                    case SIGN_UP:
                        signIn(cmd, data.remove(0, 1));
                        break;
                    case SIGN_OUT:
                        signOut(cmd, data.remove(0, 1));
                        break;
                    case USER_INFO:
                    case BUY_PRODUCT:
                        userInfo(cmd, data.remove(0, 1));
                        break;
                    case ATTACK: // response on Attack
                        attack(cmd, data.remove(0, 1));
                        break;
                    case CALL:
                        call(cmd, data.remove(0, 1));
                        break;
                    case STOPCALL:
                        stopCall(cmd, data.remove(0, 1));
                        break;
                    case FRIEND_LIST:
                        friendList(cmd, data.remove(0, 1));
                        break;
                    case ADD_FRIEND:
                        addFriend(cmd, data.remove(0, 1));
                        break;
                    case REMOVE_FRIEND:
                        removeFriend(cmd, data.remove(0, 1));
                        break;
                    case RANGE_OF_PRODUCTS:
                        rangeOfProducts(cmd, data.remove(0, 1));
                        break;
                    case ENEMY_NAME:
                        enemyName(cmd, data.remove(0, 1));
                        break;
                    case ROUND_INFO:
                        roundInfo(cmd, data.remove(0, 1));
                        break;
                    case RATING:
                        rating(cmd, data.remove(0, 1));
                        break;
                    case FULL_STATE:
                        fullState(cmd, data.remove(0, 1));
                        break;
                    case STATE_CHANGED:
                        stateChanged(cmd, data.remove(0, 1));
                        break;
                    case SCORE_CHANGED:
                        scoreChanged(cmd, data.remove(0, 1));
                        break;
                    case PLAYER_WOUNDED:
                        playerWounded(cmd, data.remove(0, 1));
                        break;
                    case FINISHED:
                        finished(cmd, data.remove(0, 1));
                        break;
                    case THING_TAKEN:
                        thingTaken(cmd, data.remove(0, 1));
                        break;
                    case ABILITY_LIST:
                        abilitiesList(cmd, data.remove(0, 1));
                        break;
                    case OBJECT_APPENDED:
                        objectAppended(cmd, data.remove(0, 1));
                        break;
                    case CHECK_PROMOCODE:
                        checkPromocode(cmd, data.remove(0, 1));
                        break;
                    case PROMOCODE_DONE:
                        promocodeDone(cmd, data.remove(0, 1));
                        break;
                    case GET_SKU_GEMS:
                        getSkuGems(cmd, data.remove(0, 1));
                        break;
                    case CHECK_PURCHASE:
                        checkPurchase(cmd, data.remove(0, 1));
                        break;
                    case EFFECT_CHANGED:
                        effectChanged(cmd, data.remove(0, 1));
                        break;
                    default:
                        if (data.length() > 1)
                            inspectError(cmd, data.get(1));
                        else throw new IllegalArgumentException("Unhandled command code");
                }
            } else throw new IllegalArgumentException("Incorrect command code");
        } else throw new IllegalArgumentException("Empty data");
    }

    private void signIn(Cmd cmd, IIntArray data) {
        if (data.length() == 1) {
            int error = data.get(0);
            if (error == 0)
                model.setAuthorized(true);
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect sign-in format");
    }

    private void signOut(Cmd cmd, IIntArray data) {
        if (data.length() == 1) {
            int error = data.get(0);
            if (error == 0)
                model.setAuthorized(false);
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect sign-out format");
    }

    private void userInfo(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0)
                model.setUserInfo(data.remove(0, 1));
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect user info format");
    }

    private void attack(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0)
                model.setEnemyName(data.remove(0, 1).toUTF8());
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect attack format");
    }

    private void call(Cmd cmd, IIntArray data) {
        if (data.length() > 3) {
            int sidH = data.get(0);
            int sidL = data.get(1);
            int sid = sidH * 256 + sidL;
            model.attacked(sid, data.remove(0, 2).toUTF8());
            if (model.notifyNewBattles)
                psObject.activate();
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect call format");
    }

    private void stopCall(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            boolean rejected = data.get(0) == 0;
            boolean missed = data.get(0) == 1;
            boolean expired = data.get(0) == 2;
            if (rejected)
                model.stopCallRejected(data.remove(0, 1).toUTF8());
            else if (missed)
                model.stopCallMissed(data.remove(0, 1).toUTF8());
            else if (expired)
                model.stopCallExpired(data.remove(0, 1).toUTF8());
            else inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect stopCall format");
    }

    private void friendList(Cmd cmd, IIntArray data) {
        if (data.length() > 1) {
            int error = data.get(0);
            int fragNumber = data.get(1);
            if (error == 0)
                model.setFriendList(data.remove(0, 2), fragNumber > 1);
            else inspectError(cmd, error);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect friend list format");
    }

    private void addFriend(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0) {
                if (data.length() > 1) {
                    int character = data.get(1);
                    model.friendAdded(character, data.remove(0, 2).toUTF8());
                } else throw new IllegalArgumentException("Incorrect addFriend format");
            } else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect add friend format");
    }

    private void removeFriend(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0)
                model.friendRemoved(data.remove(0, 1).toUTF8());
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect remove friend format");
    }

    private void rangeOfProducts(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0) {
                data.remove(0, 1);
                if (data.length() % 3 == 0)
                    model.setRangeOfProducts(data);
                else throw new IllegalArgumentException("Incorrect range-of-products triples format");
            } else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect range-of-products format");
    }

    private void enemyName(Cmd cmd, IIntArray data) {
        if (data.length() > 0)
            model.setEnemyName(data.toUTF8());
        else throw new IllegalArgumentException("Incorrect range-of-products format" + cmd);
    }

    private void roundInfo(Cmd cmd, IIntArray data) {
        if (data.length() > 6) {
            int number = data.get(0);
            int timeSec = data.get(1);
            boolean aggressor = data.get(2) != 0;
            int character1 = data.get(3);
            int character2 = data.get(4);
            int myLives = data.get(5);
            int enemyLives = data.get(6);
            // String fieldName = data.remove(0, 6).toUTF8(); // not used for now
            model.setRoundInfo(number, timeSec, aggressor, character1, character2, myLives, enemyLives);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect round info format");
    }

    private void rating(Cmd cmd, IIntArray data) {
        if (data.length() > 1) {
            int error = data.get(0);
            int type = data.get(1);
            RatingType[] types = RatingType.values();
            if (error == 0 && (0 <= type && type < types.length)) {
                model.setRating(types[type], data.remove(0, 2));
            } else inspectError(cmd, error);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect rating format");
    }

    private void fullState(Cmd cmd, IIntArray state) {
        int n = Field.HEIGHT * Field.WIDTH;
        if (state.length() >= n) {
            // field
            model.setNewField(field.copyFrom(state, n));

            // scanning additional sections
            for (int j = n; j + 1 < state.length(); j += 2) {
                int sectionCode = state.get(j);
                int sectionLen = state.get(j + 1);
                switch (sectionCode) {
                    case 1: // parse additional level objects
                        for (int i = j + 2; i < j + 2 + sectionLen; i += 3) {
                            int number = state.get(i);
                            int id = state.get(i + 1);
                            int xy = state.get(i + 2);
                            model.appendObject(number, id, xy);
                        }
                        break;
                    case 2: // parse style pack
                        if (sectionLen == 1 && j + 2 < state.length()) {
                            int pack = state.get(j + 2);
                            model.setStylePack(pack);
                        } else throw new IllegalArgumentException("Incorrect style pack");
                        break;
                    default: // don't throw exceptions, just skip
                }
                j += sectionLen;
            }
        } else if (state.length() == 1) {
            inspectError(cmd, state.get(0));
        } else throw new IllegalArgumentException("Incorrect field size");
    }

    private void stateChanged(Cmd cmd, IIntArray data) {
        if (data.length() == 4) {
            int number = data.get(0);
            int id = data.get(1);
            int xy = data.get(2);
            // int reset = data.get(3);       // not used for now
            model.setXy(number, id, xy);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect state changed format");
    }

    private void scoreChanged(Cmd cmd, IIntArray score) {
        if (score.length() == 2) {
            int score1 = score.get(0);
            int score2 = score.get(1);
            model.setScore(score1, score2);
        } else if (score.length() == 1) {
            inspectError(cmd, score.get(0));
        } else throw new IllegalArgumentException("Incorrect score format");
    }

    private void playerWounded(Cmd cmd, IIntArray data) {
        if (data.length() == 4) {
            boolean me = data.get(0) == 1;
            int cause = data.get(1);
            int myLives = data.get(2);
            int enemyLives = data.get(3);
            model.setPlayerWounded(me, cause, myLives, enemyLives);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect lives format");
    }

    private void finished(Cmd cmd, IIntArray data) {
        if (data.length() > 1) {
            boolean roundFinished = data.get(0) == 0; // 0 = finished round, 1 = finished game
            boolean gameFinished = data.get(0) == 1;
            boolean winner = data.get(1) > 0;
            if (gameFinished)
                model.gameFinished(winner);
            else if (roundFinished) {
                if (data.length() == 4) {
                    int score1 = data.get(2);
                    int score2 = data.get(3);
                    model.roundFinished(winner, score1, score2);
                } else throw new IllegalArgumentException("Incorrect finished round format");
            } else inspectError(cmd, data.get(0));
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect finished format");
    }

    private void thingTaken(Cmd cmd, IIntArray data) {
        if (data.length() == 2) {
            boolean me = data.get(0) != 0;
            int thingId = data.get(1);
            if (me)
                model.setThing(thingId);
            else model.setEnemyThing(thingId);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect thing format");
    }

    private void objectAppended(Cmd cmd, IIntArray data) {
        if (data.length() == 3) {
            int id = data.get(0);
            int objNum = data.get(1);
            int xy = data.get(2);
            model.appendObject(objNum, id, xy);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect object format");
    }

    private void checkPromocode(Cmd cmd, IIntArray data) {
        if (data.length() == 2) {
            int error = data.get(0);
            if (error == 0) {
                int ok = data.get(1);
                model.setPromocodeValid(ok == 1);
            } else inspectError(cmd, error);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect checkPromocode format");
    }

    private void promocodeDone(Cmd cmd, IIntArray data) {
        if (data.length() > 1) {
            boolean inviter = data.get(0) == 1;
            int crystals = data.get(1);
            model.setPromocodeDone(data.remove(0, 2).toUTF8(), inviter, crystals);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect 'promocode done' format");
    }

    private void getSkuGems(Cmd cmd, IIntArray data) {
        if (data.length() > 1)
            model.setSkuGems(data);
        else if (data.length() == 1)
            inspectError(cmd, data.get(0));
        else throw new IllegalArgumentException("Incorrect SKU gems format");
    }

    private void checkPurchase(Cmd cmd, IIntArray data) {
        if (data.length() > 5) {
            int error = data.get(0);
            if (error == 0) {
                int gems = (data.get(1) << 24) | (data.get(2) << 16) | (data.get(3) << 8) | data.get(4);
                String coupon = data.remove(0, 5).toUTF8();
                model.paymentDone(gems, coupon);
            } else inspectError(cmd, error);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect 'check purchase' format");
    }

    private void effectChanged(Cmd cmd, IIntArray data) {
        if (data.length() == 3) {
            int effectId = data.get(0);
            boolean added = data.get(1) == 1;
            int objNumber = data.get(2);
            if (added)
                model.addEffect(effectId, objNumber);
            else model.removeEffect(objNumber);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect effect format");
    }

    private void abilitiesList(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int count = data.get(0);
            data.remove(0, 1);
            if (data.length() == count)
                model.setAbilities(data);
            else inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect abilities format");
    }

    private void inspectError(Cmd cmd, int code) {
        switch (code) {
            case 0: // no error
            case ERR_BATTLE_NOT_FOUND: // it is possible on a battle finish in case of slow network... just skip
            case ERR_BATTLE_NOT_FOUND2:
                break;
            case ERR_SIGNIN_INCORRECT_PASSWORD:
                model.setIncorrectCredentials();
                break;
            case ERR_ATTACK_YOURSELF:
                model.setAttackYourself();
                break;
            case ERR_AGGRESSOR_BUSY:
                model.setUserBusy(true);
                break;
            case ERR_DEFENDER_BUSY:
                model.setUserBusy(false);
                break;
            case ERR_SIGN_UP:
                model.setSignUpError();
                break;
            case ERR_SIGNIN_INCORRECT_LOGIN:
                model.setIncorrectCredentials();
                break;
            case ERR_NO_CRYSTALS:
                model.setNoCrystals();
                break;
            case ERR_ADD_FRIEND:
                model.setAddFriendError();
                break;
            case ERR_NOT_HANDLED:      // trying to send a cmd with sid=token=0
            case ERR_USER_NOT_FOUND:   // incorrect sid, server was restarted, etc.
            case ERR_INCORRECT_TOKEN:  // client was restarted, trying to use the other client, etc.
                model.signIn();
                break;
            case ERR_ENEMY_NOT_FOUND:
                model.setEnemyNotFound();
                break;
            case ERR_WAIT_FOR_ENEMY:
                model.setWaitingForEnemy();
                break;
            case ERR_INCORRECT_NAME:
                model.setIncorrectName();
                break;
            case ERR_INCORRECT_EMAIL:
                model.setIncorrectEmail();
                break;
            case ERR_DUPLICATE_NAME:
                model.setDuplicateName();
                break;
            default:
                String s = String.format(Locale.getDefault(), "Unhandled error (cmd = %s, code = %d)", cmd, code);
                throw new IllegalArgumentException(s);
        }
    }
}
