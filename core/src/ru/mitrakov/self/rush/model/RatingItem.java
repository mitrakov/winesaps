package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Data class representing a single rating item (row in a ranking table)
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class RatingItem implements Serializable {
    /** User name */
    public final String name;
    /** Wins */
    public final int victories;
    /** Losses */
    public final int defeats;
    /** Total score difference (may be negative) */
    public final int score_diff;

    /**
     * Creates a new rating item (row in a ranking table)
     * @param name user name
     * @param victories wins
     * @param defeats losses
     * @param score_diff total score difference (may be negative)
     */
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
