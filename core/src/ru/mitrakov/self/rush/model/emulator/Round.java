package ru.mitrakov.self.rush.model.emulator;

import java.util.*;

import ru.mitrakov.self.rush.model.*;
import ru.mitrakov.self.rush.GcResistantIntArray;
import ru.mitrakov.self.rush.utils.collections.IIntArray;

import static ru.mitrakov.self.rush.model.Field.*;
import static ru.mitrakov.self.rush.model.Model.Ability.*;
import static ru.mitrakov.self.rush.model.Model.Character.*;
import static ru.mitrakov.self.rush.model.Model.MoveDirection.*;
import static ru.mitrakov.self.rush.model.Model.abilityValues;

/**
 * Analog of Server Round class (reconstructed from Server v.1.3.6)
 * @author Mitrakov
 */
public class Round {
    /** Reference to the Battle manager */
    private final BattleManager battleManager;
    /** Helper array for 'current abilities' (in order to avoid "new" operations and decrease GC pressure) */
    private final IIntArray abilities = new GcResistantIntArray(abilityValues.length);
    /** Helper set to store all the skills used */
    private final Set<Model.Ability> usedSkills = new LinkedHashSet<Model.Ability>();
    /** Random */
    private final Random rand = new Random(System.nanoTime());

    /** Round number, starting with 0 */
    final int number;
    /** Player 1 */
    final Player player1;
    /** Player 2 */
    final Player player2;
    /** Battlefield */
    final FieldEx field;
    /** Level name */
    final String levelname;
    /** Round Countdown Timer (it is named "stop" because in Go there is a channel "stop" to interrupt the timer) */
    final Timer stop;

    /**
     * Creates new round
     * @param character1 character of Player 1
     * @param character2 character of Player 2
     * @param number round number, starting with 0
     * @param levelName level name
     * @param skills1 list of skills for Player 1
     * @param skills2 list of skills for Player 2
     * @param swaggas1 list of swaggas for Player 1
     * @param swaggas2 list of swaggas for Player 2
     * @param battleManager {@link BattleManager}
     */
    public Round(Model.Character character1, Model.Character character2, int number, String levelName,
                 List<Model.Ability> skills1, List<Model.Ability> skills2,
                 List<Model.Ability> swaggas1, List<Model.Ability> swaggas2, BattleManager battleManager) {
        assert character1 != null && character2 != null && levelName != null && battleManager != null;
        assert number >= 0;

        this.number = number;
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

        actor1.setCharacter(character1);
        if (actor2 != null)                            // on ServerEmulator actor2 may be NULL (on Server - can't)
            actor2.setCharacter(character2);
        else actor2 = createFakeActorDifferentFrom(character1);

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
        this.stop = new Timer(true);
        this.stop.schedule(new TimerTask() {
            @Override
            public void run() {
                timeOut();
            }
        }, field.timeSec * 1000);
    }

    /**
     * Returns Player by Sid (Server's notation kept; here we just return either our Player, or enemy's Player)
     * @param isSid1 TRUE to return our Player, and FALSE - to return our enemy's Player
     * @return either our Player, or enemy's Player
     */
    Player getPlayerBySid(boolean isSid1) {
        return isSid1 ? player1 : player2;
    }

    /**
     * @param actor actor from the Battlefield
     * @return Player instance by the given Actor instance
     */
    Player getPlayerByActor(ActorEx actor) {
        assert actor != null;
        if (actor == player1.actor) return player1;
        if (actor == player2.actor) return player2;
        return null;
    }

    /**
     * Checks whether a round should be finished (usually when all the fruit have been eaten)
     */
    synchronized void checkRoundFinished() {
        // tryMutex is not necessary here (synchronized is enough)
        if (field.getFoodCountForActor(player1.actor) == 0)
            battleManager.roundFinished(true);
        /* This is a Server algorithm:
        if (player1.score > foodTotal / 2)
            battleManager.roundFinished(true);
        else if (player2.score > foodTotal / 2)
            battleManager.roundFinished(false);
        else if (field.getFoodCount() == 0)
            finishRoundForced(); */
    }

    /**
     * Invoked by the internal timer on time out
     */
    synchronized private void timeOut() {
        // tryMutex is not necessary here (synchronized is enough)
        finishRoundForced();
    }

    /**
     * Forcefully closes the round regardless of the normal game-over conditions
     */
    private void finishRoundForced() {
        battleManager.roundFinished(false);
        /* This is a Server algorithm:
        if (player1.score > player2.score) {
            battleManager.roundFinished(true);
        } else if (player2.score > player1.score) {
            battleManager.roundFinished(false);
        } else { // draw: let's check who has more lives
            if (player1.lives > player2.lives)
                battleManager.roundFinished(true);
            // note: if draw and lives are equals let's suppose the defender (player2) wins
            battleManager.roundFinished(false);
        }*/
    }

    /**
     * Performs single "move" action for Actor1
     * @param direction move direction
     */
    void move(Model.MoveDirection direction) {
        // get components
        ActorEx actor = player1.actor;
        assert actor != null;
        Cell cell = actor.getCell();
        assert cell != null;

        // calculate delta. DO NOT USE `switch` HERE! (please see details in Model.move() method)
        int delta = 0;
        if (direction == LeftDown)
            delta = field.isMoveDownPossible(cell) ? WIDTH : -1;
        else if (direction == Left)
            delta = -1;
        else if (direction == LeftUp)
            delta = field.isMoveUpPossible(cell) ? -WIDTH : -1;
        else if (direction == RightDown)
            delta = field.isMoveDownPossible(cell) ? WIDTH : 1;
        else if (direction == Right)
            delta = 1;
        else if (direction == RightUp)
            delta = field.isMoveUpPossible(cell) ? -WIDTH : 1;

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

    /**
     * Decreases the count of lives and returns whether the remaining count of lives are still > 0
     * @param me TRUE for our Player, and FALSE - for the enemy
     * @return TRUE, if remaining lives is enough to continue the round, and FALSE otherwise
     */
    boolean wound(boolean me) {
        return me ? (--player1.lives > 0) : (--player2.lives > 0);
    }

    /**
     * Restores the actor at its start place after being wounded
     * <br><b>Note:</b> here we consider only Actor1, just as on the Server both actors are taken into account
     */
    void restore() {
        ActorEx actor = player1.actor;
        assert actor != null;
        Cells.Entry1 entry = field.getEntryByActor();
        if (entry != null) {
            assert actor.getCell() != null && entry.getCell() != null;
            field.relocate(actor.getCell(), entry.getCell(), actor, true);
        } else throw new IllegalStateException("Entry not found");
    }

    /**
     * Sets the given thing (e.g. {@link ru.mitrakov.self.rush.model.Cells.UmbrellaThing}) to the player
     * <br><b>Note:</b> here we consider only Player1, just as on the Server both players are taken into account
     * @param thing thing
     */
    void setThingToPlayer(Cells.CellObjectThing thing) {
        assert field != null && player1 != null;

        Cells.CellObjectThing oldThing = player1.setThing(thing);
        if (oldThing != null)
            field.dropThing(player1.actor, oldThing);
    }

    /**
     * Method to use a thing (has no effect if the Player has no thing)
     * @see #useSkill(int)
     */
    void useThing() {
        Cells.CellObjectThing thing = player1.setThing(null);
        if (thing != null)
            field.useThing(player1.actor, thing);
    }

    /**
     * Method to apply the skill and generate one, and only one new object (e.g.
     * {@link ru.mitrakov.self.rush.model.Model.Ability#Miner Miner} can produce
     * {@link ru.mitrakov.self.rush.model.Cells.MineThing MineThing}).
     * Skill will be <b>consumed</b> after success operation
     * @param skillId skill ID
     * @return new thing generated by the skill
     * @see #useThing()
     */
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

    /**
     * Retrieves all current <b>non-consumed</b> in this round abilities (swaggas and skills)
     * @return list of abilities
     */
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

    /**
     * Applies the skill
     * <br><b>Note:</b> this method is a helper and does not exist on the Server (on Server the logic of producing new
     * things is encapsulated inside the skills themselves; here we just reuse
     * {@link ru.mitrakov.self.rush.model.Model.Ability} enum, and in order not to rewrite that code we have to have
     * such factory methods)
     * @param skill skill to get applied
     * @param objNumber sequential number for the new object that will be generated by the skill
     * @return new object generated by the skill
     */
    private Cells.CellObjectThing skillApply(Model.Ability skill, int objNumber) {
        if (usedSkills.contains(skill)) return null;
        usedSkills.add(skill);

        // DANGER CODE: "new" may cause troubles with Garbage Collector; we should investigate its impact
        if (skill == Miner) return new Cells.MineThing(TRASH_CELL, objNumber);
        if (skill == Builder) return new Cells.BeamThing(TRASH_CELL, objNumber);
        if (skill == Shaman) return new Cells.AntidoteThing(TRASH_CELL, objNumber);
        if (skill == Grenadier) return new Cells.FlashbangThing(TRASH_CELL, objNumber);
        if (skill == TeleportMan) return new Cells.TeleportThing(TRASH_CELL, objNumber);
        return null;
    }

    /**
     * Helper method to create an actor that has a character, different from the given character.
     * It can be useful to avoid generating the identical enemy that leads to generation the same non-poisoned food on
     * the battlefield.
     * <br><b>Note:</b> method doesn't exist on the Server
     * @param character base character
     * @return new Actor whose character differs from the given character
     */
    private ActorEx createFakeActorDifferentFrom(Model.Character character) {
        final double r = rand.nextDouble();
        final Model.Character newCharacter;

        // DO NOT use switch(character)!!! It causes call Character.values() that produces work for GC!
        if (character == Rabbit)
            newCharacter = r < .33 ? Hedgehog : r < .66 ? Squirrel : Cat;
        else if (character == Hedgehog)
            newCharacter = r < .33 ? Rabbit : r < .66 ? Squirrel : Cat;
        else if (character == Squirrel)
            newCharacter = r < .33 ? Rabbit : r < .66 ? Hedgehog : Cat;
        else if (character == Cat)
            newCharacter = r < .33 ? Rabbit : r < .66 ? Hedgehog : Squirrel;
        else newCharacter = Model.Character.None;

        ActorEx result = new ActorEx(TRASH_CELL, 0);
        result.setCharacter(newCharacter);
        return result;
    }
}
