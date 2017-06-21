package ru.mitrakov.self.rush;

import java.util.List;

/**
 * Created by mitrakov on 21.06.2017
 */
public interface IBillingProvider {
    public void init();
    public List<Sku> getProducts();
    public void purchaseProduct(Sku sku);

    public static final class Sku {
        public final String id;
        public final String description;
        public final String proice;
        public Sku(String id, String description, String proice) {
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
