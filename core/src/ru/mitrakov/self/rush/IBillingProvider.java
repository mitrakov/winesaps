package ru.mitrakov.self.rush;

import java.util.List;

/**
 * Created by mitrakov on 21.06.2017
 */
public interface IBillingProvider {
    interface BillingListener {
        void onResponse(String data, String signature);
    }

    void startService(BillingListener listener);
    List<Sku> getProducts();
    void purchaseProduct(Sku sku, String payload);

    @SuppressWarnings({"WeakerAccess", "NullableProblems"})
    final class Sku implements Comparable<Sku> {
        public final String id;
        public final String description;
        public final String price;
        public int value;
        Sku(String id, String description, String price) {
            this.id = id;
            this.description = description;
            this.price = price;
        }

        @Override
        public int compareTo(Sku o) {
            if (o != null)
                return value - o.value;
            return 0;
        }

        // GENERATED CODE

        @Override
        public String toString() {
            return "Sku{" + "id='" + id + '\'' + ", dscr='" + description + '\'' + ", price='" + price + '\'' + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sku sku = (Sku) o;
            return value == sku.value && id.equals(sku.id) && description.equals(sku.description) &&
                    price.equals(sku.price);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + description.hashCode();
            result = 31 * result + price.hashCode();
            result = 31 * result + value;
            return result;
        }
    }
}
