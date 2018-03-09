package ru.mitrakov.self.rush.model.emulator;

import ru.mitrakov.self.rush.model.Model;
import ru.mitrakov.self.rush.net.IHandler;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

/**
 * Created by mitrakov on 04.02.2018
 */
public class ServerEmulator {
    private final IHandler handler;
    private final BattleManager battleManager;

    public ServerEmulator(Model.IFileReader fileReader, IHandler handler) {
        assert fileReader != null && handler != null;
        this.handler = handler;
        this.battleManager = new BattleManager(this, fileReader);
    }

    public void send(IIntArray data) {
        assert data != null;

        if (data.length() > 0) {
            int code = data.get(0);
            if (0 <= code && code < Model.cmdValues.length) {
                Model.Cmd cmd = Model.cmdValues[code];
                switch (cmd) {
                    case ATTACK:
                        boolean isInviteByName = data.length() > 1 && data.get(1) == 0;
                        if (isInviteByName)
                            attack(data.remove(0, 2).toUTF8());
                        else throw new IllegalArgumentException("ATTACK: incorrect ATTACK format");
                        break;
                    case MOVE:
                        if (data.length() > 1)
                            move(data.get(1));
                        else throw new IllegalArgumentException("MOVE: direction must be provided");
                        break;
                    case USE_THING:
                        useThing();
                        break;
                    case USE_SKILL:
                        if (data.length() > 1)
                            useSkill(data.get(1));
                        else throw new IllegalArgumentException("USE_SKILL: ability must be provided");
                        break;
                    case USER_INFO:
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Cmd %s not supported by Emulator", cmd));
                }
            }
        } else throw new IllegalArgumentException("Empty data");
    }

    void receive(IIntArray data) {
        handler.onReceived(data.prepend(data.length() % 256).prepend(data.length() / 256));
    }

    private void attack(String levelName) {
        battleManager.accept(Model.Character.Hedgehog, Model.Character.None, new String[]{levelName}, 90, 1); // TODO 90 1
    }

    private void move(int direction) {
        battleManager.move(direction);
    }

    private void useThing() {
        battleManager.useThing();
    }

    private void useSkill(int code) {

    }
}
