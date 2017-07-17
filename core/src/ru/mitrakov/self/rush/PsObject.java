package ru.mitrakov.self.rush;

import java.util.*;

/**
 * Created by mitrakov on 01.03.2017
 */
@SuppressWarnings("WeakerAccess")
public class PsObject {

    public interface RatioListener {
        void onRatioChanged(float ratio);
    }

    public interface VisibleListener {
        void onVisibleChanged(boolean visible);
    }

    private RatioListener ratioListener;
    private VisibleListener visibleListener;
    private IBillingProvider billingProvider;

    public PsObject(IBillingProvider billingProvider) {
        this.billingProvider = billingProvider;
    }

    public void setRatioListener(RatioListener listener) {
        ratioListener = listener;
    }

    public void setVisibleListener(VisibleListener listener) {
        visibleListener = listener;
    }

    public IBillingProvider getBillingProvider() {
        return billingProvider;
    }

    public void raiseRatioChanged(float ratio) {
        if (ratioListener != null)
            ratioListener.onRatioChanged(ratio);
    }

    public void raiseVisibleChanged(boolean visible) {
        if (visibleListener != null)
            visibleListener.onVisibleChanged(visible);
    }

    public void hide() {
    }

    public void activate() {
    }

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
