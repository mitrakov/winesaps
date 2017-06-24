package ru.mitrakov.self.rush;

import java.util.*;
import java.util.concurrent.*;

import android.app.Activity;

import com.android.billingclient.api.*;

import static com.android.billingclient.api.BillingClient.SkuType.*;
import static com.android.billingclient.api.BillingClient.BillingResponse.*;
import static ru.mitrakov.self.rush.utils.SimpleLogger.log;

/**
 * Created by mitrakov on 21.06.2017
 */
class AndroidBillingProvider implements IBillingProvider, PurchasesUpdatedListener {
    private final Activity activity;
    private final BillingClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Sku> products = new ArrayList<>(3);
    private /*final*/ BillingListener listener;

    AndroidBillingProvider(Activity activity) {
        assert activity != null;
        this.activity = activity;
        client = new BillingClient.Builder(activity).setListener(this).build();
    }

    @Override
    public void startService(BillingListener listener) {
        assert listener != null;
        this.listener = listener;
        client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int resultCode) {
                if (resultCode == OK) {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            List<String> lst = Arrays.asList("gems_pack_small", "gems_pack", "gems_pack_big");
                            client.querySkuDetailsAsync(INAPP, lst, new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(SkuDetails.SkuDetailsResult result) {
                                    if (result.getResponseCode() == OK) {
                                        for (SkuDetails d : result.getSkuDetailsList()) {
                                            products.add(new Sku(d.getSku(), d.getDescription(), d.getPrice()));
                                        }
                                    } else log("Sku details error! Code:", result.getResponseCode());
                                }
                            });
                        }
                    });
                } else log("Billing connection error! Code:", resultCode);
            }

            @Override
            public void onBillingServiceDisconnected() {
                System.out.println("Disconnected");
            }
        });
    }

    @Override
    public List<Sku> getProducts() {
        return products;
    }

    @Override
    public void purchaseProduct(Sku sku, String payload) {
        if (client.isReady()) {
            BillingFlowParams params = new BillingFlowParams.Builder()
                    .setSku(sku.id).setType(INAPP).setAccountId(payload).build();
            client.launchBillingFlow(activity, params);
        } else log("", "Billing system is not ready!");
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        assert listener != null;
        if (responseCode == OK) {
            for (final Purchase purchase : purchases) {
                log("OrigJSON: ", purchase.getOriginalJson());
                log("Signature: ", purchase.getSignature());
                client.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(String purchaseToken, int resultCode) {
                        log("Consuming done; code = ", resultCode);
                        listener.onResponse(purchase.getOriginalJson(), purchase.getSignature());
                    }
                });
            }
        }
    }
}
