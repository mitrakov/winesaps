package ru.mitrakov.self.rush;

import java.util.*;

/**
 * Platform Specific Object.
 * Used for smoothen differences between different platforms, e.g. Desktop and Android.
 * Example: on Android Billing Provider may use Google Play Services, as long as on Desktop it may use payment gateways.
 * This class is intended to have a single instance for the current platform.
 * @author mitrakov
 */
public abstract class PsObject {
    /** Billing Provider */
    private IBillingProvider billingProvider;

    /**
     * Creates new instance of Platform Specific Object
     * @param billingProvider - billing provider (may be NULL)
     */
    public PsObject(IBillingProvider billingProvider) {
        this.billingProvider = billingProvider;
    }

    /**
     * @return billing provider
     */
    public IBillingProvider getBillingProvider() {
        return billingProvider;
    }

    /**
     * @return string representation of the platform
     */
    public abstract String getPlatform();

    /**
     * Makes the GUI invisible
     */
    public abstract void hide();

    /**
     * Marks the GUI as active/non-active
     * @param value active
     */
    public abstract void setActive(boolean value);

    /**
     * Display the notification depending on the platform
     * @param msg message text
     * @param force flag to show notification even when the GUI is still visible
     */
    public abstract void pushNotification(String msg, boolean force);

    /**
     * Returns the Android soft keyboard vendor (e.g. for standard Google keyboard it should return
     * com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME).
     * On Desktop the returned value not specified
     * @return the Android keyboard vendor
     */
    public abstract String getKeyboardVendor();

    /**
     * Runs a task periodically. By default it uses a standard Java timer (not recommended on Android)
     * @param delayMsec start delay in msec
     * @param periodMsec period delay in msec
     * @param f function to run
     */
    public void runDaemon(int delayMsec, int periodMsec, final Runnable f) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                f.run();
            }
        }, delayMsec, periodMsec);
    }

    /**
     * Runs a single task in the given delay. By default it uses a standard Java timer (not recommended on Android)
     * @param delayMsec delay in msec
     * @param f function to run
     */
    public void runTask(int delayMsec, final Runnable f) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                f.run();
            }
        }, delayMsec);
    }
}
