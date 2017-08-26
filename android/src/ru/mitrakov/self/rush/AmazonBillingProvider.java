package ru.mitrakov.self.rush;

import java.util.*;

import android.app.Activity;

import com.amazon.device.iap.*;
import com.amazon.device.iap.model.*;

import org.json.JSONException;

import static ru.mitrakov.self.rush.utils.SimpleLogger.log;

/**
 * Billing Provider for Amazon App Store
 */
class AmazonBillingProvider implements IBillingProvider, PurchasingListener {
    private final Activity activity;
    private final List<Sku> products = new ArrayList<>(3);
    private final Object productsLocker = new Object();
    private /*final*/ BillingListener listener;

    AmazonBillingProvider(Activity activity) {
        assert activity != null;
        this.activity = activity;
    }

    @Override
    public void startService(BillingListener listener) {
        assert listener != null;
        this.listener = listener;
        PurchasingService.registerListener(activity, this);

        // 1. Query all SKU details
        log("AMAZON", "Starting service");
        PurchasingService.getProductData(new TreeSet<>(SKU_LIST));
        // 2. Consume possibly outstanding purchases (recommendation from Amazon)
        PurchasingService.getPurchaseUpdates(false);
    }

    @Override
    public List<Sku> getProducts() {
        return products;
    }

    @Override
    public void purchaseProduct(Sku sku, String payload) {
        PurchasingService.purchase(sku.id);
    }

    @Override
    public void onProductDataResponse(ProductDataResponse response) {
        log("AMAZON response", response);
        assert response != null;
        if (response.getRequestStatus() == ProductDataResponse.RequestStatus.SUCCESSFUL) {
            Map<String, Product> data = response.getProductData();
            if (data != null) {
                synchronized (productsLocker) { // just in case (Async method on outside API)
                    products.clear();
                    for (Map.Entry<String, Product> e : data.entrySet()) {
                        log(e.getKey(), e.getValue());
                        Product product = e.getValue();
                        if (product != null) {
                            products.add(new Sku(product.getSku(), product.getDescription(), product.getPrice()));
                        }
                    }
                }
            }
        } else log("Amazon Sku details error! Status:", response.getRequestStatus());
    }

    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        log("AMAZON purchaseResp", response);
        assert response != null && listener != null;
        if (response.getRequestStatus() == PurchaseResponse.RequestStatus.SUCCESSFUL) {
            try {
                log("Amazon JSON: ", response.toJSON());
                handleReceipt(response.getReceipt());
            } catch (JSONException ignored) {
            }
        } else log("Amazon Purchasing error! Status:", response.getRequestStatus());
    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
        log("AMAZON updateresp", response);
        assert response != null;
        if (response.getRequestStatus() == PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) {
            for (Receipt receipt : response.getReceipts()) {
                handleReceipt(receipt);
            }
            if (response.hasMore())
                PurchasingService.getPurchaseUpdates(false);
        } else log("Amazon PurchaseUpdates error! Status:", response.getRequestStatus());
    }

    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {
    }

    private void handleReceipt(Receipt receipt) {
        assert receipt != null;
        PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
        listener.onResponse(receipt.toJSON().toString(), "AMAZON");
    }
}
