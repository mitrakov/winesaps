package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Data class representing a product: an ability item for N days for the given price
 * @author mitrakov
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class Product implements Serializable {
    /** Ability */
    public final Model.Ability ability;
    /** Duration of the ability */
    public final int days;
    /** Price of the product */
    public final int crystals;

    /**
     * Creates a new Product (an ability item for N days for the given price)
     * @param ability ability (skill or swagga)
     * @param days (duration of the ability)
     * @param crystals price of the product
     */
    public Product(Model.Ability ability, int days, int crystals) {
        assert ability != null;
        this.ability = ability;
        this.days = days;
        this.crystals = crystals;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "Product{" + "ability=" + ability + ", days=" + days + ", crystals=" + crystals + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return days == product.days && crystals == product.crystals && ability == product.ability;
    }

    @Override
    public int hashCode() {
        int result = ability.hashCode();
        result = 31 * result + days;
        result = 31 * result + crystals;
        return result;
    }
}
