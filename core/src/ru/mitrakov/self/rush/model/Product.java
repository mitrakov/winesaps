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
    public final int gems;

    /**
     * Creates a new Product (an ability item for N days for the given price)
     * @param ability ability (skill or swagga)
     * @param days (duration of the ability)
     * @param gems price of the product
     */
    public Product(Model.Ability ability, int days, int gems) {
        assert ability != null;
        this.ability = ability;
        this.days = days;
        this.gems = gems;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "Product{" + "ability=" + ability + ", days=" + days + ", gems=" + gems + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return days == product.days && gems == product.gems && ability == product.ability;
    }

    @Override
    public int hashCode() {
        int result = ability.hashCode();
        result = 31 * result + days;
        result = 31 * result + gems;
        return result;
    }
}
