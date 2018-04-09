package ru.mitrakov.self.rush;

import java.util.*;

/**
 * Interface for Billing Providers (that can differ for different platforms)
 * @author mitrakov
 */
public interface IBillingProvider {
    /** Current SKU List. Please refer to server documentation for more details */
    List<String> SKU_LIST = Arrays.asList("gems_pack_small", "gems_pack", "gems_pack_big");

    /**
     * Callback interface for Billing Providers
     */
    interface BillingListener {
        /**
         * Invoked when response from a Billing Provider comes
         * @param data string data
         * @param signature signature (might be empty for some providers)
         */
        void onResponse(String data, String signature);
    }

    /**
     * Initializes service (e.g. connecting to a Billing Provider, registering callbacks, etc.)
     * @param listener Billing Listener
     */
    void startService(BillingListener listener);
    /**
     * @return list of SKUs available (note that they're fetched from a Billing Provider and might differ from SKU_LIST)
     */
    List<Sku> getProducts();
    /**
     * Initiates billing flow
     * @param sku SKU chosen by a user
     * @param payload additional data expressed as a string (might be empty if a provider doesn't support payloads)
     */
    void purchaseProduct(Sku sku, String payload);

    /**
     * Stock Keeping Unit
     * @author mitrakov
     */
    @SuppressWarnings({"WeakerAccess", "NullableProblems"})
    final class /*case*/ Sku implements Comparable<Sku> {
        /** SKU ID */
        public final String id;
        /** SKU description */
        public final String description;
        /** SKU price (for most Billing Provider may also contain currency, e.g. USD, EUR or RUB) */
        public final String price;
        /** SKU price (gems count) */
        public int value;

        /**
         * Creates a new SKU
         * @param id SKU ID
         * @param description SKU description
         * @param price SKU price (gems count)
         */
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
            return "Sku{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", price='" + price + '\''
                    + ", value=" + value + '}';
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
