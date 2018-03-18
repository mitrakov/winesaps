package ru.mitrakov.self.rush.model.emulator;

import java.util.*;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Field.*;
import static ru.mitrakov.self.rush.model.Model.Ability.*;
import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * Created by mitrakov on 09.03.2018
 */
public class Round {
    private final TryMutex tryMutex = new TryMutex();
    private final int foodTotal;
    private final BattleManager battleManager;
    private final IIntArray abilities = new GcResistantIntArray(abilityValues.length);
    private final Set<Model.Ability> usedSkills = new LinkedHashSet<Model.Ability>();

    final int number;
    final int timeSec;
    final Player player1;
    final Player player2;
    final FieldEx field;
    final String levelname;
    final Timer stop;

    public Round(Model.Character character1, Model.Character character2, int number, String levelName, int timeSec,
                 List<Model.Ability> skills1, List<Model.Ability> skills2,
                 List<Model.Ability> swaggas1, List<Model.Ability> swaggas2, BattleManager battleManager) {
        assert character1 != null && character2 != null && levelName != null && battleManager != null;
        assert number >= 0 && timeSec > 0;

        this.number = number;
        this.timeSec = timeSec;
        this.levelname = levelName;
        this.battleManager = battleManager;

        Model.IFileReader reader = battleManager.getFileReader();
        assert reader != null;

        String path = String.format("levels/%s.level", levelName);
        byte[] level = reader.readAsByteArray(path);
        IIntArray array = new GcResistantIntArray(WIDTH * HEIGHT);
        IIntArray raw = new GcResistantIntArray(level.length);
        array.fromByteArray(level, WIDTH * HEIGHT);
        raw.fromByteArray(level, level.length);

        Environment env = battleManager.getEnvironment();
        assert env != null;

        field = new FieldEx(array, raw, battleManager);
        env.addField(field);

        ActorEx actor1 = field.actor1;
        ActorEx actor2 = field.actor2;
        assert actor1 != null && actor2 != null;
        actor1.setCharacter(character1);
        actor2.setCharacter(character2);
        for (int i = 0; i < swaggas1.size(); i++) {    // don't use iterators here (GC!)
            Model.Ability s = swaggas1.get(i);
            actor1.addSwagga(s);
        }
        for (int i = 0; i < swaggas2.size(); i++) {    // don't use iterators here (GC!)
            Model.Ability s = swaggas2.get(i);
            actor2.addSwagga(s);
        }

        field.replaceFavouriteFood(actor1, actor2);
        player1 = new Player(actor1, skills1);
        player2 = new Player(actor2, skills2);
        this.foodTotal = field.getFoodCount();
        this.stop = new Timer(true);
        this.stop.schedule(new TimerTask() {
            @Override
            public void run() {
                timeOut();
            }
        }, timeSec * 1000);
    }

    Player getPlayerByActor(ActorEx actor) {
        assert actor != null;
        if (actor == player1.actor) return player1;
        if (actor == player2.actor) return player2;
        return null;
    }

    void checkRoundFinished() {
        tryMutex.onlyOne(new Runnable() {
            @Override
            public void run() {
                if (player1.score > foodTotal / 2) {
                    battleManager.roundFinished(true);
                } else if (player2.score > foodTotal / 2) {
                    battleManager.roundFinished(false);
                } else if (field.getFoodCount() == 0) {
                    finishRoundForced();
                }
            }
        });
    }

    private void timeOut() {
        tryMutex.onlyOne(new Runnable() {
            @Override
            public void run() {
                finishRoundForced();
            }
        });
    }

    private void finishRoundForced() {
        if (player1.score > player2.score) {
            battleManager.roundFinished(true);
        } else if (player2.score > player1.score) {
            battleManager.roundFinished(false);
        } else { // draw: let's check who has more lives
            if (player1.lives > player2.lives)
                battleManager.roundFinished(true);
            // note: if draw and lives are equals let's suppose the defender (player2) wins
            battleManager.roundFinished(false);
        }
    }

    void move(Model.MoveDirection direction) {
        // get components
        ActorEx actor = player1.actor;
        assert actor != null;
        Cell cell = actor.getCell();
        assert cell != null;

        // calculate delta
        int delta = 0;
        switch (direction) {
            case LeftDown:
                delta = field.isMoveDownPossible(cell) ? WIDTH : -1;
                break;
            case Left:
                delta = -1;
                break;
            case LeftUp:
                delta = field.isMoveUpPossible(cell) ? -WIDTH : -1;
                break;
            case RightDown:
                delta = field.isMoveDownPossible(cell) ? WIDTH : 1;
                break;
            case Right:
                delta = 1;
                break;
            case RightUp:
                delta = field.isMoveUpPossible(cell) ? -WIDTH : 1;
                break;
            default:
        }

        // set actor's direction (left/right)
        if (delta == 1)
            actor.setDirectionRight(true);
        if (delta == -1)
            actor.setDirectionRight(false);

        // go!
        boolean ok = field.move(actor, actor.getXy() + delta);

        // if movement ok => inc actor's internal step counter to get effects working
        if (ok)
            actor.addStep();
    }

    boolean wound(boolean me) {
        return me ? (--player1.lives > 0) : (--player2.lives > 0);
    }

    void restore() {
        ActorEx actor = player1.actor;
        assert actor != null;
        Cells.Entry1 entry = field.getEntryByActor();
        if (entry != null) {
            assert actor.getCell() != null && entry.getCell() != null;
            field.relocate(actor.getCell(), entry.getCell(), actor, true);
        } else throw new IllegalStateException("Entry not found");
    }

    void setThingToPlayer(Cells.CellObjectThing thing) {
        assert field != null && player1 != null;

        Cells.CellObjectThing oldThing = player1.setThing(thing);
        if (oldThing != null)
            field.dropThing(player1.actor, oldThing);
    }

    void useThing() {
        Cells.CellObjectThing thing = player1.setThing(null);
        if (thing != null)
            field.useThing(player1.actor, thing);
    }

    Cells.CellObjectThing useSkill(int skillId) {
        Model.Ability skill = player1.getSkill(skillId);
        if (skill != null) {
            Cells.CellObjectThing thing = skillApply(skill, field.getNextNum());
            if (thing != null) {
                battleManager.objAppended(thing);
                setThingToPlayer(thing);
                return thing;
            }
            return null; // no error here: skill may cast nothing
        } else throw new IllegalArgumentException(String.format(Locale.getDefault(), "Skill not found %d", skillId));
    }

    synchronized IIntArray getCurrentAbilities() {
        ActorEx actor = player1.actor; assert actor != null;
        List<Model.Ability> swaggas = actor.getSwaggas();
        List<Model.Ability> skills = player1.skills;
        abilities.clear();
        for (int i = 0; i < swaggas.size(); i++) {                       // don't use iterators here
            Model.Ability s = swaggas.get(i);
            int abilityId = Arrays.binarySearch(abilityValues, s);       // don't use "cmd.ordinal()"
            abilities.add(abilityId);
        }
        for (int i = 0; i < skills.size(); i++) {
            Model.Ability s = skills.get(i);
            if (!usedSkills.contains(s)) {
                int abilityId = Arrays.binarySearch(abilityValues, s);   // don't use "cmd.ordinal()"
                abilities.add(abilityId);
            }
        }
        return abilities;
    }

    private Cells.CellObjectThing skillApply(Model.Ability skill, int objNumber) {
        if (usedSkills.contains(skill)) return null;
        usedSkills.add(skill);

        if (skill == Miner) return new Cells.MineThing(TRASH_CELL, objNumber); // TODO new!
        if (skill == Builder) return new Cells.BeamThing(TRASH_CELL, objNumber); // TODO new!
        if (skill == Shaman) return new Cells.AntidoteThing(TRASH_CELL, objNumber); // TODO new!
        if (skill == Grenadier) return new Cells.FlashbangThing(TRASH_CELL, objNumber); // TODO new!
        if (skill == TeleportMan) return new Cells.TeleportThing(TRASH_CELL, objNumber); // TODO new!
        return null;
    }
}
