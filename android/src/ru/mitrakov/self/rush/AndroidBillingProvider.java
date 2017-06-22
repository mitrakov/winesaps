package ru.mitrakov.self.rush;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientImpl;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.mitrakov.self.rush.model.Product;
import ru.mitrakov.self.rush.utils.SimpleLogger;

import static com.android.billingclient.api.BillingClient.BillingResponse.*;
import static com.android.billingclient.api.BillingClient.SkuType.*;
import static ru.mitrakov.self.rush.utils.SimpleLogger.log;

/**
 * Created by mitrakov on 21.06.2017
 */
public class AndroidBillingProvider implements IBillingProvider, PurchasesUpdatedListener {
    private final Activity activity;
    private final BillingClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Sku> products = new ArrayList<>(3);

    public AndroidBillingProvider(Activity activity) {
        assert activity != null;
        this.activity = activity;
        client = new BillingClient.Builder(activity).setListener(this).build();

    }

    @Override
    public void init() {
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
                                            System.out.println(d);
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
    public void purchaseProduct(Sku sku) {
        if (client.isReady()) {
            BillingFlowParams params = new BillingFlowParams.Builder().setSku(sku.id).setType(INAPP).build();
            client.launchBillingFlow(activity, params);
        } else log("", "Billing system is not ready!");
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        log("onPurchasesUpdated; code = ", responseCode);
        if (responseCode == OK) {
            for (Purchase purchase : purchases) {
                log("Purchase: ", purchase);
                log("Token: ", purchase.getPurchaseToken());
                client.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(String purchaseToken, int resultCode) {
                        log("CONSUMING DONE! Code = ", resultCode);
                        log("CONSUMING DONE! Token = ", purchaseToken);
                    }
                });
            }
        }
    }
}
