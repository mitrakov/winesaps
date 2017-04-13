package ru.mitrakov.self.rush.model;

import java.io.Serializable;

/**
 * Created by mitrakov on 06.03.2017
 */
@SuppressWarnings("WeakerAccess")
public final /*case*/ class Product implements Serializable {
    public final Model.Ability ability;
    public final int days;
    public final int crystals;

    Product(Model.Ability ability, int days, int crystals) {
        assert ability != null;
        this.ability = ability;
        this.days = days;
        this.crystals = crystals;
    }

    // GENERATED CODE

    @Override
    public String toString() {
        return "Product{" +
                "ability=" + ability +
                ", days=" + days +
                ", crystals=" + crystals +
                '}';
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
