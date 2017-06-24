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

    final class Sku {
        public final String id;
        final String description;
        final String proice;
        Sku(String id, String description, String proice) {
            this.id = id;
            this.description = description;
            this.proice = proice;
        }
        @Override
        public String toString() {
            return "Sku{" +
                    "id='" + id + '\'' + ", description='" + description + '\'' + ", proice='" + proice + '\'' + '}';
        }
    }
}
