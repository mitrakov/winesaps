package ru.mitrakov.self.rush;

import android.os.*;
import android.app.*;
import android.content.Intent;

/**
 * Created by mitrakov on 17.07.2017
 */
class AndroidPsObject extends PsObject {
    private final Activity activity;
    private final HandlerThread thread = new HandlerThread("psObject");

    AndroidPsObject(Activity activity) {
        super(new AndroidBillingProvider(activity));
        assert activity != null;
        this.activity = activity;
        thread.start();
    }

    @Override
    public void hide() {
        activity.moveTaskToBack(true);
    }

    @Override
    public void activate() {
        Intent intent = new Intent(activity, AndroidLauncher.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            PendingIntent.getActivity(activity, 0, intent, 0).send();
        } catch (PendingIntent.CanceledException ignored) {
        }
    }

    @Override
    public void runDaemon(int delayMsec, final int periodMsec, final Runnable f) {
        // @mitrakov 2017-07-17: stackoverflow.com/questions/20330355
        final Handler handler = new Handler(thread.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                f.run();
                handler.postDelayed(this, periodMsec);
            }
        }, delayMsec);
    }

    @Override
    public void runTask(int delayMsec, Runnable f) {
        new Handler().postDelayed(f, delayMsec); // @mitrakov 2017-07-17: stackoverflow.com/questions/20330355
    }
}
