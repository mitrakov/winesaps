package ru.mitrakov.self.rush;

import android.os.*;
import android.app.*;
import android.content.*;

/**
 * Created by mitrakov on 17.07.2017
 */
class AndroidPsObject extends PsObject {
    private final Activity activity;
    private final HandlerThread thread;
    private final Handler handler;

    private boolean active = true;

    AndroidPsObject(Activity activity) {
        super(new AndroidBillingProvider(activity));
        assert activity != null;
        this.activity = activity;
        thread = new HandlerThread("psObject"); // https://stackoverflow.com/questions/18856376
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    @Override
    public void hide() {
        activity.moveTaskToBack(true);
    }

    @Override
    public void pushNotification(String msg) {
        if (!active) { // if the app is active => no need to push notifications
            NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(activity, activity.getClass());
            PendingIntent pIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // Notification.Builder uses API 11
                Notification.Builder builder = new Notification.Builder(activity);
                builder.setContentIntent(pIntent);
                builder.setContentTitle("Winesaps");
                builder.setContentText(msg);
                builder.setSmallIcon(R.drawable.winesaps);
                builder.setDefaults(Notification.DEFAULT_ALL); // default sound, vibrate pattern and lights
                builder.setAutoCancel(true);                   // hide notification after a user clicked it
                manager.notify(1, builder.getNotification()); //"getNotification()" deprecated but "build()" uses API 16
            } else try {
                pIntent.send();
            } catch (PendingIntent.CanceledException ignored) {
            }
        }
    }

    @Override
    public void runDaemon(int delayMsec, final int periodMsec, final Runnable f) {
        handler.postDelayed(new Runnable() { // @mitrakov 2017-07-17: https://stackoverflow.com/questions/20330355
            @Override
            public void run() {
                f.run();
                handler.postDelayed(this, periodMsec);
            }
        }, delayMsec);
    }

    @Override
    public void runTask(int delayMsec, Runnable f) {
        handler.postDelayed(f, delayMsec); // @mitrakov 2017-07-17: https://stackoverflow.com/questions/20330355
    }

    void setActive(boolean value) {
        active = value;
    }

    @SuppressWarnings("unused")
    void stop() {
        thread.quit();
    }
}
