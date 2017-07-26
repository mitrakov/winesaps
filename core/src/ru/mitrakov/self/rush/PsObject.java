package ru.mitrakov.self.rush;

import java.util.*;

/**
 * Created by mitrakov on 01.03.2017
 */
@SuppressWarnings("WeakerAccess")
public abstract class PsObject {
    private IBillingProvider billingProvider;

    public PsObject(IBillingProvider billingProvider) {
        this.billingProvider = billingProvider;
    }

    public IBillingProvider getBillingProvider() {
        return billingProvider;
    }

    public abstract void hide();

    public abstract void pushNotification(String msg);

    public void runDaemon(int delayMsec, int periodMsec, final Runnable f) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                f.run();
            }
        }, delayMsec, periodMsec);
    }

    public void runTask(int delayMsec, final Runnable f) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                f.run();
            }
        }, delayMsec);
    }
}
