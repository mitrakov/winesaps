package ru.mitrakov.self.rush.model;

import java.util.Date;
import java.io.Serializable;

/**
 * Data class representing a single item of user's history of battles (1 item corresponds to 1 battle result)
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class HistoryItem implements Serializable {
    /** Date of the battle */
    public final Date date;
    /** Win/lose flag (relatively to us) */
    public final boolean win;
    /** Aggressor name */
    public final String name1;
    /** Defender name */
    public final String name2;
    /** Aggressor character */
    public final Model.Character character1;
    /** Defender character */
    public final Model.Character character2;
    /** Aggressor total score */
    public final int score1;
    /** Defender total score */
    public final int score2;

    /**
     * Creates a new history item (1 item corresponds to 1 battle result)
     * @param date date of the battle
     * @param win win/lose (relatively to us)
     * @param name1 aggressor name
     * @param name2 defender name
     * @param character1 aggressor character
     * @param character2 defender character
     * @param score1 aggressor total score
     * @param score2 defender total score
     */
    public HistoryItem(Date date, boolean win, String name1, String name2, Model.Character character1,
                       Model.Character character2, int score1, int score2) {
        assert date != null && name1 != null && name2 != null;
        this.date = new Date(date.getTime()); // recommended by FindBugs
        this.win = win;
        this.name1 = name1;
        this.name2 = name2;
        this.character1 = character1;
        this.character2 = character2;
        this.score1 = score1;
        this.score2 = score2;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "HistoryItem{" +
                "date=" + date +
                ", win=" + win +
                ", name1='" + name1 + '\'' +
                ", name2='" + name2 + '\'' +
                ", character1=" + character1 +
                ", character2=" + character2 +
                ", score1=" + score1 +
                ", score2=" + score2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryItem item = (HistoryItem) o;

        return win == item.win
                && score1 == item.score1
                && score2 == item.score2
                && date.equals(item.date)
                && name1.equals(item.name1)
                && name2.equals(item.name2)
                && character1 == item.character1
                && character2 == item.character2;

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + (win ? 1 : 0);
        result = 31 * result + name1.hashCode();
        result = 31 * result + name2.hashCode();
        result = 31 * result + character1.hashCode();
        result = 31 * result + character2.hashCode();
        result = 31 * result + score1;
        result = 31 * result + score2;
        return result;
    }
}
