package ru.mitrakov.self.rush;

import java.util.*;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.net.IHandler;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.utils.SimpleLogger.log;
import static ru.mitrakov.self.rush.model.Model.*;

/**
 * Parser is used to parse input messages from the network.
 * This class is intended to have a single instance
 * @author mitrakov
 */
class Parser implements IHandler {
    /** Server Error: Incorrect login/password */
    private static final int ERR_SIGNIN_INCORRECT_PASSWORD = 31;
    /** Server Error: You cannot attack yourself */
    private static final int ERR_ATTACK_YOURSELF = 50;
    /** Server Error: Aggressor is busy */
    private static final int ERR_AGGRESSOR_BUSY = 51;
    /** Server Error: Defender is busy */
    private static final int ERR_DEFENDER_BUSY = 52;
    /** Server Error: Battle not found (UseThing) */
    private static final int ERR_BATTLE_NOT_FOUND0 = 55;
    /** Server Error: Battle not found (GetCurrentField) */
    private static final int ERR_BATTLE_NOT_FOUND1 = 56;
    /** Server Error: Battle not found (UseSkill) */
    private static final int ERR_BATTLE_NOT_FOUND2 = 77;
    /** Server Error: AddUser DB error */
    private static final int ERR_SIGN_UP = 201;
    /** Server Error: FindUser DB Error */
    private static final int ERR_SIGNIN_INCORRECT_LOGIN = 202;
    /** Server Error: BuyProduct DB Error */
    private static final int ERR_NO_GEMS_ENOUGH = 215;
    /** Server Error: AddFriend DB Error */
    private static final int ERR_ADD_FRIEND = 223;
    /** Server Error: Command not handled */
    private static final int ERR_NOT_HANDLED = 240;
    /** Server Error: Not enough arguments for command */
    private static final int ERR_NOT_ENOUGH_ARGS = 242;
    /** Server Error: User not found */
    private static final int ERR_USER_NOT_FOUND = 245;
    /** Server Error: Incorrect token provided */
    private static final int ERR_INCORRECT_TOKEN = 246;
    /** Server Error: Enemy user not found */
    private static final int ERR_ENEMY_NOT_FOUND = 247;
    /** Server Error: Wait for enemy (in fact not a error, but we stick the Server API notation) */
    private static final int ERR_WAIT_FOR_ENEMY = 248;
    /** Server Error: Invalid name (SignUp) */
    private static final int ERR_INCORRECT_NAME = 249;
    /** Server Error: Invalid email (SignUp) */
    private static final int ERR_INCORRECT_EMAIL = 251;
    /** Server Error: Name already exists (SignUp) */
    private static final int ERR_DUPLICATE_NAME = 252;
    /** Server Error: Server gonna stop (soft shutdown) */
    private static final int ERR_SERVER_GONNA_STOP = 254;


    /** Reference to a model */
    private final Model model;
    /** Current locale for string formatting */
    private final Locale locale = Locale.getDefault();
    /** Intermediate array to copy data from the network (to avoid creating new arrays and decrease GC pressure) */
    private final IIntArray array = new GcResistantIntArray(Field.WIDTH * Field.HEIGHT);
    /** Additional array to full copy field binary data */
    private final IIntArray field = new GcResistantIntArray(Field.WIDTH * Field.HEIGHT);

    /**
     * Creates a new instance of Parser
     * @param model model (NON-NULL)
     */
    Parser(Model model) {
        assert model != null;
        this.model = model;
    }

    @Override
    public synchronized void onReceived(IIntArray data) {
        assert data != null;
        // divide the byte array into several single messages
        while (data.length() > 2) {
            int len = data.get(0) * 256 + data.get(1);
            processMsg(array.copyFrom(data.remove(0, 2), len));
            data.remove(0, len);
        }
    }

    @Override
    public void onChanged(boolean connected) {
        model.setConnected(connected);
    }

    /**
     * Parses a single message (note that the input byte array may consist of several single messages) and calls the
     * corresponding method of Model
     * @param data single message byte array
     */
    private void processMsg(IIntArray data) {
        assert data != null;
        log("Processing:", data);
        if (data.length() > 0) {
            int code = data.get(0);
            if (0 <= code && code < Model.cmdValues.length) {
                Cmd cmd = Model.cmdValues[code];
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
                    case MOVE:   // response on Move
                        move(cmd, data.remove(0, 1));
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
                    case RESTORE_STATE:
                        restoreState(cmd, data.remove(0, 1));
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
                    case GET_CLIENT_VERSION:
                        getClientVersion(cmd, data.remove(0, 1));
                        break;
                    default:
                        if (data.length() > 1)
                            inspectError(cmd, data.get(1));
                        else throw new IllegalArgumentException("Unhandled command code");
                }
            } else {
                if (code == 0xF2) {
                    int num = data.get(1) * 256 + data.get(2);
                    long t0 = ((long) data.get(3) << 56) |
                            ((long) data.get(4) << 48) |
                            ((long) data.get(5) << 40) |
                            ((long) data.get(6) << 32) |
                            ((long) data.get(7) << 24) |
                            ((long) data.get(8) << 16) |
                            ((long) data.get(9) << 8) |
                            ((long) data.get(10));
                    String s = String.format(locale, "%d: %d msec", num, System.currentTimeMillis() - t0);
                    log("DATA ", s);
                } else throw new IllegalArgumentException("Incorrect command code");
            }
        } else throw new IllegalArgumentException("Empty data");
    }

    /**
     * Parses SIGN_IN command
     * @param cmd command
     * @param data arguments
     */
    private void signIn(Cmd cmd, IIntArray data) {
        if (data.length() == 1) {
            int error = data.get(0);
            if (error == 0)
                model.setAuthorized(true);
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect sign-in format");
    }

    /**
     * Parses SIGN_OUT command
     * @param cmd command
     * @param data arguments
     */
    private void signOut(Cmd cmd, IIntArray data) {
        if (data.length() == 1) {
            int error = data.get(0);
            if (error == 0)
                model.setAuthorized(false);
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect sign-out format");
    }

    /**
     * Parses USER_INFO command
     * @param cmd command
     * @param data arguments
     */
    private void userInfo(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0)
                model.setUserInfo(data.remove(0, 1));
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect user info format");
    }

    /**
     * Parses ATTACK command
     * @param cmd command
     * @param data arguments
     */
    private void attack(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0)
                model.setEnemyName(data.remove(0, 1).toUTF8());
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect attack format");
    }

    /**
     * Parses CALL command
     * @param cmd command
     * @param data arguments
     */
    private void call(Cmd cmd, IIntArray data) {
        if (data.length() > 3) {
            int sidH = data.get(0);
            int sidL = data.get(1);
            int sid = sidH * 256 + sidL;
            String aggressor = data.remove(0, 2).toUTF8();
            model.attacked(sid, aggressor);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect call format");
    }

    /**
     * Parses STOP_CALL command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses FRIEND_LIST command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses ADD_FRIEND command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses REMOVE_FRIEND command
     * @param cmd command
     * @param data arguments
     */
    private void removeFriend(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int error = data.get(0);
            if (error == 0)
                model.friendRemoved(data.remove(0, 1).toUTF8());
            else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect remove friend format");
    }

    /**
     * Parses RANGE_OF_PRODUCTS command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses ENEMY_NAME command
     * @param cmd command
     * @param data arguments
     */
    private void enemyName(Cmd cmd, IIntArray data) {
        if (data.length() > 0)
            model.setEnemyName(data.toUTF8());
        else throw new IllegalArgumentException("Incorrect range-of-products format" + cmd);
    }

    /**
     * Parses ROUND_INFO command
     * @param cmd command
     * @param data arguments
     */
    private void roundInfo(Cmd cmd, IIntArray data) {
        if (data.length() > 6) {
            int number = data.get(0);
            int timeSec = data.get(1);
            boolean aggressor = data.get(2) != 0;
            int character1 = data.get(3);
            int character2 = data.get(4);
            int myLives = data.get(5);
            int enemyLives = data.get(6);
            String fieldName = data.remove(0, 7).toUTF8();
            model.setRoundInfo(number, timeSec, fieldName, aggressor, character1, character2, myLives, enemyLives);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect round info format");
    }

    /**
     * Parses RATING command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses FULL_STATE command
     * @param cmd command
     * @param state arguments
     */
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
                        int startK = j + 2;
                        for (int k = startK; k + 2 < startK + sectionLen && k + 2 < state.length(); k += 3) {
                            int number = state.get(k);
                            int id = state.get(k + 1);
                            int xy = state.get(k + 2);
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
        } else if (state.length() == 0) {
            model.setEmptyField();
        } else if (state.length() == 1) {
            inspectError(cmd, state.get(0));
        } else throw new IllegalArgumentException(String.format(locale, "Incorrect field size %d", state.length()));
    }

    /**
     * Parses MOVE commande
     * @param cmd command
     * @param data arguments
     */
    private void move(Cmd cmd, IIntArray data) {
        if (data.length() == 1) {
            boolean error = data.get(0) != 0;
            if (!error)
                model.moveResponse();
            else inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect 'move response' format");
    }

    /**
     * Parses STATE_CHANGED command
     * @param cmd command
     * @param data arguments
     */
    private void stateChanged(Cmd cmd, IIntArray data) {
        if (data.length() == 4) {
            int number = data.get(0);
            int id = data.get(1);
            int xy = data.get(2);
            boolean reset = data.get(3) == 1;
            model.setXy(number, id, xy, reset);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect state changed format");
    }

    /**
     * Parses SCORE_CHANGED command
     * @param cmd command
     * @param score arguments
     */
    private void scoreChanged(Cmd cmd, IIntArray score) {
        if (score.length() == 2) {
            int score1 = score.get(0);
            int score2 = score.get(1);
            model.setScore(score1, score2);
        } else if (score.length() == 1) {
            inspectError(cmd, score.get(0));
        } else throw new IllegalArgumentException("Incorrect score format");
    }

    /**
     * Parses PLAYER_WOUNDED command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses FINISHED command
     * @param cmd command
     * @param data arguments
     */
    private void finished(Cmd cmd, IIntArray data) {
        if (data.length() > 3) {
            boolean roundFinished = data.get(0) == 0; // 0 = finished round, 1 = finished game
            boolean gameFinished = data.get(0) == 1;
            boolean winner = data.get(1) > 0;
            int score1 = data.get(2);
            int score2 = data.get(3);
            if (roundFinished)
                model.roundFinished(winner, score1, score2);
            else if (gameFinished) {
                int reward = data.length() == 8
                        ? (data.get(4) << 24) | (data.get(5) << 16) | (data.get(6) << 8) | data.get(7) : 0;
                model.gameFinished(winner, score1, score2, reward);
            } else throw new IllegalArgumentException("Incorrect finished format!");
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect finished format");
    }

    /**
     * Parses THING_TAKEN command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses OBJECT_APPENDED command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     *
     * @param cmd RESTORE_STATE command
     * @param data arguments
     * @since ServerAPI 1.3.0
     */
    private void restoreState(Cmd cmd, IIntArray data) {
        if (data.length() >= 1) {
            int error = data.get(0);
            if (error == 0) {
                IIntArray chunks = data.remove(0, 1);
                if (chunks.length() % 3 == 0) {
                    log("RESTORING CHUNKS: ", chunks.length());
                    for (int i = 0; i < chunks.length(); i += 3) {
                        int num = chunks.get(i);
                        int id = chunks.get(i + 1);
                        int xy = chunks.get(i + 2);
                        log("Num:", num);
                        log("Id: ", id);
                        log("XY: ", xy);
                        model.setXy(num, id, xy, true);
                    }
                } else throw new IllegalArgumentException("Incorrect restoreState format");
            } else inspectError(cmd, error);
        } else throw new IllegalArgumentException("Incorrect restoreState size");
    }

    /**
     * Parses CHECK_PROMOCODE command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses PROMOCODE_DONE command
     * @param cmd command
     * @param data arguments
     */
    private void promocodeDone(Cmd cmd, IIntArray data) {
        if (data.length() > 1) {
            boolean inviter = data.get(0) == 1;
            int gems = (data.get(1) << 24) | (data.get(2) << 16) | (data.get(3) << 8) | data.get(4);
            model.setPromocodeDone(data.remove(0, 5).toUTF8(), inviter, gems);
        } else if (data.length() == 1) {
            inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect 'promocode done' format");
    }

    /**
     * Parses GET_SKU_GEMS command
     * @param cmd command
     * @param data arguments
     */
    private void getSkuGems(Cmd cmd, IIntArray data) {
        if (data.length() > 1)
            model.setSkuGems(data);
        else if (data.length() == 1)
            inspectError(cmd, data.get(0));
        else throw new IllegalArgumentException("Incorrect SKU gems format");
    }

    /**
     * Parses CHECK_PURCHASE command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses EFFECT_CHANGED command
     * @param cmd command
     * @param data arguments
     */
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

    /**
     * Parses ABILITY_LIST command
     * @param cmd command
     * @param data arguments
     */
    private void abilitiesList(Cmd cmd, IIntArray data) {
        if (data.length() > 0) {
            int count = data.get(0);
            data.remove(0, 1);
            if (data.length() == count)
                model.setAbilities(data);
            else inspectError(cmd, data.get(0));
        } else throw new IllegalArgumentException("Incorrect abilities format");
    }

    /**
     * Parses GET_CLIENT_VERSION command
     * @param cmd command
     * @param data arguments
     */
    private void getClientVersion(Cmd cmd, IIntArray data) {
        if (data.length() == 6)
            model.setClientVersion(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5));
        else throw new IllegalArgumentException("Incorrect client version format" + cmd);
    }

    /**
     * Parses the error code and runs the corresponding method of Model
     * @param cmd command that causes the error
     * @param code error code
     */
    private void inspectError(Cmd cmd, int code) {
        switch (code) {
            case 0:
                break; // no error
            case ERR_BATTLE_NOT_FOUND0:
            case ERR_BATTLE_NOT_FOUND1:
            case ERR_BATTLE_NOT_FOUND2:
                break; // no error
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
            case ERR_NO_GEMS_ENOUGH:
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
            case ERR_NOT_ENOUGH_ARGS: // it may happen when non-Latin characters are used as a username
            case ERR_INCORRECT_NAME:
                model.setIncorrectName();
                break;
            case ERR_INCORRECT_EMAIL:
                model.setIncorrectEmail();
                break;
            case ERR_DUPLICATE_NAME:
                model.setDuplicateName();
                break;
            case ERR_SERVER_GONNA_STOP:
                model.setServerGonnaStop();
                break;
            default:
                String s = String.format(locale, "Unhandled error (cmd = %s, code = %d)", cmd, code);
                throw new IllegalArgumentException(s);
        }
    }
}
