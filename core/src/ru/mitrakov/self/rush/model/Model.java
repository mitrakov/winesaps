package ru.mitrakov.self.rush.model;

import java.util.*;
import java.util.concurrent.*;

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
    public static final byte ABILITY_LIST = 0x1C;
    public static final byte OBJECT_APPENDED = 0x1D;

    public interface ISender {
        void send(int cmd);

        void send(int cmd, byte arg);

        void send(int cmd, byte[] data);
    }

    public enum Ability {
        None, Snorkel, Shoes, SouthWester, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16,
        a17, a18, a19, a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32, Sapper
    }

    private static final int AGGRESSOR_ID = 1;
    private static final int DEFENDER_ID = 2;
    private static final int SKILL_OFFSET = 0x20;

    // @mitrakov: getters are supposed to have a little overhead, so we make the fields "public" for efficiency
    public volatile String name;
    public volatile boolean authorized = false; // @mitrakov: must be volatile due to multithreading access
    public volatile int crystals;
    public volatile int score1 = 0;
    public volatile int score2 = 0;
    public volatile Field field;
    public volatile CellObject curActor;
    public volatile CellObject curThing;
    public final Queue<Ability> abilities = new ConcurrentLinkedQueue<Ability>(); // ....
    public final Map<Ability, Integer> abilityExpireTime = new ConcurrentHashMap<Ability, Integer>(4); // ....

    private ISender sender;
    private boolean aggressor = true;


    public Model() {
    }

    public void setSender(ISender sender) {
        this.sender = sender;
    }

    // =======================
    // === REQUEST METHODS ===
    // =======================

    public void signIn(String login, String password) {
        if (sender != null) {
            sender.send(SIGN_IN, String.format("\1%s\0%s", login, password).getBytes());
        }
    }

    public void signUp(String login, String password, String email) {
        if (sender != null) {
            sender.send(SIGN_UP); // TODO
        }
    }

    public void invite(String victim) {
        if (sender != null) {
            aggressor = true;
            sender.send(ATTACK, String.format("\0%s", victim).getBytes());
        }
    }

    public void inviteLatest() {
        if (sender != null) {
            aggressor = true;
            sender.send(ATTACK, (byte) 1);
        }
    }

    public void inviteRandom() {
        if (sender != null) {
            aggressor = true;
            sender.send(ATTACK, (byte) 2);
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

    public void useAbility(Ability ability) {
        assert ability != null;
        if (sender != null) {
            if (ability.ordinal() > SKILL_OFFSET) // only skills may be used
                sender.send(USE_FACILITY, (byte) ability.ordinal());
        }
    }

    public void useAbilityByIndex(int idx) {
        int i = 0;
        for (Ability ability : abilities) {
            if (i == idx) useAbility(ability);
            i++;
        }
    }

    // ========================
    // === RESPONSE METHODS ===
    // ========================

    public void setAuthorized() {
        authorized = true;
        if (sender != null)
            sender.send(USER_INFO);
    }

    public void setUserInfo(int[] data) {
        byte bytes[] = new byte[data.length];
        int i = 0;

        // parse name
        for (; i < data.length && data[i] != 0; i++) {
            bytes[i] = (byte) data[i];
        }
        name = new String(bytes);
        i++;

        // parse crystals
        if (i + 3 < data.length)
            crystals = (data[i] << 24) | (data[i + 1] << 16) | (data[i + 2] << 8) | (data[i + 3]); // what if > 2*10^9?
        i += 4;

        // parse abilities
        Ability[] array = Ability.values();
        abilityExpireTime.clear();
        int abilitiesCnt = data[i++];
        for (int j = 0; j < abilitiesCnt; j++, i += 3) {
            if (i + 2 < data.length) {
                int id = data[i];
                int minutes = data[i + 1] * 256 + data[i + 2];
                if (0 <= id && id < array.length)
                    abilityExpireTime.put(array[id], minutes);
            }
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

    public void setThing(int thingId) {
        curThing = Cell.newObject(thingId, 0xFF, new Field.NextNumber() {
            @Override
            public int next() {
                return 0;
            }
        });
    }

    public void setAbilities(int[] ids) {
        abilities.clear();
        Ability[] array = Ability.values();
        for (int id : ids) {
            if (0 <= id && id < array.length)
                abilities.add(array[id]);
        }
    }
}
