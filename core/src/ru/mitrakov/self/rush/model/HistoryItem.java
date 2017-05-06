package ru.mitrakov.self.rush.model;

import java.util.Date;
import java.io.Serializable;

/**
 * Created by mitrakov on 12.04.2017
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class HistoryItem implements Serializable {
    public final Date date;
    public final boolean win;
    public final String name1;
    public final String name2;
    public final Model.Character character1;
    public final Model.Character character2;
    public final int score1;
    public final int score2;

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
