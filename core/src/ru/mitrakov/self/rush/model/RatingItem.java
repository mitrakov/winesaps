package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Created by mitrakov on 06.03.2017
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class RatingItem implements Serializable {
    public final String name;
    public final int victories;
    public final int defeats;
    public final int score_diff;

    public RatingItem(String name, int victories, int defeats, int score_diff) {
        assert name != null;
        this.name = name;
        this.victories = victories;
        this.defeats = defeats;
        this.score_diff = score_diff;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "RatingItem{" +
                "name='" + name + '\'' +
                ", victories=" + victories +
                ", defeats=" + defeats +
                ", score_diff=" + score_diff +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RatingItem that = (RatingItem) o;

        return victories == that.victories && defeats == that.defeats && score_diff == that.score_diff
                && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + victories;
        result = 31 * result + defeats;
        result = 31 * result + score_diff;
        return result;
    }
}
